package io.agora.uikit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.ChorusDomain;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.service.impl.ChorusServiceImpl;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class SongControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private RtmUtil rtmUtil;

    private String roomId = "roomIdTest";
    private String roomIdNotExisted = "roomIdNotExisted";
    private String roomName = "roomNameTest";
    private String userId = "userIdTest";
    private String userIdNotExisted = "userIdNotExisted";
    private String userName = "userNameTest";
    private String userAvatar = "userAvatarTest";
    private Integer micSeatCount = 2;
    private String songCode = "songCodeTest";
    private String songCodeNotExisted = "songCodeNotExisted";

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

        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        json.put("name", "nameTest");
        json.put("singer", "singerTest");
        json.put("poster", "posterTest");
        json.put("releaseTime", "2023-03-16");
        json.put("duration", 180);
        json.put("musicUrl", "musicUrlTest");
        json.put("lrcUrl", "lrcUrlTest");
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
    public void testAdd() throws Exception {
        log.info("testAdd, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", "songCodeTest2");
        json.put("name", "nameTest");
        json.put("singer", "singerTest");
        json.put("poster", "posterTest");
        json.put("releaseTime", "2023-03-16");
        json.put("duration", 180);
        json.put("musicUrl", "musicUrlTest");
        json.put("lrcUrl", "lrcUrlTest");
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("userId", userId);
        json.put("songCode", songCode);
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorParamsSongCode() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("songCode cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorParamsOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("owner cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("owner", 1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorParamsOwnerUserId() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", "songCodeTest2");
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorParamsOwnerUserName() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", "songCodeTest2");
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userName cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorParamsOwnerUserAvatar() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", "songCodeTest2");
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userAvatar cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("songCode", songCode);
        json.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorNotOnMicSeat() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", "userIdTest2");
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
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_ON_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testAddErrorExists() throws Exception {
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
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_ALREADY_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPin() throws Exception {
        log.info("testPin, roomId:{}", roomId);

        JSONObject jsonAdd = new JSONObject();
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("userId", userId);
        ownerJson.put("userName", userName);
        ownerJson.put("userAvatar", userAvatar);
        jsonAdd.put("roomId", roomId);
        jsonAdd.put("userId", userId);
        jsonAdd.put("songCode", "songCodeTest2");
        jsonAdd.put("owner", ownerJson);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAdd.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        JSONObject jsonPlay = new JSONObject();
        jsonPlay.put("roomId", roomId);
        jsonPlay.put("userId", userId);
        jsonPlay.put("songCode", "songCodeTest2");
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPlay.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPinErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPinErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPinErrorParamsSongCode() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("songCode cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPinErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPinErrorNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCodeNotExisted);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPinErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userIdNotExisted);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlay() throws Exception {
        log.info("testPlay, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlayErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlayErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlayErrorParamsSongCode() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("songCode cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlayErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlayErrorNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCodeNotExisted);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlayErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPlayErrorStatusPlaying() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_ALREADY_PLAYING_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testRemove() throws Exception {
        log.info("testRemove, roomId:{}", roomId);

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

        JSONObject jsonChorus = new JSONObject();
        jsonChorus.put("roomId", roomId);
        jsonChorus.put("userId", userId);
        jsonChorus.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/chorus/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChorus.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, ChorusServiceImpl.METADATA_KEY);
        List<ChorusDomain> chorusList = JSON.parseArray(metadataItem.value, ChorusDomain.class);
        assertEquals("ChorusDomain(userId=userIdTest)", chorusList.get(0).toString());

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        Metadata metadata2 = rtmUtil.getChannelMetadata(roomId);
        MetadataItem metadataItem2 = rtmUtil.getChannelMetadataByKey(metadata2, ChorusServiceImpl.METADATA_KEY);
        List<ChorusDomain> chorusList2 = JSON.parseArray(metadataItem2.value, ChorusDomain.class);
        assertEquals("[]", chorusList2.toString());
    }

    @Test
    public void testRemoveErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testRemoveErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testRemoveErrorParamsSongCode() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("songCode cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testRemoveErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testRemoveErrorNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCodeNotExisted);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testRemoveErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStop() throws Exception {
        log.info("testStop, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStopErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStopErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStopErrorParamsSongCode() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("songCode cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStopErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStopErrorNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCodeNotExisted);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStopErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testStopErrorStatusNotPlaying() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("songCode", songCode);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.SONG_NOT_PLAYING_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }
}
