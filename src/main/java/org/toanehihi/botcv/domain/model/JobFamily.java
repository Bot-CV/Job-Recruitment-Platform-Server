package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "job_families")
public class JobFamily {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, unique = true, length = 100)
	private String name;

	@Column(name = "slug", nullable = false, unique = true, length = 100)
	private String slug;

	@CreationTimestamp
	@Column(name = "date_created")
	private OffsetDateTime dateCreated;

	@CreationTimestamp
	@Column(name = "date_updated")
	private OffsetDateTime dateUpdated;

	@OneToMany(mappedBy = "jobFamily")
	@Builder.Default
	private Set<SubFamily> subFamilies = new HashSet<>();
}


