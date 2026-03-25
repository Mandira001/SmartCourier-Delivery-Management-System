package com.lpu.tracking.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lpu.tracking.service.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long>{
	List<Document> findByTrackingNumber(String trackingNumber);
}
