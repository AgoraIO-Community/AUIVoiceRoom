package io.agora.uikit.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.ChorusDomain;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.ChorusJoinReq;
import io.agora.uikit.bean.req.ChorusLeaveReq;
import io.agora.uikit.service.IChorusService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.service.ISongService;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChorusServiceImpl implements IChorusService {
    @Autowired
    private RtmUtil rtmUtil;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private ISongService songService;

    // Metadata key
    public static final String METADATA_KEY = "chorus";

    /**
     * Check whether joined
     * 
     * @param method
     * @param chorusList
     * @param roomId
     * @param userId
     * @throws Exception
     */
    @Override
    public void checkAlreadyJoined(String method, List<ChorusDomain> chorusList, String roomId, String userId)
            throws Exception {
        if (chorusList.stream().anyMatch(o -> o.getUserId().equals(userId))) {
            log.error("checkAlreadyJoined-{}, already joined, roomId:{}, userId:{}", method, roomId, userId);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.CHORUS_ALREADY_JOINED_ERROR);
        }
    }

    /**
     * Clear
     * 
     * @param metadata
     */
    @Override
    public void clear(Metadata metadata) {
        log.info("clear");

        createMetadata(metadata);
    }

    /**
     * Clear chorus by user
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @return
     */
    @Override
    public void clearByUser(String method, Metadata metadata, String roomId, String userId) throws Exception {
        log.info("clearByUser-{}, start, roomId:{}, userId:{}", method, roomId, userId);

        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        if (metadataItem == null) {
            return;
        }

        // Get data
        List<ChorusDomain> chorusList = JSON.parseArray(metadataItem.value, ChorusDomain.class);
        // Remove chorus
        List<ChorusDomain> chorusListNew = chorusList.stream()
                .filter(o -> !Objects.equals(o.getUserId(), userId))
                .collect(Collectors.toList());

        metadataItem.value = JSON.toJSONString(chorusListNew);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("clearByUser", roomId, metadata, metadataItem, roomId, null);
        log.info("clearByUser-{}, success, roomId:{}, userId:{}", method, roomId, userId);
    }

    /**
     * Create metadata
     * 
     * @param metadata
     * @param chorusJoinReq
     */
    @Override
    public void createMetadata(Metadata metadata, ChorusJoinReq chorusJoinReq) {
        log.info("createMetadata, chorusJoinReq:{}", chorusJoinReq);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = METADATA_KEY;
        metadataItem.value = JSON.toJSONString(new ArrayList<>());
        metadata.setMetadataItem(metadataItem);
    }

    /**
     * Create metadata
     * 
     * @param metadata
     */
    @Override
    public void createMetadata(Metadata metadata) {
        log.info("createMetadata");

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = METADATA_KEY;
        metadataItem.value = JSON.toJSONString(new ArrayList<>());
        metadata.setMetadataItem(metadataItem);
    }

    /**
     * Join
     * 
     * @param chorusJoinReq
     * @throws Exception
     */
    @Override
    public void join(ChorusJoinReq chorusJoinReq) throws Exception {
        log.info("join, start, chorusJoinReq:{}", chorusJoinReq);

        // Acquire lock
        roomService.acquireLock(chorusJoinReq.getRoomId());
        // Get data
        Metadata metadata = rtmUtil.getChannelMetadata(chorusJoinReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("join", metadataItem, chorusJoinReq.getRoomId(), chorusJoinReq);

        // Get list
        List<ChorusDomain> chorusList = JSON.parseArray(metadataItem.value, ChorusDomain.class);
        // Check whether joined
        checkAlreadyJoined("join", chorusList, chorusJoinReq.getRoomId(), chorusJoinReq.getUserId());
        // Check song status
        songService.checkSongStatusNotPlaying("join", metadata, chorusJoinReq.getRoomId(), chorusJoinReq.getSongCode());

        // Set data
        ChorusDomain chorusDomain = new ChorusDomain();
        chorusDomain.setUserId(chorusJoinReq.getUserId());
        chorusList.add(chorusDomain);
        metadataItem.value = JSON.toJSONString(chorusList);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("join", chorusJoinReq.getRoomId(), metadata, metadataItem,
                chorusJoinReq.getRoomId(), chorusJoinReq);

        // Release lock
        roomService.releaseLock(chorusJoinReq.getRoomId());
        log.info("join, success, chorusJoinReq:{}, metadataItem.value:{}", chorusJoinReq, metadataItem.value);
    }

    /**
     * Leave
     * 
     * @param chorusLeaveReq
     * @throws Exception
     */
    @Override
    public void leave(ChorusLeaveReq chorusLeaveReq) throws Exception {
        log.info("leave, chorusLeaveReq:{}", chorusLeaveReq);

        // Acquire lock
        roomService.acquireLock(chorusLeaveReq.getRoomId());
        // Get data
        Metadata metadata = rtmUtil.getChannelMetadata(chorusLeaveReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("leave", metadataItem, chorusLeaveReq.getRoomId(), chorusLeaveReq);

        // Get list
        List<ChorusDomain> chorusList = JSON.parseArray(metadataItem.value, ChorusDomain.class);
        // Remove
        List<ChorusDomain> chorusListNew = chorusList.stream()
                .filter(o -> !Objects.equals(o.getUserId(), chorusLeaveReq.getUserId()))
                .collect(Collectors.toList());
        // Set data
        metadataItem.value = JSON.toJSONString(chorusListNew);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("leave", chorusLeaveReq.getRoomId(), metadata, metadataItem,
                chorusLeaveReq.getRoomId(), chorusLeaveReq);

        // Release lock
        roomService.releaseLock(chorusLeaveReq.getRoomId());
        log.info("leave, success, chorusLeaveReq:{}, metadataItem.value:{}", chorusLeaveReq, metadataItem.value);
    }
}
