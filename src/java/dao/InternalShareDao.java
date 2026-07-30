package dao;

import model.InternalShare;
import model.Permission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;

public class InternalShareDao extends BaseDao<InternalShare> {

    public InternalShareDao() {
        super("jdbc/SharesDB");
    }

    @Override
    public List<InternalShare> getAll() {
        return new ArrayList<>(); 
    }

    @Override
    public InternalShare getById(int id) {
        String sql = "SELECT id, document_id, user_id, shared_by, created_at FROM internalShare WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InternalShare share = mapResultSetToInternalShare(rs);
                    share.setPermissions(getPermissionsByShareId(conn, share.getId()));
                    return share;
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(InternalShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi getById InternalShare", ex);
        }
        return null;
    }

    @Override
    public boolean insert(InternalShare model) {
        String insertShareSql = "INSERT INTO internalShare (document_id, user_id, shared_by, created_at) VALUES (?, ?, ?, ?)";
        String insertJunctionSql = "INSERT INTO share_permissions (share_id, permission_id) VALUES (?, ?)";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction
            
            try (PreparedStatement psShare = conn.prepareStatement(insertShareSql, Statement.RETURN_GENERATED_KEYS)) {
                
                psShare.setInt(1, model.getDocumentId());
                psShare.setInt(2, model.getUserId());
                psShare.setInt(3, model.getSharedBy());
                psShare.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                psShare.executeUpdate();
                
                int generatedShareId = 0;
                try (ResultSet rs = psShare.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedShareId = rs.getInt(1);
                    }
                }
                
                if (generatedShareId > 0 && model.getPermissions() != null) {
                    try (PreparedStatement psJunction = conn.prepareStatement(insertJunctionSql)) {
                        for (Permission p : model.getPermissions()) {
                            psJunction.setInt(1, generatedShareId);
                            psJunction.setInt(2, p.getId());
                            psJunction.addBatch();
                        }
                        psJunction.executeBatch();
                    }
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(InternalShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi insert InternalShare", ex);
            return false;
        }
    }

    /**
     * 🆕 Chia sẻ tài liệu cho 1 người dùng, đồng thời THAY THẾ hoàn toàn cấu hình
     * chia sẻ cũ (nếu document này đã từng được chia sẻ cho đúng người dùng đó).
     * Toàn bộ thao tác (xóa bản ghi cũ + junction quyền cũ, ghi bản ghi mới + quyền mới)
     * được gói trong 1 Transaction để đảm bảo tính toàn vẹn dữ liệu.
     */
    public boolean saveOrReplace(InternalShare model) {
        String deleteJunctionOfOldShareSql =
                "DELETE FROM share_permissions WHERE share_id IN " +
                "(SELECT id FROM internalShare WHERE document_id = ? AND user_id = ?)";
        String deleteOldShareSql = "DELETE FROM internalShare WHERE document_id = ? AND user_id = ?";
        String insertShareSql = "INSERT INTO internalShare (document_id, user_id, shared_by, created_at) VALUES (?, ?, ?, ?)";
        String insertJunctionSql = "INSERT INTO share_permissions (share_id, permission_id) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try {
                // 1. Xóa quyền (junction) của cấu hình chia sẻ CŨ giữa document này và user này
                try (PreparedStatement psDelJunction = conn.prepareStatement(deleteJunctionOfOldShareSql)) {
                    psDelJunction.setInt(1, model.getDocumentId());
                    psDelJunction.setInt(2, model.getUserId());
                    psDelJunction.executeUpdate();
                }

                // 2. Xóa bản ghi chia sẻ CŨ (nếu có)
                try (PreparedStatement psDelShare = conn.prepareStatement(deleteOldShareSql)) {
                    psDelShare.setInt(1, model.getDocumentId());
                    psDelShare.setInt(2, model.getUserId());
                    psDelShare.executeUpdate();
                }

                // 3. Ghi nhận bản chia sẻ MỚI
                int generatedShareId = 0;
                try (PreparedStatement psShare = conn.prepareStatement(insertShareSql, Statement.RETURN_GENERATED_KEYS)) {
                    psShare.setInt(1, model.getDocumentId());
                    psShare.setInt(2, model.getUserId());
                    psShare.setInt(3, model.getSharedBy());
                    psShare.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    psShare.executeUpdate();

                    try (ResultSet rs = psShare.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedShareId = rs.getInt(1);
                        }
                    }
                }

                // 4. Ghi nhận các quyền MỚI tương ứng
                if (generatedShareId > 0 && model.getPermissions() != null && !model.getPermissions().isEmpty()) {
                    try (PreparedStatement psJunction = conn.prepareStatement(insertJunctionSql)) {
                        for (Permission p : model.getPermissions()) {
                            psJunction.setInt(1, generatedShareId);
                            psJunction.setInt(2, p.getId());
                            psJunction.addBatch();
                        }
                        psJunction.executeBatch();
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(InternalShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi saveOrReplace InternalShare", ex);
            return false;
        }
    }

    @Override
    public boolean update(InternalShare model) {
        return false; 
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM internalShare WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(InternalShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi delete InternalShare", ex);
            return false;
        }
    }

    /**
     * Lấy danh sách các quyền dạng String (download, edit, print) của 1 người dùng trên 1 tài liệu cụ thể
     */
    public List<String> getUserDocumentPermissions(int documentId, int userId) {
        List<String> permissions = new ArrayList<>();
        String sql = "SELECT p.permission FROM permissions p " +
                     "JOIN share_permissions sp ON p.id = sp.permission_id " +
                     "JOIN internalShare s ON s.id = sp.share_id " +
                     "WHERE s.document_id = ? AND s.user_id = ?";
                     
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, documentId);
            ps.setInt(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permission").toLowerCase());
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(InternalShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi getUserDocumentPermissions", ex);
        }
        return permissions;
    }

    /**
     * 🆕 Lấy danh sách document_id đã được chia sẻ nội bộ cho 1 người dùng.
     * Tách riêng thành hàm này (thay vì JOIN thẳng với bảng "documents") vì
     * bảng internalShare nằm ở pool "jdbc/SharesDB", trong khi bảng documents
     * nằm ở pool "jdbc/NodesDB" — nếu 2 pool này trỏ tới 2 database vật lý
     * khác nhau thì không thể JOIN chéo được. DocumentDao sẽ dùng danh sách
     * ID trả về từ đây để tự truy vấn lại bảng documents trên đúng pool của nó.
     */
    public List<Integer> getDocumentIdsSharedWithUser(int userId) {
        List<Integer> documentIds = new ArrayList<>();
        String sql = "SELECT document_id FROM internalShare WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documentIds.add(rs.getInt("document_id"));
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(InternalShareDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi getDocumentIdsSharedWithUser", ex);
        }
        return documentIds;
    }

    private List<Permission> getPermissionsByShareId(Connection conn, int shareId) throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT p.id, p.permission FROM permissions p " +
                     "JOIN share_permissions sp ON p.id = sp.permission_id " +
                     "WHERE sp.share_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(new Permission(rs.getInt("id"), rs.getString("permission")));
                }
            }
        }
        return permissions;
    }

    private InternalShare mapResultSetToInternalShare(ResultSet rs) throws SQLException {
        InternalShare share = new InternalShare();
        share.setId(rs.getInt("id"));
        share.setDocumentId(rs.getInt("document_id"));
        share.setUserId(rs.getInt("user_id"));
        share.setSharedBy(rs.getInt("shared_by"));
        share.setCreatedAt(rs.getTimestamp("created_at"));
        return share;
    }
}
