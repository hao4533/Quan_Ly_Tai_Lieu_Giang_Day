/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDateTime;

/**
 *
 * @author Ryo
 */
public class Document {

    private int id;
    private String original_name;
    private String physical_path;
    private String file_extension;
    private long file_size_bytes;
    private int folder_id;
    private int user_id;
    private LocalDateTime updated_at;

    // Hàm khởi tạo không tham số
    public Document() {
    }

    //Constructor đầy đủ tham số (Dùng khi đọc từ Database)
    public Document(int id, String original_name, String physical_path, String file_extension,
            long file_size_bytes, int folder_id, int user_id, LocalDateTime updated_at) {
        this.id = id;
        this.original_name = original_name;
        this.physical_path = physical_path;
        this.file_extension = file_extension;
        this.file_size_bytes = file_size_bytes;
        this.folder_id = folder_id;
        this.user_id = user_id;
        this.updated_at = updated_at;
    }    
    
    //Constructor không cần ID (Dùng khi tạo đối tượng mới để chuẩn bị lưu vào DB
    public Document(String original_name, String physical_path, String file_extension,
            long file_size_bytes, int folder_id, int user_id, LocalDateTime updated_at) {
        this.original_name = original_name;
        this.physical_path = physical_path;
        this.file_extension = file_extension;
        this.file_size_bytes = file_size_bytes;
        this.folder_id = folder_id;
        this.user_id = user_id;
        this.updated_at = updated_at;
    }

    //Getter và setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginal_name() {
        return original_name;
    }

    public void setOriginal_name(String original_name) {
        this.original_name = original_name;
    }

    public String getPhysical_path() {
        return physical_path;
    }

    public void setPhysical_path(String physical_path) {
        this.physical_path = physical_path;
    }

    public String getFile_extension() {
        return file_extension;
    }

    public void setFile_extension(String file_extension) {
        this.file_extension = file_extension;
    }

    public long getFile_size_bytes() {
        return file_size_bytes;
    }

    public void setFile_size_bytes(long file_size_bytes) {
        this.file_size_bytes = file_size_bytes;
    }

    public int getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(int folder_id) {
        this.folder_id = folder_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
