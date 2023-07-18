package io.agora.uikit.task;

import io.agora.uikit.bean.entity.RoomListEntity;
import io.agora.uikit.bean.req.RoomDestroyReq;
import io.agora.uikit.metric.PrometheusMetric;
import io.agora.uikit.repository.RoomListRepository;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.utils.RedisUtil;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RoomListTask {
    @Autowired
    private RoomListRepository roomListRepository;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RtmUtil rtmUtil;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private PrometheusMetric prometheusMetric;

    private String roomStatusQueue = "task_roomStatusQueue";
    private long waitSecond = 1;
    private long createTimeDelayMs = 60 * 1000;
    private String updateRoomStatusQueueMetricName = "RoomListTask-updateRoomStatusQueue";
    private String updateRoomStatusMetricName = "RoomListTask-updateRoomStatus";

    /**
     * Update room status queue
     */
    @Scheduled(cron = "${task.updateRoomStatusQueueCron}")
    public void updateRoomStatusQueue() throws Exception {
        prometheusMetric.getTaskCounter().labels(updateRoomStatusQueueMetricName, "start").inc();

        log.info("updateRoomStatusQueue start, roomStatusQueue:{}, waitSecond:{}", roomStatusQueue, waitSecond);

        // Acquire lock
        if (!redisUtil.tryLock(roomStatusQueue, waitSecond)) {
            prometheusMetric.getTaskCounter().labels(updateRoomStatusQueueMetricName, "tryLockFailed").inc();
            log.info("updateRoomStatusQueue, acquire lock failed");
            return;
        }

        // Get room list
        List<RoomListEntity> roomList = roomListRepository
                .findByCreateTimeLessThan(System.currentTimeMillis() - createTimeDelayMs);
        log.info("updateRoomStatusQueue, total:{}", roomList.size());

        // Update queue
        for (RoomListEntity roomListEntity : roomList) {
            if (!redisUtil.zsetAdd(roomStatusQueue, roomListEntity.getRoomId(), roomListEntity.getCreateTime())) {
                log.info("updateRoomStatusQueue, failed, roomId:{}", roomListEntity.getRoomId());
            }
        }

        TimeUnit.SECONDS.sleep(waitSecond);
        // Release lock
        redisUtil.unlock(roomStatusQueue);
        log.info("updateRoomStatusQueue end, roomStatusQueue:{}, waitSecond:{}", roomStatusQueue, waitSecond);
        prometheusMetric.getTaskCounter().labels(updateRoomStatusQueueMetricName, "end").inc();
    }

    /**
     * Update room status
     */
    @Scheduled(fixedDelay = 5000)
    public void updateRoomStatus() throws Exception {
        prometheusMetric.getTaskCounter().labels(updateRoomStatusMetricName, "start").inc();
        log.info("updateRoomStatus start, roomStatusQueue:{}", roomStatusQueue);

        while (true) {
            prometheusMetric.getTaskCounter().labels(updateRoomStatusMetricName, "zsetPopMaxStart").inc();

            TypedTuple<Object> roomInfo = redisUtil.zsetPopMax(roomStatusQueue);
            // Check data
            if (roomInfo == null) {
                prometheusMetric.getTaskCounter().labels(updateRoomStatusMetricName, "zsetPopMaxRoomInfoNull").inc();
                log.info("updateRoomStatus, data empty, roomStatusQueue:{}", roomStatusQueue);
                break;
            }

            Object roomIdObj = roomInfo.getValue();
            String roomId = (roomIdObj != null) ? roomIdObj.toString() : "";
            // Get online users
            Long onlineUsers = rtmUtil.whoNow(roomId);
            if (onlineUsers == -1) {
                log.info("updateRoomStatus whoNow, roomStatusQueue:{}, roomId:{}, onlineUsers:{}",
                        roomStatusQueue, roomId, onlineUsers);
                continue;
            }

            // Destroy room
            if (onlineUsers == 0) {
                RoomDestroyReq roomDestroyReq = new RoomDestroyReq();
                roomDestroyReq.setRoomId(roomId);
                roomService.destroyForNcs(roomDestroyReq);
                log.info("updateRoomStatus destroyForNcs, roomStatusQueue:{}, roomId:{}, onlineUsers:{}",
                        roomStatusQueue, roomId, onlineUsers);
                prometheusMetric.getTaskCounter().labels(updateRoomStatusMetricName, "zsetPopMaxDestroyForNcs").inc();
                continue;
            }

            // Update online users
            Update update = new Update();
            update.set("onlineUsers", onlineUsers);
            roomListRepository.updatById(roomId, update);
            prometheusMetric.getTaskCounter().labels(updateRoomStatusMetricName, "zsetPopMaxUpdateOnlineUsers").inc();

            log.info("updateRoomStatus update, roomStatusQueue:{}, roomId:{}, onlineUsers:{}", roomStatusQueue, roomId,
                    onlineUsers);
        }

        log.info("updateRoomStatus end, roomStatusQueue:{}", roomStatusQueue);
        prometheusMetric.getTaskCounter().labels(updateRoomStatusMetricName, "end").inc();
    }
}
