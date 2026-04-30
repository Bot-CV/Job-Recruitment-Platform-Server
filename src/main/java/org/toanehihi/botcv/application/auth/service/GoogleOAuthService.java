package org.toanehihi.botcv.application.auth.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GoogleOAuthService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthService(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleUserInfo verifyToken(String idToken) {
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            return GoogleUserInfo.builder()
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .picture((String) payload.get("picture"))
                    .emailVerified(payload.getEmailVerified())
                    .build();

        } catch (Exception e) {
            log.error("Failed to verify Google token: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GoogleUserInfo {
        private String email;
        private String name;
        private String picture;
        private boolean emailVerified;
    }
}
