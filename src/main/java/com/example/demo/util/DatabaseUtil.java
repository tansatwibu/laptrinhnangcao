package com.example.demo.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.ResultSet;

public class DatabaseUtil {

    public static Connection getConnection() {
        try {
            // Lấy đường dẫn tuyệt đối dựa trên project đang chạy
            String projectPath = System.getProperty("user.dir");
            String dbPath = projectPath + File.separator + "database" + File.separator + "demo.db";
            String url = "jdbc:sqlite:" + dbPath;

            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(url);

            System.out.println("✅ Kết nối SQLite thành công tại: " + dbPath);
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Không tìm thấy driver SQLite!");
            e.printStackTrace();
            return null;

        } catch (SQLException e) {
            System.out.println("❌ Không thể kết nối tới SQLite!");
            e.printStackTrace();
            return null;
        }
    }

    /** Lấy tồn kho của 1 tài sản */
    public static int getStock(String assetCode, String unit) {
        String sql = "SELECT quantity_in_stock FROM assets WHERE asset_code = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, assetCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity_in_stock");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Cập nhật tồn kho */
    public static boolean updateStock(String assetCode, String unit, int newQty) {
        String sql = "UPDATE assets SET quantity_in_stock = ? WHERE asset_code = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQty);
            ps.setString(2, assetCode);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Giảm số lượng của một tài sản và ghi lại giao dịch.
     *
     * @param assetCode Mã tài sản cần giảm.
     * @param quantityToReduce Số lượng cần giảm.
     * @param reason Lý do giảm tài sản.
     * @param username Tên người dùng thực hiện giao dịch.
     * @return true nếu giảm thành công, false nếu ngược lại.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình thao tác với cơ sở dữ liệu.
     */
    public static boolean reduceAssetQuantity(String assetCode, int quantityToReduce, String reason, String username) throws SQLException {
        Connection conn = null;
        PreparedStatement updateAssetStmt = null;
        PreparedStatement insertTransactionStmt = null;
        boolean success = false;

        try {
            conn = getConnection(); // Giả sử bạn có phương thức getConnection() để lấy kết nối
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Cập nhật số lượng tài sản
            String updateAssetSql = "UPDATE assets SET quantity_in_stock = quantity_in_stock - ? WHERE asset_code = ? AND quantity_in_stock >= ?";
            updateAssetStmt = conn.prepareStatement(updateAssetSql);
            updateAssetStmt.setInt(1, quantityToReduce);
            updateAssetStmt.setString(2, assetCode);
            updateAssetStmt.setInt(3, quantityToReduce); // Đảm bảo số lượng hiện có đủ để giảm

            int rowsAffected = updateAssetStmt.executeUpdate();

            if (rowsAffected > 0) {
                // 2. Đảm bảo bảng ghi giảm tồn tại và ghi lại giao dịch giảm vào `asset_reductions`
                String createTableSql = """
                    CREATE TABLE IF NOT EXISTS asset_reductions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        asset_code TEXT NOT NULL,
                        reduction_date TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        reason TEXT,
                        performed_by TEXT
                    )
                    """;
                try (PreparedStatement createStmt = conn.prepareStatement(createTableSql)) {
                    createStmt.execute();
                }

                String insertTransactionSql = "INSERT INTO asset_reductions(asset_code, reduction_date, quantity, reason, performed_by) VALUES (?, ?, ?, ?, ?)";
                insertTransactionStmt = conn.prepareStatement(insertTransactionSql);
                insertTransactionStmt.setString(1, assetCode);
                insertTransactionStmt.setString(2, LocalDate.now().toString());
                insertTransactionStmt.setInt(3, quantityToReduce);
                insertTransactionStmt.setString(4, reason);
                insertTransactionStmt.setString(5, username);

                insertTransactionStmt.executeUpdate();

                conn.commit(); // Hoàn thành transaction
                success = true;
            } else {
                // Nếu không có hàng nào được cập nhật, có thể do asset_id không tồn tại hoặc số lượng không đủ
                conn.rollback(); // Hoàn tác transaction
                success = false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Hoàn tác transaction nếu có lỗi
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi rollback transaction: " + ex.getMessage());
                }
            }
            throw e; // Ném lại ngoại lệ để xử lý ở tầng cao hơn
        } finally {
            if (updateAssetStmt != null) {
                updateAssetStmt.close();
            }
            if (insertTransactionStmt != null) {
                insertTransactionStmt.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true); // Đặt lại autocommit về true
                conn.close();
            }
        }
        return success;
    }

}
