package io.agora.uikit.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.SongDomain;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.bean.req.SongAddReq;
import io.agora.uikit.bean.req.SongOwnerReq;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.service.ISongService;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class SongServiceImplTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private ISongService songService;
    @Autowired
    private RtmUtil rtmUtil;

    private String roomId = "roomIdTest";
    private String roomName = "roomNameTest";
    private String userId = "userIdTest";
    private String userName = "userNameTest";
    private String userAvatar = "userAvatarTest";
    private Integer micSeatCount = 2;

    @Before
    public void before() throws Exception {
        RoomCreateReq roomCreateReq = new RoomCreateReq();
        roomCreateReq.setRoomName(roomName)
                .setUserId(userId)
                .setUserName(userName)
                .setUserAvatar(userAvatar)
                .setMicSeatCount(micSeatCount);
        String result = mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(roomCreateReq)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.roomName").value(roomName))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        R<RoomDto> r = JSON.parseObject(result, new TypeReference<R<RoomDto>>() {
        });

        roomId = r.getData().getRoomId();
        log.info("before, roomId:{}", roomId);
    }

    @After
    public void after() throws Exception {
        log.info("after, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAdd() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        int count = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            log.info("testAdd, i:{}", i);
            executorService.execute(() -> {
                assertDoesNotThrow(() -> {
                    SongOwnerReq songOwnerReq = new SongOwnerReq();
                    songOwnerReq.setUserId(userId).setUserName(userName);
                    SongAddReq songAddReq = new SongAddReq();
                    songAddReq.setRoomId(roomId).setUserId(userId)
                            .setSongCode(RandomStringUtils.random(15, true, false)).setOwner(songOwnerReq);
                    songService.add(songAddReq);
                    rtmUtil.printChannelMetadata(roomId);
                });
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        assertNotNull(metadata);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, SongServiceImpl.METADATA_KEY);
        assertNotNull(metadataItem);
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        assertEquals(count, songList.size());
    }

    @Test
    public void testClearByUser() throws Exception {
        roomService.acquireLock(roomId);
        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        songService.clearByUser("testClearByUser", metadata, roomId, userId);
        roomService.releaseLock(roomId);
    }
}
