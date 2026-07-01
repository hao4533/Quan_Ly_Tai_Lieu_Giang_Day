/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Folder;

/**
 *
 * @author Ryo
 */
public class FolderDao extends BaseDao<Folder> {

    public FolderDao(String jndiName) {
        super(jndiName);
    }

    @Override
    public List<Folder> getAll() {
        return null;
    }

    @Override
    public Folder getById(int id) {
        return null;
    }

    @Override
    public boolean insert(Folder model) {
        return false;
    }

    @Override
    public boolean update(Folder model) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    // Hàm bổ sung: Lấy các thư mục con bên trong thư mục hiện tại của User
    public List<Folder> getFoldersByParent(int parentId, int userId) {
        return null;
    }
}
