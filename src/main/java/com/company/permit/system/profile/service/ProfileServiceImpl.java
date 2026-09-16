package com.company.permit.system.profile.service;

import cn.dev33.satoken.stp.StpUtil;
import com.company.permit.framework.cache.OnlineUserCacheService;
import com.company.permit.framework.security.PasswordPolicyService;
import com.company.permit.framework.security.RsaService;
import com.company.permit.framework.util.SecurityUtils;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.dept.entity.SysDept;
import com.company.permit.system.dept.mapper.SysDeptMapper;
import com.company.permit.system.profile.dto.PasswordUpdateDTO;
import com.company.permit.system.profile.dto.ProfileUpdateDTO;
import com.company.permit.system.profile.vo.ProfileVO;
import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final RsaService rsaService;
    private final OnlineUserCacheService onlineUserCacheService;

    @Override
    public ProfileVO profile() {
        SysUser user = userMapper.selectUserByIdUnscoped(StpUtil.getLoginIdAsLong());
        ProfileVO vo = new ProfileVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setDeptId(user.getDeptId());
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            vo.setDeptName(dept == null ? "" : dept.getDeptName());
        }
        vo.setRoles(userMapper.selectRoleKeysByUserId(user.getUserId()));
        if (SecurityUtils.ADMIN_USERNAME.equals(user.getUsername())) {
            vo.setPermissions(java.util.Collections.singletonList(SecurityUtils.WILDCARD_PERM));
        } else {
            vo.setPermissions(StpUtil.getPermissionList());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(ProfileUpdateDTO dto) {
        SysUser user = userMapper.selectUserByIdUnscoped(StpUtil.getLoginIdAsLong());
        if (StringUtils.hasText(dto.getPhone()) && userMapper.selectCountByPhone(dto.getPhone(), user.getUserId()) > 0) {
            throw new ServiceException(ErrorCode.B01002);
        }
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone() == null ? "" : dto.getPhone());
        user.setEmail(dto.getEmail() == null ? "" : dto.getEmail());
        userMapper.updateById(user);
        StpUtil.getSession().set("username", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateDTO dto) {
        String oldPwd = decrypt(dto.getOldPassword());
        String newPwd = decrypt(dto.getNewPassword());
        String confirm = decrypt(dto.getConfirmPassword());
        if (!newPwd.equals(confirm)) {
            throw new ServiceException(ErrorCode.C01003, "两次输入的新密码不一致");
        }
        passwordPolicyService.validate(newPwd);
        SysUser user = userMapper.selectUserByIdUnscoped(StpUtil.getLoginIdAsLong());
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new ServiceException(ErrorCode.C01003, "旧密码错误");
        }
        if (passwordEncoder.matches(newPwd, user.getPassword())) {
            throw new ServiceException(ErrorCode.C01003, "新密码不能与旧密码相同");
        }
        user.setPassword(passwordEncoder.encode(newPwd));
        user.setPwdResetFlag(Constants.FLAG_NO);
        user.setPwdUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        Long userId = user.getUserId();
        onlineUserCacheService.remove(userId);
        StpUtil.logout();
    }

    private String decrypt(String value) {
        String plain = rsaService.decryptPassword(value);
        return plain == null ? value : plain;
    }
}
