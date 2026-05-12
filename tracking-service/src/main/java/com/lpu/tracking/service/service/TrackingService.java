package com.lpu.tracking.service.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lpu.tracking.service.dto.*;
import com.lpu.tracking.service.entity.*;
import com.lpu.tracking.service.repository.*;

@Service
public class TrackingService {

    @Autowired
    private TrackingRepository trackingRepo;

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private DeliveryProofRepository proofRepo;

    // Add Tracking Event
    // This method takes a request DTO, creates an entity, saves it, and returns a simple message.
    public String addTrackingEvent(TrackingEventRequest request) {

        TrackingEvent event = new TrackingEvent();
        event.setTrackingNumber(request.getTrackingNumber());
        event.setStatus(request.getStatus());
        event.setLocation(request.getLocation());
        event.setRemarks(request.getRemarks());

        trackingRepo.save(event);

        return "Tracking event added";
    }

    // Get Tracking History
    // This method fetches all tracking events for a given tracking number, maps them to response DTOs, and returns the list.
    public List<TrackingResponse> getTrackingHistory(String trackingNumber) {

        return trackingRepo
        // This fetches all events for a tracking number in time order.
                .findByTrackingNumberOrderByTimestampAsc(trackingNumber)
                .stream()
                // Then maps entity to DTO:
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Upload Document
    // This method handles file upload, saves the file to disk, creates a document record in the database, and returns a success message. 
    // It also includes error handling for invalid requests and file upload issues.
    public String uploadDocument(String trackingNumber, MultipartFile file) {

        if (trackingNumber == null || file.isEmpty()) {
            throw new RuntimeException("Invalid upload request");
        }

        try {
            // Creates an uploads folder inside service runtime directory.
        	String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;

            // 
            file.transferTo(new File(filePath));

            // Save document metadata to DB
            Document doc = new Document();
            doc.setTrackingNumber(trackingNumber);
            doc.setFileName(file.getOriginalFilename());
            doc.setFileType(file.getContentType());
            doc.setFilePath(filePath);

            documentRepo.save(doc);

            return "File uploaded successfully";

        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }
    
    // get document
    public List<Document> getDocuments(String trackingNumber) {

        if (trackingNumber == null) {
            throw new RuntimeException("Tracking number required");
        }

        return documentRepo.findByTrackingNumber(trackingNumber);
    }
    
    //get document by id
    // This method retrieves a document by its ID. If the document is not found, it throws an exception. 
    // This allows clients to fetch specific documents related to a tracking number.
    public Document getDocumentById(Long id) {
        return documentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
    
    // Save Delivery Proof
    // This method saves delivery proof information, including the timestamp of delivery. 
    // It takes a request DTO, creates an entity, saves it to the database, and returns a success message.
    public String saveProof(DeliveryProof proof) {
        proof.setDeliveredAt(LocalDateTime.now());
        proofRepo.save(proof);
        return "Proof saved";
    }

    // Get Proof
    // This method retrieves the most recent delivery proof for a given tracking number. 
    // If no proof is found, it throws an exception.
    public DeliveryProof getProof(String trackingNumber) {
        return proofRepo.findFirstByTrackingNumberOrderByDeliveredAtDesc(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Proof not found"));
    }

    // Mapper
    // This is a simple mapper method that converts a TrackingEvent entity to a TrackingResponse DTO.
    private TrackingResponse mapToResponse(TrackingEvent event) {

        TrackingResponse res = new TrackingResponse();
        res.setStatus(event.getStatus());
        res.setLocation(event.getLocation());
        res.setRemarks(event.getRemarks());
        res.setTimestamp(event.getTimestamp());

        return res;
    }
}
