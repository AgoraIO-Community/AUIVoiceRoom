package io.agora.uikit.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMetrics
public class TokenUtilTest {
    @Autowired
    private TokenUtil tokenUtil;

    @Test
    void testGenerateRtcToken() {
        String token = tokenUtil.generateRtcToken("970CA35de60c44645bbae8a215061b33",
                "5CFd2fd1755d40ecb72977518be15d3b", "test", "123", 3600, 3600);
        assertNotNull(token);
        log.info("testGenerateRtcToken, token:{}", token);
    }

    @Test
    void testGenerateRtcToken006() {
        String token = tokenUtil.generateRtcToken006("970CA35de60c44645bbae8a215061b33",
                "5CFd2fd1755d40ecb72977518be15d3b", "test", "123", 3600);
        assertNotNull(token);
        log.info("testGenerateRtcToken006, token:{}", token);
    }

    @Test
    void testGenerateRtmToken() {
        String token = tokenUtil.generateRtmToken("970CA35de60c44645bbae8a215061b33",
                "5CFd2fd1755d40ecb72977518be15d3b", "123", 3600);
        assertNotNull(token);
        log.info("testGenerateRtmToken, token:{}", token);
    }

    @Test
    void testGenerateRtmToken006() throws Exception {
        String token = tokenUtil.generateRtmToken006("970CA35de60c44645bbae8a215061b33",
                "5CFd2fd1755d40ecb72977518be15d3b", "123", 3600);
        assertNotNull(token);
        log.info("testGenerateRtmToken006, token:{}", token);
    }
}
