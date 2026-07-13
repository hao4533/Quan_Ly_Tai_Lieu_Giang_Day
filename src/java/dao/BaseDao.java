package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.naming.InitialContext;
import javax.sql.DataSource;


public abstract class BaseDao<T> {
    
    // Tên JNDI Resource của Service
    protected final String jndiName;

    // Hàm khởi tạo  truyền JNDI Name
    public BaseDao(String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * Hàm lấy kết nối tập trung GlassFish
     * @throws java.lang.Exception
     */
    protected Connection getConnection() throws Exception {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(this.jndiName);
        return ds.getConnection();
    }
  
    public abstract List<T> getAll();
    
    public abstract T getById(int id);
    
    public abstract boolean insert(T model);
    
    public abstract boolean update(T model);
    
    public abstract boolean delete(int id);
}