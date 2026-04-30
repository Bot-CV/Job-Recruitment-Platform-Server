
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;

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
@Table(name = "candidates")
public class Candidate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "account_id", nullable = false, unique = true)
	private Account account;

	@Column(name = "full_name")
	private String fullName;

    private String phone;

	@ManyToOne
	@JoinColumn(name = "location_id")
	private Location location;

	@Enumerated(EnumType.STRING)
	@Column(name = "seniority")
	private SeniorityLevel seniority;

	@Column(name = "salary_expect_min")
	private Integer salaryExpectMin;

	@Column(name = "salary_expect_max")
	private Integer salaryExpectMax;

	@Column(name = "currency")
	private String currency;

	@Column(name = "remote_pref")
	private Boolean remotePref;

	@Column(name = "relocation_pref")
	private Boolean relocationPref;

	@Column(name = "avatar_resource_id")
	private Long avatarResourceId;

	@Column(name = "bio", columnDefinition = "text")
	private String bio;

	@CreationTimestamp
	@Column(name = "date_created")
	private OffsetDateTime dateCreated;

	@UpdateTimestamp
	@Column(name = "date_updated")
	private OffsetDateTime dateUpdated;

	@OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<CandidateSkill> skills = new HashSet<>();
}
