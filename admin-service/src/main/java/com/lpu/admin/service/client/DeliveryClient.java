package com.lpu.admin.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lpu.admin.service.dto.DeliveryDto;
// Feign client interface to communicate with the Delivery Service. 
// It defines methods to fetch all deliveries and update delivery status, 
// which are used by the AdminService to perform various operations related to deliveries.
@FeignClient(name = "delivery-service")
public interface DeliveryClient {
    // getAllDeliveries calls the Delivery Service to retrieve a list of all deliveries, 
    // returning them as a list of DeliveryDto objects.
	@GetMapping("/deliveries")
    List<DeliveryDto> getAllDeliveries();

    // updateStatus takes a delivery ID and a new status, 
    // sends a request to the Delivery Service to update the delivery's status,
    // and returns the updated DeliveryDto object representing the delivery with its new status.
    @PutMapping("/deliveries/{id}")
    DeliveryDto updateStatus(@PathVariable Long id,
                             @RequestParam String status);
}
