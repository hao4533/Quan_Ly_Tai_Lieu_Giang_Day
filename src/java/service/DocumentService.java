/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.DocumentDao;
import java.util.List;
import model.Document;

/**
 *
 * @author Ryo
 */
public class DocumentService {
    private final DocumentDao documentDao = new DocumentDao();
    
    public boolean isOwnerOfDocument(int documentId, int userId) {
        return documentDao.checkOwnership(documentId, userId);
    }
    public Document getDocumentById(int documentId) {
        return documentDao.getById(documentId);
    }

    public List<Document> getRootDocuments(int userId) {
        return documentDao.getRootDocumentsByUserId(userId);
    }
}
