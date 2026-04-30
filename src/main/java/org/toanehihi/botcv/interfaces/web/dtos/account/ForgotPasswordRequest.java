package org.toanehihi.botcv.interfaces.web.dtos.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ForgotPasswordRequest {
    @NotBlank(message = "EMAIL_BLANK")
    @Email(message = "INVALID_EMAIL")
    private String email;
}
