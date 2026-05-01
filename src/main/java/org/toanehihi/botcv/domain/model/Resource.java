package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.toanehihi.botcv.domain.model.enums.ResourceType;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, columnDefinition = "resource_type")
    private ResourceType resourceType;

    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "name", nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;
}
