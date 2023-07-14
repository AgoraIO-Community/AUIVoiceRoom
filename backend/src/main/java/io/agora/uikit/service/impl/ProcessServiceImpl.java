package io.agora.uikit.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.*;
import io.agora.uikit.bean.enums.MicSeatStatusEnum;
import io.agora.uikit.bean.enums.ProcessRemovedActionTypeEnum;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.MicSeatEnterReq;
import io.agora.uikit.bean.req.MicSeatPayload;
import io.agora.uikit.service.IMicSeatService;
import io.agora.uikit.service.IProcessService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static io.agora.uikit.service.impl.MicSeatServiceImpl.METADATA_KEY;

@Slf4j
@Service
public class ProcessServiceImpl implements IProcessService {

    @Resource
    private IRoomService roomService;

    @Resource
    private RtmUtil rtmUtil;

    @Resource
    private IMicSeatService micSeatService;

    public static final String INVITATION_METADATA_KEY = "invitation";
    public static final String APPLICATION_METADATA_KEY = "application";

    public static final String SEAT_METADATA_KEY = "micSeat";

    @Override
    public HashMap<String, ProcessDomain> getProcessDomain(MetadataItem metadataItem) {
        return JSON.parseObject(metadataItem.value, new TypeReference<Map<String, ProcessDomain>>() {
        });
    }

    @Override
    public void createMetaData(Metadata metadata, String metaKey, HashMap<String, ProcessDomain> processDomainKV) {
        log.info("create {} MetaData:{}", metaKey, processDomainKV);
        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = metaKey;
        metadataItem.value = JSON.toJSONString(processDomainKV);
        metadata.setMetadataItem(metadataItem);
    }


    @Override
    public void startProcess(String roomId, String fromUserId, String toUserId, MicSeatPayload payload) throws Exception {
        if (StringUtils.isEmpty(toUserId)) {
            startApplicationProcess(roomId, fromUserId, payload);
        } else {
            startInvitationProcess(roomId, fromUserId, toUserId, payload);
        }
    }

