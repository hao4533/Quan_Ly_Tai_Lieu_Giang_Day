package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseDao {
    
    // Thông tin kết nối Users DB
    private static final String USERS_URL = "jdbc:postgresql://localhost:5432/j2ee_users_db";
    
    // Thông tin kết nối Shares DB
    private static final String SHARES_URL = "jdbc:postgresql://localhost:5432/j2ee_shares_db";
    
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "admin";
    
    // Kết nối tới Users DB
    protected Connection getUsersConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(USERS_URL, USERNAME, PASSWORD);
    }
    
    // Kết nối tới Shares DB
    protected Connection getSharesConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(SHARES_URL, USERNAME, PASSWORD);
    }
    
    // Giữ lại getConnection() cho tương thích với UserDao cũ
    protected Connection getConnection() throws SQLException {
        return getUsersConnection();
    }
}