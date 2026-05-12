package com.lpu.tracking.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.lpu.tracking.service.dto.TrackingEventRequest;
import com.lpu.tracking.service.entity.*;
import com.lpu.tracking.service.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class TrackingServiceTest {

    @InjectMocks
    private TrackingService trackingService;

    @Mock
    private TrackingRepository trackingRepo;

    @Mock
    private DocumentRepository documentRepo;

    @Mock
    private DeliveryProofRepository proofRepo;

    private TrackingEvent event;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        event = new TrackingEvent();
        event.setTrackingNumber("123");
        event.setStatus(TrackingStatus.IN_TRANSIT);
        event.setLocation("City");
        event.setRemarks("Test");
    }

    // Add Tracking Event
    @Test
    void testAddTrackingEvent() {

        TrackingEventRequest request = new TrackingEventRequest();
        request.setTrackingNumber("123");
        request.setStatus(TrackingStatus.IN_TRANSIT);
        request.setLocation("City");
        request.setRemarks("Test");

        String result = trackingService.addTrackingEvent(request);

        assertEquals("Tracking event added", result);
        verify(trackingRepo, times(1)).save(any(TrackingEvent.class));
    }

    // Get Tracking History
    @Test
    void testGetTrackingHistory() {

        when(trackingRepo.findByTrackingNumberOrderByTimestampAsc("123"))
                .thenReturn(List.of(event));

        List<?> result = trackingService.getTrackingHistory("123");

        assertEquals(1, result.size());
    }

    // Save Proof
    @Test
    void testSaveProof() {

        DeliveryProof proof = new DeliveryProof();
        proof.setTrackingNumber("123");

        String result = trackingService.saveProof(proof);

        assertEquals("Proof saved", result);
        verify(proofRepo, times(1)).save(any(DeliveryProof.class));
    }

    // Get Proof (Success)
    @Test
    void testGetProof_Success() {

        DeliveryProof proof = new DeliveryProof();
        proof.setTrackingNumber("123");

        when(proofRepo.findFirstByTrackingNumberOrderByDeliveredAtDesc("123"))
                .thenReturn(java.util.Optional.of(proof));

        DeliveryProof result = trackingService.getProof("123");

        assertNotNull(result);
    }

    // Get Proof (Not Found)
    @Test
    void testGetProof_NotFound() {

        when(proofRepo.findFirstByTrackingNumberOrderByDeliveredAtDesc("123"))
                .thenReturn(java.util.Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            trackingService.getProof("123");
        });

        assertEquals("Proof not found", ex.getMessage());
    }
}
