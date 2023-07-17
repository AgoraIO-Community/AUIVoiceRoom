package io.agora.uikit.controller;

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

import com.alibaba.fastjson2.JSONObject;

import io.agora.uikit.bean.enums.ReturnCodeEnum;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
public class NcsControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void testCallback() throws Exception {
        JSONObject json = new JSONObject();
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("channelName", "70b62529-2608-43f9-a062-2cd0052de45d");
        jsonPayload.put("ts", 1679379655);
        json.put("sid", "5EFF2F00027648D499BF100C1DEE1A86");
        json.put("noticeId", "1205703979:24226:102");
        json.put("productId", 1);
        json.put("eventType", 101);
        json.put("notifyMs", 1611566412672L);
        json.put("payload", jsonPayload);
        mvc.perform(MockMvcRequestBuilders.post("/v1/ncs/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Agora-Signature-V2", "96f2255073469ca16e41f1ec4f0cc8563080bd8fd8514b2bf40a2a0fbf2d78a9")
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCallbackErrorSign() throws Exception {
        JSONObject json = new JSONObject();
        json.put("sid", "5EFF2F00027648D499BF100C1DEE1A86");
        mvc.perform(MockMvcRequestBuilders.post("/v1/ncs/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Agora-Signature-V2", "signError")
                .content(json.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.NCS_SIGN_ERROR.getCode()))
                .andDo(MockMvcResultHandlers.print());
    }
}
