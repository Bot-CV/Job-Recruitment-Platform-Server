package org.toanehihi.botcv.application.auth.service;

import org.toanehihi.botcv.interfaces.web.dtos.account.AccountResponse;
import org.toanehihi.botcv.interfaces.web.dtos.account.CandidateAccountRequest;
import org.toanehihi.botcv.interfaces.web.dtos.account.RecruiterAccountRequest;
import org.toanehihi.botcv.interfaces.web.dtos.auth.AuthenticationResponse;
import org.toanehihi.botcv.interfaces.web.dtos.auth.GoogleLoginRequest;
import org.toanehihi.botcv.interfaces.web.dtos.auth.LoginRequest;
import org.toanehihi.botcv.interfaces.web.dtos.auth.LogoutRequest;
import org.toanehihi.botcv.interfaces.web.dtos.auth.RefreshTokenRequest;

public interface AuthService {
    AccountResponse candidateRegister(CandidateAccountRequest request);

    AccountResponse recruiterRegister(RecruiterAccountRequest request);

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse loginWithGoogle(GoogleLoginRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}
