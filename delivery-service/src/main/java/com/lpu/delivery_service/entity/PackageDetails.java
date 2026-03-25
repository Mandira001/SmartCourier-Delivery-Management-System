package com.lpu.delivery_service.entity;

import jakarta.persistence.*;

@Entity
public class PackageDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double weight;
    private String type;   // Document, Fragile, etc.
    private double price;

    @OneToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // Constructors
    public PackageDetails() {}

    // Getters & Setters
    public Long getId() { return id; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Delivery getDelivery() { return delivery; }
    public void setDelivery(Delivery delivery) { this.delivery = delivery; }
}
