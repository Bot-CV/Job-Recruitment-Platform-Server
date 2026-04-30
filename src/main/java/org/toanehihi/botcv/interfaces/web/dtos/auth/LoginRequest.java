package org.toanehihi.botcv.interfaces.web.dtos.auth;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String email;
    private String password;
}
