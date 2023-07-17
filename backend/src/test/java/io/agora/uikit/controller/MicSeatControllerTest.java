package io.agora.uikit.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
public class MicSeatControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private String roomId = "roomIdTest";
    private String roomIdNotExisted = "roomIdNotExisted";
    private String roomIdName = "roomNameTest";
    private String userId = "userIdTest";
    private String userName = "userNameTest";
    private String userAvatar = "userAvatarTest";
    private String micSeatUserId = "micSeatUserIdTest";
    private Integer micSeatCount = 8;
    private Integer micSeatNo = 1;

    @Before
    public void before() throws Exception {
        RoomCreateReq roomCreateReq = new RoomCreateReq();
        roomCreateReq.setRoomName(roomIdName)
                .setUserId(userId)
                .setUserName(userName)
                .setUserAvatar(userAvatar)
                .setMicSeatCount(micSeatCount);
        String result = mvc.perform(MockMvcRequestBuilders.post("/v1/room/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(roomCreateReq)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.roomName").value(roomIdName))
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
    public void testEnter() throws Exception {
        log.info("testEnter, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", micSeatUserId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterLockConcurrency() throws Exception {
        log.info("testEnterLockConcurrency, roomId:{}", roomId);

        CountDownLatch countDownLatch = new CountDownLatch(2);
        List<Future<?>> futures = new ArrayList<>();

        futures.add(threadPoolTaskExecutor.submit(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("roomId", roomId);
                json.put("ownerUserId", userId);
                json.put("micSeatNo", micSeatNo);
                json.put("isLock", 1);
                mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                        .andDo(MockMvcResultHandlers.print());
            } catch (Exception e) {
            } finally {
                countDownLatch.countDown();
            }
        }));

        futures.add(threadPoolTaskExecutor.submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);

                JSONObject json = new JSONObject();
                json.put("roomId", roomId);
                json.put("userId", micSeatUserId);
                json.put("userName", userName);
                json.put("userAvatar", userAvatar);
                json.put("micSeatNo", micSeatNo);
                mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("code")
                                .value(ReturnCodeEnum.ROOM_ACQUIRE_LOCK_ERROR.getCode()))
                        .andDo(MockMvcResultHandlers.print());
            } catch (Exception e) {
            } finally {
                countDownLatch.countDown();
            }
        }));

        countDownLatch.await();
    }

    @Test
    public void testEnterErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", micSeatUserId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorParamsUserName() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", micSeatUserId);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userName cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorParamsUserAvatar() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", micSeatUserId);
        json.put("userName", userName);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userAvatar cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", micSeatUserId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", micSeatUserId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorMicSeatNumberAlreadyOn() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_ALREADY_ON_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", micSeatUserId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testEnterErrorMicSeatNumberStatusNotIdle() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", micSeatUserId);
        json.put("userName", userName);
        json.put("userAvatar", userAvatar);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        json.put("userId", "micSeatUserId2");
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/enter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_IDLE_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKick() throws Exception {
        log.info("testKick, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickLeaveConcurrency() throws Exception {
        log.info("testKickLeaveConcurrency, roomId:{}", roomId);

        CountDownLatch countDownLatch = new CountDownLatch(2);
        List<Future<?>> futures = new ArrayList<>();

        futures.add(threadPoolTaskExecutor.submit(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("roomId", roomId);
                json.put("userId", userId);
                json.put("micSeatNo", 0);
                mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                        .andDo(MockMvcResultHandlers.print());
            } catch (Exception e) {
            } finally {
                countDownLatch.countDown();
            }
        }));

        futures.add(threadPoolTaskExecutor.submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);

                JSONObject json = new JSONObject();
                json.put("roomId", roomId);
                json.put("userId", userId);
                mvc.perform(MockMvcRequestBuilders.post("/v1/seat/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toJSONString()))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("code")
                                .value(ReturnCodeEnum.ROOM_ACQUIRE_LOCK_ERROR.getCode()))
                        .andDo(MockMvcResultHandlers.print());
            } catch (Exception e) {
            } finally {
                countDownLatch.countDown();
            }
        }));

        countDownLatch.await();
    }

    @Test
    public void testKickErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testKickErrorMicSeatNumberStatusIdle() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/kick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_IDLE_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeave() throws Exception {
        log.info("testLeave, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/leave")
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
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/leave")
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
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/leave")
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
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLeaveErrorNotOnMicSeat() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_ON_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLock() throws Exception {
        log.info("testLock, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLockErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLockErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLockErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLockErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLockErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userIdTest2");
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLockErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testLockErrorMicSeatNumberStatusNotIdle() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_IDLE_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteAudio() throws Exception {
        log.info("testMuteAudio, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteAudioErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteAudioErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteAudioErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("isMuteAudio", 1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteAudioErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        json.put("isMuteAudio", 1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteAudioErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteAudioErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteVideo() throws Exception {
        log.info("testMuteVideo, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteVideoErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteVideoErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteVideoErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteVideoErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteVideoErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testMuteVideoErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPick() throws Exception {
        log.info("testPick, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorParamsOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("owner cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("owner", 1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorParamsOwnerUserId() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorParamsOwnerUserName() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userName cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorParamsUserAvatar() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", userId);
        jsonOwner.put("userName", userName);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userAvatar cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("owner", jsonOwner);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testPickErrorMicSeatNumberStatusNotIdle() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonOwner = new JSONObject();
        jsonOwner.put("userId", micSeatUserId);
        jsonOwner.put("userName", userName);
        jsonOwner.put("userAvatar", userAvatar);
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("owner", jsonOwner);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/pick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_IDLE_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlock() throws Exception {
        log.info("testUnlock, roomId:{}", roomId);

        JSONObject jsonLock = new JSONObject();
        jsonLock.put("roomId", roomId);
        jsonLock.put("userId", userId);
        jsonLock.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonLock.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlockErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlockErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlockErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlockErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlockErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userIdTest2");
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlockErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnlockErrorMicSeatNumberStatusNotLocked() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/unlock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_LOCKED_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteAudio() throws Exception {
        log.info("testUnmuteAudio, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteAudioErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteAudioErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteAudioErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("isUnmuteAudio", 1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteAudioErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        json.put("isUnmuteAudio", 1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteAudioErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteAudioErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/audio/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnMuteVideo() throws Exception {
        log.info("testUnMuteVideo, roomId:{}", roomId);

        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 0);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteVideoErrorParamsRoomId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("roomId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteVideoErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteVideoErrorParamsMicSeatNo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo cannot be empty; "))
                .andDo(MockMvcResultHandlers.print());

        json.put("micSeatNo", -1);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("message")
                        .value("micSeatNo must be greater than or equal to 0; "))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteVideoErrorMetadataNoData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomIdNotExisted);
        json.put("userId", userId);
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteVideoErrorNotOwner() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", "userId2");
        json.put("micSeatNo", micSeatNo);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.ROOM_NOT_OWNER_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUnmuteVideoErrorMicSeatNumberNotExists() throws Exception {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("micSeatNo", 100);
        mvc.perform(MockMvcRequestBuilders.post("/v1/seat/video/unmute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }
}
