package com.company.permit.framework.dataperm;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class DataScopeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long deptId;
    private boolean all;
    private boolean self;
    private Set<Long> deptIds = new HashSet<>();

    public boolean denyAll() {
        return !all && !self && (deptIds == null || deptIds.isEmpty());
    }
}
