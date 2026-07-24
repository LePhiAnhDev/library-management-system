package com.library.domain.author;

import com.library.common.BaseEntity;
import com.library.common.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Book author. Referenced by books through the many-to-many book_authors join.
 */
@Entity
@Table(name = "authors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Author extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "biography", length = 2000)
    private String biography;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;
}
