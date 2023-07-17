package io.agora.uikit.service;

import java.util.List;

import io.agora.rtm.Metadata;
import io.agora.uikit.bean.domain.ChorusDomain;
import io.agora.uikit.bean.req.ChorusJoinReq;
import io.agora.uikit.bean.req.ChorusLeaveReq;

public interface IChorusService extends IService<ChorusJoinReq> {
    /**
     * Check whether joined
     * 
     * @param method
     * @param chorusList
     * @param roomId
     * @param userId
     * @throws Exception
     */
    void checkAlreadyJoined(String method, List<ChorusDomain> chorusList, String roomId, String userId)
            throws Exception;

    /**
     * Clear
     * 
     * @param metadata
     */
    void clear(Metadata metadata);

    /**
     * Clear chorus by user
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @return
     */
    void clearByUser(String method, Metadata metadata, String roomId, String userId) throws Exception;

    /**
     * Create metadata
     * 
     * @param metadata
     */
    void createMetadata(Metadata metadata);

    /**
     * Join
     * 
     * @param chorusJoinReq
     * @throws Exception
     */
    void join(ChorusJoinReq chorusJoinReq) throws Exception;

    /**
     * Leave
     * 
     * @param chorusLeaveReq
     * @throws Exception
     */
    void leave(ChorusLeaveReq chorusLeaveReq) throws Exception;
}
