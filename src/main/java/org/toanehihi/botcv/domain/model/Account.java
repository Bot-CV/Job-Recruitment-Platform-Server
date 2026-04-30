package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.botcv.domain.model.enums.AccountStatus;
import org.toanehihi.botcv.domain.model.enums.AuthProvider;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "accounts")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password")
	private String password;

	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, columnDefinition = "account_status")
	@Builder.Default
	private AccountStatus status = AccountStatus.ACTIVE;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, columnDefinition = "auth_provider")
	private AuthProvider provider;

	@Column(name = "verified_at")
	private OffsetDateTime verifiedAt;

	@CreationTimestamp
	@Column(name = "date_created", nullable = false)
	private OffsetDateTime dateCreated;

	@UpdateTimestamp
	@Column(name = "date_updated", nullable = false)
	private OffsetDateTime dateUpdated;
}
