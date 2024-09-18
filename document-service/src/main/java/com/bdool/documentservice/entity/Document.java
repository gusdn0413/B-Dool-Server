package com.bdool.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob // 대용량 데이터를 위한 어노테이션
    @Column(columnDefinition = "LONGTEXT")
    private String content; // 변환된 HTML을 저장
}