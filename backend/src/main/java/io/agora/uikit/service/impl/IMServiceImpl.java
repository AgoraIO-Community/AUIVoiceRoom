package io.agora.uikit.service.impl;

import com.easemob.im.server.EMException;
import com.easemob.im.server.EMService;
import com.easemob.im.server.model.EMUser;
import io.agora.uikit.service.IIMService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class IMServiceImpl implements IIMService {
    @Resource
    private EMService emService;

    @Override
    public Boolean userInChatRoom(String chatRoomId, String userName) throws Exception {
        try {
            List<String> members = emService.room().listRoomMembersAll(chatRoomId).collectList().block();
            if (members == null) {
                return false;
            }
            return members.stream().anyMatch(i -> Objects.equals(i, userName));
        } catch (EMException e) {
            log.info("acquire room members err,chatRoomId:{},errCode:{},errMessage:{}", chatRoomId, e.getErrorCode(), e.getMessage());
            throw e;
        }
    }

    @Override
    public String queryUser(String userName) throws Exception {
        try {
            EMUser user = emService.user().get(userName).block();
            if (user != null) {
                return user.getUuid();
            } else {
                return null;
            }
        } catch (Exception ex) {
            log.debug("query user:{}", ex.toString());
            return null;
        }
    }

    @Override
    @Cacheable(cacheNames = "IMToken", key = "#userName")
    public String getUserToken(String userName) {
        log.info("user:{} acquire user token from sdk", userName);
        return emService.token().getUserTokenWithInherit(userName);
    }

    @Override
    @Cacheable(cacheNames = "IMUser", key = "#userName")
    public String createUser(String userName, String password) throws Exception {
        String userId = queryUser(userName);
        if (Strings.isNotEmpty(userId)) {
            return userId;
        }
        try {
            EMUser user = emService.user().create(userName, password).block();
            if (user == null) {
                throw new Exception("create user err");
            } else {
                return user.getUuid();
            }
        } catch (Exception exception) {
            log.info("create user err:{}", exception.getMessage());
            throw new Exception("create user err");
        }
    }

    @Override
    public void deactivateUser(String chatRoomId, String userName) throws Exception {
        try {
            String userId = queryUser(userName);
            if (userId == null) {
                log.info("user:{} not exist", userName);
                return;
            }
            Boolean joined = userInChatRoom(chatRoomId, userName);
            if (!joined) {
                log.info("user {} is not in chatRoom {}", userName, chatRoomId);
                return;
            }
            emService.block().blockUserJoinRoom(userName, chatRoomId).block();
        } catch (Exception ex) {
            log.info("deactivateUser roomId:{},userName:{},err:{}", chatRoomId, userName, ex.getMessage());
        }
    }

    @Override
    public void joinChatRoom(String chatRoomId, String userName) throws Exception {
        try {
            emService.room().addRoomMember(chatRoomId, userName).block();
        } catch (Exception ex) {
            log.info("addRoomMember roomId:{},userName:{},err:{}", chatRoomId, userName, ex.getMessage());
            throw new Exception("addRoomMember err");
        }

    }

    @Override
    public String createRoom(String roomName, String description, String owner, List<String> members, Integer maxMembers, String custom) {
        try {
            return emService.room().createRoom(roomName, description, owner, members, maxMembers, custom).block();
        } catch (Exception ex) {
            log.info("create room err:{}", ex.getMessage());
            return null;
        }
    }
}