    public void startInvitationProcess(String roomId, String fromUserId, String toUserId, MicSeatPayload payload) throws Exception {
        roomService.acquireLock(roomId);
        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        if (metadata == null) {
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }
        roomService.checkIsOwner("startInvitationProcess", metadata, roomId, fromUserId);
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, INVITATION_METADATA_KEY);
        if (metadataItem == null) {
            roomService.releaseLock(roomId);
            throw new Exception("invitation config is not found");
        }
        HashMap<String, ProcessDomain> processDomainKV = getProcessDomain(metadataItem);
        if (processDomainKV == null) {
            roomService.releaseLock(roomId);
            throw new Exception("invitation config is not found");
        }
        ProcessDomain processDomain = processDomainKV.get(SEAT_METADATA_KEY);
        if (processDomain == null) {
            roomService.releaseLock(roomId);
            throw new Exception("invitation config is not found");
        }
        List<ProcessQueueDomain> queueDomain = processDomain.getQueue();
        if (queueDomain != null && processDomain.getMaxWait() != null && queueDomain.size() + 1 > processDomain.getMaxWait()) {
            roomService.releaseLock(roomId);
            throw new Exception("exceeded the maximum number of waiting");
        }
        if (queueDomain == null) {
            queueDomain = new ArrayList<>();
        } else {
            if (queueDomain.stream().anyMatch(e -> Objects.equals(e.getUserId(), toUserId))) {
                roomService.releaseLock(roomId);
                throw new Exception("invitee already exists in queue");
            }
        }
        queueDomain.add(new ProcessQueueDomain()
                .setProcessUuid(UUID.randomUUID().toString())
                .setCreateTime(System.currentTimeMillis())
                .setPayload(new ProcessMicSeatPayloadDomain()
                        .setDesc(payload.getDesc())
                        .setSeatNo(payload.getSeatNo()))
                .setUserId(toUserId));
        createMetaData(metadata, INVITATION_METADATA_KEY, processDomainKV);
        roomService.setMetadata("startInvitationProcess", roomId, metadata, metadataItem, roomId, null);
        roomService.releaseLock(roomId);
    }

    public void startApplicationProcess(String roomId, String fromUserId, MicSeatPayload payload) throws Exception {
        roomService.acquireLock(roomId);
        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        if (metadata == null) {
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, APPLICATION_METADATA_KEY);
        if (metadataItem == null) {
            roomService.releaseLock(roomId);
            throw new Exception("application config is not found");
        }
        HashMap<String, ProcessDomain> processDomainKV = getProcessDomain(metadataItem);
        if (processDomainKV == null) {
            roomService.releaseLock(roomId);
            throw new Exception("application config is not found");
        }
        ProcessDomain processDomain = processDomainKV.get(SEAT_METADATA_KEY);
        if (processDomain == null) {
            roomService.releaseLock(roomId);
            throw new Exception("application config is not found");
        }
        List<ProcessQueueDomain> queueDomain = processDomain.getQueue();
        if (queueDomain != null && processDomain.getMaxWait() != null && queueDomain.size() + 1 > processDomain.getMaxWait()) {
            roomService.releaseLock(roomId);
            throw new Exception("exceeded the maximum number of waiting");
        }
        if (queueDomain == null) {
            queueDomain = new ArrayList<>();
        } else {
            if (queueDomain.stream().anyMatch(e -> Objects.equals(e.getUserId(), fromUserId))) {
                roomService.releaseLock(roomId);
                throw new Exception("applicant already exists in the queue");
            }
        }
        queueDomain.add(new ProcessQueueDomain()
                .setProcessUuid(UUID.randomUUID().toString())
                .setCreateTime(System.currentTimeMillis())
                .setUserId(fromUserId)
                .setPayload(new ProcessMicSeatPayloadDomain()
                        .setSeatNo(payload.getSeatNo())
                        .setDesc(payload.getDesc())));
        createMetaData(metadata, APPLICATION_METADATA_KEY, processDomainKV);
        roomService.setMetadata("startApplicationProcess", roomId, metadata, metadataItem, roomId, null);
        roomService.releaseLock(roomId);
    }


    @Override
    public void endApplicationProcess(String roomId, String fromUserId, String toUserId, Boolean accept) throws Exception {
        roomService.acquireLock(roomId);
        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        if (metadata == null) {
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }
        ProcessRemovedActionTypeEnum actionTypeEnum;
        if (Objects.equals(fromUserId, toUserId)) {
            actionTypeEnum = ProcessRemovedActionTypeEnum.CANCEL;
        } else {
            if (accept) {
                roomService.checkIsOwner("endApplicationProcess", metadata, roomId, fromUserId);
                actionTypeEnum = ProcessRemovedActionTypeEnum.ACCEPTED_SUCCESS;
            } else {
                actionTypeEnum = ProcessRemovedActionTypeEnum.DECLINE;
            }
        }
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, APPLICATION_METADATA_KEY);
        if (metadataItem == null) {
            roomService.releaseLock(roomId);
            throw new Exception("application config is not found");
        }
        HashMap<String, ProcessDomain> processDomainKV = getProcessDomain(metadataItem);
        if (processDomainKV == null) {
            roomService.releaseLock(roomId);
            throw new Exception("application config is not found");
        }
        ProcessDomain processDomain = processDomainKV.get(SEAT_METADATA_KEY);
        if (processDomain == null) {
            roomService.releaseLock(roomId);
            throw new Exception("application config is not found");
        }

        List<ProcessQueueDomain> queueDomain = processDomain.getQueue();
        ProcessQueueDomain userQueueDomain;
        if (queueDomain == null) {
            roomService.releaseLock(roomId);
            throw new Exception("user is not in the application queue");
        } else {
            Optional<ProcessQueueDomain> userQueueDomainOptional = queueDomain
                    .stream()
                    .filter(i -> Objects.equals(i.getUserId(), toUserId))
                    .findFirst();
            if (userQueueDomainOptional.isEmpty()) {
                roomService.releaseLock(roomId);
                throw new Exception("user is not in the application queue");
            } else {
                userQueueDomain = userQueueDomainOptional.get();
            }

        }
        if (accept) {
            try {
                MicSeatEnterReq micSeatEnterReq = new MicSeatEnterReq()
                        .setMicSeatNo(userQueueDomain.getPayload().getSeatNo())
                        .setRoomId(roomId)
                        .setUserId(userQueueDomain.getUserId())
                        .setUserName("")
                        .setUserAvatar("");
                enterMicSeat(metadata, micSeatEnterReq);
            } catch (Exception ex) {
                actionTypeEnum = ProcessRemovedActionTypeEnum.ACCEPTED_FAILED;
                log.error("enterMicSeat error, micSeatEnterReq:{}", userQueueDomain.getPayload(), ex);
            }
        }

        queueDomain.removeIf(i -> Objects.equals(i.getUserId(), toUserId));

        List<ProcessRemovedDomain> removedDomain = processDomain.getRemoved();
        if (removedDomain == null) {
            removedDomain = new ArrayList<>();
        } else {
            removedDomain.clear();
        }
        removedDomain.add(new ProcessRemovedDomain()
                .setProcessUuid(userQueueDomain.getProcessUuid())
                .setActionType(actionTypeEnum.getCode())
                .setUserId(userQueueDomain.getUserId())
                .setCreateTime(userQueueDomain.getCreateTime())
                .setUpdateTime(System.currentTimeMillis())
                .setPayload(userQueueDomain.getPayload()));

        createMetaData(metadata, APPLICATION_METADATA_KEY, processDomainKV);
        roomService.setMetadata("endApplicationProcess", roomId, metadata, metadataItem, roomId, null);
        roomService.releaseLock(roomId);
    }

    private void enterMicSeat(Metadata metadata, MicSeatEnterReq micSeatEnterReq) throws Exception {
        log.info("enter, start, micSeatEnterReq:{}", micSeatEnterReq);

        MetadataItem micSeatMetadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("enter", micSeatMetadataItem, micSeatEnterReq.getRoomId(), micSeatEnterReq);
        // Get mic seat
        Map<String, MicSeatDomain> micSeatMap = micSeatService.getMicSeatMap(micSeatMetadataItem);
        // Check whether on mic seat no
        micSeatService.checkMicSeatNoAlreadyOn("enter", micSeatMap, micSeatEnterReq.getRoomId(), micSeatEnterReq.getUserId());
        // Check if the mic seat exists
        micSeatService.checkMicSeatNoNotExists("enter", micSeatMap, micSeatEnterReq.getRoomId(), micSeatEnterReq.getMicSeatNo());
        // Check mic seat no status, not idle
        micSeatService.checkMicSeatNoStatusNotIdle("enter", micSeatMap, micSeatEnterReq.getRoomId(), micSeatEnterReq.getMicSeatNo());

        // Set data
        MicSeatDomain micSeatMapModify = micSeatMap.get(micSeatEnterReq.getMicSeatNo().toString());
        MicSeatOwnerDomain micSeatOwnerDomain = new MicSeatOwnerDomain();
        micSeatOwnerDomain.setUserId(micSeatEnterReq.getUserId())
                .setUserName(micSeatEnterReq.getUserName())
                .setUserAvatar(micSeatEnterReq.getUserAvatar());
        micSeatMapModify.setMicSeatStatus(MicSeatStatusEnum.MIC_SEAT_STATUS_USED)
                .setOwner(micSeatOwnerDomain);
        micSeatMap.put(micSeatEnterReq.getMicSeatNo().toString(), micSeatMapModify);
        micSeatMetadataItem.value = JSON.toJSONString(micSeatMap);
        metadata.setMetadataItem(micSeatMetadataItem);

        log.info("enter, success, micSeatEnterReq:{}, micSeatMap:{}, micSeatMetadataItem.value:{}",
                micSeatEnterReq, micSeatMap, micSeatMetadataItem.value);
    }

    @Override
    public void endInvitationProcess(String roomId, String fromUserId, String toUserId, Boolean accept) throws Exception {
        String targetUserId;
        roomService.acquireLock(roomId);
        Metadata metadata = rtmUtil.getChannelMetadata(roomId);
        if (metadata == null) {
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }

        ProcessRemovedActionTypeEnum actionTypeEnum;
        if (!StringUtils.isEmpty(toUserId)) {
            targetUserId = toUserId;
            actionTypeEnum = ProcessRemovedActionTypeEnum.CANCEL;
            roomService.checkIsOwner("endInvitationProcess", metadata, roomId, fromUserId);
        } else {
            targetUserId = fromUserId;
            if (accept) {
                actionTypeEnum = ProcessRemovedActionTypeEnum.ACCEPTED_SUCCESS;
            } else {
                actionTypeEnum = ProcessRemovedActionTypeEnum.DECLINE;
            }
        }
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, INVITATION_METADATA_KEY);
        if (metadataItem == null) {
            roomService.releaseLock(roomId);
            throw new Exception("invitation config is not found");
        }
        HashMap<String, ProcessDomain> processDomainKV = getProcessDomain(metadataItem);
        if (processDomainKV == null) {
            roomService.releaseLock(roomId);
            throw new Exception("invitation config is not found");
        }
        ProcessDomain processDomain = processDomainKV.get(SEAT_METADATA_KEY);
        if (processDomain == null) {
            roomService.releaseLock(roomId);
            throw new Exception("invitation config is not found");
        }

        List<ProcessQueueDomain> queueDomain = processDomain.getQueue();
        ProcessQueueDomain userQueueDomain;
        if (queueDomain == null) {
            roomService.releaseLock(roomId);
            throw new Exception("user is not in the invitation queue");
        } else {
            Optional<ProcessQueueDomain> userQueueDomainOptional = queueDomain
                    .stream()
                    .filter(i -> Objects.equals(i.getUserId(), targetUserId)).findFirst();
            if (userQueueDomainOptional.isEmpty()) {
                roomService.releaseLock(roomId);
                throw new Exception("user is not in the invitation queue");
            } else {
                userQueueDomain = userQueueDomainOptional.get();
            }
        }

        if (accept) {
            try {
                MicSeatEnterReq micSeatEnterReq = new MicSeatEnterReq()
                        .setMicSeatNo(userQueueDomain.getPayload().getSeatNo())
                        .setRoomId(roomId)
                        .setUserId(targetUserId)
                        .setUserName("")
                        .setUserAvatar("");
                enterMicSeat(metadata, micSeatEnterReq);
            } catch (Exception ex) {
                actionTypeEnum = ProcessRemovedActionTypeEnum.ACCEPTED_FAILED;
                log.error("enterMicSeat error, micSeatEnterReq:{}", userQueueDomain.getPayload(), ex);
            }
        }

        queueDomain.removeIf(i -> Objects.equals(i.getUserId(), targetUserId));

        List<ProcessRemovedDomain> removedDomain = processDomain.getRemoved();
        if (removedDomain == null) {
            removedDomain = new ArrayList<>();
        } else {
            removedDomain.clear();
        }

        removedDomain.add(new ProcessRemovedDomain()
                .setProcessUuid(userQueueDomain.getProcessUuid())
                .setActionType(actionTypeEnum.getCode())
                .setUserId(userQueueDomain.getUserId())
                .setCreateTime(userQueueDomain.getCreateTime())
                .setUpdateTime(System.currentTimeMillis())
                .setPayload(userQueueDomain.getPayload()));

        createMetaData(metadata, INVITATION_METADATA_KEY, processDomainKV);
        roomService.setMetadata("endInvitationProcess", roomId, metadata, metadataItem, roomId, null);
        roomService.releaseLock(roomId);
    }
}

