package org.toanehihi.jobrecruitmentplatformserver.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "job_roles")
public class JobRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sub_family_id")
    @JsonIgnore
	private SubFamily subFamily;

	@CreationTimestamp
	@Column(name = "date_created")
	private OffsetDateTime dateCreated;

	@CreationTimestamp
	@Column(name = "date_updated")
	private OffsetDateTime dateUpdated;
}


