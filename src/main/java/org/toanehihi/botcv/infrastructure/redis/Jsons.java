package org.toanehihi.botcv.infrastructure.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;

public final class Jsons {
    private static final ObjectMapper M = new ObjectMapper().registerModule(new JavaTimeModule());

    private Jsons() {
    }

    public static String toJson(Object o) {
        try {
            return M.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        if (json == null)
            return Map.of();
        try {
            return M.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
