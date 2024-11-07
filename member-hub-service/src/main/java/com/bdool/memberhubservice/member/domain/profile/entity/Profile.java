package com.bdool.memberhubservice.member.domain.profile.entity;

import com.bdool.memberhubservice.member.domain.profile.entity.model.ProfileResponse;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "profiles")
@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // 이름
    private String nickname; // 별명
    private String position; // 직책
    private String status; // 상태메세지
    private String profileImgUrl; // 프로필 이미지 URL
    private Boolean isOnline; // 온라인/오프라인 표시
    private Boolean isWorkspaceCreator;

    private Long memberId;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long workspaceId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt =  LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String name, String nickname, String position, String profileImgUrl) {
        this.name = name;
        this.nickname = nickname;
        this.position = position;
        this.profileImgUrl = profileImgUrl;
        this.updatedAt = LocalDateTime.now();  // 업데이트 시간 갱신
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void updateOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public static ProfileResponse toProfileResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getNickname(),
                profile.getPosition(),
                profile.getStatus(),
                profile.getProfileImgUrl(),
                profile.getIsOnline(),
                profile.getIsWorkspaceCreator(),
                profile.getMemberId(),
                profile.getEmail(),
                profile.getWorkspaceId()
        );
    }
}

