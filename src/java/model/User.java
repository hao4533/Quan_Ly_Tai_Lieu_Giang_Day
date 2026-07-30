package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Các hằng số định nghĩa vai trò tài khoản
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    // Các thuộc tính ánh xạ chuẩn xác với các cột trong database
    private int id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String role; // "user" (đăng ký tự do) hoặc "admin" (được cấp)

    // 1. Constructor không đối số
    public User() {
        this.role = ROLE_USER;
    }

    // 2. Constructor có đầy đủ đối số, mặc định role = user (Dùng khi lấy dữ liệu từ DB lên - giữ tương thích ngược)
    public User(int id, String email, String passwordHash, String fullName) {
        this(id, email, passwordHash, fullName, ROLE_USER);
    }

    // 2b. Constructor đầy đủ đối số kèm role (Dùng khi lấy dữ liệu từ DB lên)
    public User(int id, String email, String passwordHash, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = (role == null || role.trim().isEmpty()) ? ROLE_USER : role;
    }

    // 3. Constructor không cần ID, mặc định role = user (Dùng khi đăng ký tài khoản mới qua register.jsp)
    public User(String email, String passwordHash, String fullName) {
        this(email, passwordHash, fullName, ROLE_USER);
    }

    // 3b. Constructor không cần ID kèm role (Dùng khi admin cấp tài khoản)
    public User(String email, String passwordHash, String fullName, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = (role == null || role.trim().isEmpty()) ? ROLE_USER : role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role == null || role.trim().isEmpty()) ? ROLE_USER : role;
    }

    // Hàm tiện ích kiểm tra nhanh quyền admin
    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.role);
    }
}