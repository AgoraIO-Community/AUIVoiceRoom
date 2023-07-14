package io.agora.uikit.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.req.MicSeatPayload;
import io.agora.uikit.bean.req.RoomCreateReq;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class InvitationControllerTest {

    @Resource
    private MockMvc mockMvc;

    private String roomId = "roomIdTest";

    private final String roomName = "roomNameTest";

    private final String roomOwnerUserId = "userIdTest";

    private final String normalUserId = "normalUserId";

    private final String userName = "userNameTest";

    private final String userAvatar = "userAvatarTest";
    private final Integer micSeatCount = 2;


    @Before
    public void before() throws Exception {
        RoomCreateReq roomCreateReq = new RoomCreateReq();
        roomCreateReq.setRoomName(roomName)
                .setUserId(roomOwnerUserId)
                .setUserName(userName)
                .setUserAvatar(userAvatar)
                .setMicSeatCount(micSeatCount);
        String result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
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
        json.put("userId", roomOwnerUserId);
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    public void testCreateInvitationErrorParamsRoomId() throws Exception {
        log.info("testCreateInvitationErrorParamsRoomId,roomId:{},userId:{}", roomId, roomOwnerUserId);

        JSONObject json = new JSONObject();
        json.put("fromUserId", roomOwnerUserId);
        json.put("toUserId", normalUserId);
        json.put("payload", new MicSeatPayload()
                .setSeatNo(1)
                .setDesc("test"));

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateInvitationErrorParamsFromUserId() throws Exception {
        log.info("testCreateInvitationErrorParamsFromUserId,roomId:{},userId:{}", roomId, normalUserId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("toUserId", normalUserId);
        json.put("payload", new MicSeatPayload()
                .setSeatNo(1)
                .setDesc("test"));

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("fromUserId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateInvitationErrorParamsToUserId() throws Exception {
        log.info("testCreateInvitationErrorParamsToUserId,roomId:{},userId:{}", roomId, normalUserId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("fromUserId", roomOwnerUserId);
        json.put("payload", new MicSeatPayload()
                .setSeatNo(1)
                .setDesc("test"));

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("toUserId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateInvitationErrorParamsPayload() throws Exception {
        log.info("testCreateInvitationErrorParamsFromUserId,roomId:{},userId:{}", roomId, normalUserId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("fromUserId", roomOwnerUserId);
        json.put("toUserId", normalUserId);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("payload cannot be null; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAcceptInvitationErrorParamsRoomId() throws Exception {
        log.info("testAcceptInvitationErrorParamsRoomId,roomId:{},userId:{}", roomId, roomOwnerUserId);

        JSONObject json = new JSONObject();
        json.put("fromUserId", roomOwnerUserId);
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void testAcceptInvitationErrorParamsFromUserId() throws Exception {
        log.info("testAcceptInvitationErrorParamsFromUserId,roomId:{},userId:{}", roomId, roomOwnerUserId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("fromUserId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void testEndInvitationErrorParamsRoomId() throws Exception {
        log.info("testRemoveInvitationErrorParamsRoomId,roomId:{},userId:{}", roomId, roomOwnerUserId);

        JSONObject json = new JSONObject();
        json.put("fromUserId", roomOwnerUserId);
        json.put("toUserId", normalUserId);
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void testEndInvitationErrorParamsFromUserId() throws Exception {
        log.info("testRemoveInvitationErrorParamsFromUserId,roomId:{},userId:{}", roomId, roomOwnerUserId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("toUserId", normalUserId);
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/invitation/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("fromUserId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

    }
}
