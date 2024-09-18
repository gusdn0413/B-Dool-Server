package com.bdool.documentservice.controller;

import com.bdool.documentservice.entity.Document;
import com.bdool.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        // 문서 저장
        Document savedDocument = documentService.saveDocument(file);

        // 공동작업 URL 생성
        String collaborationUrl = "/collaborate/" + savedDocument.getId();

        // URL을 클라이언트에게 리턴
        Map<String, String> response = new HashMap<>();
        response.put("url", collaborationUrl);

        return ResponseEntity.ok(response);
    }

    // 특정 HTML 문서 조회 API (HTML 콘텐츠 반환)
    @GetMapping("/{id}")
    public ResponseEntity<String> getDocumentContent(@PathVariable Long id) {
        String content = documentService.getDocumentContent(id);
        return ResponseEntity.ok(content);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocumentContent(@PathVariable Long id, @RequestBody Document updatedDocument) {
        Document updatedDoc = documentService.updateDocument(id, updatedDocument.getContent());
        return ResponseEntity.ok(updatedDoc); // 업데이트된 문서 반환
    }
}