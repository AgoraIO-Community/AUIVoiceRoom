package io.agora.asceneskit.voice.binder;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIChoristerModel;
import io.agora.auikit.model.AUILoadMusicConfiguration;
import io.agora.auikit.model.AUIMicSeatInfo;
import io.agora.auikit.model.AUIPlayStatus;
import io.agora.auikit.model.AUIUserThumbnailInfo;
import io.agora.auikit.service.IAUIChorusService;
import io.agora.auikit.service.IAUIJukeboxService;
import io.agora.auikit.service.IAUIMicSeatService;
import io.agora.auikit.service.IAUIMusicPlayerService;
import io.agora.auikit.service.callback.AUIMusicLoadStateCallback;
import io.agora.auikit.service.callback.AUISwitchSingerRoleCallback;
import io.agora.auikit.ui.musicplayer.IMusicPlayerView;
import io.agora.auikit.ui.musicplayer.MusicSettingInfo;
import io.agora.auikit.ui.musicplayer.impl.AUIMusicPlayerView;

public class AUIMusicPlayerBindable implements IAUIBindable, IAUIMusicPlayerService.AUIPlayerRespDelegate, IAUIJukeboxService.AUIJukeboxRespDelegate, IAUIChorusService.AUIChorusRespDelegate, IAUIMicSeatService.AUIMicSeatRespDelegate, IMusicPlayerView.ActionDelegate {

    private final AUIMusicPlayerView musicPlayerView;
    private final IAUIMusicPlayerService musicPlayerService;
    private final IAUIJukeboxService jukeboxService;
    private final IAUIChorusService chorusService;
    private final IAUIMicSeatService micSeatService;

    private AUIChooseMusicModel songPlayingModel = null;

    enum SingRole {
        SOLO_SINGER,
        CO_SINGER,
        AUDIENCE,
    }
    private SingRole mRole = SingRole.AUDIENCE;

    enum PlayerMusicStatus {
        ON_PLAYING,
        ON_PAUSE
    }
    final MutableLiveData<PlayerMusicStatus> playerMusicStatusLiveData = new MutableLiveData<>();

    private boolean isRoomOwner = false;
    private boolean isOnSeat = false;
    private boolean isOriginal = false;
    private String localUid;

    public AUIMusicPlayerBindable(AUIMusicPlayerView musicPlayerView, IAUIMusicPlayerService musicPlayerService, IAUIJukeboxService jukeboxService, IAUIChorusService chorusService, IAUIMicSeatService micSeatService) {
        this.musicPlayerView = musicPlayerView;
        this.musicPlayerService = musicPlayerService;
        this.jukeboxService = jukeboxService;
        this.chorusService = chorusService;
        this.micSeatService = micSeatService;
    }

    @Override
    public void bind() {
        musicPlayerService.bindRespDelegate(this);
        jukeboxService.bindRespDelegate(this);
        chorusService.bindRespDelegate(this);
        micSeatService.bindRespDelegate(this);

        musicPlayerView.setMusicPlayerActionDelegate(this);
        musicPlayerView.setEffectProperties(musicPlayerService.effectProperties());
        // 音效设置默认值
        MusicSettingInfo musicSettingInfo = new MusicSettingInfo(false,100,50,0,0);
        musicPlayerView.setMusicSettingInfo(musicSettingInfo);

        if (musicPlayerService.getRoomContext().isRoomOwner(musicPlayerService.getChannelName())) {
            isOnSeat = true;
            isRoomOwner = true;
        }
        // TODO musicPlayerService.getChannelName() is always null
        //isRoomOwner = true;
        localUid = musicPlayerService.getRoomContext().currentUserInfo.userId;

        initUI();
        initSong();
    }

    @Override
    public void unBind() {
        musicPlayerView.setMusicPlayerActionDelegate(null);
        musicPlayerService.unbindRespDelegate(this);
        jukeboxService.unbindRespDelegate(this);
        chorusService.unbindRespDelegate(this);
        micSeatService.unbindRespDelegate(this);
    }

    private void initUI() {
        if (isRoomOwner) {
            musicPlayerView.initRoomOwnerUI();
        }
    }

