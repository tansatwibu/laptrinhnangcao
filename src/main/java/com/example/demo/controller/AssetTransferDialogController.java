package com.example.demo.controller;

import com.example.demo.model.Asset;
import com.example.demo.model.User;
import com.example.demo.util.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;

import static com.example.demo.util.DatabaseUtil.getStock;
import static com.example.demo.util.DatabaseUtil.updateStock;

public class AssetTransferDialogController {

    @FXML private TextField txtAssetCode;
    @FXML private TextField txtAssetName;
    @FXML private TextField txtFromUnit;
    @FXML private TextField txtToUnit;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtPersonInCharge;
    @FXML private TextField txtDecisionNumber;

    private final HashMap<String, Asset> existingAssets = new HashMap<>();
    private User currentUser;

    // ====== RECEIVE CURRENT USER ======
    public void setCurrentUser(User user) {
        this.currentUser = user;

        if (txtPersonInCharge != null) {
            txtPersonInCharge.setText(currentUser.getFullName());
            txtPersonInCharge.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        loadExistingAssets();

        // FromUnit = QTVT
        txtFromUnit.setText("QTVT");
        txtFromUnit.setDisable(true);

        txtAssetName.setEditable(false);

        if (currentUser != null) {
            txtPersonInCharge.setText(currentUser.getFullName());
            txtPersonInCharge.setDisable(true);
        }

        // ============= CÁCH 1: chỉ check khi FOCUS LOST =============
        txtAssetCode.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {   // mất focus -> kiểm tra mã tài sản
                validateAssetCode();
            }
        });

        // ENTER không được nhấn nút Bàn Giao
        txtAssetCode.setOnAction(e -> validateAssetCode());
    }

    // ====================================================
    // LOAD ASSETS
    // ====================================================
    private void loadExistingAssets() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT asset_code, asset_name, asset_type, supplier FROM assets"
            );

            while (rs.next()) {
                Asset a = new Asset();
                a.setAssetCode(rs.getString("asset_code"));
                a.setAssetName(rs.getString("asset_name"));
                a.setAssetType(rs.getString("asset_type"));
                a.setSupplier(rs.getString("supplier"));
                existingAssets.put(a.getAssetCode(), a);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================================================
    // VALIDATE + AUTOFILL (CHỈ KHI NHẬP XONG)
    // ====================================================
    private void validateAssetCode() {
        String code = txtAssetCode.getText().trim();

        if (code.isEmpty()) {
            txtAssetName.setText("");
            return;
        }

        Asset a = existingAssets.get(code);
        if (a != null) {
            txtAssetName.setText(a.getAssetName());
        } else {
            txtAssetName.setText("");
            warn("❌ Mã tài sản không tồn tại!");
        }
    }

    // ====================================================
    // HANDLE TRANSFER
    // ====================================================
    @FXML
    protected void handleTransfer() {

        if (txtAssetCode.getText().isEmpty()
                || txtToUnit.getText().isEmpty()
                || txtQuantity.getText().isEmpty()) {
            warn("Vui lòng điền đầy đủ thông tin.");
            return;
        }

        String code = txtAssetCode.getText().trim();

        if (!existingAssets.containsKey(code)) {
            warn("Mã tài sản không hợp lệ!");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {

            conn.setAutoCommit(false);

            int qty = Integer.parseInt(txtQuantity.getText());

            // Use the same connection/transaction to read and update stock
            PreparedStatement psGetStock = conn.prepareStatement("SELECT quantity_in_stock FROM assets WHERE asset_code = ?");
            psGetStock.setString(1, code);
            ResultSet rsStock = psGetStock.executeQuery();
            int stock = rsStock.next() ? rsStock.getInt("quantity_in_stock") : 0;

            if (stock < qty) {
                warn("Không đủ số lượng trong kho QTVT!");
                conn.rollback();
                return;
            }

            // Giảm tồn kho trong cùng 1 transaction
            PreparedStatement psUpdate = conn.prepareStatement("UPDATE assets SET quantity_in_stock = ? WHERE asset_code = ?");
            psUpdate.setInt(1, stock - qty);
            psUpdate.setString(2, code);
            psUpdate.executeUpdate();

            String sql = """
                INSERT INTO asset_transactions(
                    asset_code, transaction_date, transaction_type,
                    quantity, from_unit, to_unit, performed_by, unit_price
                ) VALUES (?, ?, 'TRANSFER', ?, ?, ?, ?, 0)
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ps.setString(2, LocalDate.now().toString());
            ps.setInt(3, qty);
            ps.setString(4, "QTVT");
            ps.setString(5, txtToUnit.getText());
            ps.setString(6, currentUser.getUserId());
            ps.executeUpdate();

            conn.commit();

            info("✔ Bàn giao thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            warn("❌ Lỗi khi bàn giao tài sản!");
        }
    }

    // ====================================================
    // ALERTS
    // ====================================================
    private void warn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
