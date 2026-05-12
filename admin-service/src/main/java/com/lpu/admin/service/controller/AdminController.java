package com.lpu.admin.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lpu.admin.service.dto.DeliveryDto;
import com.lpu.admin.service.service.AdminService;

import java.util.List;
import java.util.Map;
// Controller for admin-specific endpoints, handling operations related to delivery management and reporting.
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /** Summary stats + total revenue */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.getDashboard();
    }

    /** All deliveries list */
    @GetMapping("/deliveries")
    public List<DeliveryDto> deliveries() {
        return adminService.getAllDeliveries();
    }

    /** Deliveries in exception states */
    @GetMapping("/exceptions")
    public List<DeliveryDto> exceptions() {
        return adminService.getExceptionDeliveries();
    }

    /** Update a delivery's status */
    @PutMapping("/deliveries/{id}/resolve")
    public DeliveryDto resolve(
            @PathVariable Long id,
            @RequestParam String status) {
        return adminService.resolveDelivery(id, status);
    }

    /** Revenue grouped by month – used by the Revenue Trends chart */
    @GetMapping("/revenue-trend")
    public List<Map<String, Object>> revenueTrend() {
        return adminService.getRevenueTrend();
    }

    /** Status-count report */
    @GetMapping("/reports")
    public Map<String, Long> reports() {
        return adminService.generateReports();
    }

    @GetMapping("/users")
    public String users() { return adminService.getUsers(); }

    @GetMapping("/hubs")
    public String hubs() { return adminService.getHubs(); }
}
