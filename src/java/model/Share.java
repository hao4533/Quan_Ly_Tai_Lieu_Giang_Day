/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 *
 * @author Ryo
 */
public class Share {

    private String token;
    private int documentId;
    private String recipientEmail;
    private Timestamp expireAt;

    public Share() {
    }

    public Share(String token, int documentId, String recipientEmail, Timestamp expireAt) {
        this.token = token;
        this.documentId = documentId;
        this.recipientEmail = recipientEmail;
        this.expireAt = expireAt;
    }

    // Getters và Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Timestamp getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Timestamp expireAt) {
        this.expireAt = expireAt;
    }
}
