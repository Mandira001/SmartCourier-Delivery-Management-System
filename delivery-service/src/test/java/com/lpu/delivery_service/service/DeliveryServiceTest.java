package com.lpu.delivery_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.lpu.delivery_service.dto.CreateDeliveryRequest;
import com.lpu.delivery_service.dto.DeliveryResponse;
import com.lpu.delivery_service.entity.Address;
import com.lpu.delivery_service.entity.Delivery;
import com.lpu.delivery_service.entity.DeliveryStatus;
import com.lpu.delivery_service.repository.DeliveryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

class DeliveryServiceTest {

    @InjectMocks
    private DeliveryService deliveryService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    private Delivery delivery;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        delivery = new Delivery();
        delivery.setStatus(DeliveryStatus.BOOKED);
        delivery.setTrackingNumber("123");

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("trace-123");
        when(traceContext.spanId()).thenReturn("span-123");
    }
    
    @Test
    void testCreateDelivery_Success() {

        // Arrange
        CreateDeliveryRequest request = new CreateDeliveryRequest();

        Address sender = new Address();
        sender.setName("Sender");
        sender.setCity("City");

        Address receiver = new Address();
        receiver.setName("Receiver");
        receiver.setCity("City");

        request.setSenderAddress(sender);
        request.setReceiverAddress(receiver);

        when(deliveryRepository.save(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DeliveryResponse response =
                deliveryService.createDelivery(request, "test@mail.com");

        // Assert
        assertNotNull(response);
        assertEquals(DeliveryStatus.BOOKED, response.getStatus());
        assertNotNull(response.getSenderAddress());
        assertNotNull(response.getReceiverAddress());
        assertNotNull(response.getTrackingNumber());
    }

    @Test
    void testGetById_Success() {

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.getById(1L);

        assertNotNull(result);
        assertEquals("123", result.getTrackingNumber());
    }

    @Test
    void testGetById_NotFound() {

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deliveryService.getById(1L);
        });

        assertEquals("Delivery not found", exception.getMessage());
    }

    @Test
    void testUpdateStatus_ValidTransition() {

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(delivery));

        when(deliveryRepository.save(any(Delivery.class)))
                .thenReturn(delivery);

        Delivery result = deliveryService.updateStatus(1L, DeliveryStatus.PICKED_UP);

        assertEquals(DeliveryStatus.PICKED_UP, result.getStatus());

        verify(rabbitTemplate, times(1))
                .send(eq("tracking_queue"), any(org.springframework.amqp.core.Message.class));
    }

    @Test
    void testUpdateStatus_InvalidTransition() {

        delivery.setStatus(DeliveryStatus.DELIVERED);

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(delivery));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deliveryService.updateStatus(1L, DeliveryStatus.BOOKED);
        });

        assertTrue(exception.getMessage().contains("Invalid transition"));
    }
}
