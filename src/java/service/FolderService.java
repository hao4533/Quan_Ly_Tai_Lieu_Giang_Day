/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.FolderDao;
import model.Folder;
import java.util.List;

/**
 *
 * @author Ryo
 */
public class FolderService {

    private final FolderDao folderDao;

    public FolderService() {
        this.folderDao = new FolderDao();
    }

    public List<Folder> getFolders(int user_id, int parent_id) {
        //parent_id = 0 nếu là thư mục gốc
        return folderDao.getAllFolderByParentId(user_id, parent_id);
    }

    public boolean createFolder(String name, int user_id, int parent_id) {
        String finalName = name.trim();
        int count = 1;

        // Vòng lặp kiểm tra: Nếu tên này đã tồn tại chính xác, tự động tạo ra tên mới và check tiếp
        while (folderDao.isFolderExist(user_id, parent_id, finalName)) {
            finalName = name.trim() + " (" + count + ")";
            count++;
        }

        Folder folder = new Folder();
        folder.setName(finalName);
        folder.setUserId(user_id);
        folder.setParentId(parent_id);

        return folderDao.insert(folder);
    }

    public boolean deleteFolder(int id, int user_id) {
        Folder dbFolder = folderDao.getById(id);

        //Kiểm tra tồn tại
        if (dbFolder == null) {
            System.out.println("Thư mục không tồn tại");
            return false;
        }

        //So sánh UserId của thư mục với UserId người dùng truyền vào
        if (dbFolder.getUserId() != user_id) {
            System.out.println("Không có quyền xóa thư mục");
            return false;
        }

        return folderDao.deleteSecure(id, user_id);
    }

    public boolean updateFolder(Folder folder) {
        //Kiểm tra quyền sở hữu thư mục
        if (!folderDao.isFolderOwner(folder.getId(), folder.getUserId())) {
            System.out.println("Không đủ quyền cập nhật thư mục");
            return false;
        }
        String finalName = folder.getName().trim();
        int count = 1;
        //Cập nhật tên nếu có thư mục có tên trùng lặp ở thư mục mới
        while (folderDao.isFolderExistForUpdate(folder.getUserId(), folder.getParentId(), finalName, folder.getId())) {
            finalName = folder.getName().trim() + " (" + count + ")";
            count++;
        }
        folder.setName(finalName);
        return folderDao.update(folder);
    }

    public List<Folder> searchFolder(int user_id, String searchQuery) {
        return folderDao.searchFolderName(user_id, searchQuery);
    }
}
