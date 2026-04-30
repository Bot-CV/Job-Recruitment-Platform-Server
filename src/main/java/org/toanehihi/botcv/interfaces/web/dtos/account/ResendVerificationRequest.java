package org.toanehihi.botcv.interfaces.web.dtos.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResendVerificationRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
