package com.company.permit.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.company.permit.framework.dataperm.DataScope;
import com.company.permit.system.user.dto.UserQueryDTO;
import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.vo.UserBO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @DataScope(alias = "u")
    IPage<UserBO> selectUserPage(Page<UserBO> page, @Param("q") UserQueryDTO query);

    @InterceptorIgnore(dataPermission = "1")
    SysUser selectByUsername(@Param("username") String username);

    @InterceptorIgnore(dataPermission = "1")
    SysUser selectUserByIdUnscoped(@Param("userId") Long userId);

    @InterceptorIgnore(dataPermission = "1")
    Long selectCountByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    @InterceptorIgnore(dataPermission = "1")
    Long selectCountByPhone(@Param("phone") String phone, @Param("excludeId") Long excludeId);

    @InterceptorIgnore(dataPermission = "1")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(dataPermission = "1")
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(dataPermission = "1")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @InterceptorIgnore(dataPermission = "1")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    @InterceptorIgnore(dataPermission = "1")
    List<Long> selectUserIdsByDeptIds(@Param("deptIds") List<Long> deptIds);
}
