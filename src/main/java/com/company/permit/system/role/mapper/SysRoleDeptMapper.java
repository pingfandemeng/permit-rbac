package com.company.permit.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.company.permit.system.role.entity.SysRoleDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    @InterceptorIgnore(dataPermission = "1")
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);
}
