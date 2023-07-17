package io.agora.uikit.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LockEvent;
import io.agora.rtm.MessageEvent;
import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.rtm.MetadataOptions;
import io.agora.rtm.PresenceEvent;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmConfig;
import io.agora.rtm.RtmConstants.RtmChannelType;
import io.agora.rtm.RtmEventListener;
import io.agora.rtm.StorageEvent;
import io.agora.rtm.TopicEvent;
import io.agora.uikit.bean.domain.RoomInfoDomain;
import io.agora.uikit.bean.domain.RoomInfoOwnerDomain;
import io.agora.uikit.service.ITokenService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMetrics
public class RtmUtilTest {
    @Autowired
    private RtmUtil rtmUtil;
    @Autowired
    private ITokenService tokenService;

    @Value("${token.appId}")
    private String appId;

    private String roomId = UUID.randomUUID().toString();
    private String roomName = "roomNameTest";
    private String roomInfoKey = "roomInfoTest";
    private String roomListKey = "uikitRoomListDev";

    @BeforeEach
    public void before() {
        assertTrue(rtmUtil.init());
    }

    @Test
    void testRtmClient() throws Exception {
        // Create RTM client
        RtmConfig rtmConfig = new RtmConfig();
        rtmConfig.appId = appId;
        rtmConfig.userId = UUID.randomUUID().toString();
        RtmEventListener rtmEventListener = new RtmEventListener() {
            @Override
            public void onMessageEvent(MessageEvent event) {
                log.info("onMessageEvent, event:{}", event);
            }

            @Override
            public void onPresenceEvent(PresenceEvent event) {
                log.info("onPresenceEvent, event:{}", event);
            }

            @Override
            public void onTopicEvent(TopicEvent event) {
                log.info("onTopicEvent, event:{}", event);
            }

            @Override
            public void onLockEvent(LockEvent event) {
                log.info("onLockEvent, event:{}", event);
            }

            @Override
            public void onStorageEvent(StorageEvent event) {
                log.info("onStorageEvent, event:{}", event);
            }

            @Override
            public void onConnectionStateChange(String channelName, int state, int reason) {
                log.info("onConnectionStateChange, channelName:{}, state:{}, reason:{}", channelName, state, reason);
            }

            @Override
            public void onTokenPrivilegeWillExpire(String channelName) {
                log.info("onTokenPrivilegeWillExpire, channelName:{}", channelName);
            }
        };
        rtmConfig.eventListener = rtmEventListener;
        log.info("testRtmClient, appId:{}, userId:{}", rtmConfig.appId, rtmConfig.userId);
        RtmClient rtmClient = RtmClient.create(rtmConfig);

        // Generate RTM token
        String rtmToken = tokenService.generateRtmToken(rtmConfig.userId);
        assertNotNull(rtmToken);
        log.info("testRtmClient, appId:{}, userId:{}, rtmToken:{}", rtmConfig.appId, rtmConfig.userId, rtmToken);

        // Login
        rtmClient.login(rtmToken, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                log.info("testRtmClient, rtm login success");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                log.error("testRtmClient, rtm login failed, error:{}", errorInfo.getErrorReason());
            }
        });

        // Create room
        Metadata metadata = rtmClient.getStorage().createMetadata();
        RoomInfoDomain roomInfoDomain = new RoomInfoDomain();
        RoomInfoOwnerDomain roomInfoOwnerDomain = new RoomInfoOwnerDomain();
        roomInfoOwnerDomain.setUserId("userIdTest").setUserName("userNameTest");
        roomInfoDomain.setRoomId(roomId).setRoomName(roomName).setRoomOwner(roomInfoOwnerDomain);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = roomInfoKey;
        metadataItem.value = JSON.toJSONString(roomInfoDomain);
        metadata.setMetadataItem(metadataItem);

        MetadataOptions options = new MetadataOptions();
        options.recordTs = true;
        log.info("testRtmClient, setChannelMetadata, roomName:{}", roomName);
        rtmClient.getStorage().setChannelMetadata(roomName, RtmChannelType.STREAM, metadata, options,
                "",
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.info("testRtmClient, setChannelMetadata, onSuccess");
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("testRtmClient, setChannelMetadata, error:{}", errorInfo.getErrorReason());
                    }
                });

        // Get room
        log.info("testRtmClient, getChannelMetadata, roomName:{}", roomName);
        rtmClient.getStorage().getChannelMetadata(roomName, RtmChannelType.STREAM,
                new ResultCallback<Metadata>() {
                    @Override
                    public void onSuccess(Metadata metadata) {
                        log.info("testRtmClient, getChannelMetadata, onSuccess");

                        MetadataItem[] metadataItems = metadata.getMetadataItems();
                        for (MetadataItem metadataItem : metadataItems) {
                            log.info(
                                    "testRtmClient, getChannelMetadata, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}",
                                    metadataItem.key, metadataItem.value, metadataItem.revision, metadataItem.updateTs,
                                    metadataItem.authorUserId);
                        }
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("testRtmClient, getChannelMetadata, error:{}", errorInfo.getErrorReason());
                    }
                });

        log.info("testRtmClient, sleep start");
        TimeUnit.SECONDS.sleep(5);
        log.info("testRtmClient, sleep end");
    }

    @Test
    void testInit() {
        assertTrue(rtmUtil.init());
    }

    @Test
    void testAcquireLock() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testAcquireLock, lockName:{}", lockName);

        assertTrue(rtmUtil.setLock(roomName, lockName));
        assertTrue(rtmUtil.acquireLock(roomName, lockName));
        assertTrue(rtmUtil.releaseLock(roomName, lockName));
        assertTrue(rtmUtil.removeLock(roomName, lockName));
    }

    @Test
    void testAcquireLockNotExisted() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testAcquireLockNotExisted, lockName:{}", lockName);

        assertFalse(rtmUtil.acquireLock(roomName, lockName));
    }

    // @Test
    // void testAcquireLockLoop() {
    // String lockName = UUID.randomUUID().toString().substring(0, 32);
    // log.info("testAcquireLockLoop, lockName:{}", lockName);

    // assertTrue(rtmUtil.setLock(roomName, lockName));
    // for (int i = 0; i < 10; i++) {
    // log.info("testAcquireLockLoop, i:{}", i);
    // assertTrue(rtmUtil.acquireLock(roomName, lockName));
    // assertTrue(rtmUtil.releaseLock(roomName, lockName));
    // }
    // assertTrue(rtmUtil.removeLock(roomName, lockName));
    // }

    @Test
    void testCreateClient() {
        assertTrue(rtmUtil.createClient());
    }

    @Test
    void testCreateMetadata() {
        assertTrue(rtmUtil.createClient());
        Metadata metadata = rtmUtil.createMetadata();
        assertNotNull(metadata.toString());
    }

    @Test
    void testGetChannelMetadata() {
        Metadata metadata = rtmUtil.getChannelMetadata(roomName);
        assertNotNull(metadata);
        log.info("testGetChannelMetadata, metadata:{}", metadata);

        MetadataItem[] metadataItems = metadata.getMetadataItems();
        for (MetadataItem metadataItem : metadataItems) {
            log.info(
                    "testGetChannelMetadata, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}",
                    metadataItem.key, metadataItem.value, metadataItem.revision, metadataItem.updateTs,
                    metadataItem.authorUserId);
        }
    }

    @Test
    void testGetChannelMetadataList() {
        Metadata metadata = rtmUtil.getChannelMetadata(roomListKey);
        assertNotNull(metadata);
        log.info("testGetChannelMetadataList, metadata:{}", metadata);

        MetadataItem[] metadataItems = metadata.getMetadataItems();
        for (MetadataItem metadataItem : metadataItems) {
            JSONArray jsonArray = JSON.parseArray(metadataItem.value);
            List<RoomInfoDomain> roomList = JSON.parseArray(metadataItem.value, RoomInfoDomain.class);
            log.info(
                    "testGetChannelMetadataList, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}, jsonArray:{}, roomList:{}",
                    metadataItem.key, metadataItem.value, metadataItem.revision, metadataItem.updateTs,
                    metadataItem.authorUserId, jsonArray, roomList);
        }
    }

    @Test
    void testGetChannelMetadataByKey() throws Throwable {
        Metadata metadata = rtmUtil.getChannelMetadata(roomName);
        assertNotNull(metadata);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, roomInfoKey);
        assertNotNull(metadataItem);

        log.info(
                "testGetChannelMetadata, onSuccess, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}",
                metadataItem.key, metadataItem.value, metadataItem.revision, metadataItem.updateTs,
                metadataItem.authorUserId);
    }

    @Test
    void testGetRtmToken() {
        String rtmToken = rtmUtil.getRtmToken();
        assertNotNull(rtmToken);
        log.info("testGetRtmToken, rtmToken:{}", rtmToken);
    }

    @Test
    void testLogin() {
        assertTrue(rtmUtil.createClient());
        String rtmToken = rtmUtil.getRtmToken();
        assertNotNull(rtmToken);
        log.info("testLogin, rtmToken:{}", rtmToken);

        assertTrue(rtmUtil.login(rtmToken));
    }

    @Test
    void testPrintChannelMetadata() {
        rtmUtil.printChannelMetadata(roomName);
    }

    @Test
    void testReleaseLock() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testReleaseLock, roomName:{}, lockName:{}", roomName, lockName);

        assertTrue(rtmUtil.setLock(roomName, lockName));
        assertTrue(rtmUtil.acquireLock(roomName, lockName));
        assertTrue(rtmUtil.releaseLock(roomName, lockName));
        assertTrue(rtmUtil.removeLock(roomName, lockName));
    }

    @Test
    void testReleaseLockNotExisted() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testReleaseLockNotExisted, roomName:{}, lockName:{}", roomName,
                lockName);

        assertFalse(rtmUtil.releaseLock(roomName, lockName));
    }

    @Test
    void testRemoveChannelMetadata() {
        assertTrue(rtmUtil.removeChannelMetadata(roomName));
        assertTrue(rtmUtil.removeChannelMetadata(roomListKey));
    }

    @Test
    void testRemoveLock() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testRemoveLock, lockName:{}", lockName);

        assertTrue(rtmUtil.setLock(roomName, lockName));
        assertTrue(rtmUtil.removeLock(roomName, lockName));
    }

    @Test
    void testRemoveLockNotExisted() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testRemoveLockNotExisted, lockName:{}", lockName);

        assertTrue(rtmUtil.removeLock(roomName, lockName));
    }

    @Test
    void testRenewToken() {
        assertTrue(rtmUtil.renewToken());
    }

    @Test
    void testSetChannelMetadata() {
        Metadata metadata = rtmUtil.createMetadata();
        RoomInfoDomain roomInfoDomain = new RoomInfoDomain();
        RoomInfoOwnerDomain roomInfoOwnerDomain = new RoomInfoOwnerDomain();

        roomInfoOwnerDomain.setUserId("userIdTest").setUserName("userNameTest");
        roomInfoDomain.setRoomId(roomId).setRoomName(roomName).setRoomOwner(roomInfoOwnerDomain);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = roomInfoKey;
        metadataItem.value = JSON.toJSONString(roomInfoDomain);
        metadata.setMetadataItem(metadataItem);

        log.info("testSetChannelMetadata, end");
        assertTrue(rtmUtil.setChannelMetadata(roomName, metadata));
    }

    @Test
    void testSetChannelMetadataEmoji() {
        roomId = "roomIdTest";
        Metadata metadata = rtmUtil.createMetadata();
        RoomInfoDomain roomInfoDomain = new RoomInfoDomain();
        RoomInfoOwnerDomain roomInfoOwnerDomain = new RoomInfoOwnerDomain();

        roomInfoOwnerDomain.setUserId("userIdTest").setUserName("userNameTest");
        roomInfoDomain.setRoomId(roomId).setRoomName("测试房间 emoji 😊 emoji").setRoomOwner(roomInfoOwnerDomain);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = roomInfoKey;
        metadataItem.value = JSON.toJSONString(roomInfoDomain);
        metadata.setMetadataItem(metadataItem);

        log.info("testSetChannelMetadataEmoji, roomId:{}", roomId);
        assertTrue(rtmUtil.setChannelMetadata(roomId, metadata));

        Metadata metadata2 = rtmUtil.getChannelMetadata(roomId);
        assertNotNull(metadata2);
        MetadataItem[] metadataItems2 = metadata2.getMetadataItems();
        for (MetadataItem metadataItem2 : metadataItems2) {
            JSONObject jsonObject = JSON.parseObject(metadataItem2.value);
            RoomInfoDomain roomInfoDomain2 = JSON.parseObject(metadataItem2.value, RoomInfoDomain.class);
            log.info(
                    "testSetChannelMetadataEmoji, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}, jsonObject:{}, roomInfoDomain2:{}",
                    metadataItem2.key, metadataItem2.value, metadataItem2.revision, metadataItem2.updateTs,
                    metadataItem2.authorUserId, jsonObject, roomInfoDomain2);
        }
    }

    @Test
    void testSetChannelMetadataEmojiList() {
        roomId = "roomIdTestList";
        Metadata metadata = rtmUtil.createMetadata();
        RoomInfoDomain roomInfoDomain = new RoomInfoDomain();
        List<RoomInfoDomain> roomList = new ArrayList<>();
        roomInfoDomain.setRoomId(roomId).setRoomName("测试房间 emoji 😊 emoji");
        roomList.add(roomInfoDomain);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = roomListKey;
        metadataItem.value = JSON.toJSONString(roomList);
        metadata.setMetadataItem(metadataItem);

        log.info("testSetChannelMetadataEmojiList, end, roomId:{}", roomId);
        assertTrue(rtmUtil.setChannelMetadata(roomListKey, metadata));

        Metadata metadata2 = rtmUtil.getChannelMetadata(roomListKey);
        assertNotNull(metadata2);
        MetadataItem[] metadataItems2 = metadata2.getMetadataItems();
        for (MetadataItem metadataItem2 : metadataItems2) {
            log.info(
                    "testSetChannelMetadataEmojiList, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}",
                    metadataItem2.key, metadataItem2.value, metadataItem2.revision, metadataItem2.updateTs,
                    metadataItem2.authorUserId);
            JSONArray jsonArray = JSON.parseArray(metadataItem2.value);
            List<RoomInfoDomain> roomList2 = JSON.parseArray(metadataItem2.value,
                    RoomInfoDomain.class);
            log.info(
                    "testSetChannelMetadataEmojiList, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}, jsonArray:{}, roomList2:{}",
                    metadataItem2.key, metadataItem2.value, metadataItem2.revision, metadataItem2.updateTs,
                    metadataItem2.authorUserId, jsonArray, roomList2);
        }
    }

    @Test
    void testSetLock() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testSetLock, lockName:{}", lockName);

        assertTrue(rtmUtil.setLock(roomName, lockName));
        assertTrue(rtmUtil.acquireLock(roomName, lockName));
        assertTrue(rtmUtil.releaseLock(roomName, lockName));
        assertTrue(rtmUtil.removeLock(roomName, lockName));
    }

    @Test
    void testSetLockErrorExisted() {
        String lockName = UUID.randomUUID().toString().substring(0, 32);
        log.info("testSetLockErrorExisted, lockName:{}", lockName);

        assertTrue(rtmUtil.setLock(roomName, lockName));
        assertFalse(rtmUtil.setLock(roomName, lockName));
    }

    @Test
    void testUpdateChannelMetadata() throws Throwable {
        Metadata metadata = rtmUtil.createMetadata();
        RoomInfoDomain roomInfoDomain = new RoomInfoDomain();
        RoomInfoOwnerDomain roomInfoOwnerDomain = new RoomInfoOwnerDomain();

        roomInfoOwnerDomain.setUserId("userIdTestUpdate").setUserName("userNameTest");
        roomInfoDomain.setRoomId(roomId).setRoomName(roomName).setRoomOwner(roomInfoOwnerDomain);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = roomInfoKey;
        metadataItem.value = JSON.toJSONString(roomInfoDomain);
        metadata.setMetadataItem(metadataItem);

        log.info("testUpdateChannelMetadata, end");
        assertTrue(rtmUtil.updateChannelMetadata(roomName, metadata));

        Metadata metadata2 = rtmUtil.getChannelMetadata(roomName);
        assertTrue(rtmUtil.updateChannelMetadata(roomName, metadata2));
        assertTrue(rtmUtil.updateChannelMetadata(roomName, metadata2));
    }

    @Test
    void testUpdateChannelMetadataEmojiList() {
        roomId = "roomIdTestList";
        Metadata metadata = rtmUtil.createMetadata();
        RoomInfoDomain roomInfoDomain = new RoomInfoDomain();
        List<RoomInfoDomain> roomList = new ArrayList<>();
        roomInfoDomain.setRoomId(roomId).setRoomName("测试房间 emoji 😊 emoji");
        roomList.add(roomInfoDomain);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = roomListKey;
        metadataItem.value = JSON.toJSONString(roomList);
        metadata.setMetadataItem(metadataItem);

        log.info("testUpdateChannelMetadataEmojiList, roomId:{}, roomListKey:{}",
                roomId, roomListKey);
        assertTrue(rtmUtil.updateChannelMetadata(roomListKey, metadata));

        Metadata metadata2 = rtmUtil.getChannelMetadata(roomListKey);
        assertNotNull(metadata2);
        MetadataItem[] metadataItems2 = metadata2.getMetadataItems();
        for (MetadataItem metadataItem2 : metadataItems2) {
            JSONArray jsonArray = JSON.parseArray(metadataItem2.value);
            List<RoomInfoDomain> roomList2 = JSON.parseArray(metadataItem2.value, RoomInfoDomain.class);
            log.info(
                    "testUpdateChannelMetadataEmojiList, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}, jsonArray:{}, roomList2:{}",
                    metadataItem2.key, metadataItem2.value, metadataItem2.revision, metadataItem2.updateTs,
                    metadataItem2.authorUserId, jsonArray, roomList2);
        }
    }

    @Test
    void testWhoNow() {
        Long onlineUsers = rtmUtil.whoNow(roomId);
        log.info("testwhoNow, onlineUsers:{}", onlineUsers);
    }
}
