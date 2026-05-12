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

    // ----------------------------------------------------------------
    // Dashboard – summary stats + total revenue
    // ----------------------------------------------------------------
    // getDashboard fetches all deliveries from Delivery Service and calculates summary statistics and total revenue.
    public Map<String, Object> getDashboard() {
        try {
            // Fetch all deliveries and compute stats
            List<DeliveryDto> deliveries = deliveryClient.getAllDeliveries();

            // Count by status
            long total     = deliveries.size();
            long delivered = deliveries.stream().filter(d -> "DELIVERED".equals(d.getStatus())).count();
            long inTransit = deliveries.stream().filter(d -> "IN_TRANSIT".equals(d.getStatus())).count();
            long pending   = deliveries.stream()
                    .filter(d -> "BOOKED".equals(d.getStatus()) || "PICKED_UP".equals(d.getStatus()))
                    .count();

            // Total revenue = sum of prices for ALL deliveries (not just delivered)
            double totalRevenue = deliveries.stream()
                    .mapToDouble(DeliveryDto::getPrice)
                    .sum();
            totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;

            // Prepare dashboard data
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalDeliveries", total);
            dashboard.put("delivered",        delivered);
            dashboard.put("inTransit",        inTransit);
            dashboard.put("pending",          pending);
            dashboard.put("totalRevenue",     totalRevenue);

            return dashboard;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Revenue trend – grouped by month (YYYY-MM)
    // ----------------------------------------------------------------
    // Note: This method calculates revenue based on the 'price' field of all deliveries, regardless of their status.
    public List<Map<String, Object>> getRevenueTrend() {
        try {
            List<DeliveryDto> deliveries = deliveryClient.getAllDeliveries();

            // Group price by YYYY-MM using a sorted map
            Map<String, Double> monthlyRevenue = new TreeMap<>();

            // Iterate through deliveries and sum prices by month
            for (DeliveryDto d : deliveries) {
                String createdAt = d.getCreatedAt();
                // Extract the year and month from the creation date (assuming format "YYYY-MM-DDTHH:MM:SSZ")
                if (createdAt != null && createdAt.length() >= 7) {
                    String monthKey = createdAt.substring(0, 7); // e.g. "2026-05"
                    // Sum the price for this month
                    monthlyRevenue.merge(monthKey, d.getPrice(), Double::sum);
                }
            }

            // Convert to list of maps for easier JSON serialization
            return monthlyRevenue.entrySet().stream()
                    .map(e -> {
                        // Round revenue to 2 decimal places
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("month",   e.getKey());
                        entry.put("revenue", Math.round(e.getValue() * 100.0) / 100.0);
                        return entry;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // All deliveries (for admin table)
    // ----------------------------------------------------------------
    // getAllDeliveries fetches all deliveries from the Delivery Service and returns them as a list of DeliveryDto objects.
    public List<DeliveryDto> getAllDeliveries() {
        try {
            // Fetch all deliveries from the Delivery Service
            return deliveryClient.getAllDeliveries();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Exception deliveries
    // ----------------------------------------------------------------
    // getExceptionDeliveries fetches all deliveries and filters those with status "EXCEPTION" to return a list of DeliveryDto objects representing the exception deliveries.
    public List<DeliveryDto> getExceptionDeliveries() {
        try {
            // Fetch all deliveries and filter those with status "EXCEPTION"
            return deliveryClient.getAllDeliveries().stream()
                    .filter(d -> "EXCEPTION".equals(d.getStatus()) || "FAILED".equals(d.getStatus()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Resolve / update delivery status
    // ----------------------------------------------------------------
    // resolveDelivery takes a delivery ID and a new status, calls the Delivery Service 
    // to update the delivery's status, and returns the updated DeliveryDto object.
    public DeliveryDto resolveDelivery(Long id, String status) {
        try {
            // Call the Delivery Service to update the delivery status and return the updated delivery
            return deliveryClient.updateStatus(id, status);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Status-count report
    // ----------------------------------------------------------------
    // generateReports fetches all deliveries and groups them by their status, counting how many deliveries fall into each status category, 
    // and returns a map where the keys are the delivery statuses and the values are the corresponding counts.
    public Map<String, Long> generateReports() {
        try {
            // Group deliveries by status and count them
            return deliveryClient.getAllDeliveries().stream()
                    .collect(Collectors.groupingBy(DeliveryDto::getStatus, Collectors.counting()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR: " + e.getMessage());
        }
    }

    public String getUsers() { return "User management via Auth Service"; }
    public String getHubs()  { return "Hub management module"; }
}
