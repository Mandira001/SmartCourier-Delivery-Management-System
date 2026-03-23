package com.lpu.delivery_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lpu.delivery_service.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer>{

}
