package com.bdool.documentservice.service.impl;

import com.bdool.documentservice.entity.Document;
import com.bdool.documentservice.repository.DocumentRepository;
import com.bdool.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    @Value("${file.upload-dir}")
    private final String uploadDir;
    private final RedisTemplate<String, String> redisTemplate;

    @Override

    public Document saveDocument(MultipartFile file) {
        String fileName = storeFile(file); // 파일 시스템에 HTML 파일 저장
        String content = readFileContent(file); // HTML 내용 읽어오기

        Document document = Document.builder()
                .name(fileName)
                .content(content)
                .build();

        redisTemplate.opsForValue().set("document:" + document.getId(), content);
        return documentRepository.save(document);
    }

    public String getDocumentContent(Long id) {
        String redisContent = redisTemplate.opsForValue().get("document:" + id);

        if (redisContent == null) {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found with id " + id));
            redisTemplate.opsForValue().set("document:" + id, document.getContent());
            return document.getContent();  // DB에서 가져온 원본 콘텐츠 반환
        }

        return redisContent; // Redis에 저장된 콘텐츠 반환
    }

    @Override
    public Document updateDocument(Long id, String updatedContent) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id " + id));

        // 문서 내용 업데이트
        document.setContent(updatedContent);
        // 업데이트된 문서 저장
        return documentRepository.save(document);

    }

    private String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + originalFileName; // UUID로 고유한 파일 이름 생성
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not store document " + fileName, ex);
        }
        return fileName;
    }

    // 파일의 내용을 문자열로 읽어오는 메서드 (HTML 파일 읽기)
    private String readFileContent(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read document content", e);
        }
    }
}