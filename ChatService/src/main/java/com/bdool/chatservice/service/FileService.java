package com.bdool.chatservice.service;

import com.bdool.chatservice.model.Enum.EntityType;
import com.bdool.chatservice.model.entity.FileEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public interface FileService {
    FileEntity uploadFile(MultipartFile file, EntityType entityType);
    FileEntity updateFile(UUID fileId, MultipartFile newFile);
    ResponseEntity<?> downloadFile(UUID fileId, HttpServletRequest request);
    void deleteFile(UUID fileId);
}
