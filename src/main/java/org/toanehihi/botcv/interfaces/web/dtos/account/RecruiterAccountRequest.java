package org.toanehihi.botcv.interfaces.web.dtos.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RecruiterAccountRequest {

    @NotBlank(message = "FULLNAME_BLANK")
    private String fullName;

    @NotBlank(message = "EMAIL_BLANK")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @Size(min = 8, message = "INVALID_PASSWORD")
    private String password;

    @NotBlank(message = "COMPANY_NAME_BLANK")
    private String companyName;
}
