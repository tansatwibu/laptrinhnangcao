package com.example.demo.controller;

import com.example.demo.util.DatabaseUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DatabaseViewerController implements Initializable {

    @FXML
    private ComboBox<String> tableComboBox;

    @FXML
    private TableView<ObservableList<String>> dataTableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTableNames();
        tableComboBox.setOnAction(event -> loadTableData(tableComboBox.getValue()));
    }

    private void loadTableNames() {
        ObservableList<String> tableNames = FXCollections.observableArrayList();
        try (Connection conn = DatabaseUtil.getConnection()) {
            if (conn != null) {
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE" });
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableComboBox.setItems(tableNames);
    }

    private void loadTableData(String tableName) {
        if (tableName == null || tableName.isEmpty())
            return;

        dataTableView.getColumns().clear();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        String sql = "SELECT * FROM " + tableName;
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            // Dynamic columns
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 0; i < columnCount; i++) {
                final int j = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(metaData.getColumnName(i + 1));
                col.setCellValueFactory(param -> {
                    if (param.getValue().size() > j) {
                        return new SimpleStringProperty(param.getValue().get(j));
                    } else {
                        return new SimpleStringProperty("");
                    }
                });
                dataTableView.getColumns().add(col);
            }

            // Data
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

            dataTableView.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
