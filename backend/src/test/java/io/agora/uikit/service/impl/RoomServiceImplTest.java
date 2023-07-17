package io.agora.uikit.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;

import io.agora.uikit.bean.dto.RoomListDto;
import io.agora.uikit.bean.entity.RoomListEntity;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.bean.req.RoomDestroyReq;
import io.agora.uikit.bean.req.RoomListReq;
import io.agora.uikit.service.IRoomService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMetrics
public class RoomServiceImplTest {
    @Autowired
    private IRoomService roomService;

    private String roomId = UUID.randomUUID().toString();
    private String roomName = "roomNameTest";
    private String userId = "userIdTest";
    private String userName = "userNameTest";

    @Test
    void testAcquireLock() throws Exception {
        roomService.acquireLock(roomId);
        roomService.releaseLock(roomId);
    }

    @Test
    void testAcquireLockConcurrency() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        int count = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            log.info("testAcquireLockConcurrency, i:{}", i);
            executorService.execute(() -> {
                assertDoesNotThrow(() -> {
                    roomService.acquireLock(roomId);
                    roomService.releaseLock(roomId);
                });
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
    }

    @Test
    void testAcquireLockLoop() throws Exception {
        for (int i = 0; i < 10; i++) {
            log.info("testAcquireLockLoop, i:{}", i);
            roomService.acquireLock(roomId);
            roomService.releaseLock(roomId);
        }
    }

    @Test
    void testAcquireLockError() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        executorService.execute(() -> {
            assertDoesNotThrow(() -> {
                roomService.acquireLock(roomId);
                TimeUnit.SECONDS.sleep(11);
            });
            countDownLatch.countDown();
        });
        Throwable exception = assertThrows(BusinessException.class, () -> roomService.acquireLock(roomId));
        assertEquals("Acquire lock failed", exception.getMessage());
        countDownLatch.await();
    }

    @Test
    void testAddRoomList() throws Exception {
        RoomCreateReq roomCreateReq = new RoomCreateReq();
        roomCreateReq.setRoomId(roomId).setRoomName(roomName).setUserId(userId).setUserName(userName)
                .setMicSeatCount(2);

        roomService.addRoomList(roomCreateReq);

        RoomDestroyReq roomDestroyReq = new RoomDestroyReq();
        roomDestroyReq.setRoomId(roomId);
        roomService.removeRoomList(roomDestroyReq);
    }

    @Test
    void testAddRoomListEmoji() throws Exception {
        RoomCreateReq roomCreateReq = new RoomCreateReq();
        roomCreateReq.setRoomId(roomId).setRoomName("测试房间 emoji 😊 emoji").setUserId(userId).setUserName(userName)
                .setMicSeatCount(2);

        roomService.addRoomList(roomCreateReq);

        RoomDestroyReq roomDestroyReq = new RoomDestroyReq();
        roomDestroyReq.setRoomId(roomId);
        roomService.removeRoomList(roomDestroyReq);
    }

    @Test
    void testGetRoomList() {
        RoomListReq roomListReq = new RoomListReq();
        roomListReq.setLastCreateTime(System.currentTimeMillis()).setPageSize(1);
        RoomListDto<RoomListEntity> roomList = roomService.getRoomList(roomListReq);
        log.info("testGetRoomList, roomList:{}", roomList);
    }

    @Test
    void testRemoveRoomList() throws Exception {
        RoomDestroyReq roomDestroyReq = new RoomDestroyReq();
        roomDestroyReq.setRoomId(roomId);
        roomService.removeRoomList(roomDestroyReq);
    }
}
