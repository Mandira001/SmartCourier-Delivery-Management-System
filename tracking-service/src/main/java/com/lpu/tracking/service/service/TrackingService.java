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
    public List<TrackingResponse> getTrackingHistory(String trackingNumber) {

        return trackingRepo
                .findByTrackingNumberOrderByTimestampAsc(trackingNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Upload Document
    public String uploadDocument(String trackingNumber, MultipartFile file) {

        if (trackingNumber == null || file.isEmpty()) {
            throw new RuntimeException("Invalid upload request");
        }

        try {
        	String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;

            file.transferTo(new File(filePath));

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
    public Document getDocumentById(Long id) {
        return documentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
    
    // Save Delivery Proof
    public String saveProof(DeliveryProof proof) {
        proof.setDeliveredAt(LocalDateTime.now());
        proofRepo.save(proof);
        return "Proof saved";
    }

    // Get Proof
    public DeliveryProof getProof(String trackingNumber) {
        return proofRepo.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Proof not found"));
    }

    // Mapper
    private TrackingResponse mapToResponse(TrackingEvent event) {

        TrackingResponse res = new TrackingResponse();
        res.setStatus(event.getStatus());
        res.setLocation(event.getLocation());
        res.setRemarks(event.getRemarks());
        res.setTimestamp(event.getTimestamp());

        return res;
    }
}
