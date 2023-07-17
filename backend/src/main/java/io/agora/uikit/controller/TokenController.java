package io.agora.uikit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.TokenDto;
import io.agora.uikit.bean.req.TokenReq;
import io.agora.uikit.service.ITokenService;

@Validated
@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class TokenController {
    @Autowired
    private ITokenService tokenService;

    /**
     * Generate token
     * 
     * @param tokenReq
     * @return
     */
    @PostMapping("/token/generate")
    @ResponseBody
    public R<TokenDto> generate(@Validated @RequestBody TokenReq tokenReq) {
        return R.success(tokenService.generateToken(tokenReq.getChannelName(), tokenReq.getUserId()));
    }

    /**
     * Generate token
     * 
     * @param tokenReq
     * @return
     */
    @PostMapping("/token006/generate")
    @ResponseBody
    public R<TokenDto> generateToken006(@Validated @RequestBody TokenReq tokenReq) throws Exception {
        return R.success(tokenService.generateToken006(tokenReq.getChannelName(), tokenReq.getUserId()));
    }
}