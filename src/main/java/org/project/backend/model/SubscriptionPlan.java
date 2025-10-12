package org.project.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, nullable = false, unique = true)
    private String name;

    //... các trường khác
    @Column(nullable = false)
    private BigDecimal price;
    @Column
    private Integer scanLimitPerMonth;
    @Column(columnDefinition = "TEXT")
    private String description;
    // Getters and Setters


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getScanLimitPerMonth() {
        return scanLimitPerMonth;
    }

    public void setScanLimitPerMonth(Integer scanLimitPerMonth) {
        this.scanLimitPerMonth = scanLimitPerMonth;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}