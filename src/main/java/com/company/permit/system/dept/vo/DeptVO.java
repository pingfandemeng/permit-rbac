package com.company.permit.system.dept.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeptVO {
    private Long deptId;
    private Long parentId;
    private String deptName;
    private String deptKey;
    private String ancestors;
    private Integer orderNum;
    private String status;
    private Integer version;
    private String remark;
    private List<DeptVO> children = new ArrayList<>();
}
