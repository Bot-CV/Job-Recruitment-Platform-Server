package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "job_categories")
public class JobCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private JobCategory parent;

    @Column(name = "path", columnDefinition = "ltree")
    private String path;

    @Column(name = "is_leaf", nullable = false)
    @Builder.Default
    private boolean leaf = false;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private Set<JobCategory> children = new HashSet<>();
}
