package io.agora.uikit.service;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.ProcessDomain;
import io.agora.uikit.bean.req.MicSeatPayload;

import java.util.HashMap;

public interface IProcessService {

    HashMap<String, ProcessDomain> getProcessDomain(MetadataItem metadataItem);

    void createMetaData(Metadata metadata, String metaKey, HashMap<String, ProcessDomain> processDomainKV);

    void startProcess(String roomId, String fromUserId, String toUserId, MicSeatPayload payload) throws Exception;

    void endApplicationProcess(String roomId, String fromUserId, String toUserId, Boolean accept) throws Exception;

    void endInvitationProcess(String roomId, String fromUserId, String toUserId, Boolean accept) throws Exception;
}
