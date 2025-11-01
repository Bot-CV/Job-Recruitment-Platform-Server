package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource;

import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Resource;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

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
