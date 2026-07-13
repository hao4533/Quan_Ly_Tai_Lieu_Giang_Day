package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Các thuộc tính ánh xạ chuẩn xác với các cột trong database
    private int id;
    private String email;
    private String passwordHash;
    private String fullName;

    // 1. Constructor không đối số
    public User() {
    }

    // 2. Constructor có đầy đủ đối số (Dùng khi lấy dữ liệu từ DB lên)
    public User(int id, String email, String passwordHash, String fullName) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    // 3. Constructor không cần ID (Dùng khi tạo user mới để chuẩn bị lưu xuống DB)
    public User(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    // 4. Toàn bộ các hàm Getter và Setter để truy xuất dữ liệu
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}