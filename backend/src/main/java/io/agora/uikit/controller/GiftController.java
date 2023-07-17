package io.agora.uikit.controller;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.service.IGiftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v1/gifts", produces = MediaType.APPLICATION_JSON_VALUE)
public class GiftController {
    @Resource
    private IGiftService giftService;

    @GetMapping("/list")
    @ResponseBody
    public R<List<Map<String, Object>>> list() throws Exception {
        return R.success(giftService.list());
    }
}
