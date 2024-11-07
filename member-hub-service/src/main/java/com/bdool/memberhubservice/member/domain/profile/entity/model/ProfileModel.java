package com.bdool.memberhubservice.member.domain.profile.entity.model;

import lombok.Data;

@Data
public class ProfileModel {

    private String name; // 이름
    private String position;
    private String nickname; // 별명
    private String profileImgUrl; // 프로필 이미지 URL
    private Boolean isOnline;
    private Long workspaceId;
    private Boolean isWorkspaceCreator;
}
