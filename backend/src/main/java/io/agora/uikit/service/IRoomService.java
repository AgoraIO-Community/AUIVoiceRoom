package io.agora.uikit.service;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.dto.RoomListDto;
import io.agora.uikit.bean.dto.RoomQueryDto;
import io.agora.uikit.bean.entity.RoomListEntity;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.bean.req.RoomDestroyReq;
import io.agora.uikit.bean.req.RoomLeaveReq;
import io.agora.uikit.bean.req.RoomListReq;
import io.agora.uikit.bean.req.RoomQueryReq;

public interface IRoomService extends IService<RoomCreateReq> {
    /**
     * Acquire lock
     *
     * @param roomId
     * @throws Exception
     */
    void acquireLock(String roomId) throws Exception;

    /**
     * Add room list
     *
     * @param roomCreateReq
     * @return
     */
    void addRoomList(RoomCreateReq roomCreateReq) throws Exception;

    /**
     * Check whether owner
     *
     * @param method
     * @param roomId
     * @param userId
     * @throws Exception
     */
    void checkIsOwner(String method, Metadata metadata, String roomId, String userId) throws Exception;

    /**
     * Check metadata
     *
     * @param method
     * @param metadataItem
     * @param roomId
     * @param obj
     * @throws Exception
     */
    void checkMetadata(String method, MetadataItem metadataItem, String roomId, Object obj) throws Exception;

    /**
     * Create room
     *
     * @param metadata
     * @param roomCreateReq
     * @throws Exception
     */
    void create(Metadata metadata, RoomCreateReq roomCreateReq) throws Exception;

    /**
     * Destroy room
     *
     * @param roomDestroyReq
     * @throws Exception
     */
    void destroy(RoomDestroyReq roomDestroyReq) throws Exception;

    /**
     * Destroy room for ncs
     *
     * @param roomDestroyReq
     * @throws Exception
     */
    void destroyForNcs(RoomDestroyReq roomDestroyReq) throws Exception;

    /**
     * Get lock name
     *
     * @param lockName
     * @return
     */
    String getLockName(String lockName);

    /**
     * Get metadata
     *
     * @return
     */
    Metadata getMetadata();

    /**
     * Get room list
     *
     * @param roomListReq
     * @return
     */
    RoomListDto<RoomListEntity> getRoomList(RoomListReq roomListReq);

    /**
     * Check whether owner
     *
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @throws Exception
     */
    Boolean isOwner(String method, Metadata metadata, String roomId, String userId);

    /**
     * Leave room
     *
     * @param roomLeaveReq
     * @throws Exception
     */
    void leave(RoomLeaveReq roomLeaveReq) throws Exception;

    /**
     * Query
     *
     * @param roomQueryReq
     * @throws Exception
     */
    RoomQueryDto query(RoomQueryReq roomQueryReq) throws Exception;


    Object queryAll(RoomQueryReq roomQueryReq) throws Exception;

    /**
     * Release lock
     *
     * @param roomId
     * @throws Exception
     */
    void releaseLock(String roomId) throws Exception;

    /**
     * Remove room list
     *
     * @param roomDestroyReq
     * @return
     */
    void removeRoomList(RoomDestroyReq roomDestroyReq) throws Exception;

    /**
     * Set metadata
     *
     * @param method
     * @param metadataItem
     * @param roomId
     * @param obj
     * @throws Exception
     */
    void setMetadata(String method, String channelName, Metadata metadata, MetadataItem metadataItem,
                     String roomId, Object obj) throws Exception;

    /**
     * Update metadata
     *
     * @param method
     * @param channelName
     * @param metadata
     * @param metadataItem
     * @param roomId
     * @param obj
     * @throws Exception
     */
    void updateMetadata(String method, String channelName, Metadata metadata, MetadataItem metadataItem, String roomId,
                        Object obj) throws Exception;


    /**
     * change notice
     *
     * @param roomId
     * @param userId
     * @param notice
     * @throws Exception
     */
    void changeNotice(String roomId, String userId, String notice) throws Exception;
}
