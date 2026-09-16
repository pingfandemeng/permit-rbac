package com.company.permit.system.profile.controller;

import com.company.permit.framework.log.Log;
import com.company.permit.framework.log.OperType;
import com.company.permit.framework.security.RepeatSubmit;
import com.company.permit.framework.web.R;
import com.company.permit.system.profile.dto.PasswordUpdateDTO;
import com.company.permit.system.profile.dto.ProfileUpdateDTO;
import com.company.permit.system.profile.service.ProfileService;
import com.company.permit.system.profile.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/profile")
@RequiredArgsConstructor
@Validated
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public R<ProfileVO> profile() {
        return R.ok(profileService.profile());
    }

    @PutMapping
    @Log(title = "个人中心", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> update(@Validated @RequestBody ProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return R.ok();
    }

    @PutMapping("/password")
    @Log(title = "修改密码", operType = OperType.UPDATE)
    @RepeatSubmit
    public R<Void> password(@Validated @RequestBody PasswordUpdateDTO dto) {
        profileService.updatePassword(dto);
        return R.ok();
    }
}
