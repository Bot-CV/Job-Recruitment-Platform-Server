package org.toanehihi.botcv.interfaces.web.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GoogleLoginRequest {
    @NotBlank(message = "ID_TOKEN_BLANK")
    private String idToken;
}
