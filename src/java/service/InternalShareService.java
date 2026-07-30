package service;

import dao.InternalShareDao;
import model.InternalShare;
import model.Permission;
import java.util.ArrayList;
import java.util.List;

public class InternalShareService {

    private final InternalShareDao shareDao;

    public InternalShareService() {
        this.shareDao = new InternalShareDao();
    }

    /**
     * Xử lý nghiệp vụ chia sẻ tài liệu nội bộ.
     * Nếu tài liệu đã từng được chia sẻ cho đúng targetUserId này trước đó,
     * cấu hình chia sẻ cũ sẽ bị THAY THẾ hoàn toàn bằng cấu hình mới
     * (thay vì cộng dồn thêm 1 bản ghi khác).
     */
    public boolean shareDocument(int documentId, int targetUserId, int sharedByUserId, String[] permissionIdsStr) {
        if (permissionIdsStr == null || permissionIdsStr.length == 0) {
            return false;
        }

        // Không cho phép tự chia sẻ tài liệu cho chính mình
        if (targetUserId == sharedByUserId) {
            return false;
        }

        InternalShare share = new InternalShare();
        share.setDocumentId(documentId);
        share.setUserId(targetUserId);
        share.setSharedBy(sharedByUserId);
        
        // Khởi tạo danh sách quyền từ mảng String ID
        List<Permission> permissions = new ArrayList<>();
        try {
            for (String pidStr : permissionIdsStr) {
                permissions.add(new Permission(Integer.parseInt(pidStr.trim()), null));
            }
        } catch (NumberFormatException e) {
            System.getLogger(InternalShareService.class.getName()).log(System.Logger.Level.ERROR, "Lỗi ép kiểu Permission ID", e);
            return false;
        }
        
        share.setPermissions(permissions);

        // Gọi DAO: xóa cấu hình chia sẻ cũ (nếu có) rồi ghi cấu hình mới trong 1 Transaction
        return shareDao.saveOrReplace(share);
    }
    
    /**
     * Lấy danh sách các quyền (download, edit, print) của 1 người dùng trên 1 tài liệu
     * (Hàm này sẽ được gọi ở ViewOnlineServlet)
     */
    public List<String> getUserDocumentPermissions(int documentId, int userId) {
        return shareDao.getUserDocumentPermissions(documentId, userId);
    }
}
