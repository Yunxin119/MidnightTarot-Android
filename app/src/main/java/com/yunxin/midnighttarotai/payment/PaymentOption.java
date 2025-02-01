package com.yunxin.midnighttarotai.payment;

public class PaymentOption {
    private String title;
    private String description;
    private float price;
    private PaymentType type;

    public PaymentOption(String title, String description, float price, PaymentType type) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.type = type;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }

    public PaymentType getType() {
        return type;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }
}
