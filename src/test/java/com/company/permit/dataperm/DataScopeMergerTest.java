package com.company.permit.dataperm;

import com.company.permit.framework.dataperm.DataScopeInfo;
import com.company.permit.framework.dataperm.DataScopeMerger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataScopeMergerTest {

    @Test
    public void mergeScope_任一角色全部_不过滤() {
        DataScopeInfo info = DataScopeMerger.merge(1L, 100L, Arrays.asList(
                new DataScopeMerger.RoleScope(1L, "2"),
                new DataScopeMerger.RoleScope(2L, "1")
        ), id -> Collections.singleton(101L), id -> Collections.emptySet());
        assertTrue(info.isAll());
        assertTrue(info.getDeptIds().isEmpty());
    }

    @Test
    public void mergeScope_多角色含自定义_返回并集() {
        DataScopeInfo info = DataScopeMerger.merge(8L, 100L, Arrays.asList(
                new DataScopeMerger.RoleScope(2L, "3"),
                new DataScopeMerger.RoleScope(5L, "5")
        ), id -> Collections.emptySet(), id -> new HashSet<>(Arrays.asList(200L, 201L)));
        assertFalse(info.isAll());
        Set<Long> expected = new HashSet<>(Arrays.asList(100L, 200L, 201L));
        assertEquals(expected, info.getDeptIds());
        assertFalse(info.isSelf());
    }

    @Test
    public void mergeScope_仅本人加本部门_self与部门并集() {
        DataScopeInfo info = DataScopeMerger.merge(8L, 100L, Arrays.asList(
                new DataScopeMerger.RoleScope(3L, "4"),
                new DataScopeMerger.RoleScope(4L, "3")
        ), id -> Collections.emptySet(), id -> Collections.emptySet());
        assertTrue(info.isSelf());
        assertTrue(info.getDeptIds().contains(100L));
    }

    @Test
    public void mergeScope_本部门及以下_包含子孙() {
        DataScopeInfo info = DataScopeMerger.merge(8L, 100L,
                Collections.singletonList(new DataScopeMerger.RoleScope(2L, "2")),
                id -> new HashSet<>(Arrays.asList(101L, 103L)),
                id -> Collections.emptySet());
        assertEquals(new HashSet<>(Arrays.asList(100L, 101L, 103L)), info.getDeptIds());
    }

    @Test
    public void mergeScope_无角色_拒绝全部() {
        DataScopeInfo info = DataScopeMerger.merge(8L, 100L, Collections.emptyList(),
                id -> Collections.emptySet(), id -> Collections.emptySet());
        assertTrue(info.denyAll());
    }
}
