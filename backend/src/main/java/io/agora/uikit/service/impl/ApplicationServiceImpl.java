package io.agora.uikit.service.impl;

import io.agora.rtm.Metadata;
import io.agora.uikit.bean.domain.ProcessDomain;
import io.agora.uikit.bean.req.MicSeatPayload;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.service.IApplicationService;
import io.agora.uikit.service.IProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;

import static io.agora.uikit.service.impl.ProcessServiceImpl.APPLICATION_METADATA_KEY;
import static io.agora.uikit.service.impl.ProcessServiceImpl.SEAT_METADATA_KEY;

@Slf4j
@Service
public class ApplicationServiceImpl implements IApplicationService {

    @Resource
    private IProcessService processService;

    @Override
    public void start(String roomId, String fromUserId, MicSeatPayload payload) throws Exception {
        processService.startProcess(roomId, fromUserId, null, payload);
    }

    @Override
    public void accept(String roomId, String fromUserId, String toUserId) throws Exception {
        processService.endApplicationProcess(roomId, fromUserId, toUserId, true);
    }

    @Override
    public void remove(String roomId, String fromUserId, String toUserId) throws Exception {
        processService.endApplicationProcess(roomId, fromUserId, toUserId, false);
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
        processService.createMetaData(metadata, APPLICATION_METADATA_KEY, processDomainKV);
    }
}