    private void initSong() {
        jukeboxService.getAllChooseSongList((error, songList) -> {
            if (error == null && songList != null && !songList.isEmpty()) {
                AUIChooseMusicModel localSong = songList.get(0);
                songPlayingModel = localSong;

                // 设置身份
                if (localSong.owner != null && localSong.owner.userId.equals(localUid)) {
                    mRole = SingRole.SOLO_SINGER;
                    mRole = SingRole.SOLO_SINGER;
                } else {
                    mRole = SingRole.AUDIENCE;
                }

                // 更新歌曲播放UI、播放歌曲
                isOriginal = false;
                musicPlayerView.onMusicPrepare(localSong.songCode, mRole == SingRole.AUDIENCE, isRoomOwner);
                musicStartPlay(localSong);
            }
        });
    }

    // ChorusService delegate implement
    @Override
    public void onChoristerDidEnter(AUIChoristerModel chorister) {

    }

    @Override
    public void onChoristerDidLeave(AUIChoristerModel chorister) {

    }

    @Override
    public void onSingerRoleChanged(int oldRole, int newRole) {

    }

    @Override
    public void onChoristerDidChanged() {
        chorusService.getChoristersList((error, songList) -> {
            if (mRole != SingRole.SOLO_SINGER) return;
            if (songList != null && songList.size() > 0) {
                chorusService.switchSingerRole(2, null);
            } else {
                chorusService.switchSingerRole(0, null);
            }
        });
    }

    // JukeboxService delegate implement
    @Override
    public void onAddChooseSong(@NonNull AUIChooseMusicModel song) {
        IAUIJukeboxService.AUIJukeboxRespDelegate.super.onAddChooseSong(song);
    }

    @Override
    public void onRemoveChooseSong(@NonNull AUIChooseMusicModel song) {
        IAUIJukeboxService.AUIJukeboxRespDelegate.super.onRemoveChooseSong(song);
    }

