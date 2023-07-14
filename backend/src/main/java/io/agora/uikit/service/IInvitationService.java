package io.agora.uikit.service;

import io.agora.uikit.bean.req.MicSeatPayload;
import io.agora.uikit.bean.req.RoomCreateReq;

public interface IInvitationService extends IService<RoomCreateReq> {
    void start(String roomId, String fromUserId, String toUserId, MicSeatPayload payload) throws Exception;

    void accept(String roomId, String fromUserId, String toUserId) throws Exception;

    void remove(String roomId, String fromUserId, String toUserId) throws Exception;
}
