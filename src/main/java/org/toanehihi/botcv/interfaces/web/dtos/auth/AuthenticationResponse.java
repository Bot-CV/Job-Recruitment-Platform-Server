package org.toanehihi.botcv.interfaces.web.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
}
