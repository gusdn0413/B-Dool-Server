package com.bdool.chatservice;

import com.bdool.chatservice.model.Enum.SessionType;
import com.bdool.chatservice.model.domain.SessionModel;
import com.bdool.chatservice.model.entity.MessageEntity;
import com.bdool.chatservice.model.repository.MessageRepository;
import com.bdool.chatservice.service.MessageService;
import com.bdool.chatservice.service.SessionService;
import com.opencsv.CSVReader;
import org.apache.kafka.common.protocol.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class ChatServiceApplicationTests {

    @Test
    void contextLoads() {

    }

}
