package com.company.permit.system.user.service;

import com.company.permit.framework.web.PageResult;
import com.company.permit.system.user.dto.BatchRolesDTO;
import com.company.permit.system.user.dto.BatchStatusDTO;
import com.company.permit.system.user.dto.UserCreateDTO;
import com.company.permit.system.user.dto.UserQueryDTO;
import com.company.permit.system.user.dto.UserRoleDTO;
import com.company.permit.system.user.dto.UserStatusDTO;
import com.company.permit.system.user.dto.UserUpdateDTO;
import com.company.permit.system.user.vo.UserVO;

import java.util.List;

public interface SysUserService {
    PageResult<UserVO> selectUserPage(UserQueryDTO query);

    UserVO detail(Long userId);

    Long createUser(UserCreateDTO dto);

    void updateUser(UserUpdateDTO dto);

    void removeUser(Long userId);

    void changeStatus(UserStatusDTO dto);

    void resetPwd(Long userId);

    void assignRoles(UserRoleDTO dto);

    int batchStatus(BatchStatusDTO dto);

    int batchRoles(BatchRolesDTO dto);

    int batchDelete(List<Long> userIds);
}
