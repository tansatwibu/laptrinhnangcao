package com.example.demo.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;   // ⭐ THIẾU DÒNG NÀY

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

}
