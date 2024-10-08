package com.bdool.chatservice.service.impl;

import com.bdool.chatservice.exception.ChannelNotFoundException;
import com.bdool.chatservice.model.domain.ChannelModel;
import com.bdool.chatservice.model.entity.ChannelEntity;
import com.bdool.chatservice.model.repository.ChannelRepository;
import com.bdool.chatservice.service.ChannelService;
import com.bdool.chatservice.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;

    @Override
    public ChannelEntity save(ChannelModel channel) {
        UUID channelId = UUIDUtil.getOrCreateUUID(channel.getChannelId());
        return channelRepository.save(ChannelEntity.builder()
                .channelId(channelId)
                .name(channel.getName())
                .description(channel.getDescription())
                .isPrivate(channel.getIsPrivate())
                .channelType(channel.getChannelType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profileId(channel.getProfileId())
                .workspacesId(channel.getWorkspacesId())
                .build());
    }

    @Override
    public ChannelEntity update(UUID channelId, UUID profileId, ChannelModel channel) {
        return channelRepository.findById(channelId)
                .map(existingChannel -> {
                    // 전달받은 profileId와 기존의 profileId가 동일하지 않으면 예외 발생
                    if (!existingChannel.getProfileId().equals(profileId)) {
                        throw new IllegalArgumentException("You are not authorized to update this channel.");
                    }

                    existingChannel.setName(channel.getName());
                    existingChannel.setDescription(channel.getDescription());
                    existingChannel.setIsPrivate(channel.getIsPrivate());
                    existingChannel.setChannelType(channel.getChannelType());
                    existingChannel.setUpdatedAt(LocalDateTime.now());

                    // workspacesId도 값이 null이 아니면 업데이트
                    if (channel.getWorkspacesId() != null) {
                        existingChannel.setWorkspacesId(channel.getWorkspacesId());
                    }

                    return channelRepository.save(existingChannel);
                }).orElseThrow(() -> new ChannelNotFoundException("Channel not found with ID: " + channelId));
    }




    @Override
    public List<ChannelEntity> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public List<ChannelEntity> findAllByWorkspacesId(int id) {
        return channelRepository.findAllByWorkspacesId(id);
    }

    @Override
    public Optional<ChannelEntity> findById(UUID channelId) {
        return channelRepository.findById(channelId);
    }

    @Override
    public boolean existsById(UUID channelId) {
        return channelRepository.existsById(channelId);
    }

    @Override
    public long count() {
        return channelRepository.count();
    }

    @Override
    public void deleteById(UUID channelId) {
        if (channelRepository.existsById(channelId)) {
            channelRepository.deleteById(channelId);
        } else {
            throw new RuntimeException("Channel not found with ID: " + channelId);
        }
    }
}
