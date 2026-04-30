package org.toanehihi.botcv.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.Account;

@Component
public class CurrentAccountProvider {
    public Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AccountUserDetails userDetails) {
            return userDetails.getAccount();
        }

        throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
    }

    public Long getCurrentAccountId() {
        return getCurrentAccount().getId();
    }
}
