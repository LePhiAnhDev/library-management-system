package com.library.domain.publisher;

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
 * Book publisher.
 */
@Entity
@Table(name = "publishers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Publisher extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;
}
