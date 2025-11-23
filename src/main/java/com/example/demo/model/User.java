package com.example.demo.model;

public class User {

    // ====== FROM users ======
    private String userId;       // mã NV
    private String fullName;     // họ tên
    private String department;   // phòng ban

    // ====== FROM accounts ======
    private String username;
    private String password;

    // ===== Constructors =====
    public User() {}

    public User(String userId, String fullName, String department,
                String username, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.department = department;
        this.username = username;
        this.password = password;
    }

    // ===== GET / SET =====
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
