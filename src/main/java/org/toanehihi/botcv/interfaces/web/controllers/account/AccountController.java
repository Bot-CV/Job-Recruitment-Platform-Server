package org.toanehihi.botcv.interfaces.web.controllers.account;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.toanehihi.botcv.application.account.service.AccountService;
import org.toanehihi.botcv.domain.model.enums.AccountStatus;
import org.toanehihi.botcv.interfaces.web.dtos.DataResponse;
import org.toanehihi.botcv.interfaces.web.dtos.account.ForgotPasswordRequest;
import org.toanehihi.botcv.interfaces.web.dtos.account.ResendVerificationRequest;
import org.toanehihi.botcv.interfaces.web.dtos.account.ResetPasswordRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/resend-verification")
    public DataResponse<String> resendVerificationEmail(
            @RequestBody @Valid ResendVerificationRequest request) {
        accountService.resendVerificationEmail(request);
        return DataResponse.<String>builder()
                .data("Verify email has been sent to your email")
                .build();
    }

    @GetMapping("/verify")
    public DataResponse<String> verifyEmail(@RequestParam String token) {
        accountService.verifyEmail(token);
        return DataResponse.<String>builder()
                .data("Verify email successfully")
                .build();
    }

    @PatchMapping("/{accountId}/status/{status}")
    public DataResponse<String> changeAccountStatus(@PathVariable Long accountId,
            @PathVariable AccountStatus status) {
        accountService.changeAccountStatus(accountId, status);
        return DataResponse.<String>builder()
                .data("Change account status successfully")
                .build();
    }

    @PostMapping("/forgot-password")
    public DataResponse<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        accountService.forgotPassword(request);
        return DataResponse.<String>builder()
                .data("Password reset instructions have been sent to your email")
                .build();
    }

    @PostMapping("/reset-password")
    public DataResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        accountService.resetPassword(request);
        return DataResponse.<String>builder()
                .data("Password has been reset successfully")
                .build();
    }
}
