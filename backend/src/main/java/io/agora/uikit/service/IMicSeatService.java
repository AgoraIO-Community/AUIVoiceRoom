package io.agora.uikit.service;

import java.util.Map;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.MicSeatDomain;
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
import io.agora.uikit.bean.req.RoomCreateReq;

public interface IMicSeatService extends IService<RoomCreateReq> {
    /**
     * Check whether on mic seat no
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param userId
     * @throws Exception
     */
    void checkMicSeatNoAlreadyOn(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            String userId) throws Exception;

    /**
     * Check if the mic seat exists
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @param micSeatUserId
     * @throws Exception
     */
    void checkMicSeatNoNotExists(String method, Map<String, MicSeatDomain> micSeatMap, String roomId, Integer micSeatNo)
            throws Exception;

    /**
     * Check whether not on mic seat no
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param userId
     * @throws Exception
     */
    String checkMicSeatNoNotOn(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            String userId) throws Exception;

    /**
     * Check whether not on mic seat no
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @throws Exception
     */
    String checkMicSeatNoNotOn(String method, Metadata metadata, String roomId,
            String userId) throws Exception;

    /**
     * Check mic seat no status, idle
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @throws Exception
     */
    void checkMicSeatNoStatusIdle(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            Integer micSeatNo) throws Exception;

    /**
     * Check mic seat no status, not locked
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @throws Exception
     */
    void checkMicSeatNoStatusNotLocked(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            Integer micSeatNo) throws Exception;

    /**
     * Check mic seat no status, not idle
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @param micSeatUserId
     * @throws Exception
     */
    void checkMicSeatNoStatusNotIdle(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            Integer micSeatNo)
            throws Exception;

    /**
     * Enter
     * 
     * @param micSeatEnterReq
     * @throws Exception
     */
    void enter(MicSeatEnterReq micSeatEnterReq) throws Exception;

    /**
     * Get mic seat map
     * 
     * @param metadataItem
     * @return
     */
    Map<String, MicSeatDomain> getMicSeatMap(MetadataItem metadataItem);

    /**
     * Kick
     * 
     * @param micSeatKickReq
     * @throws Exception
     */
    void kick(MicSeatKickReq micSeatKickReq) throws Exception;

    /**
     * Leave
     * 
     * @param micSeatLeaveReq
     * @throws Exception
     */
    void leave(MicSeatLeaveReq micSeatLeaveReq) throws Exception;

    /**
     * Leave
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @throws Exception
     */
    void leave(String method, Metadata metadata, String roomId, String userId) throws Exception;

    /**
     * Lock
     * 
     * @param micSeatLockReq
     * @throws Exception
     */
    void lock(MicSeatLockReq micSeatLockReq) throws Exception;

    /**
     * Mute audio
     * 
     * @param micSeatAudioMuteReq
     * @throws Exception
     */
    void muteAudio(MicSeatAudioMuteReq micSeatAudioMuteReq) throws Exception;

    /**
     * Mute video
     * 
     * @param micSeatVideoMuteReq
     * @throws Exception
     */
    void muteVideo(MicSeatVideoMuteReq micSeatVideoMuteReq) throws Exception;

    /**
     * Pick
     * 
     * @param micSeatPickReq
     * @throws Exception
     */
    void pick(MicSeatPickReq micSeatPickReq) throws Exception;

    /**
     * Unlock
     * 
     * @param micSeatUnlockReq
     * @throws Exception
     */
    void unlock(MicSeatUnlockReq micSeatUnlockReq) throws Exception;

    /**
     * Unmute audio
     * 
     * @param micSeatAudioUnmuteReq
     * @throws Exception
     */
    void unmuteAudio(MicSeatAudioUnmuteReq micSeatAudioUnmuteReq) throws Exception;

    /**
     * Unmute video
     * 
     * @param micSeatVideoUnmuteReq
     * @throws Exception
     */
    void unmuteVideo(MicSeatVideoUnmuteReq micSeatVideoUnmuteReq) throws Exception;
}
