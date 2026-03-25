package com.lpu.delivery_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lpu.delivery_service.dto.CreateDeliveryRequest;
import com.lpu.delivery_service.dto.DeliveryResponse;
import com.lpu.delivery_service.entity.Delivery;
import com.lpu.delivery_service.entity.DeliveryStatus;
import com.lpu.delivery_service.service.DeliveryService;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    // Create
    @PostMapping
    public DeliveryResponse create(
            @RequestBody CreateDeliveryRequest request,
            @RequestHeader("X-User-Email") String email) {

        return deliveryService.createDelivery(request, email);
    }

    // My Deliveries
    @GetMapping("/my")
    public List<Delivery> myDeliveries(
            @RequestHeader("X-User-Email") String email) {

        return deliveryService.getMyDeliveries(email);
    }

    // Get by ID
    @GetMapping("/{id}")
    public Delivery getById(@PathVariable Long id) {
        return deliveryService.getById(id);
    }

    // Update Status
    @PutMapping("/{id}")
    public Delivery updateStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {

        return deliveryService.updateStatus(id, status);
    }
    
    @GetMapping
    public List<Delivery> getAllDeliveries(){
    	return deliveryService.getAllDeliveries();
    }
}