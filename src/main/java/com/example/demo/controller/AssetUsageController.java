package com.example.demo.controller;

import com.example.demo.util.DatabaseUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Controller cho dialog "Đơn vị sử dụng" - chỉ 2 cột: Đơn vị và Số lượng.
 */
public class AssetUsageController {

    @FXML private Label lblHeader;
    @FXML private TableView<UnitUsage> usageTable;
    @FXML private TableColumn<UnitUsage, String> colUnit;
    @FXML private TableColumn<UnitUsage, Integer> colQuantity;

    private final ObservableList<UnitUsage> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // policy để cột co giãn, rồi bind chiều rộng tỉ lệ tránh khoảng thừa bên phải
        usageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // chia tỉ lệ: unit 70%, quantity 30% (thay đổi nếu muốn)
        colUnit.prefWidthProperty().bind(usageTable.widthProperty().multiply(0.70));
        colQuantity.prefWidthProperty().bind(usageTable.widthProperty().multiply(0.30));

        colUnit.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unit));
        colQuantity.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().quantity).asObject());

        usageTable.setItems(data);

        // cho đẹp: giới hạn tối thiểu width để tránh ói giao diện nhỏ
        usageTable.setMinWidth(400);
        usageTable.setMinHeight(200);
    }

    /**
     * Gọi từ MainMenuController.showUsageDetail(...)
     * @param assetCode mã tài sản
     * @param assetName tên tài sản (để hiển tiêu đề)
     */
    public void loadData(String assetCode, String assetName) {
        lblHeader.setText("Đơn vị sử dụng — " + assetName + " (" + assetCode + ")");

        data.clear();

        String sql = """
        SELECT to_unit AS unit,
               SUM(quantity) AS qty
        FROM asset_transactions
        WHERE asset_code = ?
          AND transaction_type = 'TRANSFER'
          AND to_unit IS NOT NULL
          AND to_unit <> ''
        GROUP BY to_unit
        HAVING qty > 0
        ORDER BY qty DESC
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, assetCode);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String unit = rs.getString("unit");
                    int qty = rs.getInt("qty");
                    data.add(new UnitUsage(unit, qty));
                }
            }

            if (data.isEmpty()) {
                usageTable.setPlaceholder(new Label("Tài sản này chưa được bàn giao tới đơn vị nào."));
            } else {
                usageTable.setPlaceholder(new Label(""));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải đơn vị sử dụng:\n" + e.getMessage());
            a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            a.showAndWait();
        }
    }


    // Simple POJO để map vào TableView
    public static class UnitUsage {
        public final String unit;
        public final int quantity;

        public UnitUsage(String unit, int quantity) {
            this.unit = unit;
            this.quantity = quantity;
        }
    }
}
