package io.agora.uikit.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.agora.uikit.bean.dto.TokenDto;
import io.agora.uikit.service.ITokenService;
import io.agora.uikit.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenServiceImpl implements ITokenService {
    @Autowired
    private TokenUtil tokenUtil;

    @Value("${token.appId}")
    private String appId;
    @Value("${token.appCertificate}")
    private String appCertificate;
    private static int tokenExpirationInSeconds = 3600 * 24;
    private static int privilegeExpirationInSeconds = 3600 * 24;

    /**
     * Generate RTC token
     * 
     * @param channelName
     * @param account
     * @return
     */
    @Override
    public String generateRtcToken(String channelName, String account) {
        log.info("generateRtcToken, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        String token = tokenUtil.generateRtcToken(appId, appCertificate, channelName, account,
                tokenExpirationInSeconds, privilegeExpirationInSeconds);
        return token;
    }

    /**
     * Generate RTC token
     * 
     * @param channelName
     * @param account
     * @return
     */
    @Override
    public String generateRtcToken006(String channelName, String account) {
        log.info("generateRtcToken006, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        String token = tokenUtil.generateRtcToken006(appId, appCertificate, channelName, account,
                privilegeExpirationInSeconds);
        return token;
    }

    /**
     * Generate RTM token
     * 
     * @param userId
     * @return
     */
    @Override
    public String generateRtmToken(String userId) {
        log.info("generateRtmToken, appId:{}, userId:{}", appId, userId);
        String token = tokenUtil.generateRtmToken(appId, appCertificate, userId, tokenExpirationInSeconds);
        return token;
    }

    /**
     * Generate RTM token
     * 
     * @param userId
     * @return
     */
    @Override
    public String generateRtmToken006(String userId) throws Exception {
        log.info("generateRtmToken006, appId:{}, userId:{}", appId, userId);
        String token = tokenUtil.generateRtmToken006(appId, appCertificate, userId, tokenExpirationInSeconds);
        return token;
    }

    /**
     * Get token
     * 
     * @param channelName
     * @param account
     * @return
     */
    @Override
    public TokenDto generateToken(String channelName, String account) {
        log.info("getToken, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        TokenDto tokenDto = new TokenDto();
        String rtcToken = generateRtcToken(channelName, account);
        String rtmToken = generateRtmToken(account);
        tokenDto.setAppId(appId);
        tokenDto.setRtcToken(rtcToken);
        tokenDto.setRtmToken(rtmToken);

        return tokenDto;
    }

    /**
     * Get token
     * 
     * @param channelName
     * @param account
     * @return
     */
    @Override
    public TokenDto generateToken006(String channelName, String account) throws Exception {
        log.info("generateToken006, appId:{}, channelName:{}, account:{}", appId, channelName, account);
        TokenDto tokenDto = new TokenDto();
        String rtcToken = generateRtcToken006(channelName, account);
        String rtmToken = generateRtmToken006(account);
        tokenDto.setAppId(appId);
        tokenDto.setRtcToken(rtcToken);
        tokenDto.setRtmToken(rtmToken);

        return tokenDto;
    }
}
