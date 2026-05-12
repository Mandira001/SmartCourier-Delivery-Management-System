package com.lpu.tracking.service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
// This is a JPA entity class that represents a document associated with a tracking number. 
// It has fields for tracking number, file name, file type, file path, and upload timestamp.
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber;

    private String fileName;

    private String fileType;

    private String filePath; // local path
    
    private LocalDateTime uploadedAt;

    @PrePersist
    public void setTimestamp() {
        this.uploadedAt = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Document() {
		super();
	}

}