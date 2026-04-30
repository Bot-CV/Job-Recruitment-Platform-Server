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
@Table(name = "skills")
public class Skill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", unique = true)
	private String name;

	// Map JSONB as text to keep it simple and portable. Add a converter later if
	// needed.
	@Column(name = "aliases", columnDefinition = "jsonb")
	private String aliases;

	@CreationTimestamp
	@Column(name = "date_created", nullable = false)
	private OffsetDateTime dateCreated;

	@ManyToMany(mappedBy = "skills")
	@Builder.Default
	private Set<Job> jobs = new HashSet<>();
}
