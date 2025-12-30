package com.example.demo.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InventoryRow {

    private final StringProperty assetCode = new SimpleStringProperty("");
    private final StringProperty assetName = new SimpleStringProperty("");
    private final IntegerProperty quantityInStock = new SimpleIntegerProperty(0);
    private final IntegerProperty actualQuantity = new SimpleIntegerProperty(0);

    public StringProperty assetCodeProperty() { return assetCode; }
    public StringProperty assetNameProperty() { return assetName; }
    public IntegerProperty quantityInStockProperty() { return quantityInStock; }
    public IntegerProperty actualQuantityProperty() { return actualQuantity; }

    public String getAssetCode() { return assetCode.get(); }
    public void setAssetCode(String v) { assetCode.set(v); }

    public String getAssetName() { return assetName.get(); }
    public void setAssetName(String v) { assetName.set(v); }

    public int getQuantityInStock() { return quantityInStock.get(); }
    public void setQuantityInStock(int v) { quantityInStock.set(v); }

    public int getActualQuantity() { return actualQuantity.get(); }
    public void setActualQuantity(int v) { actualQuantity.set(v); }

}
