package io.agora.uikit.controller;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.req.ApplicationAcceptReq;
import io.agora.uikit.bean.req.ApplicationCreateReq;
import io.agora.uikit.bean.req.ApplicationRemoveReq;
import io.agora.uikit.service.IApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v1/application", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApplicationController {

    @Resource
    private IApplicationService applicationService;

    @PostMapping("/create")
    @ResponseBody
    public R<Void> create(@Validated @RequestBody ApplicationCreateReq req) throws Exception {
        log.info("create application request,roomId:{},req:{}", req.getRoomId(), req);
        applicationService.start(req.getRoomId(), req.getFromUserId(), req.getPayload());
        return R.success(null);
    }

    @PostMapping("/accept")
    @ResponseBody
    public R<Void> accept(@Validated @RequestBody ApplicationAcceptReq req) throws Exception {
        log.info("accept application request,roomId:{},req:{}", req.getRoomId(), req);
        applicationService.accept(req.getRoomId(), req.getFromUserId(), req.getToUserId());
        return R.success(null);
    }


    @PostMapping("/cancel")
    @ResponseBody
    public R<Void> end(@Validated @RequestBody ApplicationRemoveReq req) throws Exception {
        log.info("cancel application request,roomId:{},req:{}", req.getRoomId(), req);
        applicationService.remove(req.getRoomId(), req.getFromUserId(), req.getToUserId());
        return R.success(null);
    }
}
