package com.library.domain.publisher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublisherRepository extends JpaRepository<Publisher, Long>, JpaSpecificationExecutor<Publisher> {
}
