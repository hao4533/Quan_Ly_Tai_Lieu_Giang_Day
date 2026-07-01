package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import model.User;

public class UserDao extends BaseDao {

    // ========== HASH MẬT KHẨU ==========
    // Dùng SHA-256 để mã hóa mật khẩu trước khi lưu vào DB
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi mã hóa mật khẩu", e);
        }
    }

    // ========== ĐĂNG KÝ ==========
    // Lưu user mới vào bảng users trong j2ee_users_db
    public boolean registerUser(String email, String passwordHash, String fullName) {
        String sql = "INSERT INTO users (email, password_hash, full_name) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== KIỂM TRA EMAIL ĐÃ TỒN TẠI ==========
    // Dùng trước khi đăng ký để tránh trùng email
    public boolean isEmailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true nếu tìm thấy

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== ĐĂNG NHẬP ==========
    // Cho phép login bằng email hoặc full_name, so sánh password đã hash
    public User login(String usernameOrEmail, String password) {
        String passwordHash = hashPassword(password);

        // Tìm user theo email HOẶC full_name
        String sql = "SELECT id, email, password_hash, full_name FROM users "
                   + "WHERE (email = ? OR full_name = ?) AND password_hash = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            ps.setString(3, passwordHash);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("full_name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy -> sai tài khoản hoặc mật khẩu
    }
}