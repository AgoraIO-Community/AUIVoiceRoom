package io.agora.uikit.service;

import java.util.List;

public interface IIMService {

    Boolean userInChatRoom(String chatRoomId, String userName) throws Exception;

    String queryUser(String userName) throws Exception;

    String getUserToken(String userName) throws Exception;

    String createUser(String userName, String password) throws Exception;

    void deactivateUser(String chatRoomId, String userName) throws Exception;

    void joinChatRoom(String chatRoomId, String userName) throws Exception;

    String createRoom(String roomName, String description, String owner, List<String> members, Integer maxMembers, String custom) throws Exception;
}
