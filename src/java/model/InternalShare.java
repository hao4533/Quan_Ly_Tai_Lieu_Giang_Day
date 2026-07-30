package model;

import java.sql.Timestamp;
import java.util.List;

public class InternalShare {
    private int id;
    private int documentId;
    private int userId;
    private int sharedBy;
    private Timestamp createdAt;
    // Danh sách quyền tương ứng với lượt chia sẻ này (Quan hệ nhiều-nhiều)
    private List<Permission> permissions;

    public InternalShare() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getSharedBy() { return sharedBy; }
    public void setSharedBy(int sharedBy) { this.sharedBy = sharedBy; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<Permission> getPermissions() { return permissions; }
    public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }
}