package io.agora.uikit.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;

import io.agora.uikit.bean.dto.TokenDto;
import io.agora.uikit.service.ITokenService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMetrics
public class TokenServiceImplTest {
    @Autowired
    private ITokenService tokenService;

    private String channelName = "channelNameTest";
    private String userId = "123456789";

    @Test
    void testGenerateRtcToken() {
        String token = tokenService.generateRtcToken(channelName, userId);
        assertNotNull(token);
        log.info("testGenerateRtcToken, token:{}", token);
    }

    @Test
    void testGenerateRtmToken() {
        String token = tokenService.generateRtmToken(userId);
        assertNotNull(token);
        log.info("testGenerateRtmToken, token:{}", token);
    }

    @Test
    void testGenerateToken() {
        TokenDto tokenDto = tokenService.generateToken(channelName, userId);
        assertNotNull(tokenDto.getRtcToken());
        assertNotNull(tokenDto.getRtmToken());
        log.info("testGenerateToken, tokenDto:{}", tokenDto);
    }
}
