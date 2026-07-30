package dao;

import model.Document;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import java.sql.SQLException;

public class DocumentDao extends BaseDao<Document> {

    public DocumentDao() {
        super("jdbc/NodesDB");
    }

    public List<Document> getRootDocumentsByUserId(int userId) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE user_id = ? AND folder_id IS NULL ORDER BY updated_at DESC";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Document doc = new Document();
                    doc.setId(rs.getInt("id"));
                    doc.setOriginal_name(rs.getString("original_name"));
                    doc.setPhysical_path(rs.getString("physical_path"));
                    doc.setFile_extension(rs.getString("file_extension"));
                    doc.setFile_size_bytes(rs.getLong("file_size_bytes"));
                    doc.setFolder_id(rs.getInt("folder_id"));
                    doc.setUser_id(rs.getInt("user_id"));
                    doc.setUpdated_at(rs.getObject("updated_at", LocalDateTime.class));
                    list.add(doc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // 🔧 SỬA LỖI: Lấy danh sách tài liệu mà người dùng được chia sẻ.
    //
    // TRƯỚC ĐÂY: chạy 1 câu SQL JOIN thẳng "documents" (pool jdbc/NodesDB) với
    // "internalShare" (pool jdbc/SharesDB) trên cùng 1 Connection của NodesDB.
    // Nếu 2 pool này trỏ tới 2 database vật lý khác nhau thì PostgreSQL sẽ báo
    // lỗi "relation internalshare does not exist" (bị nuốt bởi catch), khiến
    // hàm luôn trả về danh sách rỗng -> tab "Đã được chia sẻ" không hiện gì.
    //
    // BÂY GIỜ: tách làm 2 bước, mỗi bước dùng đúng pool của bảng tương ứng:
    //   1. Lấy danh sách document_id từ InternalShareDao (pool SharesDB)
    //   2. Lấy chi tiết từng Document bằng getById() (pool NodesDB)
    // =========================================================================
    public List<Document> getSharedDocumentsByUserId(int userId) {
        List<Document> list = new ArrayList<>();

        InternalShareDao internalShareDao = new InternalShareDao();
        List<Integer> sharedDocumentIds = internalShareDao.getDocumentIdsSharedWithUser(userId);

        for (Integer docId : sharedDocumentIds) {
            Document doc = getById(docId);
            if (doc != null) {
                list.add(doc);
            }
        }
        return list;
    }

    @Override
    public boolean insert(Document doc) {
        String sql = "INSERT INTO documents (original_name, physical_path, file_extension, file_size_bytes, folder_id, user_id, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doc.getOriginal_name());
            pstmt.setString(2, doc.getPhysical_path());
            pstmt.setString(3, doc.getFile_extension());
            pstmt.setLong(4, doc.getFile_size_bytes());

            if (doc.getFolder_id() == 0) {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(5, doc.getFolder_id());
            }

            pstmt.setInt(6, doc.getUser_id());
            // Sử dụng setTimestamp kết hợp java.sql.Timestamp để tương thích tốt nhất với PostgreSQL
            pstmt.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Document> getAll() {
        return null;
    }

    @Override
    public Document getById(int id) {
        String sql = "SELECT * FROM documents WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Document doc = new Document();
                    doc.setId(rs.getInt("id"));
                    doc.setOriginal_name(rs.getString("original_name"));
                    doc.setPhysical_path(rs.getString("physical_path"));
                    doc.setFile_extension(rs.getString("file_extension"));
                    doc.setFile_size_bytes(rs.getLong("file_size_bytes"));
                    doc.setFolder_id(rs.getInt("folder_id"));
                    doc.setUser_id(rs.getInt("user_id"));
                    doc.setUpdated_at(rs.getObject("updated_at", java.time.LocalDateTime.class));
                    return doc;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Document doc) {
        String sql = "UPDATE documents SET file_size_bytes = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, doc.getFile_size_bytes());
            pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(doc.getUpdated_at()));
            pstmt.setInt(3, doc.getId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    // Xóa tài liệu có kiểm tra quyền sở hữu (chỉ chủ sở hữu mới được xóa)
    public boolean deleteSecure(int id, int userId) {
        String sql = "DELETE FROM documents WHERE id = ? AND user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra người dùng có phải chủ sở hữu tài liệu hay không
    public boolean isOwner(int id, int userId) {
        String sql = "SELECT 1 FROM documents WHERE id = ? AND user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra người dùng có phải chủ sở hữu hợp pháp của tài liệu hay không (Dùng cho quy trình gửi Email)
     */
    public boolean checkOwnership(int documentId, int userId) {
        String sql = "SELECT 1 FROM documents WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, documentId);
            pstmt.setInt(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Trả về true nếu đúng chủ sở hữu
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(DocumentDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi checkOwnership", ex);
        }
        return false;
    }
}
