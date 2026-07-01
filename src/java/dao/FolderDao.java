/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Folder;

/**
 *
 * @author Ryo
 */
public class FolderDao extends BaseDao<Folder> {

    public FolderDao() {
        super("jdbc/NodesDB");
    }

    @Override
    public Folder getById(int id) {
        String sql = "SELECT * FROM folders WHERE id = ?";
        Folder folder = null;

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    folder = new Folder();
                    folder.setId(rs.getInt("id"));
                    folder.setName(rs.getString("name"));
                    folder.setParentId(rs.getInt("parent_id"));
                    folder.setUserId(rs.getInt("user_id"));
                    folder.setCreateAt(rs.getObject("created_at", LocalDateTime.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folder;
    }

    @Override
    public boolean insert(Folder folder) {
        String sql = "INSERT INTO folders (name, parent_id, user_id) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, folder.getName());
            if (folder.getParentId() == 0) {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(2, folder.getParentId());
            }
            pstmt.setInt(3, folder.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Lấy folder ở root của người dùng
    public List<Folder> getRootFolderByUserId(int user_id) {
        //Tạo danh sách chứa folder
        List<Folder> list = new ArrayList<>();
        String sql = "SELECT * FROM folders WHERE user_id=? AND parent_id IS NULL";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setInt(1, user_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Folder folder = new Folder();
                    folder.setId(rs.getInt("id"));
                    folder.setName(rs.getString("name"));
                    folder.setParentId(rs.getInt("parent_id"));
                    folder.setUserId(rs.getInt("user_id"));
                    folder.setCreateAt(rs.getObject("created_at", LocalDateTime.class));

                    // Thêm phần tử vào list
                    list.add(folder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //Lấy tất cả folder dựa theo ID
    public List<Folder> getAllFolderByParentId(int user_id, int parent_int) {
        List<Folder> list = new ArrayList<>();
        String sql;
        //Nếu lấy từ folder root thì thay đổi lấy với parent_id là NULL
        if (parent_int == 0) {
            sql = "SELECT * FROM folders WHERE user_id = ? AND parent_id IS NULL";
        } else {
            sql = "SELECT * FROM folders WHERE user_id = ? AND parent_id = ?";
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setInt(1, user_id);
            if (parent_int != 0) {
                pstmt.setInt(2, parent_int);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Folder folder = new Folder();
                    folder.setId(rs.getInt("id"));
                    folder.setName(rs.getString("name"));
                    folder.setParentId(rs.getInt("parent_id"));
                    folder.setUserId(rs.getInt("user_id"));
                    folder.setCreateAt(rs.getObject("created_at", LocalDateTime.class));

                    // Thêm phần tử vào list
                    list.add(folder);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
//Kiểm tra thư mục đã tồn tại chưa

    public boolean isFolderExist(int userId, int parentId, String name) {
        String sql;
        if (parentId == 0) {
            sql = "SELECT 1 FROM folders WHERE user_id = ? AND parent_id IS NULL AND name = ?";
        } else {
            sql = "SELECT 1 FROM folders WHERE user_id = ? AND parent_id = ? AND name = ?";
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            if (parentId == 0) {
                pstmt.setString(2, name.trim());
            } else {
                pstmt.setInt(2, parentId);
                pstmt.setString(3, name.trim());
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Trả về true nếu tên này đã tồn tại chính xác
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    //Kiểm tra thư mục đã tồn tại khi cập nhật không
    public boolean isFolderExistForUpdate(int userId, int parentId, String name, int currentFolderId) {
        String sql;
        // Thêm điều kiện AND id <> ? để loại trừ chính nó ra khi kiểm tra trùng tên
        if (parentId == 0) {
            sql = "SELECT 1 FROM folders WHERE user_id = ? AND parent_id IS NULL AND name = ? AND id <> ?";
        } else {
            sql = "SELECT 1 FROM folders WHERE user_id = ? AND parent_id = ? AND name = ? AND id <> ?";
        }
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            if (parentId == 0) {
                pstmt.setString(2, name.trim());
                pstmt.setInt(3, currentFolderId);
            } else {
                pstmt.setInt(2, parentId);
                pstmt.setString(3, name.trim());
                pstmt.setInt(4, currentFolderId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Kiểm tra người dùng có phải chủ sở hữu thư mục hay không
    public boolean isFolderOwner(int id, int user_id) {
        String sql = "SELECT * FROM folders WHERE id = ? AND user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, user_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Lấy parent_id dựa theo id của folder
    public int getParentIdById(int id) {
        String sql = "SELECT parent_id FROM folders WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    //Trả về parent_id nếu null trả về 0
                    return rs.getInt("parent_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Trả về 0 nếu không tìm thấy hoặc là thư mục gốc
        return 0;
    }

    //Tìm tên folder dựa theo tên
    public List<Folder> searchFolderName(int user_id, String query) {
        List<Folder> list = new ArrayList<>();
        String sql = "SELECT * FROM folders WHERE user_id = ? AND name ILIKE ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, user_id);

            // Cấu hình từ khóa tìm kiếm dưới dạng: %keyword%
            String searchPattern = "%" + query.trim() + "%";
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Folder folder = new Folder();
                    folder.setId(rs.getInt("id"));
                    folder.setName(rs.getString("name"));
                    folder.setParentId(rs.getInt("parent_id"));
                    folder.setUserId(rs.getInt("user_id"));
                    folder.setCreateAt(rs.getObject("created_at", LocalDateTime.class));

                    list.add(folder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //Cập nhật tên thư mục hoặc di chuyển thư mục
    @Override
    public boolean update(Folder folder) {
        String sql = "UPDATE folders SET name = ?, parent_id = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, folder.getName());

            //Nếu di chuyển ra thư mục gốc
            if (folder.getParentId() == 0) {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(2, folder.getParentId());
            }

            pstmt.setInt(3, folder.getId());
            pstmt.setInt(4, folder.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Hàm xóa có kiểm tra quyền sở hữu của người dùng
    public boolean deleteSecure(int id, int user_id) {
        String sql = "DELETE FROM folders WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, user_id);

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

    @Override
    public List<Folder> getAll() {
        return null;
    }

}
