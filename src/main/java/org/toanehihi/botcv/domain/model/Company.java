package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "companies")
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "website")
	private String website;

	private String description;

	private String phone;

	private String email;

	private String industry;

	@Column(name = "size")
	private String size;

	@Column(name = "logo_resource_id")
	private Long logoResourceId;

	@Column(name = "verified")
	@Builder.Default
	private boolean verified = false;

	@CreationTimestamp
	@Column(name = "date_created")
	private OffsetDateTime dateCreated;

	@UpdateTimestamp
	@Column(name = "date_updated")
	private OffsetDateTime dateUpdated;

	@OneToOne(mappedBy = "company")
	private Recruiter recruiter;

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<CompanyLocation> companyLocations = new HashSet<>();

	@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<AttestationResource> attestations = new HashSet<>();
}
