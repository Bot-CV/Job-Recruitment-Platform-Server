package org.toanehihi.botcv.infrastructure.persistence.mappers.resource;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.Resource;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;

@Component
public class ResourceMapper {
    public ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .mimeType(resource.getMimeType())
                .resourceType(resource.getResourceType())
                .contentType(resource.getContentType())
                .url(resource.getUrl())
                .publicId(resource.getPublicId())
                .name(resource.getName())
                .uploadedAt(resource.getUploadedAt())
                .build();
    }
}
