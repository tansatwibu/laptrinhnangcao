package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.util.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    private User currentUser;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField; // dùng khi show password
    @FXML
    private CheckBox showPasswordCheckBox;
    @FXML
    private Label errorLabel;
    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    public void initialize() {
        // Auto-focus username field
        javafx.application.Platform.runLater(() -> usernameField.requestFocus());
    }

    @FXML
    public void onShowPasswordChanged() {
        if (showPasswordCheckBox.isSelected()) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }

    @FXML
    public void onForgotPasswordClicked() {
        showAlert(Alert.AlertType.INFORMATION,
                "Quên mật khẩu",
                "Hãy liên lạc với admin của bạn để được hỗ trợ.\n\n" +
                        "📞 Hotline: 1900-xxxx\n" +
                        "📧 Email: admin@company.com");
    }

    @FXML
    protected void onLoginClicked() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = (showPasswordCheckBox.isSelected() ? passwordTextField.getText() : passwordField.getText());
        if (password == null)
            password = "";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both username and password");
            return;
        }

        System.out.println("Login attempt: " + username);

        User authenticatedUser = authenticateUser(username, password);

        if (authenticatedUser != null) {
            System.out.println("Login successful for user: " + username);
            currentUser = authenticatedUser;

            openMainWindow();

        } else {
            System.out.println("Login failed for user: " + username);
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password");
        }
    }

    /**
     * Authenticate user by username + password.
     * Expects tables:
     * - accounts(user_id, username, password)
     * - users(user_id, full_name, department)
     *
     * NOTE: currently compares plain-text password. For production use hashed
     * passwords.
     */
    private User authenticateUser(String username, String password) {
        String query = """
                SELECT u.user_id,
                       COALESCE(u.full_name, u.full_name, '') AS full_name,
                       COALESCE(u.department, '') AS department,
                       a.username, a.password
                FROM accounts a
                JOIN users u ON a.user_id = u.user_id
                WHERE a.username = ?
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String userId = rs.getString("user_id");
                    String fullName = rs.getString("full_name");
                    String department = rs.getString("department");

                    // plain-text compare (as your current DB uses). Replace with hash check if
                    // needed.
                    boolean passwordMatch = storedPassword != null && storedPassword.equals(password);

                    if (passwordMatch) {
                        // build User matching your model: User(String userId, String fullName, String
                        // department, String username, String password)
                        return new User(userId, fullName, department, username, storedPassword);
                    }
                }
            }

            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
            return null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo/view/MainMenu.fxml"));

            Parent root = loader.load();

            // LẤY controller
            MainMenuController mainController = loader.getController();
            mainController.setCurrentUser(currentUser);

            // Đổi scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quản lý tài sản - " + currentUser.getFullName());
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Không thể mở Main Menu.\n" + e.getMessage());
        }
    }
}