    @Override
    public void onUpdateChooseSong(@NonNull AUIChooseMusicModel song) {
        IAUIJukeboxService.AUIJukeboxRespDelegate.super.onUpdateChooseSong(song);
    }

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUIChooseMusicModel> songs) {
        IAUIJukeboxService.AUIJukeboxRespDelegate.super.onUpdateAllChooseSongs(songs);

        // 当前无已点歌曲
        if (songs.size() == 0) {
            mRole = SingRole.AUDIENCE;
            musicPlayerService.stopSing();
            songPlayingModel = null;
            musicPlayerView.onMusicIdle(isOnSeat);
            return;
        }

        AUIChooseMusicModel newTopSong = songs.get(0);
        if (newTopSong == null) return;
        if (songPlayingModel != null && songPlayingModel.songCode.equals(newTopSong.songCode)) return;

        if (songPlayingModel == null) {
            songPlayingModel = newTopSong;
        } else {
            musicPlayerService.stopSing();
            songPlayingModel = newTopSong;
        }

        // 设置身份
        if (newTopSong.owner != null && newTopSong.owner.userId.equals(localUid)) {
            mRole = SingRole.SOLO_SINGER;
        } else {
            mRole = SingRole.AUDIENCE;
        }

        // 更新歌曲播放UI、播放歌曲
        isOriginal = false;
        musicPlayerView.onMusicPrepare(newTopSong.songCode, mRole == SingRole.AUDIENCE, isRoomOwner);
        musicStartPlay(newTopSong);
    }

    // micSeatService delegate implement
    @Override
    public void onSeatListChange(List<AUIMicSeatInfo> seatInfoList) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onSeatListChange(seatInfoList);
    }

    @Override
    public void onAnchorEnterSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onAnchorEnterSeat(seatIndex, userInfo);
        if (userInfo.userId.equals(localUid)) {
            isOnSeat = true;
            musicPlayerView.onSeat();
        }
    }

    @Override
    public void onAnchorLeaveSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onAnchorLeaveSeat(seatIndex, userInfo);
        if (userInfo.userId.equals(localUid)) {
            isOnSeat = false;
            // 离开合唱
            if (mRole == SingRole.CO_SINGER) {
                innerLeaveChorus();
            }
            musicPlayerView.onLeaveSeat();
        }
    }

    @Override
    public void onSeatAudioMute(int seatIndex, boolean isMute) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onSeatAudioMute(seatIndex, isMute);
    }

    @Override
    public void onSeatVideoMute(int seatIndex, boolean isMute) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onSeatVideoMute(seatIndex, isMute);
    }

    @Override
    public void onSeatClose(int seatIndex, boolean isClose) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onSeatClose(seatIndex, isClose);
    }

    // musicPlayerService delegate implement
    @Override
    public void onPreludeDidAppear() {
        IAUIMusicPlayerService.AUIPlayerRespDelegate.super.onPreludeDidAppear();
    }

    @Override
    public void onPreludeDidDisappear() {
        IAUIMusicPlayerService.AUIPlayerRespDelegate.super.onPreludeDidDisappear();
    }

    @Override
    public void onPostludeDidAppear() {
        IAUIMusicPlayerService.AUIPlayerRespDelegate.super.onPostludeDidAppear();
    }

    @Override
    public void onPostludeDidDisappear() {
        IAUIMusicPlayerService.AUIPlayerRespDelegate.super.onPostludeDidDisappear();
    }

    @Override
    public void onPlayerPositionDidChange(Long position) {
        IAUIMusicPlayerService.AUIPlayerRespDelegate.super.onPlayerPositionDidChange(position);
        musicPlayerView.setProgress(position);
    }

    @Override
    public void onPitchDidChange(Float pitch) {
        IAUIMusicPlayerService.AUIPlayerRespDelegate.super.onPitchDidChange(pitch);
        musicPlayerView.setPitch(pitch);
    }

    @Override
    public void onPlayerStateChanged(int state, boolean isLocal) {
        IAUIMusicPlayerService.AUIPlayerRespDelegate.super.onPlayerStateChanged(state, isLocal);

        if (state == 4) { // pause
            musicPlayerView.onPlaying();
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
        } else if (state == 3) { // playing
            musicPlayerView.onPause();
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
        } else if (state == 6 && songPlayingModel != null && songPlayingModel.owner != null && songPlayingModel.owner.userId.equals(localUid)) { // ALL Loop Completed
            jukeboxService.removeSong(songPlayingModel.songCode, null);
        }
    }

    // actionDelegate
    @Override
    public void onClickJoinChorus() {
        innerJoinChorus();
    }

    @Override
    public void onClickLeaveChorus() {
        innerLeaveChorus();
    }

    @Override
    public void onClickSwitchSong() {
        if (songPlayingModel != null) {
            jukeboxService.removeSong(songPlayingModel.songCode, null);
        }
    }

    @Override
    public void onClickPlaying() {
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            musicPlayerService.pauseSing();
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            musicPlayerService.resumeSing();
        }
    }

    @Override
    public void onClickOriginal() {
        if (this.isOriginal) {
            musicPlayerService.selectMusicPlayerTrackMode(1);
            musicPlayerView.onAcc();
        } else {
            musicPlayerService.selectMusicPlayerTrackMode(0);
            musicPlayerView.onOriginal();
        }
        this.isOriginal = !isOriginal;
    }

    @Override
    public void onEarMonitoring(boolean enable) {
        musicPlayerService.enableEarMonitoring(enable);
    }

    @Override
    public void onSignalVolume(int volume) {
        musicPlayerService.adjustRecordingSignal(volume);
    }

    @Override
    public void onMusicVolume(int volume) {
        musicPlayerService.adjustMusicPlayerPlayoutVolume(volume);
        musicPlayerService.adjustMusicPlayerPublishVolume(volume);
    }

    @Override
    public void onMusicPitch(int pitch) {
        musicPlayerService.setAudioPitch(pitch);
    }

    @Override
    public void onAudioEffect(int effectId) {
        musicPlayerService.setAudioEffectPreset(effectId);
    }

    private void musicStartPlay(AUIChooseMusicModel newSong) {
        AUILoadMusicConfiguration config = new AUILoadMusicConfiguration();
        if (mRole == SingRole.SOLO_SINGER) {
            // 主唱
            config.autoPlay = true;
            config.mainSingerUid = Integer.parseInt(newSong.owner.userId);
            config.loadMusicMode = 2;
            musicPlayerService.loadMusic(Long.parseLong(newSong.songCode), config, new AUIMusicLoadStateCallback() {
                @Override
                public void onMusicLoadSuccess(Long songCode, String lyricUrl) {
                    if (songPlayingModel == null || !songPlayingModel.songCode.equals(songCode.toString())) {
                        // 当前无歌曲了或不是当前歌曲了
                        return;
                    }
                    musicPlayerView.downloadAndSetLrcData(lyricUrl);
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                    jukeboxService.updatePlayStatus(songCode.toString(), AUIPlayStatus.playing, null);
                    musicPlayerView.onMusicPlaying();
                }

                @Override
                public void onMusicLoadFail(Long songCode, int reason) {
                    musicPlayerView.onMusicPrepareFailed();
                }

                @Override
                public void onMusicLoadProgress(Long songCode, int percent, int status, String msg, String lyricUrl) {
                    if (songPlayingModel != null && songPlayingModel.songCode.equals(songCode.toString())) {
                        musicPlayerView.setLoadProgress(percent);
                    }
                }
            });
        } else {
            // 观众
            config.autoPlay = false;
            config.mainSingerUid = Integer.parseInt(newSong.owner.userId);
            config.loadMusicMode = 1;
            musicPlayerService.loadMusic(Long.parseLong(newSong.songCode), config, new AUIMusicLoadStateCallback() {
                @Override
                public void onMusicLoadSuccess(Long songCode, String lyricUrl) {
                    if (songPlayingModel == null || !songPlayingModel.songCode.equals(songCode.toString())) {
                        // 当前无歌曲了或不是当前歌曲了
                        return;
                    }

                    musicPlayerView.downloadAndSetLrcData(lyricUrl);
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                    musicPlayerView.onMusicPlaying();
                }

                @Override
                public void onMusicLoadFail(Long songCode, int reason) {
                    musicPlayerView.onMusicPrepareFailed();
                }

                @Override
                public void onMusicLoadProgress(Long songCode, int percent, int status, String msg, String lyricUrl) {
                    if (songPlayingModel != null && songPlayingModel.songCode.equals(songCode.toString())) {
                        musicPlayerView.setLoadProgress(percent);
                    }
                }
            });
        }
    }

    private void innerJoinChorus() {
        // 不在麦上需要自动上麦
        if (songPlayingModel == null) {
            musicPlayerView.onJoinChorusFailed();
            return;
        }
        if (!isOnSeat) {
            micSeatService.autoEnterSeat(error -> {
                if (error == null) {
                    joinChorusWithSongCode(songPlayingModel.songCode);
                } else {
                    musicPlayerView.onJoinChorusFailed();
                }
            });
        } else  {
            joinChorusWithSongCode(songPlayingModel.songCode);
        }
    }
    private void joinChorusWithSongCode(String code) {
        chorusService.joinChorus(code, chorusService.getRoomContext().currentUserInfo.userId, error -> {
            if (error == null) {
                AUILoadMusicConfiguration config = new AUILoadMusicConfiguration();
                config.autoPlay = false;
                config.mainSingerUid = Integer.parseInt(songPlayingModel.owner.userId);
                config.loadMusicMode = 0;  // LOAD Music Only
                musicPlayerService.loadMusic(Long.valueOf(songPlayingModel.songCode), config, new AUIMusicLoadStateCallback() {
                    @Override
                    public void onMusicLoadSuccess(Long songCode, String lyricUrl) {
                        chorusService.switchSingerRole(1, new AUISwitchSingerRoleCallback() {
                            @Override
                            public void onSwitchRoleSuccess() {
                                mRole = SingRole.CO_SINGER;
                                musicPlayerView.onJoinChorus(isRoomOwner);
                            }

                            @Override
                            public void onSwitchRoleFail(int reason) {

                            }
                        });
                    }

                    @Override
                    public void onMusicLoadFail(Long songCode, int reason) {

                    }

                    @Override
                    public void onMusicLoadProgress(Long songCode, int percent, int status, String msg, String lyricUrl) {

                    }
                });
            }
        });
    }

    private void innerLeaveChorus() {
        if (songPlayingModel == null) return;

        chorusService.leaveChorus(songPlayingModel.songCode, chorusService.getRoomContext().currentUserInfo.userId, error -> {
            if (error == null) {
                chorusService.switchSingerRole(3, new AUISwitchSingerRoleCallback() {
                    @Override
                    public void onSwitchRoleSuccess() {
                        mRole = SingRole.AUDIENCE;
                        musicPlayerView.onLeaveChorus();
                    }

                    @Override
                    public void onSwitchRoleFail(int reason) {

                    }
                });
            }
        });
    }
}
