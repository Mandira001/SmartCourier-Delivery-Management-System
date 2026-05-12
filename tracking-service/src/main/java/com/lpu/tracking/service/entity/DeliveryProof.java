package com.lpu.tracking.service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
// Entity class for delivery proof, storing information about the delivery, receiver, and proof image path.
@Entity
public class DeliveryProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber;

    private String receiverName;

    private String proofImage; // image path

    private LocalDateTime deliveredAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getProofImage() {
		return proofImage;
	}

	public void setProofImage(String proofImage) {
		this.proofImage = proofImage;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public DeliveryProof() {
		super();
	}
}
