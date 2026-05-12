package com.lpu.delivery_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lpu.delivery_service.entity.Delivery;
// DeliveryRepository handles database operations for deliveries and provides a custom method to fetch deliveries of a specific user.
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByCustomerEmail(String email);
}
