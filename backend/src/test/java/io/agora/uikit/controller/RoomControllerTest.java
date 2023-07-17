package io.agora.uikit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

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
import io.agora.uikit.bean.domain.MicSeatDomain;
import io.agora.uikit.bean.domain.SongDomain;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.enums.MicSeatStatusEnum;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.service.IMicSeatService;
import io.agora.uikit.service.ISongService;
import io.agora.uikit.service.impl.MicSeatServiceImpl;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class RoomControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private RtmUtil rtmUtil;
    @Autowired
    private IMicSeatService micSeatService;
    @Autowired
    private ISongService songService;

    private String roomId = "roomIdTest";
    private String roomIdNotExisted = "roomIdNotExisted";
    private String roomName = "roomNameTest";
    private String userId = "userIdTest";
    private String userIdNotExisted = "userIdNotExisted";
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
    public void testCreate() throws Exception {
        RoomCreateReq roomCreateReq = new RoomCreateReq();
        roomCreateReq.setRoomName(roomName).setUserId(userId).setUserName(userName).setUserAvatar(userAvatar)
                .setMicSeatCount(2);
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

        String roomId2 = r.getData().getRoomId();
        log.info("testCreate, roomId2:{}", roomId2);

        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, MicSeatServiceImpl.METADATA_KEY);
        Map<String, MicSeatDomain> micSeatMap = micSeatService.getMicSeatMap(metadataItem);
        assertEquals(MicSeatStatusEnum.MIC_SEAT_STATUS_USED, micSeatMap.get("0").getMicSeatStatus());
        assertEquals(userId, micSeatMap.get("0").getOwner().getUserId());
        assertEquals(userName, micSeatMap.get("0").getOwner().getUserName());
        assertEquals(userAvatar, micSeatMap.get("0").getOwner().getUserAvatar());

        JSONObject json = new JSONObject();
        json.put("roomId", roomId2);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomName", "测试房间");
        json.put("userId", userId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatCount", micSeatCount);
        String result = mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        R<RoomDto> r = JSON.parseObject(result, new TypeReference<R<RoomDto>>() {
        });

        String roomId2 = r.getData().getRoomId();
        log.info("testCreateJson, roomId2:{}", roomId2);

        JSONObject jsonDestroy = new JSONObject();
        jsonDestroy.put("roomId", roomId2);
        jsonDestroy.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDestroy.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateJsonEmoji() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomName", "测试房间 emoji 😊 emoji");
        json.put("userId", userId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatCount", micSeatCount);
        String result = mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        R<RoomDto> r = JSON.parseObject(result, new TypeReference<R<RoomDto>>() {
        });

        String roomId2 = r.getData().getRoomId();
        log.info("testCreateJson, roomId2:{}", roomId2);

        JSONObject jsonDestroy = new JSONObject();
        jsonDestroy.put("roomId", roomId2);
        jsonDestroy.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDestroy.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateErrorParamsRoomName() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatCount", micSeatCount);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomName cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomName", roomName);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatCount", micSeatCount);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateErrorParamsUserName() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomName", roomName);
        json.put("userId", userId);
        json.put("userAvatar", userAvatar);
        json.put("micSeatCount", micSeatCount);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userName cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateErrorParamsUserAvatar() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomName", roomName);
        json.put("userId", userId);
        json.put("userName", userName);
        json.put("micSeatCount", micSeatCount);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userAvatar cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateErrorParamsMicSeatCount() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomName", roomName);
        json.put("userId", userId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatCount", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatCount must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testDestroyErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testDestroyErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testDestroyErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testDestroyErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/destroy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeave() throws Exception {
        JSONObject jsonSong = new JSONObject();
        JSONObject ownerJsonSong = new JSONObject();
        ownerJsonSong.put("userId", userId);
        ownerJsonSong.put("userName", userName);
        ownerJsonSong.put("userAvatar", userAvatar);
        jsonSong.put("roomId", roomId);
        jsonSong.put("userId", userId);
        jsonSong.put("songCode", songCode);
        jsonSong.put("owner", ownerJsonSong);
        mvc.perform(MockMvcRequestBuilders.post("/v1/song/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSong.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, MicSeatServiceImpl.METADATA_KEY);
        Map<String, MicSeatDomain> micSeatMap = micSeatService.getMicSeatMap(metadataItem);
        assertEquals(MicSeatStatusEnum.MIC_SEAT_STATUS_USED, micSeatMap.get("0").getMicSeatStatus());
        assertEquals(userId, micSeatMap.get("0").getOwner().getUserId());
        assertEquals(userName, micSeatMap.get("0").getOwner().getUserName());
        assertEquals(userAvatar, micSeatMap.get("0").getOwner().getUserAvatar());
        SongDomain songDomain = songService.getSongSingle(metadata, songCode);
        assertEquals(userId, songDomain.getOwner().getUserId());

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        Metadata metadata2 = rtmUtil.getChannelMetadata(roomId);
        MetadataItem metadataItem2 = rtmUtil.getChannelMetadataByKey(metadata2,
                MicSeatServiceImpl.METADATA_KEY);
        Map<String, MicSeatDomain> micSeatMap2 = micSeatService.getMicSeatMap(metadataItem2);
        assertEquals(MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE,
                micSeatMap2.get("0").getMicSeatStatus());
        assertEquals(null, micSeatMap2.get("0").getOwner().getUserId());
        assertEquals(null, micSeatMap2.get("0").getOwner().getUserName());
        assertEquals(null, micSeatMap2.get("0").getOwner().getUserAvatar());
        SongDomain songDomain2 = songService.getSongSingle(metadata2, songCode);
        assertEquals(null, songDomain2);
    }

    @Test
    public void testLeaveNotOnMicSeat() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userIdNotExisted);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testList() throws Exception {
        JSONObject json = new JSONObject();
        json.put("lastCreateTime", System.currentTimeMillis());
        json.put("pageSize", 1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void testListErrorParamsPageSize() throws Exception {
        JSONObject json = new JSONObject();
        json.put("lastCreateTime", System.currentTimeMillis());
        json.put("pageSize", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("pageSize must be greater than 0; "))
                .andDo(MockMvcResultHandlers.print());

        JSONObject json2 = new JSONObject();
        json.put("lastCreateTime", System.currentTimeMillis());
        json2.put("pageSize", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("pageSize must be greater than 0; "))
                .andDo(MockMvcResultHandlers.print());

        JSONObject json3 = new JSONObject();
        json.put("lastCreateTime", System.currentTimeMillis());
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json3.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("pageSize must be greater than 0; "))
                .andDo(MockMvcResultHandlers.print());

        JSONObject json4 = new JSONObject();
        json.put("lastCreateTime", System.currentTimeMillis());
        json4.put("pageSize", 1000);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json4.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("message").value("pageSize must be less than or equal to 50; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testQuery() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void testQueryErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testQueryErrorNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        mvc.perform(MockMvcRequestBuilders.post("/v1/room/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
    }
}
