package io.agora.uikit.service.impl;

import com.alibaba.fastjson2.JSON;
import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.RoomInfoDomain;
import io.agora.uikit.bean.domain.RoomInfoOwnerDomain;
import io.agora.uikit.bean.dto.RoomListDto;
import io.agora.uikit.bean.dto.RoomQueryDto;
import io.agora.uikit.bean.entity.RoomListEntity;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.*;
import io.agora.uikit.repository.RoomListRepository;
import io.agora.uikit.service.IChorusService;
import io.agora.uikit.service.IMicSeatService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.service.ISongService;
import io.agora.uikit.utils.RedisUtil;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class RoomServiceImpl implements IRoomService {
    @Autowired
    private RtmUtil rtmUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Lazy(true)
    private IMicSeatService micSeatService;
    @Autowired
    @Lazy(true)
    private ISongService songService;
    @Autowired
    @Lazy(true)
    private IChorusService chorusService;
    @Autowired
    @Lazy(true)
    private RoomListRepository roomListRepository;

    // Metadata key
    public static final String METADATA_KEY = "roomInfo";
    private long TRY_LOCK_TIMEOUT_SECOND = 10;

    /**
     * Acquire lock
     *
     * @param roomId
     * @throws Exception
     */
    @Override
    public void acquireLock(String roomId) throws Exception {
        String lockName = getLockName(roomId);
        log.info("acquireLock, roomId:{}, lockName:{}", roomId, lockName);

        if (!redisUtil.tryLock(lockName, TRY_LOCK_TIMEOUT_SECOND)) {
            log.error("acquireLock, failed, roomId:{}, lockName:{}, TRY_LOCK_TIMEOUT_SECOND:{}", roomId, lockName,
                    TRY_LOCK_TIMEOUT_SECOND);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_ACQUIRE_LOCK_ERROR);
        }
    }

    /**
     * Add room list
     *
     * @param roomCreateReq
     * @return
     */
    @Override
    public void addRoomList(RoomCreateReq roomCreateReq) throws Exception {
        // Set data
        RoomListEntity roomListEntity = new RoomListEntity();
        RoomInfoOwnerDomain roomInfoOwnerDomain = new RoomInfoOwnerDomain();
        roomInfoOwnerDomain.setUserId(roomCreateReq.getUserId())
                .setUserName(roomCreateReq.getUserName())
                .setUserAvatar(roomCreateReq.getUserAvatar());
        roomListEntity.setRoomId(roomCreateReq.getRoomId())
                .setRoomName(roomCreateReq.getRoomName())
                .setRoomOwner(roomInfoOwnerDomain)
                .setOnlineUsers(1L)
                .setMicSeatCount(roomCreateReq.getMicSeatCount())
                .setMicSeatStyle(roomCreateReq.getMicSeatStyle())
                .setCreateTime(System.currentTimeMillis());
        // Insert data
        roomListRepository.insert(roomListEntity);

        log.info("addRoomList, roomCreateReq:{}, roomListEntity:{}", roomCreateReq, roomListEntity);
    }

    /**
     * Check whether owner
     *
     * @param method
     * @param roomId
     * @param userId
     * @throws Exception
     */
    @Override
    public void checkIsOwner(String method, Metadata metadata, String roomId, String userId) throws Exception {
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        if (metadataItem == null) {
            releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }
        RoomInfoDomain roomInfoDomain = JSON.parseObject(metadataItem.value, RoomInfoDomain.class);
        if (!Objects.equals(roomInfoDomain.getRoomOwner().getUserId(), userId)) {
            log.error("checkIsOwner-{}, not room owner, roomId:{}, ownerUserId:{}, userId:{}", method, roomId,
                    roomInfoDomain.getRoomOwner().getUserId(), userId);
            releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_OWNER_ERROR);
        }
    }

    /**
     * Check metadata
     *
     * @param method
     * @param metadataItem
     * @param roomId
     * @param obj
     * @throws Exception
     */
    @Override
    public void checkMetadata(String method, MetadataItem metadataItem, String roomId, Object obj) throws Exception {
        // Check data
        if (metadataItem == null) {
            log.error("checkMetadata-{}, metadata no data, obj:{}", method, obj);
            releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR);
        }
    }

    /**
     * Create room
     *
     * @param metadata
     * @param roomCreateReq
     * @throws Exception
     */
    @Override
    public void create(Metadata metadata, RoomCreateReq roomCreateReq) throws Exception {
        log.info("create, start, roomCreateReq:{}", roomCreateReq);

        // Set data
        setMetadata("create", roomCreateReq.getRoomId(), metadata, null,
                roomCreateReq.getRoomId(), roomCreateReq);
        // Add room list
        addRoomList(roomCreateReq);
        log.info("create, success, roomCreateReq:{}", roomCreateReq);
    }

    /**
     * Create metadata
     *
     * @param metadata
     * @param roomCreateReq
     */
    @Override
    public void createMetadata(Metadata metadata, RoomCreateReq roomCreateReq) {
        log.info("createMetadata, roomCreateReq:{}", roomCreateReq);

        RoomInfoDomain roomInfoDomain = new RoomInfoDomain();
        RoomInfoOwnerDomain roomInfoOwnerDomain = new RoomInfoOwnerDomain();
        roomInfoOwnerDomain.setUserId(roomCreateReq.getUserId())
                .setUserName(roomCreateReq.getUserName())
                .setUserAvatar(roomCreateReq.getUserAvatar());

        roomInfoDomain.setRoomId(roomCreateReq.getRoomId())
                .setRoomName(roomCreateReq.getRoomName())
                .setRoomNotice("")
                .setRoomOwner(roomInfoOwnerDomain)
                .setMicSeatCount(roomCreateReq.getMicSeatCount())
                .setMicSeatStyle(roomCreateReq.getMicSeatStyle())
                .setCreateTime(System.currentTimeMillis());

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = METADATA_KEY;
        metadataItem.value = JSON.toJSONString(roomInfoDomain);
        metadata.setMetadataItem(metadataItem);

        log.info("createMetadata, end, roomCreateReq:{}", roomCreateReq);
    }

    /**
     * Destroy room
     *
     * @param roomDestroyReq
     * @throws Exception
     */
    @Override
    public void destroy(RoomDestroyReq roomDestroyReq) throws Exception {
        log.info("destroy, start, roomDestroyReq:{}", roomDestroyReq);

        // Acquire lock
        acquireLock(roomDestroyReq.getRoomId());
        // Get data
        Metadata metadata = rtmUtil.getChannelMetadata(roomDestroyReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        checkMetadata("destroy", metadataItem, roomDestroyReq.getRoomId(), roomDestroyReq);
        // Check whether owner
        checkIsOwner("destroy", metadata, roomDestroyReq.getRoomId(), roomDestroyReq.getUserId());
        // Release lock
        releaseLock(roomDestroyReq.getRoomId());

        // Remove room
        if (!rtmUtil.removeChannelMetadata(roomDestroyReq.getRoomId())) {
            log.error("destroy, removeChannelMetadata failed, roomDestroyReq:{}", roomDestroyReq);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.RTM_REMOVE_CHANNEL_METADATA_ERROR);
        }
        // Remove room list
        removeRoomList(roomDestroyReq);
        log.info("destroy, success, roomDestroyReq:{}", roomDestroyReq);
    }

    /**
     * Destroy room for ncs
     *
     * @param roomDestroyReq
     * @throws Exception
     */
    @Override
    public void destroyForNcs(RoomDestroyReq roomDestroyReq) throws Exception {
        log.info("destroyForNcs, start, roomDestroyReq:{}", roomDestroyReq);

        // Remove room list
        removeRoomList(roomDestroyReq);

        // Acquire lock
        acquireLock(roomDestroyReq.getRoomId());
        // Get data
        Metadata metadata = rtmUtil.getChannelMetadata(roomDestroyReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        checkMetadata("destroyForNcs", metadataItem, roomDestroyReq.getRoomId(), roomDestroyReq);
        // Release lock
        releaseLock(roomDestroyReq.getRoomId());

        // Remove room
        if (!rtmUtil.removeChannelMetadata(roomDestroyReq.getRoomId())) {
            log.error("destroyForNcs, removeChannelMetadata failed, roomDestroyReq:{}", roomDestroyReq);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.RTM_REMOVE_CHANNEL_METADATA_ERROR);
        }
        log.info("destroyForNcs, success, roomDestroyReq:{}", roomDestroyReq);
    }

    /**
     * Get lock name
     *
     * @param lockName
     * @return
     */
    @Override
    public String getLockName(String lockName) {
        if (lockName.length() > 32) {
            return lockName.substring(0, 32);
        }
        return lockName;
    }

    /**
     * Get metadata
     *
     * @return
     */
    @Override
    public Metadata getMetadata() {
        return rtmUtil.createMetadata();
    }

    /**
     * Get room list
     *
     * @param roomListReq
     * @return
     */
    @Override
    public RoomListDto<RoomListEntity> getRoomList(RoomListReq roomListReq) {
        log.info("getRoomList, roomListReq:{}", roomListReq);

        Pageable pageable = PageRequest.of(0, roomListReq.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<RoomListEntity> roomList = roomListRepository.findByCreateTimeLessThan(roomListReq.getLastCreateTime(),
                pageable);

        RoomListDto<RoomListEntity> roomListDto = new RoomListDto<>();
        roomListDto.setPageSize(roomListReq.getPageSize()).setCount(roomList.getNumberOfElements())
                .setList(roomList.getContent());

        return roomListDto;
    }

    /**
     * Check whether owner
     *
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @throws Exception
     */
    @Override
    public Boolean isOwner(String method, Metadata metadata, String roomId, String userId) {
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        RoomInfoDomain roomInfoDomain = JSON.parseObject(metadataItem.value, RoomInfoDomain.class);
        if (Objects.equals(roomInfoDomain.getRoomOwner().getUserId(), userId)) {
            log.info("isOwner-{}, room owner, roomId:{}, ownerUserId:{}, userId:{}", method, roomId,
                    roomInfoDomain.getRoomOwner().getUserId(), userId);
            return true;
        }
        return false;
    }

    /**
     * Leave room
     *
     * @param roomLeaveReq
     * @throws Exception
     */
    @Override
    public void leave(RoomLeaveReq roomLeaveReq) throws Exception {
        log.info("leave, start, roomLeaveReq:{}", roomLeaveReq);

        // Acquire lock
        acquireLock(roomLeaveReq.getRoomId());
        // Get data
        Metadata metadata = rtmUtil.getChannelMetadata(roomLeaveReq.getRoomId());
        // Leave mic seat
        micSeatService.leave("leave", metadata, roomLeaveReq.getRoomId(), roomLeaveReq.getUserId());
        // Clear song by user
        songService.clearByUser("leave", metadata, roomLeaveReq.getRoomId(), roomLeaveReq.getUserId());
        // Clear chorus by user
        chorusService.clearByUser("leave", metadata, roomLeaveReq.getRoomId(), roomLeaveReq.getUserId());

        // Release lock
        releaseLock(roomLeaveReq.getRoomId());
        log.info("leave, success, roomLeaveReq:{}", roomLeaveReq);
    }

    /**
     * Query
     *
     * @param roomQueryReq
     */
    @Override
    public RoomQueryDto query(RoomQueryReq roomQueryReq) {
        // Get data
        Metadata metadata = rtmUtil.getChannelMetadata(roomQueryReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        if (metadataItem == null) {
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }

        RoomInfoDomain roomInfoDomain = JSON.parseObject(metadataItem.value, RoomInfoDomain.class);
        RoomQueryDto roomQueryDto = new RoomQueryDto();
        roomQueryDto.setRoomInfo(roomInfoDomain);

        return roomQueryDto;
    }

    @Override
    public Object queryAll(RoomQueryReq roomQueryReq) throws Exception {
        Metadata metadata = rtmUtil.getChannelMetadata(roomQueryReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        if (metadataItem == null) {
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }
        return metadata;
    }

    /**
     * Release lock
     *
     * @param roomId
     * @throws Exception
     */
    @Override
    public void releaseLock(String roomId) throws Exception {
        String lockName = getLockName(roomId);
        log.info("releaseLock, roomId:{}, lockName:{}", roomId, lockName);

        if (!redisUtil.unlock(lockName)) {
            log.error("releaseLock, failed, roomId:{}, lockName:{}", roomId, lockName);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_RELEASE_LOCK_ERROR);
        }
    }

    /**
     * Remove room list
     *
     * @param roomDestroyReq
     * @return
     */
    @Override
    public void removeRoomList(RoomDestroyReq roomDestroyReq) throws Exception {
        roomListRepository.deleteById(roomDestroyReq.getRoomId());
        log.info("removeRoomList, roomDestroyReq:{}", roomDestroyReq);
    }

    /**
     * Set metadata
     *
     * @param method
     * @param metadataItem
     * @param roomId
     * @param obj
     * @throws Exception
     */
    @Override
    public void setMetadata(String method, String channelName, Metadata metadata, MetadataItem metadataItem,
                            String roomId, Object obj) throws Exception {
        if (Boolean.FALSE.equals(rtmUtil.setChannelMetadata(channelName, metadata))) {
            log.error("setMetadata-{}, setChannelMetadata failed, channelName:{}, obj:{}, metadataItem.value:{}",
                    method, channelName, obj, metadataItem.value);
            releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.RTM_SET_CHANNEL_METADATA_ERROR);
        }
    }

    /**
     * Update metadata
     *
     * @param method
     * @param channelName
     * @param metadataItem
     * @param roomId
     * @param obj
     * @throws Exception
     */
    @Override
    public void updateMetadata(String method, String channelName, Metadata metadata, MetadataItem metadataItem,
                               String roomId, Object obj) throws Exception {
        if (Boolean.FALSE.equals(rtmUtil.updateChannelMetadata(channelName, metadata))) {
            log.error("updateMetadata-{}, updateChannelMetadata failed, channelName:{}, obj:{}, metadataItem.value:{}",
                    method, channelName, obj, metadataItem.value);
            releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.RTM_UPDATE_CHANNEL_METADATA_ERROR);
        }
    }

    /**
     * change notice
     *
     * @param roomId
     * @param notice
     * @throws Exception
     */
    @Override
    public void changeNotice(String roomId, String userId, String notice) throws Exception {
        acquireLock(roomId);
        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        checkIsOwner("changeNotice", metadata, roomId, userId);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        RoomInfoDomain roomInfoDomain = JSON.parseObject(metadataItem.value, RoomInfoDomain.class);
        roomInfoDomain.setRoomNotice(notice);
        metadataItem.value = JSON.toJSONString(roomInfoDomain);
        metadata.setMetadataItem(metadataItem);
        updateMetadata("changeNotice", roomId, metadata, metadataItem, roomId, roomInfoDomain);
        releaseLock(roomId);
    }
}
