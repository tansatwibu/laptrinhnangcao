package com.example.demo.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class AssetTransaction {

    // ========== ENUM NGAY TRONG CLASS ==========
    public enum TransactionType {
        IMPORT,
        TRANSFER,
        REDUCE
    }

    // ===========================================

    private String assetCode;
    private TransactionType transactionType;
    private String transactionDate;
    private int quantity;
    private String fromUnit;
    private String toUnit;

    // gộp person_in_charge + performed_by
    private String performedBy;

    private String conditionStatus;
    private String reason;
    private String decisionNumber;
    private int productionYear;

    private final DoubleProperty unitPrice = new SimpleDoubleProperty(0.0);

    // ===== GETTERS - SETTERS =====

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFromUnit() {
        return fromUnit;
    }

    public void setFromUnit(String fromUnit) {
        this.fromUnit = fromUnit;
    }

    public String getToUnit() {
        return toUnit;
    }

    public void setToUnit(String toUnit) {
        this.toUnit = toUnit;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDecisionNumber() {
        return decisionNumber;
    }

    public void setDecisionNumber(String decisionNumber) {
        this.decisionNumber = decisionNumber;
    }

    public int getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(int productionYear) {
        this.productionYear = productionYear;
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public void setUnitPrice(double v) {
        unitPrice.set(v);
    }

    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }
}
