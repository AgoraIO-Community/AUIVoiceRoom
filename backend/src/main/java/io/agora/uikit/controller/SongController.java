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
import io.agora.uikit.bean.dto.SongDto;
import io.agora.uikit.bean.req.SongAddReq;
import io.agora.uikit.bean.req.SongPinReq;
import io.agora.uikit.bean.req.SongPlayReq;
import io.agora.uikit.bean.req.SongRemoveReq;
import io.agora.uikit.bean.req.SongStopReq;
import io.agora.uikit.service.ISongService;

@Validated
@RestController
@RequestMapping(value = "/v1/song", produces = MediaType.APPLICATION_JSON_VALUE)
public class SongController {
    @Autowired
    private ISongService songService;

    /**
     * Add song
     * 
     * @param songAddReq
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public R<SongDto> add(@Validated @RequestBody SongAddReq songAddReq) throws Exception {
        songService.add(songAddReq);
        return R.success(new SongDto().setRoomId(songAddReq.getRoomId()));
    }

    /**
     * Pin song
     * 
     * @param songPinReq
     * @return
     */
    @PostMapping("/pin")
    @ResponseBody
    public R<SongDto> pin(@Validated @RequestBody SongPinReq songPinReq) throws Exception {
        songService.pin(songPinReq);
        return R.success(new SongDto().setRoomId(songPinReq.getRoomId()));
    }

    /**
     * Remove song
     * 
     * @param songRemoveReq
     * @return
     */
    @PostMapping("/remove")
    @ResponseBody
    public R<SongDto> remove(@Validated @RequestBody SongRemoveReq songRemoveReq) throws Exception {
        songService.remove(songRemoveReq);
        return R.success(new SongDto().setRoomId(songRemoveReq.getRoomId()));
    }

    /**
     * Play song
     * 
     * @param songPlayReq
     * @return
     */
    @PostMapping("/play")
    @ResponseBody
    public R<SongDto> play(@Validated @RequestBody SongPlayReq songPlayReq) throws Exception {
        songService.play(songPlayReq);
        return R.success(new SongDto().setRoomId(songPlayReq.getRoomId()));
    }

    /**
     * Stop song
     * 
     * @param songStopReq
     * @return
     */
    @PostMapping("/stop")
    @ResponseBody
    public R<SongDto> stop(@Validated @RequestBody SongStopReq songStopReq) throws Exception {
        songService.stop(songStopReq);
        return R.success(new SongDto().setRoomId(songStopReq.getRoomId()));
    }
}