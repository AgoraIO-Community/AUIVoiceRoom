package io.agora.uikit.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import com.alibaba.fastjson2.TypeReference;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.NcsEventTypeEnum;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.NcsPayloadReq;
import io.agora.uikit.bean.req.NcsReq;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.service.INcsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class NcsServiceImplTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private INcsService ncsService;

    private String roomId = "roomIdTest";
    private String roomIdNotExisted = "roomIdNotExisted";
    private String roomName = "roomNameTest";
    private String userId = "userIdTest";
    private String userIdNotExisted = "userIdTestNotExisted";
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
    }

    @Test
    public void testProcessEventChannelDestroy() throws Exception {
        log.info("testProcessEventChannelDestroy, roomId:{}", roomId);

        NcsReq ncsReq = new NcsReq();
        NcsPayloadReq ncsPayloadReq = new NcsPayloadReq();
        ncsPayloadReq.setChannelName(roomId).setAccount(userId);
        ncsReq.setEventType(NcsEventTypeEnum.CHANNEL_DESTROY.getCode())
                .setNoticeId("1206008149:14940:100")
                .setNotifyMs(System.currentTimeMillis())
                .setPayload(ncsPayloadReq)
                .setProductId(1)
                .setSid("test");
        ncsService.processEvent(ncsReq);
    }

    @Test
    public void testProcessEventChannelDestroyErrorNotExisted() throws Exception {
        NcsReq ncsReq = new NcsReq();
        NcsPayloadReq ncsPayloadReq = new NcsPayloadReq();
        ncsPayloadReq.setChannelName(roomIdNotExisted).setAccount(userId);
        ncsReq.setEventType(NcsEventTypeEnum.CHANNEL_DESTROY.getCode())
                .setNoticeId("1206008149:14940:100")
                .setNotifyMs(System.currentTimeMillis())
                .setPayload(ncsPayloadReq)
                .setProductId(1)
                .setSid("test");
        Throwable exception = assertThrows(BusinessException.class, () -> ncsService.processEvent(ncsReq));
        assertEquals("RTM metadata no data", exception.getMessage());
    }

    @Test
    public void testProcessEventBroadcasterLeaveChannelRoomOwner() throws Exception {
        log.info("testProcessEventBroadcasterLeaveChannel, roomId:{}", roomId);

        NcsReq ncsReq = new NcsReq();
        NcsPayloadReq ncsPayloadReq = new NcsPayloadReq();
        ncsPayloadReq.setChannelName(roomId).setAccount(userId);
        ncsReq.setEventType(NcsEventTypeEnum.BROADCASTER_LEAVE_CHANNEL.getCode())
                .setNoticeId("1206008149:14940:101")
                .setNotifyMs(System.currentTimeMillis())
                .setPayload(ncsPayloadReq)
                .setProductId(1)
                .setSid("test");
        ncsService.processEvent(ncsReq);
    }

    @Test
    public void testProcessEventBroadcasterLeaveChannelNotRoomOwner() throws Exception {
        log.info("testProcessEventBroadcasterLeaveChannelNotRoomOwner, roomId:{}", roomId);

        NcsReq ncsReq = new NcsReq();
        NcsPayloadReq ncsPayloadReq = new NcsPayloadReq();
        ncsPayloadReq.setChannelName(roomId).setAccount(userIdNotExisted);
        ncsReq.setEventType(NcsEventTypeEnum.BROADCASTER_LEAVE_CHANNEL.getCode())
                .setNoticeId("1206008149:14940:101")
                .setNotifyMs(System.currentTimeMillis())
                .setPayload(ncsPayloadReq)
                .setProductId(1)
                .setSid("test");
        ncsService.processEvent(ncsReq);
    }

    @Test
    public void testProcessEventBroadcasterLeaveChannelRoomOwnerErrorMetadataNoData() throws Exception {
        NcsReq ncsReq = new NcsReq();
        NcsPayloadReq ncsPayloadReq = new NcsPayloadReq();
        ncsPayloadReq.setChannelName(roomIdNotExisted).setAccount(userId);
        ncsReq.setEventType(NcsEventTypeEnum.BROADCASTER_LEAVE_CHANNEL.getCode())
                .setNoticeId("1206008149:14940:101")
                .setNotifyMs(System.currentTimeMillis())
                .setPayload(ncsPayloadReq)
                .setProductId(1)
                .setSid("test");
        ncsService.processEvent(ncsReq);
    }
}
