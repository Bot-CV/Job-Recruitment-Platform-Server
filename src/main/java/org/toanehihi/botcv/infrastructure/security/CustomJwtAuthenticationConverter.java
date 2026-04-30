package org.toanehihi.botcv.infrastructure.security;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.infrastructure.persistence.repositories.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AccountRepository accountRepository;

    @Override
    @Nullable
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        String email = jwt.getSubject();
        log.info("Converting JWT {} to Authentication Token", email);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        AccountUserDetails userDetails = new AccountUserDetails(account);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        log.info("Authenticated account: {} with authorities: {}", email, authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, jwt, authorities);
    }
}
