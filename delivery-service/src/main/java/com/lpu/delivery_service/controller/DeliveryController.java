package com.lpu.delivery_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lpu.delivery_service.dto.CreateDeliveryRequest;
import com.lpu.delivery_service.dto.DeliveryResponse;
import com.lpu.delivery_service.entity.Delivery;
import com.lpu.delivery_service.entity.DeliveryStatus;
import com.lpu.delivery_service.service.DeliveryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/deliveries")
// This class handles HTTP requests related to delivery operations. 
// It uses the DeliveryService to perform business logic and is secured with role-based access control.
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    // Create
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public DeliveryResponse create(
            @Valid @RequestBody CreateDeliveryRequest request,
            @RequestHeader("X-User-Email") String email) {

        return deliveryService.createDelivery(request, email);
    }

    // My Deliveries
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<Delivery> myDeliveries(
            @RequestHeader("X-User-Email") String email) {

        return deliveryService.getMyDeliveries(email);
    }

    // Get by ID
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public Delivery getById(@PathVariable Long id) {
        return deliveryService.getById(id);
    }

    // Update Status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Delivery updateStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {

        return deliveryService.updateStatus(id, status);
    }
    
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<Delivery> getAllDeliveries(){
    	return deliveryService.getAllDeliveries();
    }
}