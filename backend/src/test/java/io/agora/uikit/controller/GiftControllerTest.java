package io.agora.uikit.controller;

import io.agora.uikit.bean.enums.ReturnCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class GiftControllerTest {
    @Resource
    private MockMvc mockMvc;

    @Test
    public void testList() throws Exception {
        log.info("testList");

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/gifts/list"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ReturnCodeEnum.SUCCESS.getCode()))
                .andDo(MockMvcResultHandlers.print());

    }
}
