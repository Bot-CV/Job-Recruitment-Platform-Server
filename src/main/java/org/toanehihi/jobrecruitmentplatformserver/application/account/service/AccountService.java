package org.toanehihi.jobrecruitmentplatformserver.application.account.service;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.AccountStatus;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.ForgotPasswordRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.ResendVerificationRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.ResetPasswordRequest;

public interface AccountService {

    void resendVerificationEmail(ResendVerificationRequest request);

    void verifyEmail(String token);

    void changeAccountStatus(Long accountId, AccountStatus status);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
