package com.example.demo.controller;

import com.example.demo.model.Asset; // Import nếu bạn muốn truyền đối tượng Asset vào dialog
import com.example.demo.util.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ReduceAssetDialogController {

    @FXML
    private TextField assetIdField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextArea reasonArea;

    private Stage dialogStage;
    private Asset selectedAsset; // Để lưu trữ tài sản được chọn từ MainMenu

    // Phương thức để thiết lập Stage cho dialog
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Phương thức để khởi tạo dữ liệu cho dialog (ví dụ: điền trước mã tài sản)
    public void setAsset(Asset asset) {
        this.selectedAsset = asset;
        if (asset != null) {
            assetIdField.setText(asset.getAssetCode());
            assetIdField.setEditable(false); // Không cho phép chỉnh sửa mã tài sản
        }
    }

    @FXML
    private void handleReduceAsset() {
        String assetId = assetIdField.getText();
        String quantityText = quantityField.getText();
        String reason = reasonArea.getText();

        if (assetId.isEmpty() || quantityText.isEmpty() || reason.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        int quantityToReduce;
        try {
            quantityToReduce = Integer.parseInt(quantityText);
            if (quantityToReduce <= 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số lượng giảm phải lớn hơn 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Số lượng giảm phải là một số nguyên hợp lệ.");
            return;
        }

        // Lấy username của người dùng hiện tại (bạn cần có một cách để lấy thông tin
        // này)
        // Ví dụ: String currentUser = LoginController.getCurrentUser();
        String currentUser = "admin"; // Thay thế bằng cách lấy người dùng hiện tại

        try {
            boolean success = DatabaseUtil.reduceAssetQuantity(assetId, quantityToReduce, reason, currentUser);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Giảm tài sản thành công!");
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Không thể giảm tài sản. Vui lòng kiểm tra lại mã tài sản và số lượng.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi giảm tài sản: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}