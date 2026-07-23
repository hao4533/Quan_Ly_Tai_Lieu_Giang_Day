package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import model.Share;

public class ShareDao extends BaseDao<Share> {

    // JNDI Name cấu hình Connection Pool trên Server (GlassFish/Payara)
    public ShareDao() {
        super("jdbc/SharesDB");
    }

    @Override
    public List<Share> getAll() {
        List<Share> shares = new ArrayList<>();
        String sql = "SELECT token, document_id, recipient_email, expire_at FROM shares";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                shares.add(mapResultSetToShare(rs));
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(ShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi getAll shares", ex);
        }
        return shares;
    }

    @Override
    public Share getById(int id) {
        // Bảng shares dùng khóa chính là token (UUID String) nên không dùng hàm này
        return null;
    }

    /**
     * Tìm thông tin chia sẻ bằng mã Token (UUID) phục vụ xem tài liệu trực tuyến
     */
    public Share getByToken(String token) {
        String sql = "SELECT token, document_id, recipient_email, expire_at FROM shares WHERE token = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShare(rs);
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(ShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi getByToken", ex);
        }
        return null;
    }

    @Override
    public boolean insert(Share model) {
        String sql = "INSERT INTO shares (token, document_id, recipient_email, expire_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, model.getToken());
            ps.setInt(2, model.getDocumentId());
            ps.setString(3, model.getRecipientEmail());
            ps.setTimestamp(4, model.getExpireAt());

            return ps.executeUpdate() > 0;
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(ShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi insert share", ex);
            return false; // Đã sửa: Trả về false khi xảy ra lỗi ngoài ý muốn!
        }
    }

    @Override
    public boolean update(Share model) {
        String sql = "UPDATE shares SET document_id = ?, recipient_email = ?, expire_at = ? WHERE token = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, model.getDocumentId());
            ps.setString(2, model.getRecipientEmail());
            ps.setTimestamp(3, model.getExpireAt());
            ps.setString(4, model.getToken());

            return ps.executeUpdate() > 0;
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(ShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi update share", ex);
            return false; // Đã sửa: Trả về false khi xảy ra lỗi!
        }
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    /**
     * Xóa/Thu hồi liên kết chia sẻ dựa theo Token UUID
     */
    public boolean deleteByToken(String token) {
        String sql = "DELETE FROM shares WHERE token = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);

            return ps.executeUpdate() > 0;
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(ShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi deleteByToken", ex);
            return false;
        }
    }

    /**
     * Hàm tiện ích ánh xạ từ ResultSet sang Object Model
     */
    private Share mapResultSetToShare(ResultSet rs) throws SQLException {
        Share share = new Share();
        share.setToken(rs.getString("token"));
        share.setDocumentId(rs.getInt("document_id"));
        share.setRecipientEmail(rs.getString("recipient_email"));
        share.setExpireAt(rs.getTimestamp("expire_at"));
        return share;
    }
}