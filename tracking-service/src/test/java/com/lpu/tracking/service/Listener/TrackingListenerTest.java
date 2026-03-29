package com.lpu.tracking.service.Listener;

import static org.mockito.Mockito.*;

import com.lpu.tracking.service.entity.TrackingEvent;
import com.lpu.tracking.service.listener.TrackingListener;
import com.lpu.tracking.service.repository.TrackingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class TrackingListenerTest {

    @InjectMocks
    private TrackingListener trackingListener;

    @Mock
    private TrackingRepository trackingRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConsumeMessage() {

        String payload = "ABC123|IN_TRANSIT";

        MessageProperties props = new MessageProperties();
        props.setHeader("traceId", "test-trace-id");
        props.setHeader("spanId", "test-span-id");

        Message message = new Message(payload.getBytes(), props);

        trackingListener.consume(message);

        verify(trackingRepository, times(1))
                .save(any(TrackingEvent.class));
    }
}