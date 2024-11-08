package com.bdool.chatservice.service.impl;

import com.bdool.chatservice.exception.ChannelNotFoundException;
import com.bdool.chatservice.model.Enum.ChannelType;
import com.bdool.chatservice.model.domain.ChannelModel;
import com.bdool.chatservice.model.entity.ChannelEntity;
import com.bdool.chatservice.model.entity.ParticipantEntity;
import com.bdool.chatservice.model.repository.ChannelRepository;
import com.bdool.chatservice.model.repository.ParticipantRepository;
import com.bdool.chatservice.service.ChannelService;
import com.bdool.chatservice.sse.ChannelSSEService;
import com.bdool.chatservice.sse.model.ChannelAddResponse;
import com.bdool.chatservice.sse.model.ChannelDeleteResponse;
import com.bdool.chatservice.sse.model.ChannelRenameResponse;
import com.bdool.chatservice.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelSSEService channelSSEService;
    private final ParticipantRepository participantRepository;

    @Override
    @Transactional
    public ChannelEntity save(ChannelModel channelModel) {
        // DM 채널일 경우 추가 검증
        if (channelModel.getChannelType() == ChannelType.DM) {
            if (channelModel.getDmRequestId() == null) {
                throw new IllegalArgumentException("DM channels must have a target profile ID (dmRequestId).");
            }
            if (channelRepository.existsByWorkspacesIdAndDmRequestId(channelModel.getWorkspacesId(), channelModel.getDmRequestId())) {
                throw new IllegalArgumentException("A DM channel already exists with this dmRequestId in the workspace.");
            }
        } else {
            // 일반 채널일 경우 dmRequestId를 null로 설정
            channelModel.setDmRequestId(null);
        }

        // ChannelEntity 생성
        UUID channelId = UUIDUtil.getOrCreateUUID(channelModel.getChannelId());
        ChannelEntity channelEntity = ChannelEntity.builder()
                .channelId(channelId)
                .name(channelModel.getName())
                .description(channelModel.getDescription())
                .isPrivate(channelModel.getIsPrivate() != null ? channelModel.getIsPrivate() : true) // 기본값 설정
                .channelType(channelModel.getChannelType())
                .workspacesId(channelModel.getWorkspacesId())
                .profileId(channelModel.getProfileId())
                .dmRequestId(channelModel.getDmRequestId()) // DM일 경우 dmRequestId 설정, 일반 채널은 null
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 채널 저장
        ChannelEntity savedChannel = channelRepository.save(channelEntity);

        ChannelAddResponse addResponse = new ChannelAddResponse();
        addResponse.setChannelId(savedChannel.getChannelId());
        addResponse.setChannelName(savedChannel.getName());
        channelSSEService.notifyChannelAdd(addResponse);

        // DM 또는 일반 채널 모두 생성자 정보를 참석자로 저장
        participantRepository.save(
                ParticipantEntity.builder()
                        .participantId(UUIDUtil.getOrCreateUUID(null))
                        .channelId(savedChannel.getChannelId())
                        .isOnline(true)
                        .joinedAt(LocalDateTime.now())
                        .nickname(channelModel.getNickname())
                        .profileId(channelModel.getProfileId())
                        .profileURL(channelModel.getProfileURl())
                        .build()
        );

        return savedChannel;
    }


    @Override
    @Transactional
    public ChannelEntity update(UUID channelId, UUID profileId, ChannelModel channel) {
        return channelRepository.findById(channelId)
                .map(existingChannel -> {
                    // 전달받은 profileId와 기존의 profileId가 동일하지 않으면 예외 발생
                    if (!existingChannel.getProfileId().equals(profileId)) {
                        throw new IllegalArgumentException("You are not authorized to update this channel.");
                    }

                    // Optional을 사용하여 null이 아닐 경우에만 필드를 업데이트
                    Optional.ofNullable(channel.getName()).ifPresent(name -> {
                        existingChannel.setName(name);
                        updateChannelName(channelId, name);
                    });
                    Optional.ofNullable(channel.getDescription()).ifPresent(existingChannel::setDescription);
                    Optional.ofNullable(channel.getIsPrivate()).ifPresent(existingChannel::setIsPrivate);
                    Optional.ofNullable(channel.getChannelType()).ifPresent(existingChannel::setChannelType);

                    // workspacesId는 필수 필드이므로, null이 아니어야 업데이트
                    Optional.ofNullable(channel.getWorkspacesId()).ifPresentOrElse(
                            existingChannel::setWorkspacesId,
                            () -> { throw new ChannelNotFoundException("Workspace ID cannot be null during channel update."); }
                    );

                    existingChannel.setUpdatedAt(LocalDateTime.now());

                    return channelRepository.save(existingChannel);
                }).orElseThrow(() -> new ChannelNotFoundException("Channel not found with ID: " + channelId));
    }


    @Override
    public List<ChannelEntity> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public List<ChannelEntity> findAllByWorkspacesId(Long id) {
        return channelRepository.findAllByWorkspacesId(id);
    }

    @Override
    public ChannelEntity findDefaultChannelsByWorkspacesId(Long workspacesId) {
        return channelRepository.findByWorkspacesIdAndChannelType(workspacesId, "DEFAULT");
    }

    @Override
    public Optional<ChannelEntity> findById(UUID channelId) {
        return channelRepository.findById(channelId);
    }

    @Override
    @Transactional
    public void deleteById(UUID channelId) {
        if (channelRepository.existsById(channelId)) {
            channelRepository.deleteById(channelId);
            ChannelDeleteResponse deleteResponse = new ChannelDeleteResponse();
            deleteResponse.setChannelId(channelId);
            channelSSEService.notifyChannelDelete(deleteResponse);
        } else {
            throw new RuntimeException("Channel not found with ID: " + channelId);
        }
    }

    public ChannelEntity updateChannelName(UUID channelId, String newName) {
        return channelRepository.findById(channelId).map(existingChannel -> {
            existingChannel.setName(newName);
            existingChannel.setUpdatedAt(LocalDateTime.now());
            ChannelEntity updatedChannel = channelRepository.save(existingChannel);

            // 채널 이름 변경 SSE 알림
            ChannelRenameResponse renameResponse = new ChannelRenameResponse(updatedChannel.getChannelId(), newName);
            channelSSEService.notifyChannelRename(renameResponse);

            return updatedChannel;
        }).orElseThrow(() -> new ChannelNotFoundException("Channel not found with ID: " + channelId));
    }
}
