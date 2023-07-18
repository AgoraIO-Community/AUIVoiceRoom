package io.agora.uikit.service;

import io.agora.uikit.bean.dto.TokenDto;

public interface ITokenService {
    /**
     * Generate RTC token
     * 
     * @param channelName
     * @param account
     * @return
     */
    String generateRtcToken(String channelName, String account);

    /**
     * Generate RTC token
     * 
     * @param channelName
     * @param account
     * @return
     */
    String generateRtcToken006(String channelName, String account);

    /**
     * Generate RTM token
     * 
     * @param userId
     * @return
     */
    String generateRtmToken(String userId);

    /**
     * Generate RTM token
     * 
     * @param userId
     * @return
     */
    String generateRtmToken006(String userId) throws Exception;

    /**
     * Generate token
     * 
     * @param channelName
     * @param account
     * @return
     */
    TokenDto generateToken(String channelName, String account);

    /**
     * Generate token
     * 
     * @param channelName
     * @param account
     * @return
     */
    TokenDto generateToken006(String channelName, String account) throws Exception;
}
