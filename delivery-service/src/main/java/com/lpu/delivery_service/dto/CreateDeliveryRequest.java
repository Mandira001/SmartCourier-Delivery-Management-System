package com.lpu.delivery_service.dto;

import com.lpu.delivery_service.entity.Address;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CreateDeliveryRequest {
	@Valid
	@NotNull(message = "Sender address is required")
	private Address senderAddress;
	@Valid
	@NotNull(message = "Receiver address is required")
    private Address receiverAddress;
	public Address getSenderAddress() {
		return senderAddress;
	}
	public void setSenderAddress(Address senderAddress) {
		this.senderAddress = senderAddress;
	}
	public Address getReceiverAddress() {
		return receiverAddress;
	}
	public void setReceiverAddress(Address receiverAddress) {
		this.receiverAddress = receiverAddress;
	}
    
    
}
