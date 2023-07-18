package io.agora.uikit.service;

import java.util.List;

import io.agora.rtm.Metadata;
import io.agora.rtm.MetadataItem;
import io.agora.uikit.bean.domain.SongDomain;
import io.agora.uikit.bean.req.SongAddReq;
import io.agora.uikit.bean.req.SongPinReq;
import io.agora.uikit.bean.req.SongPlayReq;
import io.agora.uikit.bean.req.SongRemoveReq;
import io.agora.uikit.bean.req.SongStopReq;

public interface ISongService extends IService<SongAddReq> {
    /**
     * Add song
     * 
     * @param songAddReq
     * @return
     */
    void add(SongAddReq songAddReq) throws Exception;

    /**
     * Check song whether exists
     * 
     * @param method
     * @param metadataItem
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    void checkSongExists(String method, MetadataItem metadataItem, String roomId, String songCode)
            throws Exception;

    /**
     * Check song whether not exists
     * 
     * @param method
     * @param songDomain
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    void checkSongNotExists(String method, SongDomain songDomain, String roomId, String songCode)
            throws Exception;

    /**
     * Check song whether not exists
     * 
     * @param method
     * @param metadataItem
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    void checkSongNotExists(String method, MetadataItem metadataItem, String roomId, String songCode)
            throws Exception;

    /**
     * Check song whether not exists
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    void checkSongNotExists(String method, Metadata metadata, String roomId, String songCode)
            throws Exception;

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
    void checkUserSongOrRoomOwner(String method, Metadata metadata, List<SongDomain> songList, String roomId,
            String userId, String songCode) throws Exception;

    /**
     * Check song status whether not playing
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    void checkSongStatusNotPlaying(String method, Metadata metadata, String roomId, String songCode)
            throws Exception;

    /**
     * Check song status whether not playing
     * 
     * @param method
     * @param songList
     * @param roomId
     * @param songCode
     * @throws Exception
     */
    void checkSongStatusNotPlaying(String method, List<SongDomain> songList, String roomId, String songCode)
            throws Exception;

    /**
     * Check song status whether playing
     * 
     * @param method
     * @param songList
     * @param roomId
     * @throws Exception
     */
    void checkSongStatusPlaying(String method, List<SongDomain> songList, String roomId)
            throws Exception;

    /**
     * Check song status whether playing
     * 
     * @param songList
     * @param songCode
     * @throws Exception
     */
    Boolean checkSongStatusPlaying(List<SongDomain> songList, String songCode);

    /**
     * Clear song by user
     * 
     * @param method
     * @param metadata
     * @param roomId
     * @param userId
     * @return
     */
    void clearByUser(String method, Metadata metadata, String roomId, String userId) throws Exception;

    /**
     * Get song
     * 
     * @param metadata
     * @param songCode
     * @return
     * @throws Exception
     */
    SongDomain getSong(Metadata metadata, String songCode) throws Exception;

    /**
     * Get song
     * 
     * @param metadata
     * @param songCode
     * @param roomId
     * @return
     * @throws Exception
     */
    SongDomain getSong(Metadata metadata, String songCode, String roomId) throws Exception;

    /**
     * Get song
     * 
     * @param metadata
     * @param songCode
     * @return
     * @throws Exception
     */
    SongDomain getSongSingle(Metadata metadata, String songCode);

    /**
     * Get song single
     * 
     * @param metadataItem
     * @param songCode
     */
    SongDomain getSongSingle(MetadataItem metadataItem, String songCode);

    /**
     * Get song single
     * 
     * @param songList
     * @param songCode
     */
    SongDomain getSongSingle(List<SongDomain> songList, String songCode);

    /**
     * Pin song
     * 
     * @param songPinReq
     * @return
     */
    void pin(SongPinReq songPinReq) throws Exception;

    /**
     * Play song
     * 
     * @param songPlayReq
     * @return
     */
    void play(SongPlayReq songPlayReq) throws Exception;

    /**
     * Remove song
     * 
     * @param songRemoveReq
     * @return
     */
    void remove(SongRemoveReq songRemoveReq) throws Exception;

    /**
     * Stop song
     * 
     * @param songStopReq
     * @return
     */
    void stop(SongStopReq songStopReq) throws Exception;
}
