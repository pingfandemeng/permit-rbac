package com.company.permit.monitor.online.service;

import cn.dev33.satoken.stp.StpUtil;
import com.company.permit.framework.cache.OnlineUserCacheService;
import com.company.permit.framework.cache.PermissionCacheService;
import com.company.permit.framework.web.PageResult;
import com.company.permit.monitor.online.vo.OnlineUserVO;
import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final OnlineUserCacheService onlineUserCacheService;
    private final SysUserMapper userMapper;
    private final PermissionCacheService permissionCacheService;

    public PageResult<OnlineUserVO> page(int pageNum, int pageSize) {
        long total = onlineUserCacheService.size();
        long offset = (long) (pageNum - 1) * pageSize;
        List<Long> ids = onlineUserCacheService.page(offset, pageSize);
        List<OnlineUserVO> rows = new ArrayList<>();
        for (Long id : ids) {
            SysUser user = userMapper.selectUserByIdUnscoped(id);
            if (user == null) {
                continue;
            }
            OnlineUserVO vo = new OnlineUserVO();
            vo.setUserId(user.getUserId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setLoginIp(user.getLastLoginIp());
            vo.setLoginTime(user.getLastLoginTime() == null ? null
                    : user.getLastLoginTime().atZone(java.time.ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli());
            rows.add(vo);
        }
        return new PageResult<>(total, rows);
    }

    public void forceLogout(Long userId) {
        permissionCacheService.evict(userId);
        onlineUserCacheService.remove(userId);
        StpUtil.kickout(userId);
    }
}
