/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 *
 * @author Ryo
 */
public class Share implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int documentId;     // Tài liệu nào đang được chia sẻ
    private int sharedByUserId; // ID người thực hiện chia sẻ (chủ sở hữu)
    private int sharedToUserId; // ID người nhận được quyền xem/tải tài liệu
    private LocalDateTime sharedAt; // Thời gian thực hiện liên kết chia sẻ

    //Constructor không tham số
    public Share() {
    }

    //Constructor đầy đủ tham số
    public Share(int id, int documentId, int sharedByUserId, int sharedToUserId, LocalDateTime sharedAt) {
        this.id = id;
        this.documentId = documentId;
        this.sharedByUserId = sharedByUserId;
        this.sharedToUserId = sharedToUserId;
        this.sharedAt = sharedAt;
    }

    //Constructor dùng khi chuẩn bị tạo bản ghi chia sẻ mới
    public Share(int documentId, int sharedByUserId, int sharedToUserId, LocalDateTime sharedAt) {
        this.documentId = documentId;
        this.sharedByUserId = sharedByUserId;
        this.sharedToUserId = sharedToUserId;
        this.sharedAt = sharedAt;
    }

    //Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getSharedByUserId() {
        return sharedByUserId;
    }

    public void setSharedByUserId(int sharedByUserId) {
        this.sharedByUserId = sharedByUserId;
    }

    public int getSharedToUserId() {
        return sharedToUserId;
    }

    public void setSharedToUserId(int sharedToUserId) {
        this.sharedToUserId = sharedToUserId;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }
}
