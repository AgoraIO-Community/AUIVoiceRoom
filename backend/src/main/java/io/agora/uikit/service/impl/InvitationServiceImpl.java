package io.agora.uikit.service.impl;

import io.agora.rtm.Metadata;
import io.agora.uikit.bean.domain.ProcessDomain;
import io.agora.uikit.bean.req.MicSeatPayload;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.service.IInvitationService;
import io.agora.uikit.service.IProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

import static io.agora.uikit.service.impl.ProcessServiceImpl.INVITATION_METADATA_KEY;
import static io.agora.uikit.service.impl.ProcessServiceImpl.SEAT_METADATA_KEY;

@Slf4j
@Service
public class InvitationServiceImpl implements IInvitationService {
    @Resource
    private IProcessService processService;

    @Override
    public void start(String roomId, String fromUserId, String toUserId, MicSeatPayload payload) throws Exception {
        processService.startProcess(roomId, fromUserId, toUserId, payload);
    }

    @Override
    public void accept(String roomId, String fromUserId, String toUserId) throws Exception {
        processService.endInvitationProcess(roomId, fromUserId, toUserId, true);
    }

    @Override
    public void remove(String roomId, String fromUserId, String toUserId) throws Exception {
        processService.endInvitationProcess(roomId, fromUserId, toUserId, false);
    }

    @Override
    public void createMetadata(Metadata metadata, RoomCreateReq roomCreateReq) {
        log.info("createMetadata, roomCreateReq:{}", roomCreateReq);
        ProcessDomain processDomain = new ProcessDomain()
                .setIsEnabled(true)
                .setTimeout(60)
                .setRemoved(new ArrayList<>())
                .setQueue(new ArrayList<>())
                .setMaxWait(10)
                .setMaxAccept(10);
        HashMap<String, ProcessDomain> processDomainKV = new HashMap<>();
        processDomainKV.put(SEAT_METADATA_KEY, processDomain);
        processService.createMetaData(metadata, INVITATION_METADATA_KEY, processDomainKV);
    }
}
