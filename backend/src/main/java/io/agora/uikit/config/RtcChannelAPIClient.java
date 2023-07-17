package io.agora.uikit.config;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.agora.uikit.service.IRtcChannelAPIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RtcChannelAPIClient {

    @Value("https://api.agora.io")
    private String url;

    @Value("${token.basicAuth.username}")
    private String username;

    @Value("${token.basicAuth.password}")
    private String password;

    @Bean
    public IRtcChannelAPIService rtcChannelAPIService() {
        return Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .target(IRtcChannelAPIService.class, url);
    }
}
