package io.agora.uikit.controller;

import io.agora.rtm.Metadata;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.dto.RoomListDto;
import io.agora.uikit.bean.dto.RoomQueryDto;
import io.agora.uikit.bean.entity.RoomListEntity;
import io.agora.uikit.bean.req.*;
import io.agora.uikit.service.*;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(value = "/v1/room", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomController {
    @Resource
    private IRoomService roomService;

    @Resource
    private IMicSeatService micSeatService;

    @Resource
    private ISongService songService;

    @Resource
    private IChorusService chorusService;

    @Resource
    private IInvitationService invitationService;

    @Resource
    private IApplicationService applicationService;

    /**
     * Create
     *
     * @param roomCreateReq
     * @return
     * @throws Exception
     */
    @PostMapping("/create")
    @ResponseBody
    public R<RoomDto> create(@Validated @RequestBody RoomCreateReq roomCreateReq) throws Exception {
        if (roomCreateReq.getRoomId() == null) {
            roomCreateReq.setRoomId(UUID.randomUUID().toString());
        }

        Metadata metadata = roomService.getMetadata();
        roomService.createMetadata(metadata, roomCreateReq);
        micSeatService.createMetadata(metadata, roomCreateReq);
        songService.createMetadata(metadata, new SongAddReq());
        chorusService.createMetadata(metadata, new ChorusJoinReq());
        applicationService.createMetadata(metadata, roomCreateReq);
        invitationService.createMetadata(metadata, roomCreateReq);
        roomService.create(metadata, roomCreateReq);

        return R.success(new RoomDto().setRoomName(roomCreateReq.getRoomName())
                .setRoomId(roomCreateReq.getRoomId()));
    }

    /**
     * Destroy
     *
     * @param roomDestroyReq
     * @return
     * @throws Exception
     */
    @PostMapping("/destroy")
    @ResponseBody
    public R<RoomDto> destroy(@Validated @RequestBody RoomDestroyReq roomDestroyReq) throws Exception {
        roomService.destroy(roomDestroyReq);
        return R.success(new RoomDto().setRoomId(roomDestroyReq.getRoomId()));
    }

    /**
     * Leave
     *
     * @param roomLeaveReq
     * @return
     * @throws Exception
     */
    @PostMapping("/leave")
    @ResponseBody
    public R<RoomDto> leave(@Validated @RequestBody RoomLeaveReq roomLeaveReq) throws Exception {
        roomService.leave(roomLeaveReq);
        return R.success(new RoomDto().setRoomId(roomLeaveReq.getRoomId()));
    }

    /**
     * List
     *
     * @param roomListReq
     * @return
     * @throws Exception
     */
    @PostMapping("/list")
    @ResponseBody
    public R<RoomListDto<RoomListEntity>> list(@Validated @RequestBody RoomListReq roomListReq) throws Exception {
        if (roomListReq.getLastCreateTime() == null || roomListReq.getLastCreateTime() == 0) {
            roomListReq.setLastCreateTime(System.currentTimeMillis());
        }
        RoomListDto<RoomListEntity> roomList = roomService.getRoomList(roomListReq);
        return R.success(roomList);
    }

    /**
     * Query
     *
     * @param roomQueryReq
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    @ResponseBody
    public R<RoomQueryDto> query(@Validated @RequestBody RoomQueryReq roomQueryReq) throws Exception {
        RoomQueryDto roomQueryDto = roomService.query(roomQueryReq);
        return R.success(roomQueryDto);
    }

    @PostMapping("/queryAll")
    @ResponseBody
    public R<Object> queryAll(@Validated @RequestBody RoomQueryReq roomQueryReq) throws Exception {
        Object obj = roomService.queryAll(roomQueryReq);
        return R.success(obj);
    }

    @PostMapping("/notice")
    @ResponseBody
    public R<Void> changeNotice(@Validated @RequestBody RoomChangeNoticeReq req) throws Exception {
        roomService.changeNotice(req.getRoomId(), req.getUserId(), req.getNotice());
        return R.success(null);
    }
}