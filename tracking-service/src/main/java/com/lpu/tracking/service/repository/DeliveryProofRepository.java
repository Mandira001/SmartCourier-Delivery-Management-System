package com.lpu.tracking.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lpu.tracking.service.entity.DeliveryProof;
// Repository interface for delivery proof, providing methods to retrieve delivery proof information based on tracking number and delivery time.
public interface DeliveryProofRepository extends JpaRepository<DeliveryProof, Long>{
    Optional<DeliveryProof> findFirstByTrackingNumberOrderByDeliveredAtDesc(String trackingNumber);

}
