package io.agora.uikit.service;

import io.agora.uikit.bean.req.MicSeatPayload;
import io.agora.uikit.bean.req.RoomCreateReq;

public interface IApplicationService extends IService<RoomCreateReq> {
    void start(String roomId, String fromUserId, MicSeatPayload payload) throws Exception;

    void accept(String roomId, String fromUserId, String toUserId) throws Exception;

    void remove(String roomId, String fromUserId, String toUserId) throws Exception;
}
