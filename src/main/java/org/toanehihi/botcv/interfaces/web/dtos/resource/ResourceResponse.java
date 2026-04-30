package org.toanehihi.botcv.interfaces.web.dtos.resource;

import java.time.OffsetDateTime;

import org.toanehihi.botcv.domain.model.enums.ResourceType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceResponse {
    private Long id;
    private String mimeType;
    private ResourceType resourceType;
    private String contentType;
    private String url;
    private String publicId;
    private String name;
    private OffsetDateTime uploadedAt;
}
