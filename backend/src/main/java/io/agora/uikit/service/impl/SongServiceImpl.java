package io.agora.uikit.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.SongDomain;
import io.agora.uikit.bean.domain.SongOwnerDomain;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.enums.SongStatusEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.SongAddReq;
import io.agora.uikit.bean.req.SongPinReq;
import io.agora.uikit.bean.req.SongPlayReq;
import io.agora.uikit.bean.req.SongRemoveReq;
import io.agora.uikit.bean.req.SongStopReq;
import io.agora.uikit.service.IChorusService;
import io.agora.uikit.service.IMicSeatService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.service.ISongService;
import io.agora.uikit.utils.RtmUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SongServiceImpl implements ISongService {
    @Autowired
    private RtmUtil rtmUtil;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private IMicSeatService micSeatService;
    @Autowired
    @Lazy(true)
    private IChorusService chorusService;

    // Metadata key
    public static final String METADATA_KEY = "song";

    /**
     * Add song
     * 
     * @param songAddReq
     * @return
     */
    @Override
    public void add(SongAddReq songAddReq) throws Exception {
        log.info("add, start, songAddReq:{}", songAddReq);

        // Acquire lock
        roomService.acquireLock(songAddReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(songAddReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("add", metadataItem, songAddReq.getRoomId(), songAddReq);
        // Check whether not on mic seat no
        micSeatService.checkMicSeatNoNotOn("add", metadata, songAddReq.getRoomId(),
                songAddReq.getOwner().getUserId());
        // Check song whether exists
        checkSongExists("add", metadataItem, songAddReq.getRoomId(), songAddReq.getSongCode());

        // Get song list
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        // Set data
        SongOwnerDomain songOwnerDomain = new SongOwnerDomain();
        songOwnerDomain.setUserId(songAddReq.getOwner().getUserId())
                .setUserName(songAddReq.getOwner().getUserName())
                .setUserAvatar(songAddReq.getOwner().getUserAvatar());
        SongDomain songDomain = new SongDomain();
        songDomain.setSongCode(songAddReq.getSongCode())
                .setName(songAddReq.getName())
                .setSinger(songAddReq.getSinger())
                .setPoster(songAddReq.getPoster())
                .setReleaseTime(songAddReq.getReleaseTime())
                .setDuration(songAddReq.getDuration())
                .setMusicUrl(songAddReq.getMusicUrl())
                .setLrcUrl(songAddReq.getLrcUrl())
                .setOwner(songOwnerDomain)
                .setStatus(SongStatusEnum.SONG_STATUS_NOT_PLAYING)
                .setPinAt((long) 0)
                .setCreateAt(System.currentTimeMillis());
        songList.add(songDomain);
        metadataItem.key = METADATA_KEY;
        metadataItem.value = JSON.toJSONString(songList);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("add", songAddReq.getRoomId(), metadata, metadataItem,
                songAddReq.getRoomId(), songAddReq);

        // Release lock
        roomService.releaseLock(songAddReq.getRoomId());
        log.info("add, success, songAddReq:{}, metadataItem.value:{}", songAddReq, metadataItem.value);
    }

    /**
     * Check song whether exists
     * 
     * @param method
     * @param metadataItem
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    @Override
    public void checkSongExists(String method, MetadataItem metadataItem, String roomId, String songCode)
            throws Exception {
        SongDomain songDomain = getSongSingle(metadataItem, songCode);
        if (songDomain != null) {
            log.error("checkSongExists-{}, song already exists, songCode:{}", method, songCode);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.SONG_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Check song whether not exists
     * 
     * @param method
     * @param songDomain
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    @Override
    public void checkSongNotExists(String method, SongDomain songDomain, String roomId, String songCode)
            throws Exception {
        if (songDomain == null) {
            log.error("checkSongNotExists-{}, song not exists, songCode:{}", method, songCode);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.SONG_NOT_EXISTS_ERROR);
        }
    }

    /**
     * Check song whether not exists
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    @Override
    public void checkSongNotExists(String method, Metadata metadata, String roomId, String songCode)
            throws Exception {
        SongDomain songDomain = getSongSingle(metadata, songCode);
        checkSongNotExists(method, songDomain, roomId, songCode);
    }

    /**
     * Check song whether not exists
     * 
     * @param method
     * @param metadataItem
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    @Override
    public void checkSongNotExists(String method, MetadataItem metadataItem, String roomId, String songCode)
            throws Exception {
        SongDomain songDomain = getSongSingle(metadataItem, songCode);
        checkSongNotExists(method, songDomain, roomId, songCode);
    }

    /**
     * Check user whether song or room owner
     * 
     * @param method
     * @param metadata
     * @param songList
     * @param roomId
     * @param userId
     * @param songCode
     * @throws Exception
     */
    @Override
    public void checkUserSongOrRoomOwner(String method, Metadata metadata, List<SongDomain> songList, String roomId,
            String userId, String songCode) throws Exception {
        if (roomService.isOwner("checkUserSongOrRoomOwner", metadata, roomId, userId)) {
            return;
        }

        SongDomain songDomain = getSongSingle(songList, songCode);
        if (songDomain != null && !Objects.equals(songDomain.getOwner().getUserId(), userId)) {
            log.error("checkUserSongOrRoomOwner-{}, not song owner, roomId:{}, userId:{}, songCode:{}", method, roomId,
                    userId, songCode);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.SONG_NOT_OWNER_ERROR);
        }
    }

    /**
     * Check song status whether not playing
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    @Override
    public void checkSongStatusNotPlaying(String method, Metadata metadata, String roomId, String songCode)
            throws Exception {
        SongDomain songDomain = getSong(metadata, songCode, roomId);
        if (Objects.equals(songDomain.getStatus(), SongStatusEnum.SONG_STATUS_NOT_PLAYING)) {
            log.error("checkSongStatusNotPlaying-{}, song not playing, roomId:{}, songCode:{}", method, roomId,
                    songCode);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.SONG_NOT_PLAYING_ERROR);
        }
    }

    /**
     * Check song status whether not playing
     * 
     * @param method
     * @param songList
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    @Override
    public void checkSongStatusNotPlaying(String method, List<SongDomain> songList, String roomId, String songCode)
            throws Exception {
        SongDomain songDomain = getSongSingle(songList, songCode);
        if (Objects.equals(songDomain.getStatus(), SongStatusEnum.SONG_STATUS_NOT_PLAYING)) {
            log.error("checkSongStatusNotPlaying-{}, song not playing, roomId:{}, songCode:{}", method, roomId,
                    songCode);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.SONG_NOT_PLAYING_ERROR);
        }
    }

    /**
     * Check song status whether playing
     * 
     * @param method
     * @param songList
     * @param roomId
     * @throws Exception
     */
    @Override
    public void checkSongStatusPlaying(String method, List<SongDomain> songList, String roomId)
            throws Exception {
        Optional<SongDomain> songDomain = songList.stream()
                .filter(o -> Objects.equals(o.getStatus(), SongStatusEnum.SONG_STATUS_PLAYING))
                .findFirst();
        if (songDomain.isPresent()) {
            log.error("checkSongStatusPlaying-{}, a song already playing, roomId:{}", method, roomId);
            roomService.releaseLock(roomId);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.SONG_ALREADY_PLAYING_ERROR);
        }
    }

    /**
     * Check song status whether playing
     * 
     * @param songList
     * @param songCode
     * @throws Exception
     */
    @Override
    public Boolean checkSongStatusPlaying(List<SongDomain> songList, String songCode) {
        SongDomain songDomain = getSongSingle(songList, songCode);
        if (songDomain != null && Objects.equals(songDomain.getStatus(), SongStatusEnum.SONG_STATUS_PLAYING)) {
            return true;
        }
        return false;
    }

    /**
     * Clear song by user
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @return
     */
    @Override
    public void clearByUser(String method, Metadata metadata, String roomId, String userId) throws Exception {
        log.info("clearByUser-{}, start, roomId:{}, userId:{}", method, roomId, userId);

        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        if (metadataItem == null) {
            return;
        }

        // Get data
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        // Remove song
        List<SongDomain> songListNew = songList.stream()
                .filter(o -> !Objects.equals(o.getOwner().getUserId(), userId))
                .collect(Collectors.toList());

        metadataItem.value = JSON.toJSONString(songListNew);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("clearByUser", roomId, metadata, metadataItem, roomId, null);
        log.info("clearByUser-{}, success, roomId:{}, userId:{}", method, roomId, userId);
    }

    /**
     * Create metadata
     * 
     * @param metadata
     * @param songAddReq
     */
    @Override
    public void createMetadata(Metadata metadata, SongAddReq songAddReq) {
        log.info("createMetadata, songAddReq:{}", songAddReq);

        MetadataItem metadataItem = new MetadataItem();
        metadataItem.key = METADATA_KEY;
        metadataItem.value = JSON.toJSONString(new ArrayList<>());
        metadata.setMetadataItem(metadataItem);
    }

    /**
     * Get song
     * 
     * @param metadata
     * @param songCode
     */
    @Override
    public SongDomain getSong(Metadata metadata, String songCode) throws Exception {
        return getSong(metadata, songCode, null);
    }

    /**
     * Get song
     * 
     * @param metadata
     * @param songCode
     * @param roomId
     */
    @Override
    public SongDomain getSong(Metadata metadata, String songCode, String roomId) throws Exception {
        SongDomain songDomain = getSongSingle(metadata, songCode);
        if (songDomain == null) {
            log.error("getSong, song not existed, songCode:{}, roomId:{}", songCode, roomId);
            if (roomId != null) {
                roomService.releaseLock(roomId);
            }
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.RTM_METADATA_NO_DATA_ERROR);
        }

        return songDomain;
    }

    /**
     * Get song single
     * 
     * @param metadata
     * @param songCode
     */
    @Override
    public SongDomain getSongSingle(Metadata metadata, String songCode) {
        log.info("getSongSingle, start, songCode:{}", songCode);

        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        if (metadataItem == null) {
            log.info("getSongSingle, metadata no data, songCode:{}", songCode);
            return null;
        }

        return getSongSingle(metadataItem, songCode);
    }

    /**
     * Get song single
     * 
     * @param metadataItem
     * @param songCode
     */
    @Override
    public SongDomain getSongSingle(MetadataItem metadataItem, String songCode) {
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        return getSongSingle(songList, songCode);
    }

    /**
     * Get song single
     * 
     * @param songList
     * @param songCode
     */
    @Override
    public SongDomain getSongSingle(List<SongDomain> songList, String songCode) {
        log.info("getSongSingle, start, songCode:{}", songCode);

        Optional<SongDomain> songDomain = songList.stream().filter(o -> Objects.equals(o.getSongCode(), songCode))
                .findFirst();
        if (songDomain.isPresent()) {
            return songDomain.get();
        }

        log.info("getSongSingle, song not existed, songCode:{}", songCode);
        return null;
    }

    /**
     * Pin song
     * 
     * @param songPinReq
     * @return
     */
    @Override
    public void pin(SongPinReq songPinReq) throws Exception {
        log.info("pin, start, songPinReq:{}", songPinReq);

        // Acquire lock
        roomService.acquireLock(songPinReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(songPinReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("pin", metadataItem, songPinReq.getRoomId(), songPinReq);
        // Check song whether not exists
        checkSongNotExists("pin", metadataItem, songPinReq.getRoomId(), songPinReq.getSongCode());
        // Check whether owner
        roomService.checkIsOwner("pin", metadata, songPinReq.getRoomId(), songPinReq.getUserId());

        // Set data
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        songList.stream().filter(o -> Objects.equals(o.getSongCode(), songPinReq.getSongCode()))
                .forEach(o -> o.setPinAt(System.currentTimeMillis()));
        // Sort
        List<SongDomain> songListNew = songList.stream()
                .filter(o -> Objects.equals(o.getStatus(), SongStatusEnum.SONG_STATUS_PLAYING))
                .collect(Collectors.toList());
        List<SongDomain> songListNotPlaying = songList.stream()
                .filter(o -> Objects.equals(o.getStatus(), SongStatusEnum.SONG_STATUS_NOT_PLAYING))
                .sorted(Comparator.comparing(SongDomain::getPinAt).reversed()
                        .thenComparing(SongDomain::getCreateAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        songListNew.addAll(songListNotPlaying);
        metadataItem.value = JSON.toJSONString(songListNew);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("pin", songPinReq.getRoomId(), metadata, metadataItem,
                songPinReq.getRoomId(), songPinReq);

        // Release lock
        roomService.releaseLock(songPinReq.getRoomId());
        log.info("pin, songPinReq:{}, metadataItem.value:{}", songPinReq, metadataItem.value);
    }

    /**
     * Play song
     * 
     * @param songPlayReq
     * @return
     */
    @Override
    public void play(SongPlayReq songPlayReq) throws Exception {
        log.info("play, start, songPlayReq:{}", songPlayReq);

        // Acquire lock
        roomService.acquireLock(songPlayReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(songPlayReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("play", metadataItem, songPlayReq.getRoomId(), songPlayReq);
        // Check song whether not exists
        checkSongNotExists("play", metadataItem, songPlayReq.getRoomId(), songPlayReq.getSongCode());
        // Get song list
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        // Check user whether song or room owner
        checkUserSongOrRoomOwner("play", metadata, songList, songPlayReq.getRoomId(), songPlayReq.getUserId(),
                songPlayReq.getSongCode());
        // Check song status whether playing
        checkSongStatusPlaying("play", songList, songPlayReq.getRoomId());
        // Set data
        songList.stream().filter(o -> Objects.equals(o.getSongCode(), songPlayReq.getSongCode()))
                .forEach(o -> o.setStatus(SongStatusEnum.SONG_STATUS_PLAYING));
        metadataItem.value = JSON.toJSONString(songList);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("play", songPlayReq.getRoomId(), metadata, metadataItem,
                songPlayReq.getRoomId(), songPlayReq);

        // Release lock
        roomService.releaseLock(songPlayReq.getRoomId());
        log.info("play, success, songPlayReq:{}, metadataItem.value:{}", songPlayReq,
                metadataItem.value);
    }

    /**
     * Remove song
     * 
     * @param songRemoveReq
     * @return
     */
    @Override
    public void remove(SongRemoveReq songRemoveReq) throws Exception {
        log.info("remove, start, songRemoveReq:{}", songRemoveReq);

        // Acquire lock
        roomService.acquireLock(songRemoveReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(songRemoveReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("remove", metadataItem, songRemoveReq.getRoomId(), songRemoveReq);
        // Check song whether not exists
        checkSongNotExists("remove", metadataItem, songRemoveReq.getRoomId(), songRemoveReq.getSongCode());
        // Get data
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        // Check user whether song or room owner
        checkUserSongOrRoomOwner("remove", metadata, songList, songRemoveReq.getRoomId(), songRemoveReq.getUserId(),
                songRemoveReq.getSongCode());
        // Remove song
        List<SongDomain> songListNew = songList.stream()
                .filter(o -> !Objects.equals(o.getSongCode(), songRemoveReq.getSongCode()))
                .collect(Collectors.toList());
        metadataItem.value = JSON.toJSONString(songListNew);
        metadata.setMetadataItem(metadataItem);

        // Remove chorus
        if (checkSongStatusPlaying(songList, songRemoveReq.getSongCode())) {
            chorusService.clear(metadata);
        }

        // Update data
        roomService.updateMetadata("remove", songRemoveReq.getRoomId(), metadata, metadataItem,
                songRemoveReq.getRoomId(), songRemoveReq);

        // Release lock
        roomService.releaseLock(songRemoveReq.getRoomId());
        log.info("remove, success, songRemoveReq:{}, metadataItem.value:{}", songRemoveReq, metadataItem.value);
    }

    /**
     * Stop song
     * 
     * @param songStopReq
     * @return
     */
    @Override
    public void stop(SongStopReq songStopReq) throws Exception {
        log.info("stop, start, songPlayReq:{}", songStopReq);

        // Acquire lock
        roomService.acquireLock(songStopReq.getRoomId());
        Metadata metadata = rtmUtil.getChannelMetadata(songStopReq.getRoomId());
        MetadataItem metadataItem = rtmUtil.getChannelMetadataByKey(metadata, METADATA_KEY);
        // Check data
        roomService.checkMetadata("stop", metadataItem, songStopReq.getRoomId(), songStopReq);
        // Check song whether not exists
        checkSongNotExists("stop", metadataItem, songStopReq.getRoomId(), songStopReq.getSongCode());
        // Get song list
        List<SongDomain> songList = JSON.parseArray(metadataItem.value, SongDomain.class);
        // Check user whether song or room owner
        checkUserSongOrRoomOwner("stop", metadata, songList, songStopReq.getRoomId(), songStopReq.getUserId(),
                songStopReq.getSongCode());
        // Check song status whether not playing
        checkSongStatusNotPlaying("stop", songList, songStopReq.getRoomId(), songStopReq.getSongCode());
        // Set data
        songList.stream().filter(o -> Objects.equals(o.getSongCode(), songStopReq.getSongCode()))
                .forEach(o -> o.setStatus(SongStatusEnum.SONG_STATUS_NOT_PLAYING));
        metadataItem.value = JSON.toJSONString(songList);
        metadata.setMetadataItem(metadataItem);

        // Update data
        roomService.updateMetadata("stop", songStopReq.getRoomId(), metadata, metadataItem,
                songStopReq.getRoomId(), songStopReq);

        // Release lock
        roomService.releaseLock(songStopReq.getRoomId());
        log.info("stop, success, songStopReq:{}, metadataItem.value:{}", songStopReq,
                metadataItem.value);
    }
}
