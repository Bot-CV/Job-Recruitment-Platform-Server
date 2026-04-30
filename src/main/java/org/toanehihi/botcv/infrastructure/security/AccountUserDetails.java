package org.toanehihi.botcv.infrastructure.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.domain.model.enums.AccountStatus;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class AccountUserDetails implements UserDetails {

    private final Account account;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().getName()));
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.getStatus() != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.getStatus() == AccountStatus.ACTIVE;
    }
}
