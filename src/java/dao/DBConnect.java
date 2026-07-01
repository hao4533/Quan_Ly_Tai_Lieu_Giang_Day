/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Ryo
 */
public class DBConnect {
    public static Connection getConnection(String jndiName) throws NamingException, SQLException {
        //Khởi tạo ngữ cảnh tìm kiếm của GlassFish
        InitialContext ctx = new InitialContext();
        
        //Tìm kiếm Resource thông qua JNDI Name
        DataSource ds = (DataSource) ctx.lookup(jndiName);
        
        //Trả về kết nối lấy từ Connection Pool
        return ds.getConnection();
    }
}