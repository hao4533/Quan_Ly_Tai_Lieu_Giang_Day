package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Share;

public class ShareDao extends BaseDao<Share> {

    public ShareDao() {
        super("jdbc/SharesDB");
    }

    @Override
    public List<Share> getAll() {
        return null;

    }

    @Override
    public Share getById(int id) {
        return null;
    }

    @Override
    public boolean insert(Share model) {
        return false;
    }

    @Override
    public boolean update(Share model) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;

    }
}
