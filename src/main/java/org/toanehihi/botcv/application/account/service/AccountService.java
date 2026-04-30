package org.toanehihi.botcv.application.account.service;

import org.toanehihi.botcv.domain.model.enums.AccountStatus;
import org.toanehihi.botcv.interfaces.web.dtos.account.ForgotPasswordRequest;
import org.toanehihi.botcv.interfaces.web.dtos.account.ResendVerificationRequest;
import org.toanehihi.botcv.interfaces.web.dtos.account.ResetPasswordRequest;

public interface AccountService {

    void resendVerificationEmail(ResendVerificationRequest request);

    void verifyEmail(String token);

    void changeAccountStatus(Long accountId, AccountStatus status);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
