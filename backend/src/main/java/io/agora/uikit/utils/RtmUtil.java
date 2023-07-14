package io.agora.uikit.utils;

import io.agora.rtc2.Constants;
import io.agora.rtm.*;
import io.agora.rtm.RtmConstants.RtmChannelType;
import io.agora.uikit.metric.PrometheusMetric;
import io.prometheus.client.Histogram;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class RtmUtil {
    @Autowired
    private TokenUtil tokenUtil;
    @Autowired
    private PrometheusMetric prometheusMetric;

    @Value("${token.appId}")
    private String appId;
    @Value("${token.appCertificate}")
    private String appCertificate;
    private int tokenExpirationInSeconds = 3600 * 24;
    private String userId = UUID.randomUUID().toString();
    private long timeout = 5;
    private AtomicBoolean rtmLogin = new AtomicBoolean(false);
    private RtmClient rtmClient;

    private final RtmEventListener rtmEventListener = new RtmEventListener() {
        @Override
        public void onMessageEvent(MessageEvent event) {
            log.debug("onMessageEvent, event:{}", event);
        }

        @Override
        public void onPresenceEvent(PresenceEvent event) {
            log.debug("onPresenceEvent, event:{}", event);
        }

        @Override
        public void onTopicEvent(TopicEvent event) {
            log.debug("onTopicEvent, event:{}", event);
        }

        @Override
        public void onLockEvent(LockEvent event) {
            log.debug("onLockEvent, event:{}", event);
        }

        @Override
        public void onStorageEvent(StorageEvent event) {
            log.debug("onStorageEvent, event:{}", event);
        }

        @Override
        public void onConnectionStateChange(String channelName, int state, int reason) {
            log.debug("onConnectionStateChange, channelName:{}, state:{}, reason:{}", channelName, state, reason);
        }

        @Override
        public void onTokenPrivilegeWillExpire(String channelName) {
            log.info("onTokenPrivilegeWillExpire, channelName:{}", channelName);
            // Renew token
            while (true) {
                if (renewToken()) {
                    break;
                }
            }
        }
    };

    /**
     * Init
     *
     * @return
     */
    @PostConstruct
    public Boolean init() {
        log.info("init, appId:{}, userId:{}, tokenExpirationInSeconds:{}, timeout:{}", appId, userId,
                tokenExpirationInSeconds, timeout);
        createClient();
        return login(getRtmToken());
    }

    /**
     * Acquire lock
     *
     * @param channelName
     * @param lockName
     * @return
     */
    public Boolean acquireLock(String channelName, String lockName) {
        log.debug("acquireLock, start, channelName:{}, lockName:{}", channelName,
                lockName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        rtmClient.getLock().acquireLock(channelName, RtmChannelType.STREAM, lockName,
                true,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.debug("acquireLock, onSuccess, channelName:{}, lockName:{}", channelName,
                                lockName);
                        res.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("acquireLock, onFailure, error:{}, channelName:{}, lockName:{}",
                                errorInfo,
                                channelName, lockName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("acquireLock, await failed, error:{}, channelName:{}, lockName:{}",
                    e, channelName, lockName);
            return false;
        }

        log.debug("acquireLock, end, res:{}, channelName:{}, lockName:{}", res.get(),
                channelName, lockName);
        return res.get();
    }

    /**
     * Create client
     *
     * @return
     */
    public boolean createClient() {
        if (rtmClient != null) {
            log.info("createClient, rtm client created, appId:{}, userId:{}", appId, userId);
            return true;
        }

        log.info("createClient, create rtm client, appId:{}, userId:{}", appId, userId);

        RtmConfig rtmConfig = new RtmConfig();
        rtmConfig.appId = appId;
        rtmConfig.userId = userId;
        rtmConfig.eventListener = rtmEventListener;
        RtmConfig.LogConfig logConfig = new RtmConfig.LogConfig();
        logConfig.level = Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_NONE);
        rtmConfig.logConfig = logConfig;

        try {
            log.info("createClient, create rtm client, appId:{}, userId:{}", rtmConfig.appId, rtmConfig.userId);
            rtmClient = RtmClient.create(rtmConfig);
        } catch (Exception e) {
            log.error("createClient, error:{}, appId:{}, userId:{}", e.getMessage(), rtmConfig.appId, rtmConfig.userId);
            return false;
        }

        log.info("createClient, success, appId:{}, userId:{}", rtmConfig.appId, rtmConfig.userId);
        return true;
    }

    /**
     * Create metadata
     *
     * @return
     */
    public Metadata createMetadata() {
        return rtmClient.getStorage().createMetadata();
    }

    /**
     * Get channel metadata
     *
     * @param channelName
     * @return
     */
    public Metadata getChannelMetadata(String channelName) {
        Histogram.Timer histogramRequestTimer = prometheusMetric.getRtmRequestsDurationSecondsHistogram()
                .labels("getChannelMetadata").startTimer();
        log.debug("getChannelMetadata, start, channelName:{}", channelName);

        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Metadata> resultMap = new HashMap<>();
        rtmClient.getStorage().getChannelMetadata(channelName, RtmChannelType.STREAM,
                new ResultCallback<Metadata>() {
                    @Override
                    public void onSuccess(Metadata metadata) {
                        log.debug("getChannelMetadata, onSuccess, channelName:{}", channelName);

                        Metadata metadataCopy = createMetadata();
                        metadataCopy.setMajorRevision(-1);
                        MetadataItem[] metadataItems = metadata.getMetadataItems();
                        for (MetadataItem metadataItem : metadataItems) {
                            metadataItem.revision = -1;
                            metadataCopy.setMetadataItem(metadataItem);
                        }
                        resultMap.put("metadata", metadataCopy);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("getChannelMetadata, onFailure, error:{}, channelName:{}", errorInfo, channelName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("getChannelMetadata, await failed, error:{}, channelName:{}", e, channelName);
            histogramRequestTimer.observeDuration();
            return null;
        }

        histogramRequestTimer.observeDuration();
        log.debug("getChannelMetadata, end, channelName:{}, metadata:{}", channelName, resultMap.get("metadata"));
        return resultMap.get("metadata");
    }

    /**
     * Get channel metadata by key
     *
     * @param metadata
     * @param key
     * @return
     */
    public MetadataItem getChannelMetadataByKey(Metadata metadata, String key) {
        log.debug("getChannelMetadataByKey, key:{}", key);

        MetadataItem[] metadataItems = metadata.getMetadataItems();
        for (MetadataItem metadataItem : metadataItems) {
            if (Objects.equals(metadataItem.key, key)) {
                log.debug("getChannelMetadataByKey, data existed, key:{}", key);
                return metadataItem;
            }
        }

        log.debug("getChannelMetadataByKey, data not existed, key:{}", key);
        return null;
    }

    /**
     * Get RTM token
     *
     * @return
     */
    public String getRtmToken() {
        log.info("getRtmToken, appId:{}, userId:{}, tokenExpirationInSeconds:{}", appId, userId,
                tokenExpirationInSeconds);
        return tokenUtil.generateRtmToken(appId, appCertificate, userId, tokenExpirationInSeconds);
    }

    /**
     * Login
     *
     * @param rtmToken
     * @return
     */
    public Boolean login(String rtmToken) {
        Histogram.Timer histogramRequestTimer = prometheusMetric.getRtmRequestsDurationSecondsHistogram()
                .labels("login").startTimer();
        if (rtmLogin.get()) {
            log.info("login, rtm logged, appId:{}, rtmToken:{}", appId, rtmToken);
            histogramRequestTimer.observeDuration();
            return true;
        }

        log.info("login, start, appId:{}, rtmToken:{}", appId, rtmToken);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        rtmClient.login(rtmToken, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                log.info("login, rtm login success, appId:{}, rtmToken:{}", appId, rtmToken);
                rtmLogin.set(true);
                res.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                log.error("login, rtm login failed, error:{}, appId:{}, rtmToken:{}", errorInfo, appId,
                        rtmToken);
                latch.countDown();
            }
        });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("login, await failed, error:{}, appId:{}, rtmToken:{}", e, appId, rtmToken);
            histogramRequestTimer.observeDuration();
            return false;
        }

        histogramRequestTimer.observeDuration();
        log.info("login, end, res:{}, appId:{}, rtmToken:{}", res.get(), appId, rtmToken);
        return res.get();
    }

    /**
     * Print channel metadata
     *
     * @param channelName
     * @return
     */
    public void printChannelMetadata(String channelName) {
        log.info("printChannelMetadata, start, channelName:{}", channelName);

        Metadata metadata = getChannelMetadata(channelName);
        MetadataItem[] metadataItems = metadata.getMetadataItems();
        for (MetadataItem metadataItem : metadataItems) {
            log.info(
                    "printChannelMetadata, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}",
                    metadataItem.key, metadataItem.value, metadataItem.revision, metadataItem.updateTs,
                    metadataItem.authorUserId);
        }
        log.info("printChannelMetadata, end, channelName:{}, metadata:{}", channelName, metadata);
    }

    /**
     * Print channel metadata
     *
     * @param channelName
     * @param metadata
     * @return
     */
    public void printChannelMetadata(String method, String channelName, Metadata metadata) {
        log.info("printChannelMetadata-{}, start, channelName:{}", method, channelName);

        MetadataItem[] metadataItems = metadata.getMetadataItems();
        for (MetadataItem metadataItem : metadataItems) {
            log.info(
                    "printChannelMetadata-{}, metadataItems, key:{}, value:{}, revision:{}, updateTs:{}, authorUserId:{}",
                    method, metadataItem.key, metadataItem.value, metadataItem.revision, metadataItem.updateTs,
                    metadataItem.authorUserId);
        }
        log.info("printChannelMetadata-{}, end, channelName:{}, metadata:{}", method, channelName, metadata);
    }

    /**
     * Release lock
     *
     * @param channelName
     * @param lockName
     * @return
     */
    public Boolean releaseLock(String channelName, String lockName) {
        log.debug("releaseLock, start, channelName:{}, lockName:{}", channelName,
                lockName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        rtmClient.getLock().releaseLock(channelName, RtmChannelType.STREAM, lockName,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.debug("releaseLock, onSuccess, channelName:{}, lockName:{}", channelName,
                                lockName);
                        res.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("releaseLock, onFailure, error:{}, channelName:{}, lockName:{}",
                                errorInfo,
                                channelName, lockName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("releaseLock, await failed, error:{}, channelName:{}, lockName:{}",
                    e, channelName, lockName);
            return false;
        }

        log.debug("releaseLock, end, res:{}, channelName:{}, lockName:{}", res.get(),
                channelName, lockName);
        return res.get();
    }

    /**
     * Remove lock
     *
     * @param channelName
     * @param lockName
     * @return
     */
    public Boolean removeLock(String channelName, String lockName) {
        log.debug("removeLock, start, channelName:{}, lockName:{}", channelName, lockName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        rtmClient.getLock().removeLock(channelName, RtmChannelType.STREAM, lockName,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.debug("removeLock, onSuccess, channelName:{}, lockName:{}", channelName, lockName);
                        res.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("removeLock, onFailure, error:{}, channelName:{}, lockName:{}",
                                errorInfo,
                                channelName, lockName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("removeLock, await failed, error:{}, channelName:{}, lockName:{}", e, channelName, lockName);
            return false;
        }

        log.debug("removeLock, end, res:{}, channelName:{}, lockName:{}", res.get(), channelName, lockName);
        return res.get();
    }

    /**
     * Remove channel metadata
     *
     * @param channelName
     * @return
     */
    public Boolean removeChannelMetadata(String channelName) {
        Histogram.Timer histogramRequestTimer = prometheusMetric.getRtmRequestsDurationSecondsHistogram()
                .labels("removeChannelMetadata").startTimer();
        log.debug("removeChannelMetadata, start, channelName:{}", channelName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        Metadata metadata = rtmClient.getStorage().createMetadata();
        MetadataOptions options = new MetadataOptions();
        options.recordTs = true;
        rtmClient.getStorage().removeChannelMetadata(channelName, RtmChannelType.STREAM, metadata,
                options, "",
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.debug("removeChannelMetadata, onSuccess, channelName:{}", channelName);
                        res.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("removeChannelMetadata, onFailure, error:{}, channelName:{}", errorInfo, channelName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("removeChannelMetadata, await failed, error:{}, channelName:{}", e, channelName);
            histogramRequestTimer.observeDuration();
            return false;
        }

        histogramRequestTimer.observeDuration();
        log.debug("removeChannelMetadata, end, res:{}, channelName:{}", res.get(), channelName);
        return res.get();
    }

    /**
     * Renew token
     *
     * @return
     */
    public Boolean renewToken() {
        Histogram.Timer histogramRequestTimer = prometheusMetric.getRtmRequestsDurationSecondsHistogram()
                .labels("renewToken").startTimer();

        String rtmToken = getRtmToken();
        int res = rtmClient.renewToken(rtmToken);
        if (res != 0) {
            log.error("renewToken, failed, appId:{}, rtmToken:{}, res:{}", appId, rtmToken, res);
            histogramRequestTimer.observeDuration();
            return false;
        }

        histogramRequestTimer.observeDuration();
        log.info("renewToken, success, appId:{}, rtmToken:{}, res:{}", appId, rtmToken, res);
        return true;
    }

    /**
     * Set channel metadata
     *
     * @param channelName
     * @param metadata
     * @return
     */
    public Boolean setChannelMetadata(String channelName, Metadata metadata) {
        Histogram.Timer histogramRequestTimer = prometheusMetric.getRtmRequestsDurationSecondsHistogram()
                .labels("setChannelMetadata").startTimer();

        log.debug("setChannelMetadata, start, channelName:{}", channelName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        MetadataOptions options = new MetadataOptions();
        options.recordTs = true;
        rtmClient.getStorage().setChannelMetadata(channelName, RtmChannelType.STREAM, metadata, options,
                "",
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.debug("setChannelMetadata, onSuccess, channelName:{}", channelName);
                        res.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("setChannelMetadata, onFailure, error:{}, channelName:{}", errorInfo, channelName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("setChannelMetadata, await failed, error:{}, channelName:{}", e, channelName);
            histogramRequestTimer.observeDuration();
            return false;
        }

        histogramRequestTimer.observeDuration();
        log.debug("setChannelMetadata, end, res:{}, channelName:{}", res.get(), channelName);
        return res.get();
    }

    /**
     * Set lock
     *
     * @param channelName
     * @param lockName
     * @return
     */
    public Boolean setLock(String channelName, String lockName) {
        log.debug("setLock, start, channelName:{}, lockName:{}", channelName, lockName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        // 30 seconds, lock offline ttl
        rtmClient.getLock().setLock(channelName, RtmChannelType.STREAM, lockName, 30,
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.debug("setLock, onSuccess, channelName:{}, lockName:{}", channelName, lockName);
                        res.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("setLock, onFailure, error:{}, channelName:{}, lockName:{}", errorInfo, channelName,
                                lockName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("setLock, await failed, error:{}, channelName:{}, lockName:{}", e, channelName, lockName);
            return false;
        }

        log.debug("setLock, end, res:{}, channelName:{}, lockName:{}", res.get(), channelName, lockName);
        return res.get();
    }

    /**
     * Update channel metadata
     *
     * @param channelName
     * @param metadata
     * @return
     */
    public Boolean updateChannelMetadata(String channelName, Metadata metadata) {
        Histogram.Timer histogramRequestTimer = prometheusMetric.getRtmRequestsDurationSecondsHistogram()
                .labels("updateChannelMetadata").startTimer();

        log.debug("updateChannelMetadata, start, channelName:{}", channelName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean res = new AtomicBoolean(false);
        MetadataOptions options = new MetadataOptions();
        options.recordTs = true;
        rtmClient.getStorage().updateChannelMetadata(channelName, RtmChannelType.STREAM, metadata,
                options, "",
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        log.debug("updateChannelMetadata, onSuccess, channelName:{}", channelName);
                        res.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("updateChannelMetadata, onFailure, error:{}, channelName:{}", errorInfo, channelName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("updateChannelMetadata, await failed, error:{}, channelName:{}", e, channelName);
            histogramRequestTimer.observeDuration();
            return false;
        }

        histogramRequestTimer.observeDuration();
        log.debug("updateChannelMetadata, end, res:{}, channelName:{}", res.get(), channelName);
        return res.get();
    }

    /**
     * Who now
     *
     * @param channelName
     * @return
     */
    public Long whoNow(String channelName) {
        Histogram.Timer histogramRequestTimer = prometheusMetric.getRtmRequestsDurationSecondsHistogram()
                .labels("whoNow").startTimer();

        log.debug("whoNow, start, channelName:{}", channelName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong res = new AtomicLong(0L);
        PresenceOptions options = new PresenceOptions();
        options.includeUserId = true;
        options.includeState = false;
        rtmClient.getPresence().whoNow(channelName, RtmChannelType.STREAM, options,
                new ResultCallback<WhoNowResult>() {
                    @Override
                    public void onSuccess(WhoNowResult responseInfo) {
                        log.debug("whoNow, onSuccess, channelName:{}, totalOccupancy:{}", channelName,
                                responseInfo.totalOccupancy);
                        res.set(responseInfo.totalOccupancy);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        log.error("whoNow, onFailure, error:{}, channelName:{}", errorInfo, channelName);
                        latch.countDown();
                    }
                });

        try {
            latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("whoNow, await failed, error:{}, channelName:{}", e, channelName);
            histogramRequestTimer.observeDuration();
            return -1L;
        }

        histogramRequestTimer.observeDuration();
        log.debug("whoNow, end, res:{}, channelName:{}", res.get(), channelName);
        return res.get();
    }
}
