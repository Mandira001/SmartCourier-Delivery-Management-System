package com.lpu.delivery_service.dto;

public class DeliveryRequest {
	private String sender;
	private String receiver;
	private String address;
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public DeliveryRequest() {
		super();
	}
	public DeliveryRequest(String sender, String receiver, String address) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.address = address;
	}
}
