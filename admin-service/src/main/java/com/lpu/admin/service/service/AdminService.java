package com.lpu.admin.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lpu.admin.service.client.DeliveryClient;
import com.lpu.admin.service.dto.DeliveryDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private DeliveryClient deliveryClient;

    // Dashboard
    public Map<String, Object> getDashboard() {
        try {
            List<DeliveryDto> deliveries = deliveryClient.getAllDeliveries();

            long total = deliveries.size();

            long delivered = deliveries.stream()
                    .filter(d -> "DELIVERED".equals(d.getStatus()))
                    .count();

            long inTransit = deliveries.stream()
                    .filter(d -> "IN_TRANSIT".equals(d.getStatus()))
                    .count();

            long pending = deliveries.stream()
                    .filter(d -> "BOOKED".equals(d.getStatus()) ||
                                 "PICKED_UP".equals(d.getStatus()))
                    .count();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalDeliveries", total);
            dashboard.put("delivered", delivered);
            dashboard.put("inTransit", inTransit);
            dashboard.put("pending", pending);

            return dashboard;

        } catch (Exception e) {
        	e.printStackTrace();   // 👈 ADD THIS
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // Monitoring
    public List<DeliveryDto> getAllDeliveries() {
        try {
            return deliveryClient.getAllDeliveries();
        } catch (Exception e) {
        	e.printStackTrace();   // 👈 ADD THIS
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // Exception Handling
    public List<DeliveryDto> getExceptionDeliveries() {
        try {
            return deliveryClient.getAllDeliveries()
                    .stream()
                    .filter(d ->
                            "FAILED".equals(d.getStatus()) ||
                            "DELAYED".equals(d.getStatus()) ||
                            "RETURNED".equals(d.getStatus())
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
        	e.printStackTrace();   // 👈 ADD THIS
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // Resolve
    public DeliveryDto resolveDelivery(Long id, String status) {
        try {
            return deliveryClient.updateStatus(id, status);
        } catch (Exception e) {
        	e.printStackTrace();   // 👈 ADD THIS
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // Reports
    public Map<String, Long> generateReports() {
        try {
            List<DeliveryDto> deliveries = deliveryClient.getAllDeliveries();

            return deliveries.stream()
                    .collect(Collectors.groupingBy(
                            DeliveryDto::getStatus,
                            Collectors.counting()
                    ));
        } catch (Exception e) {
        	e.printStackTrace();   // 👈 ADD THIS
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // Users
    public String getUsers() {
        return "User management via Auth Service";
    }

    // Hubs
    public String getHubs() {
        return "Hub management module";
    }
}
