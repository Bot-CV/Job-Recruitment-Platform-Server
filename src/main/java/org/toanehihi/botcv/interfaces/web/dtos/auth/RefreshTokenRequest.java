package org.toanehihi.botcv.interfaces.web.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {
    @NotBlank(message = "REFRESH_TOKEN_REQUIRED")
    private String refreshToken;
}
