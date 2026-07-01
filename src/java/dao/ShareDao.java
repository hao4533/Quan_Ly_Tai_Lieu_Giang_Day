package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShareDao extends BaseDao {

    public List<String> getAllShares() {
        String sql = "SELECT * FROM shares"; // đổi theo tên bảng thực tế
        List<String> list = new ArrayList<>();
        
        try (Connection conn = getSharesConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(rs.getString(1)); // đổi theo cột thực tế
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}