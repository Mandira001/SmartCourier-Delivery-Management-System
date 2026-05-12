package com.lpu.tracking.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lpu.tracking.service.entity.Document;
// Repository interface for documents, providing methods to retrieve documents based on tracking number.
public interface DocumentRepository extends JpaRepository<Document, Long>{
	List<Document> findByTrackingNumber(String trackingNumber);
}
