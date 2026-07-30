package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import model.Permission;

public class PermissionDao extends BaseDao<Permission> {

    // JNDI Name cấu hình Connection Pool
    public PermissionDao() {
        super("jdbc/SharesDB");
    }

    @Override
    public List<Permission> getAll() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT id, permission FROM permissions";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPermission(rs));
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(PermissionDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi getAll permissions", ex);
        }
        return list;
    }

    @Override
    public Permission getById(int id) {
        String sql = "SELECT id, permission FROM permissions WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermission(rs);
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.getLogger(PermissionDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi getById permission", ex);
        }
        return null;
    }

    @Override
    public boolean insert(Permission model) {
        String sql = "INSERT INTO permissions (permission) VALUES (?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, model.getPermission());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(PermissionDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi insert permission", ex);
            return false;
        }
    }

    @Override
    public boolean update(Permission model) {
        String sql = "UPDATE permissions SET permission = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, model.getPermission());
            ps.setInt(2, model.getId());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(PermissionDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi update permission", ex);
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM permissions WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.getLogger(PermissionDao.class.getName()).log(System.Logger.Level.ERROR, "Lỗi delete permission", ex);
            return false;
        }
    }

    /**
     * Hàm tiện ích ánh xạ từ ResultSet sang Object Model
     */
    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        Permission p = new Permission();
        p.setId(rs.getInt("id"));
        p.setPermission(rs.getString("permission"));
        return p;
    }
}