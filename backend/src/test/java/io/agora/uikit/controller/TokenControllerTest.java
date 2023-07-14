package io.agora.uikit.controller;

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

import com.alibaba.fastjson2.JSONObject;

import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.req.TokenReq;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class TokenControllerTest {
    @Autowired
    private MockMvc mvc;

    private String channelName = "channelNameTest";
    private String userId = "123456789";

    @Test
    public void testGenerate() throws Exception {
        JSONObject json = new JSONObject();
        json.put("channelName", channelName);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/token/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtcToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtmToken").exists())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGenerateErrorParamsRoomName() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/token/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtcToken").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtmToken").doesNotExist())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGenerateErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("channelName", channelName);
        mvc.perform(MockMvcRequestBuilders.post("/v1/token/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtcToken").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtmToken").doesNotExist())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGenerateTokenReq() throws Exception {
        TokenReq tokenReq = new TokenReq();
        tokenReq.setChannelName(channelName);
        tokenReq.setUserId(userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/token/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONObject.toJSONString(tokenReq)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtcToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtmToken").isNotEmpty())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGenerateToken006() throws Exception {
        JSONObject json = new JSONObject();
        json.put("channelName", channelName);
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/token006/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtcToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtmToken").exists())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGenerateToken006ErrorParamsRoomName() throws Exception {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        mvc.perform(MockMvcRequestBuilders.post("/v1/token006/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtcToken").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtmToken").doesNotExist())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testGenerateToken006ErrorParamsUserId() throws Exception {
        JSONObject json = new JSONObject();
        json.put("channelName", channelName);
        mvc.perform(MockMvcRequestBuilders.post("/v1/token006/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtcToken").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("data.rtmToken").doesNotExist())
                .andDo(MockMvcResultHandlers.print());
    }
}
