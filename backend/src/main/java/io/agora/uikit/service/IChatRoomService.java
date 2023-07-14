package io.agora.uikit.service;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.ChatRoomDomain;
import io.agora.uikit.bean.dto.ChatRoomDto;
import io.agora.uikit.bean.dto.ChatUserDto;

import java.util.List;

public interface IChatRoomService {

    ChatRoomDomain getChatRoomDomain(MetadataItem metadataItem);

    void createMetadata(Metadata metadata, ChatRoomDomain chatRoomDomain);

    ChatUserDto createUser(String userName, String password) throws Exception;

    ChatRoomDto creatRoom(String roomId, String description, String userId, String userName, List<String> members, String custom) throws Exception;

    void joinChatRoom(String chatRoomId, String userName) throws Exception;
    void deactivateUser(String roomId, String userId) throws Exception;
}
