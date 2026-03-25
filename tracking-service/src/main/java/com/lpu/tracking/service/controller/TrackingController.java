package com.lpu.tracking.service.controller;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lpu.tracking.service.dto.*;
import com.lpu.tracking.service.entity.*;
import com.lpu.tracking.service.service.TrackingService;
import org.springframework.http.*;

@RestController
@RequestMapping("/tracking")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    // Add Event
    @PostMapping("/events")
    public String addEvent(@RequestBody TrackingEventRequest request) {
        return trackingService.addTrackingEvent(request);
    }

    // Get Tracking History
    @GetMapping("/{trackingNumber}")
    public List<TrackingResponse> getTracking(@PathVariable String trackingNumber) {
        return trackingService.getTrackingHistory(trackingNumber);
    }

    // Upload Document
    @PostMapping("/documents/upload")
    public String uploadDocument(
            @RequestParam String trackingNumber,
            @RequestParam("file") MultipartFile file) {

        return trackingService.uploadDocument(trackingNumber, file);
    }
    
    //get document
    @GetMapping("/documents/{trackingNumber}")
    public List<Document> getDocuments(@PathVariable String trackingNumber) {
        return trackingService.getDocuments(trackingNumber);
    }
    
    //download document
    @GetMapping("/documents/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {

        Document doc = trackingService.getDocumentById(id);

        File file = new File(doc.getFilePath());
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + doc.getFileName())
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .body(resource);
    }

    // Save Proof
    @PostMapping("/proof")
    public String saveProof(@RequestBody DeliveryProof proof) {
        return trackingService.saveProof(proof);
    }

    // Get Proof
    @GetMapping("/{trackingNumber}/proof")
    public DeliveryProof getProof(@PathVariable String trackingNumber) {
        return trackingService.getProof(trackingNumber);
    }
}
