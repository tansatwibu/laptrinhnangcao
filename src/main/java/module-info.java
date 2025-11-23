module com.example.demo {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // SQLite JDBC không có module-name nên require theo dạng automatic module:
    requires org.xerial.sqlitejdbc;

    // mở cho JavaFX reflection
    opens com.example.demo to javafx.fxml;
    opens com.example.demo.controller to javafx.fxml;
    opens com.example.demo.model to javafx.base;

    // export package cho code khác sử dụng
    exports com.example.demo;
    exports com.example.demo.controller;
    exports com.example.demo.model;
}
