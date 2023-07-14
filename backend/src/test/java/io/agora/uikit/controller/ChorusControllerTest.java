package io.agora.uikit.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.req.RoomCreateReq;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class ChorusControllerTest {
    @Autowired
    private MockMvc mvc;

    private String roomId = "roomIdTest";
    private String roomName = "roomNameTest";
    private String roomIdNotExisted = "roomIdNotExisted";
    private String userId = "userIdTest";
    private String userName = "userNameTest";
    private String userAvatar = "userAvatarTest";
    private Integer micSeatCount = 2;
    private String songCode = "songCodeTest";

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

        // Add song
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
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
    public void testJoin() throws Exception {
        log.info("testJoin, roomId:{}", roomId);

        JSONObject jsonSong = new JSONObject();
        jsonSong.put("roomId", roomId);
        jsonSong.put("userId", userId);
        jsonSong.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSong.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorParamsRoomId() throws Exception {
        log.info("testJoinErrorParamsRoomId, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorParamsSongCode() throws Exception {
        log.info("testJoinErrorParamsSongCode, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("songCode cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorParamsUserId() throws Exception {
        log.info("testJoinErrorParamsUserId, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorAcquireLock() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorAlreadyJoined() throws Exception {
        log.info("testJoinErrorAlreadyJoined, roomId:{}", roomId);

        JSONObject jsonSong = new JSONObject();
        jsonSong.put("roomId", roomId);
        jsonSong.put("userId", userId);
        jsonSong.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSong.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.CHORUS_ALREADY_JOINED_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorSongNoData() throws Exception {
        log.info("testJoinErrorSongNoData, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", "songCodeTestNotExisted");
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testJoinErrorSongNotPlaying() throws Exception {
        log.info("testJoinErrorSongNotPlaying, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_PLAYING_ERROR.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Song not playing"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeave() throws Exception {
        log.info("testLeave, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorParamsRoomId() throws Exception {
        log.info("testLeaveErrorParamsRoomId, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorParamsSongCode() throws Exception {
        log.info("testLeaveErrorParamsSongCode, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("songCode cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorParamsUserId() throws Exception {
        log.info("testLeaveErrorParamsUserId, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorAcquireLock() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("songCode", songCode);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }
}
