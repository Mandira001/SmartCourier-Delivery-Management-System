package com.lpu.admin.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import com.lpu.admin.service.client.DeliveryClient;
import com.lpu.admin.service.dto.DeliveryDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private DeliveryClient deliveryClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Dashboard
    @Test
    void testGetDashboard() {

        DeliveryDto d1 = new DeliveryDto();
        d1.setStatus("DELIVERED");

        DeliveryDto d2 = new DeliveryDto();
        d2.setStatus("IN_TRANSIT");

        when(deliveryClient.getAllDeliveries())
                .thenReturn(List.of(d1, d2));

        Map<String, Object> result = adminService.getDashboard();

        assertEquals(2, result.get("totalDeliveries"));
        assertEquals(1, result.get("delivered"));
        assertEquals(1, result.get("inTransit"));
    }

    // Get All Deliveries
    @Test
    void testGetAllDeliveries() {

        when(deliveryClient.getAllDeliveries())
                .thenReturn(List.of());

        List<DeliveryDto> result = adminService.getAllDeliveries();

        assertNotNull(result);
    }

    // Exception Deliveries
    @Test
    void testGetExceptionDeliveries() {

        DeliveryDto d1 = new DeliveryDto();
        d1.setStatus("FAILED");

        DeliveryDto d2 = new DeliveryDto();
        d2.setStatus("DELIVERED");

        when(deliveryClient.getAllDeliveries())
                .thenReturn(List.of(d1, d2));

        List<DeliveryDto> result = adminService.getExceptionDeliveries();

        assertEquals(1, result.size());
    }

    // Resolve Delivery
    @Test
    void testResolveDelivery() {

        DeliveryDto dto = new DeliveryDto();

        when(deliveryClient.updateStatus(1L, "DELIVERED"))
                .thenReturn(dto);

        DeliveryDto result = adminService.resolveDelivery(1L, "DELIVERED");

        assertNotNull(result);
    }

    // ✅ Test Reports
    @Test
    void testGenerateReports() {

        DeliveryDto d1 = new DeliveryDto();
        d1.setStatus("DELIVERED");

        DeliveryDto d2 = new DeliveryDto();
        d2.setStatus("DELIVERED");

        when(deliveryClient.getAllDeliveries())
                .thenReturn(List.of(d1, d2));

        Map<String, Long> result = adminService.generateReports();

        assertEquals(2L, result.get("DELIVERED"));
    }
}
