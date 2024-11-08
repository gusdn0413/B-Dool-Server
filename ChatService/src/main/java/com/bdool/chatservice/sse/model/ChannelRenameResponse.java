package com.bdool.chatservice.sse.model;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelRenameResponse {
    private UUID channelId;
    private String newChannelName;
}
