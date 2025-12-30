package com.example.demo.controller;

import com.example.demo.model.AssetTransaction;
import com.example.demo.util.DatabaseUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AssetTransactionController {

    @FXML
    private Label lblHeader;
    @FXML
    private TableView<AssetTransaction> transactionTable;

    @FXML
    private TableColumn<AssetTransaction, String> colDate;
    @FXML
    private TableColumn<AssetTransaction, String> colType;
    @FXML
    private TableColumn<AssetTransaction, String> colQuantity;
    @FXML
    private TableColumn<AssetTransaction, String> colFrom;
    @FXML
    private TableColumn<AssetTransaction, String> colTo;
    @FXML
    private TableColumn<AssetTransaction, String> colUser;
    @FXML
    private TableColumn<AssetTransaction, String> colPrice;

    private final ObservableList<AssetTransaction> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionDate()));

        // FIX: enum → String
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionType().toString()));

        colQuantity.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));

        colFrom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFromUnit()));

        colTo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getToUnit()));

        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPerformedBy()));

        colPrice.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("%,.0f", c.getValue().getUnitPrice())));

        transactionTable.setItems(data);
    }

    /** Load lịch sử giao dịch theo asset_code */
    public void loadData(String assetCode, String assetName) {

        lblHeader.setText("Lịch sử giao dịch – " + assetName + " (" + assetCode + ")");
        data.clear();

        String sql = """
                    SELECT * FROM (
                        SELECT transaction_date, transaction_type, quantity,
                               IFNULL(from_unit, '') AS from_unit,
                               IFNULL(to_unit, '') AS to_unit,
                               IFNULL(performed_by, '') AS performed_by,
                               IFNULL(unit_price, 0) AS unit_price,
                               IFNULL(reason, '') AS reason
                        FROM asset_transactions
                        WHERE asset_code = ?
                        UNION ALL
                        SELECT reduction_date AS transaction_date, 'REDUCE' AS transaction_type, quantity,
                               '' AS from_unit, '' AS to_unit,
                               IFNULL(performed_by, '') AS performed_by,
                               0 AS unit_price,
                               IFNULL(reason, '') AS reason
                        FROM asset_reductions
                        WHERE asset_code = ?
                    ) ORDER BY transaction_date DESC
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, assetCode);
            ps.setString(2, assetCode);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    AssetTransaction t = new AssetTransaction();

                    t.setTransactionDate(rs.getString("transaction_date"));

                    // FIX: convert SQL string → enum
                    String typeStr = rs.getString("transaction_type");
                    try {
                        t.setTransactionType(AssetTransaction.TransactionType.valueOf(typeStr));
                    } catch (Exception ex) {
                        // Unknown type: default to IMPORT
                        t.setTransactionType(AssetTransaction.TransactionType.IMPORT);
                    }

                    t.setQuantity(rs.getInt("quantity"));
                    t.setFromUnit(rs.getString("from_unit"));
                    t.setToUnit(rs.getString("to_unit"));
                    t.setPerformedBy(rs.getString("performed_by"));
                    t.setUnitPrice(rs.getDouble("unit_price"));
                    t.setReason(rs.getString("reason"));
                    data.add(t);
                }
            }

            if (data.isEmpty()) {
                transactionTable.setPlaceholder(new Label("Không có giao dịch nào."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi tải lịch sử giao dịch!").showAndWait();
        }
    }
}
