package com.company.permit.system.user.convert;

import com.company.permit.framework.util.DesensitizeUtils;
import com.company.permit.system.user.vo.UserBO;
import com.company.permit.system.user.vo.UserVO;

import java.util.ArrayList;
import java.util.List;

public final class UserConvert {
    private UserConvert() {
    }

    public static UserVO toVo(UserBO bo) {
        if (bo == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setUserId(bo.getUserId());
        vo.setUsername(bo.getUsername());
        vo.setNickname(bo.getNickname());
        vo.setPhone(DesensitizeUtils.phone(bo.getPhone()));
        vo.setEmail(DesensitizeUtils.email(bo.getEmail()));
        vo.setDeptId(bo.getDeptId());
        vo.setDeptName(bo.getDeptName());
        vo.setStatus(bo.getStatus());
        vo.setPwdResetFlag(bo.getPwdResetFlag());
        vo.setLockStatus(bo.getLockStatus());
        vo.setCreateTime(bo.getCreateTime());
        vo.setVersion(bo.getVersion());
        vo.setRemark(bo.getRemark());
        return vo;
    }

    public static List<UserVO> toVoList(List<UserBO> list) {
        List<UserVO> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (UserBO bo : list) {
            result.add(toVo(bo));
        }
        return result;
    }
}
