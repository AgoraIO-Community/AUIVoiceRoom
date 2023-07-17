package io.agora.uikit.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.agora.uikit.bean.dto.ChatRoomDto;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.service.IChatRoomService;
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
@AutoConfigureMetrics
@AutoConfigureMockMvc
public class UserControllerTest {
    @Resource
    private MockMvc mockMvc;
    @Resource
    private IChatRoomService chatRoomService;
    private String roomId = "roomIdTest";
    private final String roomName = "roomNameTest";

    private final String userId = "1234567";

    private final String userName = "1234567";
    private final Long kicOutUserUID = 12345678L;
    private final String kicOutUserName = "12345678";
    private final String userAvatar = "userAvatarTest";
    private final Integer micSeatCount = 2;

    @Before
    public void before() throws Exception {
        // create room
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

        // create owner chat room user

        log.info("test create owner chat user,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/users/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
        log.info("test create chat room,roomId:{},userId:{}", roomId, userId);


        // create kickOut chat room user
        log.info("test create kickOut chat user,roomId:{},userId:{}", roomId, userId);
        jsonObject.clear();
        jsonObject.put("userName", kicOutUserName);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/users/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        // create chat room
        log.info("test create chat room,roomId:{},userId:{}", roomId, userId);
        jsonObject.clear();
        jsonObject.put("roomId", roomId);
        jsonObject.put("userId", userId);
        jsonObject.put("userName", userName);

        String createChatRoomResult = mockMvc.perform(MockMvcRequestBuilders.post("/v1/chatRoom/rooms/create")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        R<ChatRoomDto> chatRoomDtoR = JSON.parseObject(createChatRoomResult, new TypeReference<R<ChatRoomDto>>() {
        });

        // join chat room
        log.info("test join chat room,roomId:{},userId:{}", roomId, userId);
        chatRoomService.joinChatRoom(chatRoomDtoR.getData().getChatRoomId(), kicOutUserName);
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
    public void testKickOut() throws Exception {
        log.info("testKickOut,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operatorId", userId);
        jsonObject.put("roomId", roomId);
        jsonObject.put("uid", kicOutUserUID);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/kickOut")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickOutErrorParamsOperatorId() throws Exception {
        log.info("testKickOutErrorParamsOperatorId,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roomId", roomId);
        jsonObject.put("uid", kicOutUserUID);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/kickOut")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("operatorId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void testKickOutErrorParamsRoomId() throws Exception {
        log.info("testKickOutErrorParamsRoomId,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operatorId", userId);
        jsonObject.put("uid", kicOutUserUID);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/kickOut")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickOutErrorParamsUID() throws Exception {
        log.info("testKickOutErrorParamsUID,roomId:{},userId:{}", roomId, userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operatorId", userId);
        jsonObject.put("roomId", roomId);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/kickOut")
                        .contentType("application/json")
                        .content(jsonObject.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("uid cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }
}
