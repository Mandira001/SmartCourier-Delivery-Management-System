package com.lpu.admin.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lpu.admin.service.dto.DeliveryDto;
import com.lpu.admin.service.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/deliveries")
    public List<DeliveryDto> deliveries() {
        return adminService.getAllDeliveries();
    }

    @GetMapping("/exceptions")
    public List<DeliveryDto> exceptions() {
        return adminService.getExceptionDeliveries();
    }

    @PutMapping("/deliveries/{id}/resolve")
    public DeliveryDto resolve(
            @PathVariable Long id,
            @RequestParam String status) {

        return adminService.resolveDelivery(id, status);
    }

    @GetMapping("/reports")
    public Map<String, Long> reports() {
        return adminService.generateReports();
    }

    @GetMapping("/users")
    public String users() {
        return adminService.getUsers();
    }

    @GetMapping("/hubs")
    public String hubs() {
        return adminService.getHubs();
    }
}
