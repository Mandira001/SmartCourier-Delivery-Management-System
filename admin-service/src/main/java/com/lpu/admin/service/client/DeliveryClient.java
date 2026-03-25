package com.lpu.admin.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lpu.admin.service.dto.DeliveryDto;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {
	@GetMapping("/deliveries")
    List<DeliveryDto> getAllDeliveries();

    @PutMapping("/deliveries/{id}")
    DeliveryDto updateStatus(@PathVariable Long id,
                             @RequestParam String status);
}
