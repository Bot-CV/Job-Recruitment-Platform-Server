package org.toanehihi.botcv.application.token.service;

public interface TokenService {
    void addToBlacklist(String token, long expiryTime);

    void addToken(String prefix, String token, String value);

    boolean isBlacklisted(String token);

    String getValue(String key);

    void deleteValue(String key);
}
