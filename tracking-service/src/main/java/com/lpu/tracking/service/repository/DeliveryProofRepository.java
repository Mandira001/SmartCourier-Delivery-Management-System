package com.lpu.tracking.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lpu.tracking.service.entity.DeliveryProof;

public interface DeliveryProofRepository extends JpaRepository<DeliveryProof, Long>{
    Optional<DeliveryProof> findByTrackingNumber(String trackingNumber);

}
