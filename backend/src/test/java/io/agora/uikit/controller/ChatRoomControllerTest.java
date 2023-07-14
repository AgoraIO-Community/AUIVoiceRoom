package io.agora.uikit.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
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
public class ChatRoomControllerTest {
    @Resource
    private MockMvc mockMvc;

    private String roomId = "roomIdTest";

    private final String roomName = "roomNameTest";

    private final String userId = "userIdTest";

    private final String userName = "userNameTest";

    private final String userAvatar = "userAvatarTest";
    private final Integer micSeatCount = 2;

    private final String description = "descriptionTest";

    private final String custom = "customTest";

    @Before
    public void before() throws Exception {
        RoomCreateReq roomCreateReq = new RoomCreateReq();
        roomCreateReq.setRoomName(roomName)
                .setUserId(userId)
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
        json.put("userId", userId);
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateChatRoom() throws Exception {
        log.info("test create chat room,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roomId", roomId);
        jsonObject.put("userId", userId);
        jsonObject.put("userName", userName);
        jsonObject.put("description", description);
        jsonObject.put("custom", custom);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/rooms/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void testCreateChatRoomErrorParamsRoomId() throws Exception {
        log.info("testCreateChatRoomErrorParamsRoomId,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("userName", userName);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/rooms/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateChatRoomErrorParamsUserId() throws Exception {
        log.info("testCreateChatRoomErrorParamsUserId,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roomId", roomId);
        jsonObject.put("userName", userName);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/rooms/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateChatRoomErrorParamsUserName() throws Exception {
        log.info("testCreateChatRoomErrorParamsUserName,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roomId", roomId);
        jsonObject.put("userId", userId);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/rooms/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userName cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateChatUser() throws Exception {
        log.info("test create chat user,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/users/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateChatUserErrorParamsUserName() throws Exception {
        log.info("testCreateChatUserErrorParamsUserName,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/users/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userName cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }
}
