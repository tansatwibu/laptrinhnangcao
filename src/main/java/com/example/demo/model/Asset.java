package com.example.demo.model;

public class Asset {

    private String assetCode;
    private String assetName;
    private String assetType;
    private String supplier;

    private int quantityTotal;
    private int quantityInStock;

    // Tổng giá trị = SUM(unit_price * quantity) trong asset_transactions
    private double totalValue;

    // ===== GETTER / SETTER =====

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public int getQuantityTotal() {
        return quantityTotal;
    }

    public void setQuantityTotal(int quantityTotal) {
        this.quantityTotal = quantityTotal;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetCode='" + assetCode + '\'' +
                ", assetName='" + assetName + '\'' +
                ", assetType='" + assetType + '\'' +
                ", supplier='" + supplier + '\'' +
                ", quantityTotal=" + quantityTotal +
                ", quantityInStock=" + quantityInStock +
                ", totalValue=" + totalValue +
                '}';
    }
}
