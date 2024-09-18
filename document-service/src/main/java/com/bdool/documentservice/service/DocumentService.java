package com.bdool.documentservice.service;

import com.bdool.documentservice.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentService {

    Document saveDocument(MultipartFile file) throws IOException;

    String getDocumentContent(Long id);

    Document updateDocument(Long id, String content);
}
