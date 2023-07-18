package io.agora.uikit.config;

import com.easemob.im.server.EMProperties;
import com.easemob.im.server.EMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmServiceConfig {
    @Value("${em.auth.appKey}")
    private String appKey;

    @Value("${em.auth.clientId}")
    private String clientId;

    @Value("${em.auth.clientSecret}")
    private String clientSecret;

    @Bean
    public EMService service() {
        EMProperties properties = EMProperties.builder()
                .setAppkey(appKey)
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .turnOffUserNameValidation()
                .build();
        return new EMService(properties);
    }
}
