package com.bdool.chatservice.model.repository;

import com.bdool.chatservice.model.entity.ChannelEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChannelRepository extends MongoRepository<ChannelEntity, UUID> {
}
