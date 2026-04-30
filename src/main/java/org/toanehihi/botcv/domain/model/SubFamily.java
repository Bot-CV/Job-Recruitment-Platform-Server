package org.toanehihi.botcv.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "sub_families")
public class SubFamily {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, unique = true, length = 100)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_family_id", nullable = false)
    @JsonIgnore
	private JobFamily jobFamily;

	@CreationTimestamp
	@Column(name = "date_created", nullable = false)
	private OffsetDateTime dateCreated;

	@CreationTimestamp
	@Column(name = "date_updated", nullable = false)
	private OffsetDateTime dateUpdated;

	@OneToMany(mappedBy = "subFamily", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<JobRole> jobRoles = new HashSet<>();
}


