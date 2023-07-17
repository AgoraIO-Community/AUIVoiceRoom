package io.agora.uikit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMetrics
public class HmacShaTest {
    @Autowired
    private HmacShaUtil hmacShaUtil;

    @Test
    void testHmacSha256() {
        String requestBody = "{\"eventType\":10,\"noticeId\":\"4eb720f0-8da7-11e9-a43e-53f411c2761f\",\"notifyMs\":1560408533119,\"payload\":{\"a\":\"1\",\"b\":2},\"productId\":1}";
        String secret = "secret";
        assertEquals("de96da5acf03b0021ac3b4fa2225e7ae6f3533a30d50bb02c08ea4fa748bda24",
                hmacShaUtil.hmacSha256(requestBody, secret));
    }
}
