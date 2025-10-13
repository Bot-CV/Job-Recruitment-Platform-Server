package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.toanehihi.jobrecruitmentplatformserver.application.auth.service.AuthService;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.AccountResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.CandidateAccountRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.RecruiterAccountRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.AuthenticationResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.GoogleLoginRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.LoginRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.LogoutRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.RefreshTokenRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/candidate")
    public DataResponse<AccountResponse> candidateRegister(@Valid @RequestBody CandidateAccountRequest request) {
        return DataResponse.<AccountResponse>builder()
                .data(authService.candidateRegister(request))
                .build();
    }

    @PostMapping("/register/recruiter")
    public DataResponse<AccountResponse> recruiterRegister(@Valid @RequestBody RecruiterAccountRequest request) {
        return DataResponse.<AccountResponse>builder()
                .data(authService.recruiterRegister(request))
                .build();
    }

    @PostMapping("/login")
    public DataResponse<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        log.info("Login controller!!!");
        return DataResponse.<AuthenticationResponse>builder()
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/login/google")
    public DataResponse<AuthenticationResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        return DataResponse.<AuthenticationResponse>builder()
                .data(authService.loginWithGoogle(request))
                .build();
    }

    @PostMapping("/refresh")
    public DataResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return DataResponse.<AuthenticationResponse>builder()
                .data(authService.refreshToken(request))
                .build();
    }

    @PostMapping("/logout")
    public DataResponse<String> refreshToken(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return DataResponse.<String>builder()
                .data("Logout successfully")
                .build();
    }
}
