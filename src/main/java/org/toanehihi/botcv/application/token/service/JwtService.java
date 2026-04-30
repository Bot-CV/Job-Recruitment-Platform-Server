package org.toanehihi.botcv.application.token.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.Account;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final TokenService tokenService;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String tokenIssuer;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.security.oauth2.resourceserver.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${spring.security.oauth2.resourceserver.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public String generateAccessToken(Account account) {
        return generateToken(account, accessTokenExpiration, "access_token");
    }

    public String generateRefreshToken(Account account) {
        return generateToken(account, refreshTokenExpiration, "refresh_token");
    }

    public String extractEmail(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes());
            boolean verified = signedJWT.verify(verifier);
            if (!verified) {
                return false;
            }
            Date expireTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (!expireTime.after(new Date())) {
                return false;
            }
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            return issuer.equals(tokenIssuer);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String tokenType = signedJWT.getJWTClaimsSet().getStringClaim("token_type");
            return "refresh_token".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getTokenExpiryDuration(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            Date now = new Date();
            return Math.max(0, expiration.getTime() - now.getTime());
        } catch (Exception e) {
            return 0;
        }
    }

    public void blacklistToken(String token) {
        long expiryTime = getTokenExpiryDuration(token);
        tokenService.addToBlacklist(token, expiryTime);
    }

    private String generateToken(Account account, long expiration, String tokenType) {
        try {
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(account.getEmail())
                    .issuer(tokenIssuer)
                    .issueTime(new Date())
                    .expirationTime(new Date(
                            Instant.now().plus(expiration, ChronoUnit.MILLIS).toEpochMilli()))
                    .claim("role", account.getRole().getName())
                    .claim("token_type", tokenType)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS512).build(), jwtClaimsSet);

            signedJWT.sign(new MACSigner(secretKey.getBytes()));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Can not create token: ", e);
            throw new AppException(ErrorCode.JWT_GENERATION_ERROR);
        }
    }
}
