package com.example.demo.model;

import javafx.beans.property.*;

public class NewAssetRow {

    private final StringProperty assetCode = new SimpleStringProperty("");
    private final StringProperty assetName = new SimpleStringProperty("");
    private final StringProperty assetType = new SimpleStringProperty("");

    private final IntegerProperty productionYear = new SimpleIntegerProperty(0);
    private final DoubleProperty unitPrice = new SimpleDoubleProperty(0);
    private final IntegerProperty quantity = new SimpleIntegerProperty(0);

    // ======= PROPERTIES =======

    public StringProperty assetCodeProperty() { return assetCode; }
    public StringProperty assetNameProperty() { return assetName; }
    public StringProperty assetTypeProperty() { return assetType; }

    public IntegerProperty productionYearProperty() { return productionYear; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public IntegerProperty quantityProperty() { return quantity; }

    // ======= GETTERS / SETTERS =======

    public String getAssetCode() { return assetCode.get(); }
    public void setAssetCode(String v) { assetCode.set(v); }

    public String getAssetName() { return assetName.get(); }
    public void setAssetName(String v) { assetName.set(v); }

    public String getAssetType() { return assetType.get(); }
    public void setAssetType(String v) { assetType.set(v); }

    public int getProductionYear() { return productionYear.get(); }
    public void setProductionYear(int v) { productionYear.set(v); }

    public double getUnitPrice() { return unitPrice.get(); }
    public void setUnitPrice(double v) { unitPrice.set(v); }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int v) { quantity.set(v); }

    @Override
    public String toString() {
        return "NewAssetRow{" +
                "assetCode='" + getAssetCode() + '\'' +
                ", assetName='" + getAssetName() + '\'' +
                ", assetType='" + getAssetType() + '\'' +
                ", productionYear=" + getProductionYear() +
                ", unitPrice=" + getUnitPrice() +
                ", quantity=" + getQuantity() +
                '}';
    }
}
