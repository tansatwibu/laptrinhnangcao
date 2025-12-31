package com.example.demo.controller;

import com.example.demo.model.InventoryRow;
import com.example.demo.util.DatabaseUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventoryDialogController {

    @FXML private TableView<InventoryRow> inventoryTable;
    @FXML private TableColumn<InventoryRow, String> colCode;
    @FXML private TableColumn<InventoryRow, String> colName;
    @FXML private TableColumn<InventoryRow, Integer> colInStock;
    @FXML private TableColumn<InventoryRow, Integer> colActual;

    private final ObservableList<InventoryRow> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colCode.setCellValueFactory(c -> c.getValue().assetCodeProperty());
        colName.setCellValueFactory(c -> c.getValue().assetNameProperty());
        colInStock.setCellValueFactory(c -> c.getValue().quantityInStockProperty().asObject());
        colActual.setCellValueFactory(c -> c.getValue().actualQuantityProperty().asObject());

        // Converter that displays empty string when value is < 0 (not set),
        // and parses empty input as -1 (not set)
        StringConverter<Integer> emptyAwareConverter = new StringConverter<>() {
            @Override
            public String toString(Integer object) {
                if (object == null || object < 0) return "";
                return object.toString();
            }

            @Override
            public Integer fromString(String string) {
                if (string == null || string.trim().isEmpty()) return -1;
                try {
                    return Integer.valueOf(string.trim());
                } catch (NumberFormatException ex) {
                    return -1;
                }
            }
        };

        colActual.setCellFactory(TextFieldTableCell.forTableColumn(emptyAwareConverter));
        colActual.setOnEditCommit(e -> {
            Integer v = e.getNewValue();
            e.getRowValue().setActualQuantity(v == null ? -1 : v);
        });

        inventoryTable.setEditable(true);

        loadAssets();
    }

    private void loadAssets() {
        rows.clear();

        String sql = "SELECT asset_code, asset_name, quantity_in_stock FROM assets ORDER BY asset_name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryRow r = new InventoryRow();
                r.setAssetCode(rs.getString("asset_code"));
                r.setAssetName(rs.getString("asset_name"));
                int q = rs.getInt("quantity_in_stock");
                r.setQuantityInStock(q);
                r.setActualQuantity(-1); // mặc định để trống, chờ người dùng nhập
                rows.add(r);
            }

            inventoryTable.setItems(rows);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Called by dialog OK button filter; returns true if saved */
    public boolean handleSave() {
        // Ensure any active cell edit is committed/ended before reading values
        try {
            inventoryTable.edit(-1, null);
        } catch (Exception ignored) {
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            String sql = "UPDATE assets SET quantity_in_stock = ? WHERE asset_code = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            int changed = 0;
            for (InventoryRow r : rows) {
                // skip rows where actual is not set (negative)
                int actual = r.getActualQuantity();
                if (actual >= 0 && actual != r.getQuantityInStock()) {
                    ps.setInt(1, actual);
                    ps.setString(2, r.getAssetCode());
                    ps.executeUpdate();
                    changed++;
                }
            }

            conn.commit();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
