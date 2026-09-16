package com.company.permit.system.profile.service;

import com.company.permit.system.profile.dto.PasswordUpdateDTO;
import com.company.permit.system.profile.dto.ProfileUpdateDTO;
import com.company.permit.system.profile.vo.ProfileVO;

public interface ProfileService {
    ProfileVO profile();

    void updateProfile(ProfileUpdateDTO dto);

    void updatePassword(PasswordUpdateDTO dto);
}
