package org.toanehihi.botcv.interfaces.web.dtos.account;

import java.time.OffsetDateTime;

import org.toanehihi.botcv.domain.model.enums.AccountStatus;
import org.toanehihi.botcv.domain.model.enums.AuthProvider;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    private Long id;
    private String email;
    private String roleName;
    private AccountStatus status;
    private AuthProvider provider;
    private OffsetDateTime verifiedAt;
    private OffsetDateTime dateCreated;
}
