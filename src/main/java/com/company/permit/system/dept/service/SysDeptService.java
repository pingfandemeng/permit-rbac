package com.company.permit.system.dept.service;

import com.company.permit.system.dept.dto.DeptSaveDTO;
import com.company.permit.system.dept.vo.DeptVO;

import java.util.List;

public interface SysDeptService {
    List<DeptVO> tree(String status);

    List<DeptVO> treeForScope();

    Long createDept(DeptSaveDTO dto);

    void updateDept(DeptSaveDTO dto);

    void removeDept(Long deptId);

    DeptVO detail(Long deptId);
}
