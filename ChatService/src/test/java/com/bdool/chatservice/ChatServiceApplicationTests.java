package com.bdool.chatservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.config.import=optional:configserver:")
class ChatServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}

