package io.agora.uikit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.agora.uikit.bean.dto.ChorusDto;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.req.ChorusJoinReq;
import io.agora.uikit.bean.req.ChorusLeaveReq;
import io.agora.uikit.service.IChorusService;

@Validated
@RestController
@RequestMapping(value = "/v1/chorus", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChorusController {
    @Autowired
    private IChorusService chorusService;

    /**
     * Join
     * 
     * @param chorusJoinReq
     * @return
     * @throws Exception
     */
    @PostMapping("/join")
    @ResponseBody
    public R<ChorusDto> join(@Validated @RequestBody ChorusJoinReq chorusJoinReq) throws Exception {
        chorusService.join(chorusJoinReq);
        return R.success(new ChorusDto().setRoomId(chorusJoinReq.getRoomId()));
    }

    /**
     * Leave
     * 
     * @param chorusLeaveReq
     * @return
     * @throws Exception
     */
    @PostMapping("/leave")
    @ResponseBody
    public R<ChorusDto> leave(@Validated @RequestBody ChorusLeaveReq chorusLeaveReq) throws Exception {
        chorusService.leave(chorusLeaveReq);
        return R.success(new ChorusDto().setRoomId(chorusLeaveReq.getRoomId()));
    }
}