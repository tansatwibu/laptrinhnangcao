package com.example.demo.controller;

import com.example.demo.model.NewAssetRow;
import com.example.demo.model.Asset;
import com.example.demo.model.User;
import com.example.demo.util.DatabaseUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;

import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;

public class AddAssetDialogController {

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }


    @FXML private TextField txtDecisionNumber;
    @FXML private TextField txtSupplier;

    @FXML private TableView<NewAssetRow> assetTable;
    @FXML private TableColumn<NewAssetRow, String> colAssetCode;
    @FXML private TableColumn<NewAssetRow, String> colAssetName;
    @FXML private TableColumn<NewAssetRow, String> colAssetType;

    @FXML private TableColumn<NewAssetRow, Integer> colProductionYear;
    @FXML private TableColumn<NewAssetRow, Double> colUnitPrice;
    @FXML private TableColumn<NewAssetRow, Integer> colQuantity;

    private final ObservableList<NewAssetRow> rows = FXCollections.observableArrayList();

    // asset_code → asset metadata (không có đơn giá)
    private final HashMap<String, Asset> existingAssets = new HashMap<>();

    @FXML
    public void initialize() {
        loadExistingAssets();

        assetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupEditableTable();

        // preload 20 empty rows
        for (int i = 0; i < 20; i++)
            rows.add(new NewAssetRow());

        assetTable.setItems(rows);
    }


    /** Load tài sản để autocomplete */
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


    /** Table setup */
    private void setupEditableTable() {

        colAssetCode.setCellValueFactory(c -> c.getValue().assetCodeProperty());
        colAssetName.setCellValueFactory(c -> c.getValue().assetNameProperty());
        colAssetType.setCellValueFactory(c -> c.getValue().assetTypeProperty());

        colProductionYear.setCellValueFactory(c -> c.getValue().productionYearProperty().asObject());
        colUnitPrice.setCellValueFactory(c -> c.getValue().unitPriceProperty().asObject());
        colQuantity.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());

        colAssetCode.setCellFactory(col -> new AutoCompleteCodeCell());
        colAssetName.setCellFactory(TextFieldTableCell.forTableColumn());
        colAssetType.setCellFactory(TextFieldTableCell.forTableColumn());
        colProductionYear.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colUnitPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        colAssetCode.setOnEditCommit(e -> e.getRowValue().setAssetCode(e.getNewValue()));
        colAssetName.setOnEditCommit(e -> e.getRowValue().setAssetName(e.getNewValue()));
        colAssetType.setOnEditCommit(e -> e.getRowValue().setAssetType(e.getNewValue()));
        colProductionYear.setOnEditCommit(e -> e.getRowValue().setProductionYear(e.getNewValue()));
        colUnitPrice.setOnEditCommit(e -> e.getRowValue().setUnitPrice(e.getNewValue()));
        colQuantity.setOnEditCommit(e -> e.getRowValue().setQuantity(e.getNewValue()));
    }


    /** SAVE */
    public boolean handleSave() {

        if (txtDecisionNumber.getText().isEmpty() || txtSupplier.getText().isEmpty()) {
            warn("Vui lòng nhập Số quyết định và Nhà cung cấp.");
            return false;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {

            conn.setAutoCommit(false);

            String sqlAsset = """
                INSERT INTO assets(asset_code, asset_name, asset_type, supplier, quantity_total, quantity_in_stock)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(asset_code)
                DO UPDATE SET
                    quantity_total = quantity_total + excluded.quantity_total,
                    quantity_in_stock = quantity_in_stock + excluded.quantity_in_stock
                """;

            String sqlTrans = """
                INSERT INTO asset_transactions(
                    asset_code, transaction_type, transaction_date,
                    quantity, from_unit, to_unit,
                    condition_status, reason, performed_by,
                    decision_number, production_year, unit_price
                )
                VALUES (?, 'IMPORT', ?, ?, NULL, NULL, NULL, NULL, ?, ?, ?, ?)
                """;

            PreparedStatement psA = conn.prepareStatement(sqlAsset);
            PreparedStatement psT = conn.prepareStatement(sqlTrans);

            int valid = 0;

            for (NewAssetRow row : rows) {

                if (empty(row.getAssetCode())
                        || empty(row.getAssetName())
                        || empty(row.getAssetType())
                        || row.getUnitPrice() <= 0
                        || row.getQuantity() <= 0
                        || row.getProductionYear() <= 1900)
                    continue;

                // Insert/update ASSET
                psA.setString(1, row.getAssetCode());
                psA.setString(2, row.getAssetName());
                psA.setString(3, row.getAssetType());
                psA.setString(4, txtSupplier.getText());
                psA.setInt(5, row.getQuantity());
                psA.setInt(6, row.getQuantity());
                psA.executeUpdate();

                // Insert TRANSACTION
                psT.setString(1, row.getAssetCode());
                psT.setString(2, LocalDate.now().toString());
                psT.setInt(3, row.getQuantity());
                psT.setString(4, currentUser.getUserId());
                psT.setString(5, txtDecisionNumber.getText());
                psT.setInt(6, row.getProductionYear());
                psT.setDouble(7, row.getUnitPrice());
                psT.executeUpdate();

                valid++;
            }

            conn.commit();

            if (valid == 0) {
                warn("Không có dòng hợp lệ để lưu.");
                return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            warn("Lỗi khi lưu dữ liệu vào DB!");
            return false;
        }
    }


    private boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void warn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }


    // ============================================================
    //  AUTOCOMPLETE CELL (đã FIX caret jumping)
    // ============================================================

    class AutoCompleteCodeCell extends TableCell<NewAssetRow, String> {

        private final TextField editor = new TextField();
        private final ContextMenu menu = new ContextMenu();
        private boolean listening = false;

        public AutoCompleteCodeCell() {
            // Listener AUTOCOMPLETE (được attach đúng 1 lần)
            editor.textProperty().addListener((obs, oldV, newV) -> {
                if (!isEditing()) return;

                // Cập nhật vào row
                if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                    getTableView().getItems().get(getIndex()).setAssetCode(newV);
                }

                // Autocomplete
                if (newV == null || newV.isEmpty()) {
                    menu.hide();
                    return;
                }

                menu.getItems().clear();

                existingAssets.keySet().stream()
                        .filter(s -> s.toLowerCase().contains(newV.toLowerCase()))
                        .limit(10)
                        .forEach(code -> {
                            MenuItem item = new MenuItem(code);
                            item.setOnAction(e -> selectCode(code));
                            menu.getItems().add(item);
                        });

                if (!menu.getItems().isEmpty()) {
                    menu.show(editor, Side.BOTTOM, 0, 0);
                } else {
                    menu.hide();
                }
            });

            editor.setOnAction(e -> selectCode(editor.getText()));
        }

        private void selectCode(String code) {
            editor.setText(code);
            menu.hide();

            NewAssetRow row = getTableView().getItems().get(getIndex());
            row.setAssetCode(code);

            if (existingAssets.containsKey(code)) {
                Asset a = existingAssets.get(code);
                row.setAssetName(a.getAssetName());
                row.setAssetType(a.getAssetType());
            }

            getTableView().refresh();
        }

        @Override
        protected void updateItem(String value, boolean empty) {
            super.updateItem(value, empty);

            if (empty) {
                setGraphic(null);
            } else {
                // Chỉ update editor nếu giá trị thực sự thay đổi → KHÔNG reset caret
                if (!editor.getText().equals(value)) {
                    editor.setText(value);
                }
                setGraphic(editor);
            }
        }
    }

}
