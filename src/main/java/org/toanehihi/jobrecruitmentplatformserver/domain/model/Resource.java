package org.toanehihi.jobrecruitmentplatformserver.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;

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

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "owner_id")
	private Long ownerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "owner_type")
	private ResourceType ownerType;

	@Column(name = "url")
	private String url;

	@Column(name = "public_id", nullable = false, unique = true)
	private String publicId;

	@Column(name = "name")
	private String name;

	@CreationTimestamp
	@Column(name = "uploaded_at")
	private OffsetDateTime uploadedAt;
}
