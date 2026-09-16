package com.company.permit.system.dept.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.company.permit.framework.dataperm.DataScope;
import com.company.permit.system.dept.entity.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptMapper extends BaseMapper<SysDept> {

    @DataScope(alias = "d")
    List<SysDept> selectDeptList(@Param("status") String status);

    @InterceptorIgnore(dataPermission = "1")
    List<SysDept> selectAllDeptsUnscoped();

    @InterceptorIgnore(dataPermission = "1")
    List<Long> selectChildDeptIds(@Param("deptId") Long deptId);

    @InterceptorIgnore(dataPermission = "1")
    SysDept selectByKey(@Param("deptKey") String deptKey);
}
