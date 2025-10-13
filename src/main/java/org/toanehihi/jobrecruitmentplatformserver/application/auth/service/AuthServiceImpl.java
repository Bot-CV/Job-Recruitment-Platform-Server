package org.toanehihi.jobrecruitmentplatformserver.application.auth.service;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.application.token.service.JwtService;
import org.toanehihi.jobrecruitmentplatformserver.application.token.service.TokenService;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Candidate;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Recruiter;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Role;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.AccountStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.AuthProvider;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.account.AccountMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.AccountRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CandidateRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.RecruiterRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.RoleRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.AccountResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.CandidateAccountRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.account.RecruiterAccountRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.AuthenticationResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.GoogleLoginRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.LoginRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.LogoutRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.auth.RefreshTokenRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final AccountMapper accountMapper;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final GoogleOAuthService googleOAuthService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AccountResponse candidateRegister(CandidateAccountRequest request) {
        Role role = roleRepository.findByName("CANDIDATE")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Optional<Account> existingAccount = accountRepository.findByEmail(request.getEmail());
        if (existingAccount.isPresent()) {
            Account account = existingAccount.get();
            if (account.getPassword() != null) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);
            } else {
                account.setPassword(passwordEncoder.encode(request.getPassword()));
                account.setProvider(AuthProvider.LOCAL);
                Account savedAccount = accountRepository.save(account);
                return accountMapper.toResponse(savedAccount);
            }
        }

        Account account = accountMapper.toCandidateAccount(request);
        account.setRole(role);
        account.setProvider(AuthProvider.LOCAL);
        Account savedAccount = accountRepository.save(account);

        Candidate candidate = Candidate.builder()
                .account(savedAccount)
                .fullName(request.getFullName())
                .avatarResourceId(123L) // create default avatar later
                .build();
        candidateRepository.save(candidate);

        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional
    public AccountResponse recruiterRegister(RecruiterAccountRequest request) {
        log.info("Login service!!!");
        Role role = roleRepository.findByName("RECRUITER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTED);
        }

        Account account = accountMapper.toRecruiterAccount(request);
        account.setRole(role);
        Account savedAccount = accountRepository.save(account);

        Company company = Company.builder()
                .name(request.getCompanyName())
                .build();
        Company savedCompany = companyRepository.save(company);

        Recruiter recruiter = Recruiter.builder()
                .account(savedAccount)
                .fullName(request.getFullName())
                .avatarResourceId(123L) // create default avatar later
                .company(savedCompany)
                .build();
        recruiterRepository.save(recruiter);

        return accountMapper.toResponse(savedAccount);
    }

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        Account existedAccount = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        if (existedAccount.getPassword() == null) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            Account account = (Account) authentication.getPrincipal();
            if (account.getStatus() == AccountStatus.SUSPENDED) {
                throw new AppException(ErrorCode.AUTH_ACCOUNT_SUSPENDED);
            }

            String accessToken = jwtService.generateAccessToken(account);
            String refreshToken = jwtService.generateRefreshToken(account);
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    @Transactional
    public AuthenticationResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleOAuthService.GoogleUserInfo googleUserInfo = googleOAuthService.verifyToken(request.getIdToken());

        Optional<Account> existingAccount = accountRepository.findByEmail(googleUserInfo.getEmail());

        Account account;

        if (existingAccount.isPresent()) {
            account = existingAccount.get();

            if (account.getStatus() == AccountStatus.SUSPENDED) {
                throw new AppException(ErrorCode.AUTH_ACCOUNT_SUSPENDED);
            }
        } else {
            Role role = roleRepository.findByName("CANDIDATE")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            account = Account.builder()
                    .email(googleUserInfo.getEmail())
                    .password(null)
                    .role(role)
                    .status(AccountStatus.ACTIVE)
                    .provider(AuthProvider.GOOGLE)
                    .verifiedAt(googleUserInfo.isEmailVerified() ? OffsetDateTime.now() : null)
                    .build();

            account = accountRepository.save(account);

            Candidate candidate = Candidate.builder()
                    .account(account)
                    .fullName(googleUserInfo.getName())
                    .avatarResourceId(123L)
                    .build();

            candidateRepository.save(candidate);
            log.info("New account created with Google: {}", account.getEmail());
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (tokenService.isBlacklisted(refreshToken)) {
            throw new AppException(ErrorCode.JWT_TOKEN_BLACKLISTED);
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new AppException(ErrorCode.JWT_EXPIRED_TOKEN);
        }

        String email = jwtService.extractEmail(refreshToken);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        String newAccessToken = jwtService.generateAccessToken(account);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken != null && !refreshToken.isEmpty()) {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
            }

            jwtService.blacklistToken(refreshToken);
            log.info("Refresh token blacklisted successfully");
        } else {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }
    }
}
