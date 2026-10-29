package com.logiclegend.inventory.model;

public class Product {
    private int id;
    private String productCode;
    private String name;
    private String category;
    private double costPrice;
    private double unitPrice;
    private int stockQuantity;
    private int reorderLevel;
    private String createdAt;

    public Product() {}

    public Product(String productCode, String name, String category,
                   double costPrice, double unitPrice, int stockQuantity, int reorderLevel) {
        this.productCode = productCode;
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (₹%,.2f, Stock: %d)", productCode, name, unitPrice, stockQuantity);
    }
}

