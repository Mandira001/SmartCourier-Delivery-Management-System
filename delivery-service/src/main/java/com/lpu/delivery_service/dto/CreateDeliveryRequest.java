package com.lpu.delivery_service.dto;

import com.lpu.delivery_service.entity.Address;

public class CreateDeliveryRequest {
	private Address senderAddress;
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
