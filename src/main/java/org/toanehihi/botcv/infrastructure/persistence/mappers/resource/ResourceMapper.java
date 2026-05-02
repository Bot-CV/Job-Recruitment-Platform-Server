package org.toanehihi.botcv.infrastructure.persistence.mappers.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.Resource;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;

@Component
public class ResourceMapper {

    @Value("${app.cloudinary.base-url:}")
    private String cloudinaryBaseUrl;

    public ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .resourceType(resource.getResourceType())
                .contentType(resource.getContentType())
                .publicId(resource.getPublicId())
                .size(resource.getSize())
                .name(resource.getName())
                .url(buildUrl(resource.getPublicId()))
                .uploadedAt(resource.getUploadedAt())
                .build();
    }

    private String buildUrl(String publicId) {
        if (publicId == null || cloudinaryBaseUrl == null || cloudinaryBaseUrl.isBlank()) {
            return null;
        }
        return cloudinaryBaseUrl + "/" + publicId;
    }
}
