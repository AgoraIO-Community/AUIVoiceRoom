package io.agora.uikit.controller;

import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.req.InvitationAcceptReq;
import io.agora.uikit.bean.req.InvitationCreateReq;
import io.agora.uikit.bean.req.InvitationRemoveReq;
import io.agora.uikit.service.IInvitationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v1/invitation", produces = MediaType.APPLICATION_JSON_VALUE)
public class InvitationController {
    @Resource
    private IInvitationService invitationService;

    @PostMapping("/create")
    @ResponseBody
    public R<Void> create(@Validated @RequestBody InvitationCreateReq req) throws Exception {
        log.info("create invitation request,roomId:{},req:{}", req.getRoomId(), req);
        invitationService.start(req.getRoomId(), req.getFromUserId(), req.getToUserId(), req.getPayload());
        return R.success(null);
    }

    @PostMapping("/accept")
    @ResponseBody
    public R<Void> accept(@Validated @RequestBody InvitationAcceptReq req) throws Exception {
        log.info("accept invitation request,roomId:{},req:{}", req.getRoomId(), req);
        invitationService.accept(req.getRoomId(), req.getFromUserId(), null);
        return R.success(null);
    }

    @PostMapping("/cancel")
    @ResponseBody
    public R<Void> end(@Validated @RequestBody InvitationRemoveReq req) throws Exception {
        log.info("cancel invitation request,roomId:{},req:{}", req.getRoomId(), req);
        invitationService.remove(req.getRoomId(), req.getFromUserId(), req.getToUserId());
        return R.success(null);
    }
}
