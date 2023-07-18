package io.agora.uikit.service.impl;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.MicSeatDomain;
import io.agora.uikit.bean.domain.MicSeatOwnerDomain;
import io.agora.uikit.bean.enums.MicSeatStatusEnum;
import io.agora.uikit.bean.enums.MuteAudioEnum;
import io.agora.uikit.bean.enums.MuteVideoEnum;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
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
import io.agora.uikit.service.IChorusService;
import io.agora.uikit.service.IMicSeatService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.service.ISongService;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MicSeatServiceImpl implements IMicSeatService {
    @Autowired
    private RtmUtil rtmUtil;
    @Autowired
    private IRoomService roomService;
    @Autowired
    @Lazy(true)
    private ISongService songService;
    @Autowired
    @Lazy(true)
    private IChorusService chorusService;

    // Metadata key
    public static final String METADATA_KEY = "micSeat";

    /**
     * Check whether on mic seat no
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param userId
     * @throws Exception
     */
    @Override
    public void checkMicSeatNoAlreadyOn(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            String userId) throws Exception {
        for (MicSeatDomain micSeatDomain : micSeatMap.values()) {
            if (Objects.equals(micSeatDomain.getOwner().getUserId(), userId)) {
                log.error("checkMicSeatNoAlreadyOn-{}, already on mic seat, roomId:{}, userId:{}", method, roomId,
                        userId);
                roomService.releaseLock(roomId);
                throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.MIC_SEAT_NUMBER_ALREADY_ON_ERROR);
            }
        }
    }

    /**
     * Check if the mic seat exists
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @throws Exception
     */
    @Override
    public void checkMicSeatNoNotExists(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            Integer micSeatNo) throws Exception {
        MicSeatDomain micSeatDomain = micSeatMap.get(micSeatNo.toString());
        if (micSeatDomain == null) {
            log.error("checkMicSeatNoNotExists-{}, mic seat number not exists, roomId:{}, micSeatNo:{}", method, roomId,
                    micSeatNo);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_EXISTS_ERROR);
        }
    }

    /**
     * Check whether not on mic seat no
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param userId
     * @throws Exception
     */
    @Override
    public String checkMicSeatNoNotOn(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            String userId) throws Exception {
        String micSeatNo = null;
        for (MicSeatDomain micSeatDomain : micSeatMap.values()) {
            if (Objects.equals(micSeatDomain.getOwner().getUserId(), userId)) {
                micSeatNo = micSeatDomain.getMicSeatNo().toString();
                break;
            }
        }

        if (micSeatNo == null) {
            log.error("checkMicSeatNoNotOn-{}, not on mic seat, roomId:{}, userId:{}", method, roomId, userId);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_ON_ERROR);
        }

        return micSeatNo;
    }

    /**
     * Check whether not on mic seat no
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @throws Exception
     */
    @Override
    public String checkMicSeatNoNotOn(String method, Metadata metadata, String roomId,
            String userId) throws Exception {
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        return checkMicSeatNoNotOn(method, micSeatMap, roomId, userId);
    }

    /**
     * Check mic seat no status, idle
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @throws Exception
     */
    @Override
    public void checkMicSeatNoStatusIdle(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            Integer micSeatNo) throws Exception {
        MicSeatDomain micSeatDomain = micSeatMap.get(micSeatNo.toString());
        if (Objects.equals(micSeatDomain.getMicSeatStatus(), MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE)) {
            log.error("checkMicSeatNoStatusIdle-{}, mic seat number idle, roomId:{}, micSeatNo:{}", method, roomId,
                    micSeatNo);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.MIC_SEAT_NUMBER_IDLE_ERROR);
        }
    }

    /**
     * Check mic seat no status, not idle
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @throws Exception
     */
    @Override
    public void checkMicSeatNoStatusNotIdle(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            Integer micSeatNo) throws Exception {
        MicSeatDomain micSeatDomain = micSeatMap.get(micSeatNo.toString());
        if (!Objects.equals(micSeatDomain.getMicSeatStatus(), MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE)) {
            log.error("checkMicSeatNoStatusNotIdle-{}, mic seat number not idle, roomId:{}, micSeatNo:{}", method,
                    roomId, micSeatNo);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_IDLE_ERROR);
        }
    }

    /**
     * Check mic seat no status, not locked
     * 
     * @param method
     * @param micSeatMap
     * @param roomId
     * @param micSeatNo
     * @throws Exception
     */
    @Override
    public void checkMicSeatNoStatusNotLocked(String method, Map<String, MicSeatDomain> micSeatMap, String roomId,
            Integer micSeatNo) throws Exception {
        MicSeatDomain micSeatDomain = micSeatMap.get(micSeatNo.toString());
        if (!Objects.equals(micSeatDomain.getMicSeatStatus(), MicSeatStatusEnum.MIC_SEAT_STATUS_LOCKED)) {
            log.error("checkMicSeatNoStatusNotLocked-{}, mic seat number not locked, roomId:{}, micSeatNo:{}", method,
                    roomId, micSeatNo);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.MIC_SEAT_NUMBER_NOT_LOCKED_ERROR);
        }
    }

    /**
     * Create metadata
     * 
     * @param metadata
     * @param roomReq
     */
    @Override
    public void createMetadata(Metadata metadata, RoomCreateReq roomReq) {
        JSONObject micSeatMap = new JSONObject();

        for (Integer i = 0; i < roomReq.getMicSeatCount(); i++) {
            MicSeatDomain micSeatDomain = new MicSeatDomain();
            MicSeatOwnerDomain micSeatOwnerDomain = new MicSeatOwnerDomain();
            micSeatOwnerDomain.setUserId("")
                    .setUserName("")
                    .setUserAvatar("");
            micSeatDomain.setMicSeatNo(i)
                    .setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE)
                    .setIsMuteAudio(MuteAudioEnum.MUTE_AUDIO_NO)
                    .setIsMuteVideo(MuteVideoEnum.MUTE_VIDEO_NO);

            // Owner automatically assigned to mic seat
            if (i == 0) {
                micSeatOwnerDomain.setUserId(roomReq.getUserId())
                        .setUserName(roomReq.getUserName())
                        .setUserAvatar(roomReq.getUserAvatar());
                micSeatDomain.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_USED);
            }

            micSeatDomain.setOwner(micSeatOwnerDomain);
            micSeatMap.put(i.toString(), micSeatDomain);
        }

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = METADATA_KEY;
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);
    }

    /**
     * Enter
     * 
     * @param micSeatEnterReq
     * @throws Exception
     */
    @Override
    public void enter(MicSeatEnterReq micSeatEnterReq) throws Exception {
        log.info("enter, start, micSeatEnterReq:{}", micSeatEnterReq);

        // Acquire lock
        roomService.acquireLock(micSeatEnterReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatEnterReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("enter", metadataItem, micSeatEnterReq.getRoomId(), micSeatEnterReq);
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check whether on mic seat no
        checkMicSeatNoAlreadyOn("enter", micSeatMap, micSeatEnterReq.getRoomId(), micSeatEnterReq.getUserId());
        // Check if the mic seat exists
        checkMicSeatNoNotExists("enter", micSeatMap, micSeatEnterReq.getRoomId(), micSeatEnterReq.getMicSeatNo());
        // Check mic seat no status, not idle
        checkMicSeatNoStatusNotIdle("enter", micSeatMap, micSeatEnterReq.getRoomId(), micSeatEnterReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatEnterReq.getMicSeatNo().toString());
        MicSeatOwnerDomain micSeatOwnerDomain = new MicSeatOwnerDomain();
        micSeatOwnerDomain.setUserId(micSeatEnterReq.getUserId())
                .setUserName(micSeatEnterReq.getUserName())
                .setUserAvatar(micSeatEnterReq.getUserAvatar());
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_USED)
                .setOwner(micSeatOwnerDomain);
        micSeatMap.put(micSeatEnterReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("enter", micSeatEnterReq.getRoomId(), metadata, metadataItem,
                micSeatEnterReq.getRoomId(), micSeatEnterReq);

        // Release lock
        roomService.releaseLock(micSeatEnterReq.getRoomId());
        log.info("enter, success, micSeatEnterReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatEnterReq, micSeatMap, metadataItem.value);
    }

    /**
     * Get mic seat map
     * 
     * @param metadataItem
     * @return
     */
    public Map<String, MicSeatDomain> getMicSeatMap(MetadataItem metadataItem) {
        return JSON.parseObject(metadataItem.value,
                new TypeReference<Map<String, MicSeatDomain>>() {
                });
    }

    /**
     * Kick
     * 
     * @param micSeatKickReq
     * @throws Exception
     */
    @Override
    public void kick(MicSeatKickReq micSeatKickReq) throws Exception {
        log.info("kick, start, micSeatKickReq:{}", micSeatKickReq);

        // Acquire lock
        roomService.acquireLock(micSeatKickReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatKickReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("kick", metadataItem, micSeatKickReq.getRoomId(), micSeatKickReq);
        // Check whether owner
        roomService.checkIsOwner("kick", metadata, micSeatKickReq.getRoomId(), micSeatKickReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("kick", micSeatMap, micSeatKickReq.getRoomId(), micSeatKickReq.getMicSeatNo());
        // Check mic seat no status, idle
        checkMicSeatNoStatusIdle("kick", micSeatMap, micSeatKickReq.getRoomId(), micSeatKickReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatKickReq.getMicSeatNo().toString());
        String ownerUserid = micSeatMapModify.getOwner().getUserId();
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE)
                .setOwner(new MicSeatOwnerDomain());
        micSeatMap.put(micSeatKickReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("kick", micSeatKickReq.getRoomId(), metadata, metadataItem,
                micSeatKickReq.getRoomId(), micSeatKickReq);

        // Clear song by user
        songService.clearByUser("kick", metadata, micSeatKickReq.getRoomId(), ownerUserid);
        // Clear chorus by user
        chorusService.clearByUser("kick", metadata, micSeatKickReq.getRoomId(), ownerUserid);

        // Release lock
        roomService.releaseLock(micSeatKickReq.getRoomId());
        log.info("kick, success, micSeatKickReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatKickReq, micSeatMap, metadataItem.value);
    }

    /**
     * Leave
     * 
     * @param micSeatLeaveReq
     * @throws Exception
     */
    @Override
    public void leave(MicSeatLeaveReq micSeatLeaveReq) throws Exception {
        log.info("leave, start, micSeatLeaveReq:{}", micSeatLeaveReq);

        // Acquire lock
        roomService.acquireLock(micSeatLeaveReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatLeaveReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("leave", metadataItem, micSeatLeaveReq.getRoomId(), micSeatLeaveReq);
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check whether not on mic seat no
        String micSeatNo = checkMicSeatNoNotOn("leave", micSeatMap, micSeatLeaveReq.getRoomId(),
                micSeatLeaveReq.getUserId());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatNo);
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE)
                .setOwner(new MicSeatOwnerDomain());
        micSeatMap.put(micSeatNo, micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("leave", micSeatLeaveReq.getRoomId(), metadata, metadataItem,
                micSeatLeaveReq.getRoomId(), micSeatLeaveReq);

        // Clear song by user
        songService.clearByUser("leave", metadata, micSeatLeaveReq.getRoomId(), micSeatLeaveReq.getUserId());
        // Clear chorus by user
        chorusService.clearByUser("leave", metadata, micSeatLeaveReq.getRoomId(), micSeatLeaveReq.getUserId());

        // Release lock
        roomService.releaseLock(micSeatLeaveReq.getRoomId());
        log.info("leave, success, micSeatLeaveReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatLeaveReq, micSeatMap, metadataItem.value);
    }

    /**
     * Leave
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @throws Exception
     */
    @Override
    public void leave(String method, Metadata metadata, String roomId, String userId) throws Exception {
        log.info("leave-{}, start, roomId:{}, userId:{}", method, roomId, userId);

        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("leave", metadataItem, roomId, null);
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Find mic seat on
        String micSeatNo = null;
        for (MicSeatDomain micSeatDomain : micSeatMap.values()) {
            if (Objects.equals(micSeatDomain.getOwner().getUserId(), userId)) {
                micSeatNo = micSeatDomain.getMicSeatNo().toString();
                break;
            }
        }
        if (micSeatNo == null) {
            log.info("leave-{}, micSeatNo null, roomId:{}, userId:{}, micSeatNo:{}", method, roomId, userId, micSeatNo);
            return;
        }

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatNo);
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE)
                .setOwner(new MicSeatOwnerDomain());
        micSeatMap.put(micSeatNo, micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("leave", roomId, metadata, metadataItem, roomId, null);
        log.info("leave-{}, success, roomId:{}, userId:{}, metadataItem.value:{}", method, roomId, userId,
                metadataItem.value);
    }

    /**
     * Lock
     * 
     * @param micSeatLockReq
     * @throws Exception
     */
    @Override
    public void lock(MicSeatLockReq micSeatLockReq) throws Exception {
        log.info("lock, start, micSeatLockReq:{}", micSeatLockReq);

        // Acquire lock
        roomService.acquireLock(micSeatLockReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatLockReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("lock", metadataItem, micSeatLockReq.getRoomId(), micSeatLockReq);
        // Check whether owner
        roomService.checkIsOwner("lock", metadata, micSeatLockReq.getRoomId(), micSeatLockReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("lock", micSeatMap, micSeatLockReq.getRoomId(),
                micSeatLockReq.getMicSeatNo());
        // Check mic seat no status, not idle
        checkMicSeatNoStatusNotIdle("lock", micSeatMap, micSeatLockReq.getRoomId(), micSeatLockReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatLockReq.getMicSeatNo().toString());
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_LOCKED);
        micSeatMap.put(micSeatLockReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("lock", micSeatLockReq.getRoomId(), metadata, metadataItem,
                micSeatLockReq.getRoomId(), micSeatLockReq);

        // Release lock
        roomService.releaseLock(micSeatLockReq.getRoomId());
        log.info(
                "lock, success, micSeatLockReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatLockReq, micSeatMap, metadataItem.value);
    }

    /**
     * Mute audio
     * 
     * @param micSeatAudioMuteReq
     * @throws Exception
     */
    @Override
    public void muteAudio(MicSeatAudioMuteReq micSeatAudioMuteReq) throws Exception {
        log.info("muteAudio, start, micSeatAudioMuteReq:{}", micSeatAudioMuteReq);

        // Acquire lock
        roomService.acquireLock(micSeatAudioMuteReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatAudioMuteReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("muteAudio", metadataItem, micSeatAudioMuteReq.getRoomId(), micSeatAudioMuteReq);
        // Check whether owner
        roomService.checkIsOwner("muteAudio", metadata, micSeatAudioMuteReq.getRoomId(),
                micSeatAudioMuteReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("muteAudio", micSeatMap, micSeatAudioMuteReq.getRoomId(),
                micSeatAudioMuteReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatAudioMuteReq.getMicSeatNo().toString());
        micSeatMapModify.setIsMuteAudio(MuteAudioEnum.MUTE_AUDIO_YES);
        micSeatMap.put(micSeatAudioMuteReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("muteAudio", micSeatAudioMuteReq.getRoomId(), metadata, metadataItem,
                micSeatAudioMuteReq.getRoomId(), micSeatAudioMuteReq);

        // Release lock
        roomService.releaseLock(micSeatAudioMuteReq.getRoomId());
        log.info(
                "muteAudio, success, micSeatAudioMuteReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatAudioMuteReq, micSeatMap, metadataItem.value);
    }

    /**
     * Mute video
     * 
     * @param micSeatVideoMuteReq
     * @throws Exception
     */
    @Override
    public void muteVideo(MicSeatVideoMuteReq micSeatVideoMuteReq) throws Exception {
        log.info("muteVideo, start, micSeatVideoMuteReq:{}", micSeatVideoMuteReq);

        // Acquire lock
        roomService.acquireLock(micSeatVideoMuteReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatVideoMuteReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("muteVideo", metadataItem, micSeatVideoMuteReq.getRoomId(), micSeatVideoMuteReq);
        // Check whether owner
        roomService.checkIsOwner("muteVideo", metadata, micSeatVideoMuteReq.getRoomId(),
                micSeatVideoMuteReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("muteVideo", micSeatMap, micSeatVideoMuteReq.getRoomId(),
                micSeatVideoMuteReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatVideoMuteReq.getMicSeatNo().toString());
        micSeatMapModify.setIsMuteVideo(MuteVideoEnum.MUTE_VIDEO_YES);
        micSeatMap.put(micSeatVideoMuteReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("muteVideo", micSeatVideoMuteReq.getRoomId(), metadata, metadataItem,
                micSeatVideoMuteReq.getRoomId(), micSeatVideoMuteReq);

        // Release lock
        roomService.releaseLock(micSeatVideoMuteReq.getRoomId());
        log.info(
                "muteVideo, success, micSeatVideoMuteReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatVideoMuteReq, micSeatMap, metadataItem.value);
    }

    /**
     * Pick
     * 
     * @param micSeatPickReq
     * @throws Exception
     */
    @Override
    public void pick(MicSeatPickReq micSeatPickReq) throws Exception {
        log.info("pick, start, micSeatPickReq:{}", micSeatPickReq);

        // Acquire lock
        roomService.acquireLock(micSeatPickReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatPickReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("pick", metadataItem, micSeatPickReq.getRoomId(), micSeatPickReq);
        // Check whether owner
        roomService.checkIsOwner("pick", metadata, micSeatPickReq.getRoomId(),
                micSeatPickReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("pick", micSeatMap, micSeatPickReq.getRoomId(), micSeatPickReq.getMicSeatNo());
        // Check mic seat no status, not idle
        checkMicSeatNoStatusNotIdle("pick", micSeatMap, micSeatPickReq.getRoomId(), micSeatPickReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatPickReq.getMicSeatNo().toString());
        MicSeatOwnerDomain micSeatOwnerDomain = new MicSeatOwnerDomain();
        micSeatOwnerDomain.setUserId(micSeatPickReq.getOwner().getUserId())
                .setUserName(micSeatPickReq.getOwner().getUserName())
                .setUserAvatar(micSeatPickReq.getOwner().getUserAvatar());
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_USED)
                .setOwner(micSeatOwnerDomain);
        micSeatMap.put(micSeatPickReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("pick", micSeatPickReq.getRoomId(), metadata, metadataItem,
                micSeatPickReq.getRoomId(), micSeatPickReq);

        // Release lock
        roomService.releaseLock(micSeatPickReq.getRoomId());
        log.info("pick, success, micSeatPickReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatPickReq, micSeatMap, metadataItem.value);
    }

    /**
     * Unlock
     * 
     * @param micSeatUnlockReq
     * @throws Exception
     */
    @Override
    public void unlock(MicSeatUnlockReq micSeatUnlockReq) throws Exception {
        log.info("unlock, start, micSeatUnlockReq:{}", micSeatUnlockReq);

        // Acquire lock
        roomService.acquireLock(micSeatUnlockReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatUnlockReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("unlock", metadataItem, micSeatUnlockReq.getRoomId(), micSeatUnlockReq);
        // Check whether owner
        roomService.checkIsOwner("unlock", metadata, micSeatUnlockReq.getRoomId(), micSeatUnlockReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("unlock", micSeatMap, micSeatUnlockReq.getRoomId(),
                micSeatUnlockReq.getMicSeatNo());
        // Check mic seat no status, not locked
        checkMicSeatNoStatusNotLocked("unlock", micSeatMap, micSeatUnlockReq.getRoomId(),
                micSeatUnlockReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatUnlockReq.getMicSeatNo().toString());
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_IDLE);
        micSeatMap.put(micSeatUnlockReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("unlock", micSeatUnlockReq.getRoomId(), metadata, metadataItem,
                micSeatUnlockReq.getRoomId(), micSeatUnlockReq);

        // Release lock
        roomService.releaseLock(micSeatUnlockReq.getRoomId());
        log.info(
                "unlock, success, micSeatUnlockReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatUnlockReq, micSeatMap, metadataItem.value);
    }

    /**
     * Unmute audio
     * 
     * @param micSeatAudioUnmuteReq
     * @throws Exception
     */
    @Override
    public void unmuteAudio(MicSeatAudioUnmuteReq micSeatAudioUnmuteReq) throws Exception {
        log.info("unmuteAudio, start, micSeatAudioUnmuteReq:{}", micSeatAudioUnmuteReq);

        // Acquire lock
        roomService.acquireLock(micSeatAudioUnmuteReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatAudioUnmuteReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("unmuteAudio", metadataItem, micSeatAudioUnmuteReq.getRoomId(),
                micSeatAudioUnmuteReq);
        // Check whether owner
        roomService.checkIsOwner("unmuteAudio", metadata, micSeatAudioUnmuteReq.getRoomId(),
                micSeatAudioUnmuteReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("unmuteAudio", micSeatMap, micSeatAudioUnmuteReq.getRoomId(),
                micSeatAudioUnmuteReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatAudioUnmuteReq.getMicSeatNo().toString());
        micSeatMapModify.setIsMuteAudio(MuteAudioEnum.MUTE_AUDIO_NO);
        micSeatMap.put(micSeatAudioUnmuteReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("unmuteAudio", micSeatAudioUnmuteReq.getRoomId(), metadata, metadataItem,
                micSeatAudioUnmuteReq.getRoomId(), micSeatAudioUnmuteReq);

        // Release lock
        roomService.releaseLock(micSeatAudioUnmuteReq.getRoomId());
        log.info(
                "unmuteAudio, success, micSeatAudioUnmuteReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatAudioUnmuteReq, micSeatMap, metadataItem.value);
    }

    /**
     * Unmute video
     * 
     * @param micSeatVideoUnmuteReq
     * @throws Exception
     */
    @Override
    public void unmuteVideo(MicSeatVideoUnmuteReq micSeatVideoUnmuteReq) throws Exception {
        log.info("unmuteVideo, start, micSeatVideoUnmuteReq:{}", micSeatVideoUnmuteReq);

        // Acquire lock
        roomService.acquireLock(micSeatVideoUnmuteReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(micSeatVideoUnmuteReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("unmuteVideo", metadataItem, micSeatVideoUnmuteReq.getRoomId(),
                micSeatVideoUnmuteReq);
        // Check whether owner
        roomService.checkIsOwner("unmuteVideo", metadata, micSeatVideoUnmuteReq.getRoomId(),
                micSeatVideoUnmuteReq.getUserId());
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = getMicSeatMap(metadataItem);
        // Check if the mic seat exists
        checkMicSeatNoNotExists("unmuteVideo", micSeatMap, micSeatVideoUnmuteReq.getRoomId(),
                micSeatVideoUnmuteReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatVideoUnmuteReq.getMicSeatNo().toString());
        micSeatMapModify.setIsMuteVideo(MuteVideoEnum.MUTE_VIDEO_NO);
        micSeatMap.put(micSeatVideoUnmuteReq.getMicSeatNo().toString(), micSeatMapModify);
        metadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("unmuteVideo", micSeatVideoUnmuteReq.getRoomId(), metadata, metadataItem,
                micSeatVideoUnmuteReq.getRoomId(), micSeatVideoUnmuteReq);

        // Release lock
        roomService.releaseLock(micSeatVideoUnmuteReq.getRoomId());
        log.info(
                "unmuteVideo, success, micSeatVideoUnmuteReq:{}, micSeatMap:{}, metadataItem.value:{}",
                micSeatVideoUnmuteReq, micSeatMap, metadataItem.value);
    }
}
