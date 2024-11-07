package com.bdool.memberhubservice.member.domain.profile.service.impl;

import com.bdool.memberhubservice.member.domain.global.util.JwtUtil;
import com.bdool.memberhubservice.member.domain.member.entity.Member;
import com.bdool.memberhubservice.member.domain.member.service.MemberService;
import com.bdool.memberhubservice.member.domain.profile.entity.Profile;
import com.bdool.memberhubservice.member.domain.profile.entity.model.*;
import com.bdool.memberhubservice.member.domain.profile.entity.model.sse.ProfileNicknameResponse;
import com.bdool.memberhubservice.member.domain.profile.entity.model.sse.ProfileOnlineResponse;
import com.bdool.memberhubservice.member.domain.profile.repository.ProfileRepository;
import com.bdool.memberhubservice.member.domain.profile.service.ProfileService;
import com.bdool.memberhubservice.notification.NotificationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bdool.memberhubservice.member.domain.profile.entity.Profile.toProfileResponse;
import static com.bdool.memberhubservice.member.domain.profile.entity.model.ProfileFindResponse.fromEntity;
import static com.bdool.memberhubservice.member.domain.profile.service.NotificationServiceHelper.createNotificationModel;
import static com.bdool.memberhubservice.member.domain.profile.service.NotificationServiceHelper.sendNotification;
import static com.bdool.memberhubservice.member.domain.profile.service.ParticipantServiceHelper.*;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final MemberService memberService;
    private final ProfileSSEService sseService;
    private final JwtUtil jwtUtil;
    private final WebClient webClient;
    @Value("${notification-service.url}")
    private String notificationServiceUrl;
    @Value("${channel-service.url}")
    private String channelServiceUrl;

    @Transactional
    @Override
    public ProfileResponse save(ProfileModel profileModel, Long memberId) {
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
        Profile profile = Profile.builder()
                .name(profileModel.getName())
                .position(profileModel.getPosition())
                .nickname(profileModel.getNickname())
                .profileImgUrl(profileModel.getProfileImgUrl())
                .memberId(member.getId())
                .workspaceId(profileModel.getWorkspaceId())
                .isWorkspaceCreator(profileModel.getIsWorkspaceCreator())
                .isOnline(false)
                .email(member.getEmail())
                .build();
        Profile savedProfile = profileRepository.save(profile);
        return toProfileResponse(savedProfile);
    }

    @Transactional
    @Override
    public ProfileResponse saveByInvitation(ProfileModel profileModel, Long memberId, Long workspaceId) {
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
        Profile profile = Profile.builder()
                .name(profileModel.getName())
                .nickname(profileModel.getNickname())
                .profileImgUrl(profileModel.getProfileImgUrl())
                .memberId(member.getId())
                .isOnline(false)
                .workspaceId(workspaceId)
                .isWorkspaceCreator(false)
                .email(member.getEmail())
                .build();
        Profile savedProfile = profileRepository.save(profile);

        notifyWorkspaceMembers(profileModel, workspaceId);

        return toProfileResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    @Override
    public ProfileFindResponse getProfileById(Long profileId) {
        Profile profile = profileRepository.findById(profileId).orElseThrow();
        return fromEntity(profile);
    }


    @Override
    public List<ProfileResponse> getProfileByWorkspaceId(Long workspaceId) {
        List<Profile> profiles = profileRepository.findProfilesByWorkspaceId(workspaceId);
        return profiles.stream()
                .map(Profile::toProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProfileMemberIdResponse> getProfileByMemberId(Long memberId) {
        List<Profile> profiles = profileRepository.findByMemberId(memberId);

        return profiles.stream()
                .map(profile -> new ProfileMemberIdResponse(
                        profile.getNickname(),
                        profile.getProfileImgUrl(),
                        profile.getWorkspaceId()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteById(Long profileId) {
        profileRepository.deleteById(profileId);
    }

    @Transactional
    @Override
    public ProfileFindResponse update(Long profileId, ProfileUpdateRequest profileUpdateRequest) {
        Profile findProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));
        findProfile.updateProfile(profileUpdateRequest.getName(),profileUpdateRequest.getNickname(),
                profileUpdateRequest.getPosition(),
                profileUpdateRequest.getProfileImgUrl());
        profileRepository.save(findProfile);

        ProfileNicknameResponse profileNicknameResponse = new ProfileNicknameResponse(
                profileId,
                findProfile.getWorkspaceId(),
                findProfile.getNickname());
        sseService.notifyNicknameChange(profileNicknameResponse);
        sendNicknameToChannelService(webClient, channelServiceUrl, profileId, findProfile.getNickname());
        return fromEntity(findProfile);
    }

    @Transactional
    @Override
    public String updateStatus(Long profileId, String status) {
        Profile findProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));
        findProfile.updateStatus(status);
        profileRepository.save(findProfile);

        return findProfile.getStatus();
    }

    @Transactional
    @Override
    public Boolean updateOnline(Long profileId, Boolean isOnline) {
        Profile findProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("profile not found"));
        findProfile.updateOnline(isOnline);
        Profile save = profileRepository.save(findProfile);

        ProfileOnlineResponse profileOnlineResponse = new ProfileOnlineResponse(
                profileId, findProfile.getWorkspaceId(), findProfile.getIsOnline()
        );

        sseService.notifyOnlineChange(profileOnlineResponse);
        sendOnlineStatusToChannelService(webClient, channelServiceUrl, profileId, isOnline);

        return save.getIsOnline();
    }

    @Override
    public Optional<Profile> findProfileById(Long invitorId) {
        return profileRepository.findById(invitorId);
    }

    @Override
    public List<Profile> getProfileByToken(String accessToken) {
        Map<String, Object> objectMap = jwtUtil.extractEmail(accessToken);
        String email = (String) objectMap.get("sub");
        return profileRepository.findByEmail(email);
    }

    @Override
    public Profile getProfileByMemberIdAndWorkspaceId(Long memberId, Long workspaceId) {
        return profileRepository.findByMemberIdAndWorkspaceId(memberId, workspaceId);
    }

    private void notifyWorkspaceMembers(ProfileModel profileModel, Long workspaceId) {
        List<Profile> profilesInWorkspace = profileRepository.findProfilesByWorkspaceId(workspaceId);
        for (Profile profileInWorkspace : profilesInWorkspace) {
            NotificationModel notificationModel = createNotificationModel(profileModel, workspaceId, profileInWorkspace.getId());

            sendNotification(webClient, notificationServiceUrl, notificationModel);
        }
    }
}