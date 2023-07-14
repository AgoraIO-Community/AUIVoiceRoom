package io.agora.uikit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.req.NcsReq;
import io.agora.uikit.service.INcsService;

@RestController
@RequestMapping(value = "/v1/ncs", produces = MediaType.APPLICATION_JSON_VALUE)
public class NcsController {
    @Autowired
    private INcsService ncsService;

    /**
     * Callback
     * 
     * @param signature
     * @param requestBody
     * @return
     * @throws Exception
     */
    @PostMapping("/callback")
    @ResponseBody
    public R<Void> callback(@RequestHeader("Agora-Signature-V2") String signature, @RequestBody String requestBody)
            throws Exception {
        NcsReq ncsReq = JSON.parseObject(requestBody, NcsReq.class);
        ncsService.checkSign(signature, requestBody);
        ncsService.processEvent(ncsReq);

        return R.success(null);
    }
}