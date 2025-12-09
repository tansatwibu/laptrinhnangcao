package com.example.demo.controller;

import com.example.demo.model.Asset;
import com.example.demo.util.DatabaseUtil;
import com.example.demo.model.User;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.Parent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainMenuController {
    private User currentUser;


    @FXML private Label lblUsername;

    @FXML private TableView<Asset> assetTable;

    @FXML private TableColumn<Asset, String> colCode;
    @FXML private TableColumn<Asset, String> colName;
    @FXML private TableColumn<Asset, String> colType;
    @FXML private TableColumn<Asset, String> colSupplier;

    @FXML private TableColumn<Asset, String> colValue;
    @FXML private TableColumn<Asset, String> colQuantityTotal;
    @FXML private TableColumn<Asset, String> colQuantityInStock;

    @FXML private TableColumn<Asset, String> colUsageDetail;   // hyperlink
    @FXML private TableColumn<Asset, String> colDetail;        // hyperlink

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;

    private final ObservableList<Asset> assetList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {

        assetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        loadAssets();

        // Tạo 2 cột hyperlink
        colUsageDetail.setCellFactory(col -> createHyperlinkCell("Xem đơn vị sử dụng"));
        colDetail.setCellFactory(col -> createHyperlinkCell("Xem lịch sử giao dịch"));
    }


    /** Tạo cell hyperlink */
    private TableCell<Asset, String> createHyperlinkCell(String text) {
        return new TableCell<>() {
            final Hyperlink link = new Hyperlink(text);

            {
                link.setOnAction(e -> {
                    Asset a = getTableView().getItems().get(getIndex());
                    if (text.contains("đơn vị")) showUsageDetail(a);
                    else showTransactionDetail(a);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : link);
            }
        };
    }


    /** ===================== LOAD ASSET LIST ===================== */
    private void loadAssets() {

        assetList.clear();

        String sql = """
            SELECT a.asset_code, a.asset_name, a.asset_type, a.supplier,
                   a.quantity_total, a.quantity_in_stock,
                   IFNULL((
                       SELECT SUM(t.unit_price * t.quantity)
                       FROM asset_transactions t
                       WHERE t.asset_code = a.asset_code AND t.transaction_type = 'IMPORT'
                   ), 0) AS total_value
            FROM assets a
            ORDER BY a.asset_name
            """;

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Asset a = new Asset();
                a.setAssetCode(rs.getString("asset_code"));
                a.setAssetName(rs.getString("asset_name"));
                a.setAssetType(rs.getString("asset_type"));
                a.setSupplier(rs.getString("supplier"));
                a.setQuantityTotal(rs.getInt("quantity_total"));
                a.setQuantityInStock(rs.getInt("quantity_in_stock"));
                a.setTotalValue(rs.getDouble("total_value"));

                assetList.add(a);
            }

            // Gán dữ liệu vào bảng
            colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssetCode()));
            colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssetName()));
            colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssetType()));
            colSupplier.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSupplier()));

            colQuantityTotal.setCellValueFactory(c ->
                    new SimpleStringProperty(String.valueOf(c.getValue().getQuantityTotal()))
            );

            colQuantityInStock.setCellValueFactory(c ->
                    new SimpleStringProperty(String.valueOf(c.getValue().getQuantityInStock()))
            );

            colValue.setCellValueFactory(c ->
                    new SimpleStringProperty(String.format("%,.0f", c.getValue().getTotalValue()))
            );

            assetTable.setItems(assetList);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể tải danh sách tài sản!").showAndWait();
        }
    }


    /* ==================== DETAIL POPUP PLACEHOLDER =================== */

    private void showUsageDetail(Asset asset) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo/view/AssetUsageDialog.fxml")
            );

            DialogPane pane = loader.load();

            AssetUsageController controller = loader.getController();
            controller.loadData(asset.getAssetCode(), asset.getAssetName());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Đơn vị sử dụng");
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở chi tiết đơn vị sử dụng!").showAndWait();
        }
    }

    private void showTransactionDetail(Asset asset) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo/view/AssetTransactionDialog.fxml")
            );
            DialogPane pane = loader.load();

            AssetTransactionController controller = loader.getController();
            controller.loadData(asset.getAssetCode(), asset.getAssetName());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Lịch sử giao dịch");
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /* ==================== BUTTON EVENTS =================== */

    @FXML
    private void handleAddAsset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/AddAssetDialog.fxml"));
            DialogPane pane = loader.load();
            AddAssetDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Nhập/Tăng tài sản");
            dialog.setResizable(false);
            dialog.initOwner(assetTable.getScene().getWindow());

            ButtonType okButtonType = pane.getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    .findFirst().orElse(null);

            if (okButtonType != null) {
                Button okBtn = (Button) pane.lookupButton(okButtonType);
                okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!controller.handleSave()) {
                        event.consume();
                    } else {
                        loadAssets();  // refresh lại
                    }
                });
            }

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở cửa sổ nhập!").showAndWait();
        }
    }


    @FXML private void handleTransferAsset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/AssetTransferDialog.fxml"));
            DialogPane pane = loader.load();
            AssetTransferDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Bàn giao tài sản");
            dialog.setResizable(false);
            dialog.initOwner(assetTable.getScene().getWindow());

            // Lấy button "Bàn giao" trong FXML
            ButtonType okButtonType = pane.getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    .findFirst().orElse(null);

            if (okButtonType != null) {
                Button okBtn = (Button) pane.lookupButton(okButtonType);
                okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    controller.handleTransfer(); // gọi phương thức handleTransfer trong controller
                    loadAssets(); // refresh lại bảng sau khi bàn giao
                });
            }

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở cửa sổ bàn giao!").showAndWait();
        }
    }
    @FXML private void handleInventory() { System.out.println("Kiểm kê..."); }
    @FXML private void handleReport() { System.out.println("Báo cáo..."); }
    @FXML private void handleLogout() { System.out.println("Đăng xuất..."); }

    @FXML private void handleFilter() {
        System.out.println("Lọc theo: " + filterType.getValue() + " = " + searchField.getText());
    }

    @FXML private void handleResetFilter() {
        searchField.clear();
        filterType.getSelectionModel().clearSelection();
        loadAssets();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        lblUsername.setText(user.getFullName());   // hoặc user.getUsername()
    }


    @FXML
    private void handleReduceAsset() {
        try {
            // Lấy tài sản đang được chọn từ bảng
            Asset selectedAsset = assetTable.getSelectionModel().getSelectedItem();
            if (selectedAsset == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một tài sản để giảm.").showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/view/ReduceAssetDialog.fxml"));
            Parent root = loader.load();

            ReduceAssetDialogController controller = loader.getController();

            // Tạo Stage mới cho dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Giảm tài sản");
            dialogStage.initModality(Modality.WINDOW_MODAL); // Đặt dialog là modal
            dialogStage.initOwner(lblUsername.getScene().getWindow()); // Đặt owner là cửa sổ chính
            dialogStage.setScene(new javafx.scene.Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setAsset(selectedAsset); // Truyền tài sản đã chọn vào dialog

            dialogStage.showAndWait(); // Hiển thị và đợi dialog đóng

            // Sau khi dialog đóng, làm mới danh sách tài sản trong bảng
            loadAssets();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở hộp thoại giảm tài sản: " + e.getMessage()).showAndWait();
        }
    }

}
