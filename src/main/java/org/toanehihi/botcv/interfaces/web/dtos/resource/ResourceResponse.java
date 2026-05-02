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
    private ResourceType resourceType;
    private String contentType;
    private String publicId;
    private Long size;
    private String name;
    private String url;
    private OffsetDateTime uploadedAt;
}
