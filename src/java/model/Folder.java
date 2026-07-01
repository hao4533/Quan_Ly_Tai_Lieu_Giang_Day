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
public class Folder {
    private int id;
    private String name;
    private int parent_id;
    private int user_id;
    private LocalDateTime create_at;
    
    //Hàm khởi tạo không tham số
    public Folder() {
    }
    
    // Hàm khởi tạo đầy đủ tham số
    public Folder(int id, String name, int parent_id, int user_id, LocalDateTime create_at) {
        this.id = id;
        this.name = name;
        this.parent_id = parent_id;
        this.user_id = user_id;
        this.create_at = create_at;
    }
    
    //Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName(){
        return this.name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public int getParentId(){
        return this.parent_id;
    }
    
    public void setParentId(int parent_id) {
        this.parent_id = parent_id;
    }
    
    public int getUserId(){
        return this.user_id;
    }
    
    public void setUserId(int user_id) {
        this.user_id = user_id;
    }
    
    public LocalDateTime getCreateAt(){
        return this.create_at;
    }
    
    public void setCreateAt(LocalDateTime create_at) {
        this.create_at = create_at;
    }
    
}
