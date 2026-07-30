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

    public UserDao() {
        super("jdbc/UsersDB");
    }

    // ========== HASH MẬT KHẨU ==========
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

        } catch (Exception e) {
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
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== ĐĂNG NHẬP CHỈ BẰNG EMAIL ==========
    public User login(String email, String password) {
        String passwordHash = hashPassword(password);

        // SQL chỉ lọc duy nhất theo cột email (kèm role để phân luồng dashboard/admin)
        String sql = "SELECT id, email, password_hash, full_name, role FROM users WHERE email = ? AND password_hash = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy toàn bộ danh sách người dùng (phục vụ trang quản trị admin.jsp)
    @Override
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, email, password_hash, full_name, role FROM users ORDER BY id ASC";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("role")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public User getById(int id) {
        String sql = "SELECT id, email, password_hash, full_name, role FROM users WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(User model) {
        if (model == null) {
            return false;
        }

        // Đảm bảo các tên cột (email, password_hash, full_name, role) viết thường hoàn toàn đúng chuẩn Postgres
        // 🔒 Luôn ép role = 'user' tại tầng DAO: tài khoản đăng ký qua register.jsp KHÔNG được phép tự phong admin,
        // dù có bị chỉnh sửa tham số ở tầng trên. Admin chỉ được cấp thủ công trực tiếp trong DB.
        String sql = "INSERT INTO users (email, password_hash, full_name, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, model.getEmail());
            ps.setString(2, model.getPasswordHash());
            ps.setString(3, model.getFullName());
            ps.setString(4, User.ROLE_USER);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("BIẾN CỐ KẾT NỐI: LỖI THỰC THI TẠI TẦNG DAO");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(User model) {
        // Tùy chọn triển khai sau
        return false;
    }

    public boolean updateFullName(int userId, String newFullName) {
        String sql = "UPDATE users SET full_name = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newFullName.trim());
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        return deleteUserAccount(id);
    }

    /**
     * Xóa tài khoản người dùng một cách AN TOÀN. 🔒 Điều kiện "AND role <>
     * 'admin'" được đặt ngay trong câu SQL: dù tầng controller có lỡ gọi nhầm,
     * câu lệnh cũng KHÔNG BAO GIỜ xóa được tài khoản có role = 'admin'. Nếu id
     * đó là admin, executeUpdate() sẽ trả về 0 dòng bị ảnh hưởng -> hàm trả về
     * false.
     */
    public boolean deleteUserAccount(int id) {
        String sql = "DELETE FROM users WHERE id = ? AND role <> 'admin'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
