package com.bdool.memberhubservice.member.domain.profile.entity.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProfileUpdateRequest {
    private String name;
    private String nickname;
    private String position;
    private String profileImgUrl;
}
