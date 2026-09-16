package com.company.permit.system.auth;

import com.company.permit.framework.web.R;
import com.company.permit.system.auth.dto.LoginDTO;
import com.company.permit.system.auth.service.AuthService;
import com.company.permit.system.auth.vo.AuthInfoVO;
import com.company.permit.system.auth.vo.CaptchaVO;
import com.company.permit.system.auth.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @GetMapping("/captcha")
    public R<CaptchaVO> captcha() {
        return R.ok(authService.createCaptcha());
    }

    @PostMapping("/login")
    public R<LoginVO> login(@Validated @RequestBody LoginDTO dto, HttpServletRequest request) {
        return R.ok(authService.login(dto, request));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @GetMapping("/info")
    public R<AuthInfoVO> info() {
        return R.ok(authService.info());
    }

    @GetMapping("/check")
    public R<AuthInfoVO> check() {
        return R.ok(authService.check());
    }
}
