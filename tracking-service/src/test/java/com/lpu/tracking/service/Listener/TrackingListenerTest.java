package com.lpu.tracking.service.Listener;

import static org.mockito.Mockito.*;

import com.lpu.tracking.service.entity.TrackingEvent;
import com.lpu.tracking.service.listener.TrackingListener;
import com.lpu.tracking.service.repository.TrackingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class TrackingListenerTest {

    @InjectMocks
    private TrackingListener trackingListener;

    @Mock
    private TrackingRepository trackingRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Test message consumption
    @Test
    void testConsumeMessage() {

        String message = "ABC123|IN_TRANSIT";

        trackingListener.consume(message);

        verify(trackingRepository, times(1))
                .save(any(TrackingEvent.class));
    }
}