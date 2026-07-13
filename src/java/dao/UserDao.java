package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDao extends BaseDao<User> {

    // Constructor bắt buộc để truyền JNDI Name lên BaseDao
    public UserDao(String jndiName) {
        super(jndiName);
    }

    // ========== HASH MẬT KHẨU ==========
    // Dùng SHA-256 để mã hóa mật khẩu (Xóa @Override vì đây là hàm tự định nghĩa)
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
    public boolean registerUser(String email, String passwordHash, String fullName) {
        String sql = "INSERT INTO users (email, password_hash, full_name) VALUES (?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);

            return ps.executeUpdate() > 0;

        } catch (Exception e) { // Đổi sang Exception tổng quát vì getConnection() throws Exception
            e.printStackTrace();
            return false;
        }
    }

    // ========== KIỂM TRA EMAIL ĐÃ TỒN TẠI ==========
    public boolean isEmailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true nếu tìm thấy
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== ĐĂNG NHẬP CHỈ BẰNG EMAIL ==========
    public User login(String email, String password) {
        String passwordHash = hashPassword(password);

        // SQL chỉ lọc duy nhất theo cột email
        String sql = "SELECT id, email, password_hash, full_name FROM users WHERE email = ? AND password_hash = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("full_name")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy hoặc sai thông tin
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>();
    }

    @Override
    public User getById(int id) {
        return null;
    }

    @Override
    public boolean insert(User model) {
        if (model == null) {
            return false;
        }

        // Đảm bảo các tên cột (email, password_hash, full_name) viết thường hoàn toàn đúng chuẩn Postgres
        String sql = "INSERT INTO users (email, password_hash, full_name) VALUES (?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, model.getEmail());
            ps.setString(2, model.getPasswordHash());
            ps.setString(3, model.getFullName());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("BIẾN CỐ KẾT NỐI: LỖI THỰC THI TẠI TẦNG DAO");
            e.printStackTrace(); // In ra toàn bộ StackTrace để biết chính xác lỗi sai tên bảng hay sai kiểu dữ liệu
            return false;
        }
    }

    @Override
    public boolean update(User model) {
        // Tùy chọn triển khai sau
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }
}
