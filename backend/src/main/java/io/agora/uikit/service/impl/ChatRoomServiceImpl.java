package io.agora.uikit.service.impl;

import com.alibaba.fastjson2.JSON;
import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.ChatRoomDomain;
import io.agora.uikit.bean.dto.ChatRoomDto;
import io.agora.uikit.bean.dto.ChatUserDto;
import io.agora.uikit.service.IChatRoomService;
import io.agora.uikit.service.IIMService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ChatRoomServiceImpl implements IChatRoomService {
    @Resource
    private IIMService imService;

    @Resource
    private RtmUtil rtmUtil;
    @Resource
    private IRoomService roomService;

    @Value("${em.chatRoom.maxUsers}")
    private Integer maxMembers;

    @Value("${em.auth.appKey}")
    private String appKey;

    public static final String METADATA_KEY = "chatRoom";

    @Override
    public ChatRoomDomain getChatRoomDomain(MetadataItem metadataItem) {
        if (metadataItem == null || StringUtils.isBlank(metadataItem.value)) {
            return null;
        }
        return JSON.parseObject(metadataItem.value, ChatRoomDomain.class);
    }

    @Override
    public void createMetadata(Metadata metadata, ChatRoomDomain chatRoomDomain) {
        log.info("create chat room MetaData:{}", chatRoomDomain);
        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = METADATA_KEY;
        metadataItem.value = JSON.toJSONString(chatRoomDomain);
        metadata.setMetadataItem(metadataItem);
    }

    @Override
    public ChatUserDto createUser(String userName, String password) throws Exception {
        String userUuid = imService.createUser(userName, password);
        String token = imService.getUserToken(userName);
        if (token == null) {
            throw new Exception("get user token err");
        }
        return new ChatUserDto()
                .setUserUUID(userUuid)
                .setUserName(userName)
                .setAccessToken(token)
                .setAppKey(appKey);
    }

    @Override
    public ChatRoomDto creatRoom(String roomId, String description, String userId, String userName, List<String> members, String custom) throws Exception {
        log.debug("create room,roomId:{},description:{},owner:{},members:{},custom:{}", roomId, description, userId, members, custom);
        roomService.acquireLock(roomId);
        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        roomService.checkIsOwner("createRoom", metadata, roomId, userId);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        String chatRoomId;
        if (metadataItem != null) {
            log.info("chatRoomDomain:{}", metadataItem.value);
            ChatRoomDomain chatRoomDomain = getChatRoomDomain(metadataItem);
            if (chatRoomDomain != null && !StringUtils.isEmpty(chatRoomDomain.getChatRoomId())) {
                log.info("chatRoomId:{}", chatRoomDomain.getChatRoomId());
                roomService.releaseLock(roomId);
                return new ChatRoomDto().setChatRoomId(chatRoomDomain.getChatRoomId());
            }
        }

        chatRoomId = imService.createRoom(roomId, description, userName, members, maxMembers, custom);
        if (chatRoomId == null) {
            roomService.releaseLock(roomId);
            throw new Exception("create room err");
        }
        ChatRoomDomain chatRoomDomain = new ChatRoomDomain().setChatRoomId(chatRoomId);
        createMetadata(metadata, chatRoomDomain);
        roomService.setMetadata("creatRoom", roomId, metadata, null, roomId, null);
        roomService.releaseLock(roomId);
        return new ChatRoomDto().setChatRoomId(chatRoomId);
    }

    @Override
    public void joinChatRoom(String chatRoomId, String userName) throws Exception {
        imService.joinChatRoom(chatRoomId, userName);
    }

    @Override
    public void deactivateUser(String roomId, String userId) throws Exception {
        imService.deactivateUser(roomId, userId);
    }

}
