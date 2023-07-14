package io.agora.uikit.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.enums.NcsEventTypeEnum;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.NcsReq;
import io.agora.uikit.bean.req.RoomDestroyReq;
import io.agora.uikit.bean.req.RoomLeaveReq;
import io.agora.uikit.service.INcsService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.utils.HmacShaUtil;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NcsServiceImpl implements INcsService {
    @Autowired
    private IRoomService roomService;
    @Autowired
    private HmacShaUtil hmacShaUtil;
    @Autowired
    private RtmUtil rtmUtil;

    @Value("${ncs.secret}")
    private String secret;

    /**
     * Check sign
     * 
     * @param signature
     * @param requestBody
     * @throws Exception
     */
    @Override
    public void checkSign(String signature, String requestBody) throws Exception {
        String sign = hmacShaUtil.hmacSha256(requestBody, secret);
        log.info("checkSign, signature:{}, sign:{}", signature, sign);

        if (!Objects.equals(signature, sign)) {
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.NCS_SIGN_ERROR);
        }
    }

    /**
     * Process event
     * docs:
     * https://docs.agora.io/cn/video-call-4.x/rtc_channel_event?platform=All%20Platforms
     * 
     * @param ncsReq
     */
    @Override
    public void processEvent(NcsReq ncsReq) throws Exception {
        log.info("processEvent, start, ncsReq:{}", ncsReq);

        // Check event type
        NcsEventTypeEnum ncsEventTypeEnum = NcsEventTypeEnum.getEnumByCode(ncsReq.getEventType());
        if (ncsEventTypeEnum == null) {
            log.info("processEvent, ncsEventTypeEnum is null, ncsReq:{}", ncsReq);
            return;
        }

        // Process event
        switch (ncsEventTypeEnum) {
            case CHANNEL_CREATE:
                log.info("processEvent, CHANNEL_CREATE, ncsReq:{}", ncsReq);
                break;
            case CHANNEL_DESTROY:
                log.info("processEvent, CHANNEL_DESTROY, ncsReq:{}", ncsReq);

                // Destroy room
                RoomDestroyReq roomDestroyReq = new RoomDestroyReq();
                roomDestroyReq.setRoomId(ncsReq.getPayload().getChannelName());
                roomService.destroyForNcs(roomDestroyReq);
                break;
            case BROADCASTER_JOIN_CHANNEL:
                log.info("processEvent, BROADCASTER_JOIN_CHANNEL, ncsReq:{}", ncsReq);
                break;
            case BROADCASTER_LEAVE_CHANNEL:
                log.info("processEvent, BROADCASTER_LEAVE_CHANNEL, ncsReq:{}", ncsReq);

                Metadata metadata = rtmUtil.getChannelMetadata(ncsReq.getPayload().getChannelName());
                MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, RoomServiceImpl.METADATA_KEY);
                // Check data
                if (metadataItem == null) {
                    log.info("processEvent, metadata no data, ncsReq:{}", ncsReq);
                    return;
                }

                // Check whether owner
                if (roomService.isOwner("processEvent", metadata, ncsReq.getPayload().getChannelName(),
                        ncsReq.getPayload().getAccount())) {
                    // Destroy room
                    RoomDestroyReq roomDestroyReqBroadcasterLeaveChannel = new RoomDestroyReq();
                    roomDestroyReqBroadcasterLeaveChannel.setRoomId(ncsReq.getPayload().getChannelName());
                    roomService.destroyForNcs(roomDestroyReqBroadcasterLeaveChannel);
                    return;
                }

                // Leave room
                RoomLeaveReq roomLeaveReq = new RoomLeaveReq();
                roomLeaveReq.setRoomId(ncsReq.getPayload().getChannelName())
                        .setUserId(ncsReq.getPayload().getAccount());
                roomService.leave(roomLeaveReq);
                break;
            default:
                log.info("processEvent, default, ncsReq:{}", ncsReq);
                break;
        }
    }
}
