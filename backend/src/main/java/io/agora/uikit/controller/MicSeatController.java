package io.agora.uikit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.agora.uikit.bean.dto.MicSeatDto;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.req.MicSeatAudioMuteReq;
import io.agora.uikit.bean.req.MicSeatAudioUnmuteReq;
import io.agora.uikit.bean.req.MicSeatEnterReq;
import io.agora.uikit.bean.req.MicSeatKickReq;
import io.agora.uikit.bean.req.MicSeatLeaveReq;
import io.agora.uikit.bean.req.MicSeatLockReq;
import io.agora.uikit.bean.req.MicSeatPickReq;
import io.agora.uikit.bean.req.MicSeatUnlockReq;
import io.agora.uikit.bean.req.MicSeatVideoMuteReq;
import io.agora.uikit.bean.req.MicSeatVideoUnmuteReq;
import io.agora.uikit.service.IMicSeatService;

@Validated
@RestController
@RequestMapping(value = "/v1/seat", produces = MediaType.APPLICATION_JSON_VALUE)
public class MicSeatController {
    @Autowired
    private IMicSeatService micSeatService;

    /**
     * Enter
     * 
     * @param MicSeatEnterReq
     * @return
     * @throws Exception
     */
    @PostMapping("/enter")
    @ResponseBody
    public R<MicSeatDto> enter(@Validated @RequestBody MicSeatEnterReq MicSeatEnterReq) throws Exception {
        micSeatService.enter(MicSeatEnterReq);
        return R.success(new MicSeatDto().setRoomId(MicSeatEnterReq.getRoomId())
                .setMicSeatNo(MicSeatEnterReq.getMicSeatNo()));
    }

    /**
     * Kick
     * 
     * @param micSeatKickReq
     * @return
     * @throws Exception
     */
    @PostMapping("/kick")
    @ResponseBody
    public R<MicSeatDto> kick(@Validated @RequestBody MicSeatKickReq micSeatKickReq) throws Exception {
        micSeatService.kick(micSeatKickReq);
        return R.success(new MicSeatDto().setRoomId(micSeatKickReq.getRoomId())
                .setMicSeatNo(micSeatKickReq.getMicSeatNo()));
    }

    /**
     * Leave
     * 
     * @param micSeatLeaveReq
     * @return
     * @throws Exception
     */
    @PostMapping("/leave")
    @ResponseBody
    public R<MicSeatDto> leave(@Validated @RequestBody MicSeatLeaveReq micSeatLeaveReq) throws Exception {
        micSeatService.leave(micSeatLeaveReq);
        return R.success(new MicSeatDto().setRoomId(micSeatLeaveReq.getRoomId()));
    }

    /**
     * Lock
     * 
     * @param micSeatLockReq
     * @return
     * @throws Exception
     */
    @PostMapping("/lock")
    @ResponseBody
    public R<MicSeatDto> lock(@Validated @RequestBody MicSeatLockReq micSeatLockReq) throws Exception {
        micSeatService.lock(micSeatLockReq);
        return R.success(new MicSeatDto().setRoomId(micSeatLockReq.getRoomId())
                .setMicSeatNo(micSeatLockReq.getMicSeatNo()));
    }

    /**
     * Mute audio
     * 
     * @param micSeatAudioMuteReq
     * @return
     * @throws Exception
     */
    @PostMapping("/audio/mute")
    @ResponseBody
    public R<MicSeatDto> muteAudio(@Validated @RequestBody MicSeatAudioMuteReq micSeatAudioMuteReq) throws Exception {
        micSeatService.muteAudio(micSeatAudioMuteReq);
        return R.success(new MicSeatDto().setRoomId(micSeatAudioMuteReq.getRoomId())
                .setMicSeatNo(micSeatAudioMuteReq.getMicSeatNo()));
    }

    /**
     * Mute video
     * 
     * @param micSeatVideoMuteReq
     * @return
     * @throws Exception
     */
    @PostMapping("/video/mute")
    @ResponseBody
    public R<MicSeatDto> muteVideo(@Validated @RequestBody MicSeatVideoMuteReq micSeatVideoMuteReq) throws Exception {
        micSeatService.muteVideo(micSeatVideoMuteReq);
        return R.success(new MicSeatDto().setRoomId(micSeatVideoMuteReq.getRoomId())
                .setMicSeatNo(micSeatVideoMuteReq.getMicSeatNo()));
    }

    /**
     * Pick
     * 
     * @param micSeatPickReq
     * @return
     * @throws Exception
     */
    @PostMapping("/pick")
    @ResponseBody
    public R<MicSeatDto> pick(@Validated @RequestBody MicSeatPickReq micSeatPickReq) throws Exception {
        micSeatService.pick(micSeatPickReq);
        return R.success(new MicSeatDto().setRoomId(micSeatPickReq.getRoomId())
                .setMicSeatNo(micSeatPickReq.getMicSeatNo()));
    }

    /**
     * Unlock
     * 
     * @param micSeatUnlockReq
     * @return
     * @throws Exception
     */
    @PostMapping("/unlock")
    @ResponseBody
    public R<MicSeatDto> unlock(@Validated @RequestBody MicSeatUnlockReq micSeatUnlockReq) throws Exception {
        micSeatService.unlock(micSeatUnlockReq);
        return R.success(new MicSeatDto().setRoomId(micSeatUnlockReq.getRoomId())
                .setMicSeatNo(micSeatUnlockReq.getMicSeatNo()));
    }

    /**
     * Unmute audio
     * 
     * @param micSeatAudioUnmuteReq
     * @return
     * @throws Exception
     */
    @PostMapping("/audio/unmute")
    @ResponseBody
    public R<MicSeatDto> UnmuteAudio(@Validated @RequestBody MicSeatAudioUnmuteReq micSeatAudioUnmuteReq)
            throws Exception {
        micSeatService.unmuteAudio(micSeatAudioUnmuteReq);
        return R.success(new MicSeatDto().setRoomId(micSeatAudioUnmuteReq.getRoomId())
                .setMicSeatNo(micSeatAudioUnmuteReq.getMicSeatNo()));
    }

    /**
     * Unmute video
     * 
     * @param micSeatVideoUnmuteReq
     * @return
     * @throws Exception
     */
    @PostMapping("/video/unmute")
    @ResponseBody
    public R<MicSeatDto> unmuteVideo(@Validated @RequestBody MicSeatVideoUnmuteReq micSeatVideoUnmuteReq)
            throws Exception {
        micSeatService.unmuteVideo(micSeatVideoUnmuteReq);
        return R.success(new MicSeatDto().setRoomId(micSeatVideoUnmuteReq.getRoomId())
                .setMicSeatNo(micSeatVideoUnmuteReq.getMicSeatNo()));
    }
}