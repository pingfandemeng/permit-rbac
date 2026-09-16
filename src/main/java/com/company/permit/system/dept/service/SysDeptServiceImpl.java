package com.company.permit.system.dept.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.permit.framework.dataperm.DataScopeService;
import com.company.permit.framework.web.Constants;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import com.company.permit.system.dept.dto.DeptSaveDTO;
import com.company.permit.system.dept.entity.SysDept;
import com.company.permit.system.dept.mapper.SysDeptMapper;
import com.company.permit.system.dept.vo.DeptVO;
import com.company.permit.system.user.entity.SysUser;
import com.company.permit.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;
    private final DataScopeService dataScopeService;

    @Override
    public List<DeptVO> tree(String status) {
        return buildTree(deptMapper.selectDeptList(status));
    }

    @Override
    public List<DeptVO> treeForScope() {
        return tree(Constants.STATUS_NORMAL);
    }

    @Override
    public DeptVO detail(Long deptId) {
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new ServiceException(ErrorCode.C01003, "部门不存在");
        }
        return toVo(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDept(DeptSaveDTO dto) {
        checkKey(dto.getDeptKey(), null);
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        String ancestors = "0";
        if (parentId != 0L) {
            dataScopeService.checkDeptInScope(parentId);
            SysDept parent = deptMapper.selectById(parentId);
            if (parent == null) {
                throw new ServiceException(ErrorCode.C01003, "上级部门不存在");
            }
            int level = parent.getAncestors() == null ? 1 : parent.getAncestors().split(",").length + 1;
            if (level >= Constants.MAX_DEPT_LEVEL) {
                throw new ServiceException(ErrorCode.B04004);
            }
            ancestors = parent.getAncestors() + "," + parent.getDeptId();
        }
        SysDept dept = new SysDept();
        dept.setParentId(parentId);
        dept.setDeptName(dto.getDeptName());
        dept.setDeptKey(dto.getDeptKey());
        dept.setAncestors(ancestors);
        dept.setOrderNum(dto.getOrderNum() == null ? 0 : dto.getOrderNum());
        dept.setStatus(dto.getStatus() == null ? Constants.STATUS_NORMAL : dto.getStatus());
        dept.setRemark(dto.getRemark());
        deptMapper.insert(dept);
        dataScopeService.evictByDeptChange(dept.getDeptId());
        return dept.getDeptId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptSaveDTO dto) {
        dataScopeService.checkDeptInScope(dto.getDeptId());
        SysDept db = deptMapper.selectById(dto.getDeptId());
        if (db == null) {
            throw new ServiceException(ErrorCode.C01003, "部门不存在");
        }
        checkKey(dto.getDeptKey(), dto.getDeptId());
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        if (dto.getDeptId().equals(parentId)) {
            throw new ServiceException(ErrorCode.B04005);
        }
        if (parentId != 0L) {
            SysDept parent = deptMapper.selectById(parentId);
            if (parent == null) {
                throw new ServiceException(ErrorCode.C01003, "上级部门不存在");
            }
            if (parent.getAncestors() != null && ("," + parent.getAncestors() + ",").contains("," + dto.getDeptId() + ",")) {
                throw new ServiceException(ErrorCode.B04005);
            }
            int level = parent.getAncestors() == null ? 1 : parent.getAncestors().split(",").length + 1;
            if (level >= Constants.MAX_DEPT_LEVEL) {
                throw new ServiceException(ErrorCode.B04004);
            }
            db.setAncestors(parent.getAncestors() + "," + parent.getDeptId());
        } else {
            db.setAncestors("0");
        }
        boolean parentChanged = !parentId.equals(db.getParentId());
        String oldAncestors = db.getAncestors();
        db.setParentId(parentId);
        db.setDeptName(dto.getDeptName());
        db.setDeptKey(dto.getDeptKey());
        db.setOrderNum(dto.getOrderNum() == null ? 0 : dto.getOrderNum());
        db.setStatus(dto.getStatus() == null ? db.getStatus() : dto.getStatus());
        db.setRemark(dto.getRemark());
        if (dto.getVersion() != null) {
            db.setVersion(dto.getVersion());
        }
        int rows = deptMapper.updateById(db);
        if (rows == 0) {
            throw new ServiceException(ErrorCode.C01002);
        }
        if (parentChanged) {
            refreshChildrenAncestors(db, oldAncestors);
        }
        dataScopeService.evictByDeptChange(db.getDeptId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDept(Long deptId) {
        dataScopeService.checkDeptInScope(deptId);
        Long children = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, deptId));
        if (children != null && children > 0) {
            throw new ServiceException(ErrorCode.B04002);
        }
        Long users = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId));
        if (users != null && users > 0) {
            throw new ServiceException(ErrorCode.B04003);
        }
        deptMapper.deleteById(deptId);
        dataScopeService.evictByDeptChange(deptId);
    }

    private void refreshChildrenAncestors(SysDept parent, String unused) {
        List<SysDept> children = deptMapper.selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, parent.getDeptId()));
        for (SysDept child : children) {
            child.setAncestors(parent.getAncestors() + "," + parent.getDeptId());
            deptMapper.updateById(child);
            refreshChildrenAncestors(child, unused);
        }
    }

    private void checkKey(String deptKey, Long excludeId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptKey, deptKey);
        if (excludeId != null) {
            wrapper.ne(SysDept::getDeptId, excludeId);
        }
        if (deptMapper.selectCount(wrapper) > 0) {
            throw new ServiceException(ErrorCode.B04001);
        }
    }

    private List<DeptVO> buildTree(List<SysDept> depts) {
        List<DeptVO> vos = depts.stream().map(this::toVo).collect(Collectors.toList());
        Map<Long, DeptVO> map = vos.stream().collect(Collectors.toMap(DeptVO::getDeptId, v -> v));
        List<DeptVO> roots = new ArrayList<>();
        for (DeptVO vo : vos) {
            if (vo.getParentId() == null || vo.getParentId() == 0L || !map.containsKey(vo.getParentId())) {
                roots.add(vo);
            } else {
                map.get(vo.getParentId()).getChildren().add(vo);
            }
        }
        return roots;
    }

    private DeptVO toVo(SysDept dept) {
        DeptVO vo = new DeptVO();
        vo.setDeptId(dept.getDeptId());
        vo.setParentId(dept.getParentId());
        vo.setDeptName(dept.getDeptName());
        vo.setDeptKey(dept.getDeptKey());
        vo.setAncestors(dept.getAncestors());
        vo.setOrderNum(dept.getOrderNum());
        vo.setStatus(dept.getStatus());
        vo.setVersion(dept.getVersion());
        vo.setRemark(dept.getRemark());
        return vo;
    }
}
