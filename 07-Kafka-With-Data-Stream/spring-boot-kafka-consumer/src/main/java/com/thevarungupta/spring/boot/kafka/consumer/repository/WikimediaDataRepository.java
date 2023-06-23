package com.thevarungupta.spring.boot.kafka.consumer.repository;

import com.thevarungupta.spring.boot.kafka.consumer.entity.WikimediaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikimediaDataRepository extends JpaRepository<WikimediaData, Long> {
}
