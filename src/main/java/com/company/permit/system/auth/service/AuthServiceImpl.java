package com.company.permit.system.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.company.permit.framework.cache.CacheKeys;
import com.company.permit.framework.cache.OnlineUserCacheService;
import com.company.permit.framework.cache.PermissionCacheService;
import com.company.permit.framework.security.RsaService;
import com.company.permit.framework.util.IpUtils;
import com.company.permit.framework.util.SecurityUtils;
import com.company.permit.framework.util.UserAgentUtils;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.monitor.loginlog.entity.SysLoginLog;
import com.company.permit.monitor.loginlog.mapper.SysLoginLogMapper;
import com.company.permit.system.auth.dto.LoginDTO;
import com.company.permit.system.auth.vo.AuthInfoVO;
import com.company.permit.system.auth.vo.CaptchaVO;
import com.company.permit.system.auth.vo.LoginVO;
import com.company.permit.system.auth.vo.RouterVO;
import com.company.permit.system.config.service.SysConfigService;
import com.company.permit.system.dept.entity.SysDept;
import com.company.permit.system.dept.mapper.SysDeptMapper;
import com.company.permit.system.menu.entity.SysMenu;
import com.company.permit.system.menu.mapper.SysMenuMapper;
import com.company.permit.system.menu.service.SysMenuService;
import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.mapper.SysUserMapper;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RsaService rsaService;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysMenuMapper menuMapper;
    private final SysMenuService menuService;
    private final SysConfigService configService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService permissionCacheService;
    private final OnlineUserCacheService onlineUserCacheService;
    private final SysLoginLogMapper loginLogMapper;

    @Override
    public CaptchaVO createCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(com.wf.captcha.base.Captcha.TYPE_DEFAULT);
        String key = IdUtil.fastSimpleUUID();
        stringRedisTemplate.opsForValue().set(CacheKeys.captcha(key), captcha.text().toLowerCase(), 2, TimeUnit.MINUTES);
        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaKey(key);
        vo.setImg(captcha.toBase64());
        vo.setPublicKey(rsaService.publicKeyPem());
        return vo;
    }

    @Override
    public LoginVO login(LoginDTO dto, HttpServletRequest request) {
        String ip = IpUtils.getIp(request);
        checkIpLimit(ip);
        verifyCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode());
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user != null && isLocked(user)) {
            recordLogin(dto.getUsername(), ip, request, "1", ErrorCode.A01005.getMsg());
            int minutes = remainingLockMinutes(user);
            throw new ServiceException(ErrorCode.A01005, "账号已锁定，请 " + minutes + " 分钟后再试");
        }
        String rawPassword = rsaService.decryptPassword(dto.getPassword());
        if (rawPassword == null) {
            rawPassword = dto.getPassword();
        }
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            onPasswordFail(dto.getUsername(), user, ip, request);
            throw new ServiceException(ErrorCode.A01006);
        }
        if (Constants.STATUS_DISABLE.equals(user.getStatus())) {
            recordLogin(dto.getUsername(), ip, request, "1", ErrorCode.A01004.getMsg());
            throw new ServiceException(ErrorCode.A01004);
        }
        clearRetry(dto.getUsername());
        StpUtil.login(user.getUserId());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("pwdResetFlag", user.getPwdResetFlag());
        permissionCacheService.evictPermissions(user.getUserId());
        onlineUserCacheService.add(user.getUserId());
        user.setLastLoginIp(ip);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLockStatus(Constants.FLAG_NO);
        userMapper.updateById(user);
        recordLogin(dto.getUsername(), ip, request, "0", "登录成功");
        log.info("用户 {} 登录成功", user.getUsername());
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setTokenName("Authorization");
        vo.setPwdResetRequired(Constants.FLAG_YES.equals(user.getPwdResetFlag()));
        return vo;
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            onlineUserCacheService.remove(userId);
            StpUtil.logout();
        }
    }

    @Override
    public AuthInfoVO info() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectUserByIdUnscoped(userId);
        if (user == null) {
            throw new ServiceException(ErrorCode.B01003);
        }
        AuthInfoVO vo = new AuthInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setDeptId(user.getDeptId());
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            vo.setDeptName(dept == null ? "" : dept.getDeptName());
        }
        vo.setRoles(userMapper.selectRoleKeysByUserId(userId));
        if (SecurityUtils.ADMIN_USERNAME.equals(user.getUsername())) {
            vo.setPermissions(java.util.Collections.singletonList(SecurityUtils.WILDCARD_PERM));
            List<SysMenu> all = menuMapper.selectList(null);
            vo.setRouters(menuService.buildRouters(all));
        } else {
            vo.setPermissions(StpUtil.getPermissionList());
            vo.setRouters(menuService.buildRouters(menuMapper.selectMenusByUserId(userId)));
        }
        vo.setPwdResetRequired(Constants.FLAG_YES.equals(user.getPwdResetFlag()));
        return vo;
    }

    @Override
    public AuthInfoVO check() {
        return info();
    }

    private void verifyCaptcha(String key, String code) {
        String cacheKey = CacheKeys.captcha(key);
        String expected = stringRedisTemplate.opsForValue().get(cacheKey);
        stringRedisTemplate.delete(cacheKey);
        if (expected == null) {
            throw new ServiceException(ErrorCode.A02002);
        }
        if (!expected.equalsIgnoreCase(StrUtil.trim(code))) {
            throw new ServiceException(ErrorCode.A02001);
        }
    }

    private void checkIpLimit(String ip) {
        String key = CacheKeys.ipLimit(ip);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        if (count != null && count > 10) {
            throw new ServiceException(ErrorCode.A01008);
        }
    }

    private boolean isLocked(SysUser user) {
        if (!Constants.FLAG_YES.equals(user.getLockStatus()) || user.getLockTime() == null) {
            return false;
        }
        int minutes = configService.getInt(Constants.CFG_LOGIN_LOCK, 15);
        if (user.getLockTime().plusMinutes(minutes).isAfter(LocalDateTime.now())) {
            return true;
        }
        user.setLockStatus(Constants.FLAG_NO);
        user.setLockTime(null);
        userMapper.updateById(user);
        clearRetry(user.getUsername());
        return false;
    }

    private int remainingLockMinutes(SysUser user) {
        int minutes = configService.getInt(Constants.CFG_LOGIN_LOCK, 15);
        long remain = java.time.Duration.between(LocalDateTime.now(), user.getLockTime().plusMinutes(minutes)).toMinutes();
        return (int) Math.max(remain, 1);
    }

    private void onPasswordFail(String username, SysUser user, String ip, HttpServletRequest request) {
        recordLogin(username, ip, request, "1", ErrorCode.A01006.getMsg());
        int maxRetry = configService.getInt(Constants.CFG_LOGIN_RETRY, 5);
        String key = CacheKeys.pwdRetry(username);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        int lockMinutes = configService.getInt(Constants.CFG_LOGIN_LOCK, 15);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, lockMinutes, TimeUnit.MINUTES);
        }
        if (user != null && count != null && count >= maxRetry) {
            user.setLockStatus(Constants.FLAG_YES);
            user.setLockTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    private void clearRetry(String username) {
        stringRedisTemplate.delete(CacheKeys.pwdRetry(username));
    }

    private void recordLogin(String username, String ip, HttpServletRequest request, String status, String msg) {
        try {
            String ua = request.getHeader("User-Agent");
            SysLoginLog logEntity = new SysLoginLog();
            logEntity.setUsername(username);
            logEntity.setLoginIp(ip);
            logEntity.setBrowser(UserAgentUtils.browser(ua));
            logEntity.setOs(UserAgentUtils.os(ua));
            logEntity.setStatus(status);
            logEntity.setMsg(msg);
            logEntity.setLoginTime(LocalDateTime.now());
            loginLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("写入登录日志失败", e);
        }
    }
}
