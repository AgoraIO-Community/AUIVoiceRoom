package io.agora.uikit.controller;

import io.agora.uikit.bean.dto.ChatRoomDto;
import io.agora.uikit.bean.dto.ChatUserDto;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.req.ChatRoomCreateRoomReq;
import io.agora.uikit.bean.req.ChatRoomCreateUserReq;
import io.agora.uikit.service.IChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v1/chatRoom", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatRoomController {
    @Resource
    private IChatRoomService chatRoomService;

    @PostMapping("/rooms/create")
    @ResponseBody
    public R<ChatRoomDto> createRoom(@Validated @RequestBody ChatRoomCreateRoomReq req) throws Exception {
        log.info("create chat room,roomId:{},req:{}", req.getRoomId(), req);
        ChatRoomDto chatRoomDto = chatRoomService.creatRoom(req.getRoomId(), req.getDescription(), req.getUserId(), req.getUserName(), new ArrayList<>() {
            {
                add(req.getUserName());
            }
        }, req.getCustom());
        return R.success(chatRoomDto);
    }

    @PostMapping("/users/create")
    @ResponseBody
    public R<ChatUserDto> createUser(@Validated @RequestBody ChatRoomCreateUserReq req) throws Exception {
        String password = UUID.randomUUID().toString().replaceAll("-", "");
        log.info("create chat room user,req:{},generated password:{}", req, password);
        ChatUserDto user = chatRoomService.createUser(req.getUserName(), password);
        return R.success(user);
    }

}
