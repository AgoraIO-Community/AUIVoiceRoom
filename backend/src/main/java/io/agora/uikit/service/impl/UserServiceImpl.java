package io.agora.uikit.service.impl;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.ChatRoomDomain;
import io.agora.uikit.bean.dto.KickOutRuleDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.enums.RtcChannelRulesEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.service.IChatRoomService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.service.IRtcChannelService;
import io.agora.uikit.service.IUserService;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

import static io.agora.uikit.service.impl.ChatRoomServiceImpl.METADATA_KEY;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private IRtcChannelService rtcChannelService;
    @Resource
    private IChatRoomService chatRoomService;
    @Resource
    private IRoomService roomService;
    @Resource
    private RtmUtil rtmUtil;


    @Value("${token.appId}")
    private String appId;

    @Override
    public KickOutRuleDto kickOut(String operatorId, String cname, Long uid) throws Exception {
        log.debug("kickOut user,roomId:{},uid:{}", cname, uid);
        roomService.acquireLock(cname);
        Metadata metadata = rtmUtil.getChannelMetadata(cname);
        roomService.checkIsOwner("createRoom", metadata, cname, operatorId);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        ChatRoomDomain chatRoomDomain = chatRoomService.getChatRoomDomain(metadataItem);
        roomService.releaseLock(cname);
        try {
            rtcChannelService.kickOut(appId, cname, uid, 60, new ArrayList<>() {
                {
                    add(RtcChannelRulesEnum.JOIN_CHANNEL.getRule());
                }
            });
            if (chatRoomDomain != null && Strings.isNotBlank(chatRoomDomain.getChatRoomId())) {
                chatRoomService.deactivateUser(chatRoomDomain.getChatRoomId(), String.valueOf(uid));
            } else {
                log.info("chatRoom no exist,cname:{},operatorId:{},uid:{}", cname, operatorId, uid);
            }

            log.info("kick out user:{},cname:{} successfully", uid, cname);
            return new KickOutRuleDto().setUid(uid);
        } catch (Exception ex) {
            log.info("failed to kick out user:{},cname:{},err:{}", uid, cname, ex.getMessage());
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.USER_KICK_OUT_ERROR);
        }

    }
}
