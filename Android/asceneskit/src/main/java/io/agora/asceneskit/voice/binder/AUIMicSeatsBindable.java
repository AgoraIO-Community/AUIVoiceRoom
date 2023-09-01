package io.agora.asceneskit.voice.binder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.agora.asceneskit.R;
import io.agora.asceneskit.voice.AUIVoiceRoomService;
import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIChoristerModel;
import io.agora.auikit.model.AUIMicSeatInfo;
import io.agora.auikit.model.AUIMicSeatStatus;
import io.agora.auikit.model.AUIRoomContext;
import io.agora.auikit.model.AUIUserInfo;
import io.agora.auikit.model.AUIUserThumbnailInfo;
import io.agora.auikit.service.IAUIChorusService;
import io.agora.auikit.service.IAUIInvitationService;
import io.agora.auikit.service.IAUIJukeboxService;
import io.agora.auikit.service.IAUIMicSeatService;
import io.agora.auikit.service.IAUIUserService;
import io.agora.auikit.service.callback.AUICallback;
import io.agora.auikit.service.callback.AUIChooseSongListCallback;
import io.agora.auikit.service.callback.AUIChoristerListCallback;
import io.agora.auikit.service.callback.AUIException;
import io.agora.auikit.ui.basic.AUIAlertDialog;
import io.agora.auikit.ui.micseats.IMicSeatDialogView;
import io.agora.auikit.ui.micseats.IMicSeatItemView;
import io.agora.auikit.ui.micseats.IMicSeatsView;
import io.agora.auikit.ui.micseats.MicSeatStatus;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;

public class AUIMicSeatsBindable extends IRtcEngineEventHandler implements
        IAUIBindable,
        IMicSeatsView.ActionDelegate,
        IAUIMicSeatService.AUIMicSeatRespDelegate,
        IAUIChorusService.AUIChorusRespDelegate,
        IAUIJukeboxService.AUIJukeboxRespDelegate,
        IAUIUserService.AUIUserRespDelegate {
    private final IMicSeatsView micSeatsView;
    private final IAUIUserService userService;
    private final IAUIMicSeatService micSeatService;
    private final IAUIJukeboxService jukeboxService;
    private final IAUIChorusService chorusService;
    private final IAUIInvitationService invitationService;
    private final AUIVoiceRoomService mVoiceService;
    private AUIRoomContext roomContext;
    private Handler mMainHandler;
    private String mLeadSingerId = "";
    private RtcEngine mRtcEngine;
    private Map<Integer, String> mSeatMap = new HashMap();
    private Map<String,Integer> mVolumeMap = new HashMap();
    private Context context;
    private AUIAlertDialog auiAlertDialog;

    private LinkedList<String> mAccompanySingers = new LinkedList<String>();

    public AUIMicSeatsBindable(
            Context context,
            IMicSeatsView micSeatsView,
            AUIVoiceRoomService voiceService) {
        this.mVoiceService = voiceService;
        this.userService = voiceService.getUserService();
        this.micSeatsView = micSeatsView;
        this.micSeatService = voiceService.getMicSeatsService();
        this.jukeboxService = voiceService.getJukeboxService();
        this.chorusService = voiceService.getChorusService();
        this.invitationService = voiceService.getInvitationService();
        this.mRtcEngine = voiceService.getMRtcEngine();
        this.roomContext = AUIRoomContext.shared();
        this.context = context;
        mSeatMap.put(0,roomContext.getRoomOwner(micSeatService.getChannelName()));
        mVolumeMap.put(roomContext.getRoomOwner(micSeatService.getChannelName()),0);

        // update view
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int seatIndex = 0; seatIndex < seatViewList.length; seatIndex++) {
            AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(seatIndex);
            updateSeatView(seatIndex, micSeatInfo);
        }
    }

    @Override
    public void bind() {
        mMainHandler = new Handler(Looper.getMainLooper());
        userService.bindRespDelegate(this);
        micSeatService.bindRespDelegate(this);
        jukeboxService.bindRespDelegate(this);
        chorusService.bindRespDelegate(this);
        micSeatsView.setMicSeatActionDelegate(this);
        mRtcEngine.addHandler(this);

        jukeboxService.getAllChooseSongList(new AUIChooseSongListCallback() {
            @Override
            public void onResult(@Nullable AUIException error, @Nullable List<AUIChooseMusicModel> songList) {
                if (songList != null && songList.size() != 0) {
                    AUIChooseMusicModel song = songList.get(0);
                    mLeadSingerId = song.owner.userId;
                    runOnUiThread(() -> updateChorusTag() );
                }
            }
        });
        chorusService.getChoristersList(new AUIChoristerListCallback() {
            @Override
            public void onResult(@Nullable AUIException error, @Nullable List<AUIChoristerModel> songList) {
                for (AUIChoristerModel song : songList) {
                    mAccompanySingers.add(song.userId);
                }
                if (mAccompanySingers.size() != 0) {
                    runOnUiThread(() -> updateChorusTag() );
                }
            }
        });
    }

    @Override
    public void unBind() {
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        userService.unbindRespDelegate(this);
        micSeatService.unbindRespDelegate(this);
        jukeboxService.unbindRespDelegate(this);
        chorusService.unbindRespDelegate(this);

        mRtcEngine.removeHandler(this);
        mRtcEngine.registerAudioFrameObserver(null);

        micSeatsView.setMicSeatActionDelegate(null);
    }
    private void runOnUiThread(@NonNull Runnable runnable) {
        if (mMainHandler != null) {
            if (mMainHandler.getLooper().getThread() == Thread.currentThread()) {
                runnable.run();
            } else {
                mMainHandler.post(runnable);
            }
        }
    }
    /** IAUIMicSeatService.AUIMicSeatRespDelegate implements. */
    @Override
    public void onSeatListChange(List<AUIMicSeatInfo> seatInfoList) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onSeatListChange(seatInfoList);
    }

    @Override
    public void onAnchorEnterSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        AUIUserInfo auiUserInfo = userService.getUserInfo(userInfo.userId);
        mSeatMap.put(seatIndex,userInfo.userId);
        mVolumeMap.put(userInfo.userId,seatIndex);
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if (auiUserInfo != null){
            seatView.setTitleText(auiUserInfo.userName);
            seatView.setUserAvatarImageUrl(auiUserInfo.userAvatar);
        }
    }

    @Override
    public void onAnchorLeaveSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        String uid = mSeatMap.get(seatIndex - 1);
        if (uid != null && uid.equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex);
        }
        mVolumeMap.remove(userInfo.userId);
        updateSeatView(seatIndex, null);
        micSeatsView.stopRippleAnimation(seatIndex);
    }

    @Override
    public void onSeatAudioMute(int seatIndex, boolean isMute) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        seatView.setAudioMuteVisibility(isMute ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSeatVideoMute(int seatIndex, boolean isMute) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        updateSeatView(seatIndex, seatInfo);
        micSeatsView.stopRippleAnimation(seatIndex);
    }

    @Override
    public void onSeatClose(int seatIndex, boolean isClose) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if (seatInfo.seatStatus == AUIMicSeatStatus.idle) {
            seatView.setMicSeatState(MicSeatStatus.idle);
        } else if (seatInfo.seatStatus == AUIMicSeatStatus.locked) {
            seatView.setMicSeatState(MicSeatStatus.locked);
        } else {
            seatView.setMicSeatState(MicSeatStatus.used);
        }

        micSeatsView.stopRippleAnimation(seatIndex);
    }

    private void updateSeatView(int seatIndex, @Nullable AUIMicSeatInfo micSeatInfo) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if (micSeatInfo == null || micSeatInfo.seatStatus == AUIMicSeatStatus.idle) {
            seatView.setTitleIndex(seatIndex + 1);
            seatView.setAudioMuteVisibility(View.GONE);
            seatView.setVideoMuteVisibility(View.GONE);
            seatView.setUserAvatarImageDrawable(null);
            seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            return;
        }
        AUIUserInfo userInfo = null;
        if (micSeatInfo.user != null) {
            userInfo = userService.getUserInfo(micSeatInfo.user.userId);
            if (userInfo != null){
                mVolumeMap.put(userInfo.userId,seatIndex);
                micSeatInfo.user.userName = userInfo.userName;
                micSeatInfo.user.userAvatar = userInfo.userAvatar;
            }
        }
        seatView.setRoomOwnerVisibility((seatIndex == 0) ? View.VISIBLE : View.GONE);

        if (micSeatInfo.seatStatus == AUIMicSeatStatus.locked){
            seatView.setMicSeatState(MicSeatStatus.locked);
        }

        boolean isAudioMute = (micSeatInfo.muteAudio != 0);
        if (userInfo != null) {
            isAudioMute = isAudioMute || (userInfo.muteAudio == 1);
        }
        seatView.setAudioMuteVisibility(isAudioMute ? View.VISIBLE : View.GONE);

        boolean isVideoMute = (micSeatInfo.muteVideo != 0);
        seatView.setVideoMuteVisibility(isVideoMute ? View.VISIBLE : View.GONE);

        if (micSeatInfo.user != null) {
            seatView.setTitleText(micSeatInfo.user.userName);
            seatView.setUserAvatarImageUrl(micSeatInfo.user.userAvatar);

            if (micSeatInfo.user.userId.equals(mLeadSingerId)) {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.LeadSinger);
            } else if (mAccompanySingers.contains(micSeatInfo.user.userId)) {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.SecondarySinger);
            } else {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            }
        }
    }

    private void setLeadSingerId(String str) {
        if (str.equals(mLeadSingerId)) {
            return;
        }
        mLeadSingerId = str;
        updateChorusTag();
    }

    private void updateChorusTag() {
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int i = 0; i < seatViewList.length; i++) {
            AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(i);
            IMicSeatItemView itemView = seatViewList[i];
            if (micSeatInfo.user == null || micSeatInfo.user.userId.isEmpty()) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
                continue;
            }
            if (micSeatInfo.user.userId.equals(mLeadSingerId)) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.LeadSinger);
            } else if (mAccompanySingers.contains(micSeatInfo.user.userId)) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.SecondarySinger);
            } else {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            }
        }
    }

    /** AUIMicSeatDialogView.ActionDelegate implements. */
    @Override
    public boolean onClickSeat(int index, IMicSeatDialogView dialogView) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(index);
        if (seatInfo == null ||  seatInfo.user == null){ return true;}
        AUIUserInfo userInfo = userService.getUserInfo(seatInfo.user.userId);
        if (userInfo != null) {
            dialogView.setUserInfo(userInfo.userName);
        }
        boolean isEmptySeat = (seatInfo.user == null || seatInfo.user.userId.length() == 0);
        boolean isCurrentUser = seatInfo.user != null && seatInfo.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId);
        boolean isRoomOwner = micSeatService.getRoomContext().isRoomOwner(micSeatService.getChannelName());
        boolean inSeat = false;
        for (int i = 0; i <= 7; i++) {
            AUIMicSeatInfo info = micSeatService.getMicSeatInfo(i);
            if (info != null){
                if (info.user != null && info.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId)) {
                    inSeat = true;
                    break;
                }
            }
        }
        if (isRoomOwner) {
            if (isEmptySeat) {
                dialogView.addInvite((seatInfo.seatStatus != AUIMicSeatStatus.locked));
                dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                dialogView.addCloseSeat((seatInfo.seatStatus == AUIMicSeatStatus.locked));
            } else {
                if (isCurrentUser) {
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                } else {
                    dialogView.addKickSeat();
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                    dialogView.addCloseSeat((seatInfo.seatStatus == AUIMicSeatStatus.locked));
                }
            }
        } else {
            if (isEmptySeat) {
                if (inSeat) {
                    return false;
                } else {
                    dialogView.addEnterSeat((seatInfo.seatStatus != AUIMicSeatStatus.locked));
                }
            } else {
                if (isCurrentUser) {
                    dialogView.addLeaveSeat();
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClickEnterSeat(int index) {
        showEnterDialog(index);
    }

    public void showEnterDialog(int index) {
        auiAlertDialog = new AUIAlertDialog(context);
        auiAlertDialog.setTitle(context.getString(R.string.voice_room_apply_action));
        auiAlertDialog.setMessage(context.getString(R.string.voice_room_apply_micSeat,index+1));
        auiAlertDialog.setPositiveButton(context.getString(R.string.voice_room_confirm), view -> {
            invitationService.sendApply(index, new AUICallback() {
                @Override
                public void onResult(@Nullable AUIException error) {
                    if (error == null){
                        Toast.makeText(context, "申请成功!", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "申请失败!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            auiAlertDialog.dismiss();
        });
        auiAlertDialog.show();
    }

    @Override
    public void onClickLeaveSeat(int index) {
        micSeatService.leaveSeat(null);
    }

    @Override
    public void onClickKickSeat(int index) {
        micSeatService.kickSeat(index, null);
    }

    @Override
    public void onClickCloseSeat(int index, boolean isClose) {
        micSeatService.closeSeat(index, isClose, null);
    }

    @Override
    public void onClickMuteAudio(int index, boolean mute) {
        micSeatService.muteAudioSeat(index, mute, null);
    }

    @Override
    public void onClickMuteVideo(int index, boolean mute) {
        micSeatService.muteVideoSeat(index, mute, null);
    }

    @Override
    public void onClickInvited(int index) {
        micSeatService.onClickInvited(index);
    }

    /** IAUIMicSeatService.AUIChorusRespDelegate implements. */
    @Override
    public void onChoristerDidEnter(AUIChoristerModel chorister) {
        if (mAccompanySingers.contains(chorister.userId)) {
            return;
        }
        mAccompanySingers.add(chorister.userId);
        updateChorusTag();
    }

    @Override
    public void onChoristerDidLeave(AUIChoristerModel chorister) {
        if (mAccompanySingers.contains(chorister.userId)) {
            mAccompanySingers.remove(chorister.userId);
            updateChorusTag();
        }
    }

    @Override
    public void onSingerRoleChanged(int oldRole, int newRole) {

    }

    @Override
    public void onChoristerDidChanged() {

    }
    /** IAUIMicSeatService.AUIJukeboxRespDelegate implements. */
    @Override
    public void onAddChooseSong(@NonNull AUIChooseMusicModel song) {

    }

    @Override
    public void onRemoveChooseSong(@NonNull AUIChooseMusicModel song) {

    }

    @Override
    public void onUpdateChooseSong(@NonNull AUIChooseMusicModel song) {


    }

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUIChooseMusicModel> songs) {
        if (songs.size() != 0) {
            AUIChooseMusicModel song = songs.get(0);
            mAccompanySingers.clear();
            setLeadSingerId(song.owner.userId);
        } else {
            mAccompanySingers.clear();
            setLeadSingerId("");
        }
    }
    /** IAUIUserService.AUIUserRespDelegate implements. */
    @Override
    public void onRoomUserSnapshot(@NonNull String roomId, @Nullable List<AUIUserInfo> userList) {
        // update view
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int seatIndex = 0; seatIndex < seatViewList.length; seatIndex++) {
            AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(seatIndex);
            updateSeatView(seatIndex, micSeatInfo);
        }
    }

    @Override
    public void onRoomUserEnter(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserLeave(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserUpdate(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onUserAudioMute(@NonNull String userId, boolean mute) {
        for (int i = 0; i <= micSeatService.getMicSeatSize(); i++) {
            AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(i);
            if (seatInfo != null && seatInfo.user != null && seatInfo.user.userId.equals(userId)) {
                updateSeatView(i, seatInfo);
                break;
            }
        }
    }

    @Override
    public void onUserVideoMute(@NonNull String userId, boolean mute) {

    }

    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        for (AudioVolumeInfo speaker : speakers) {
            int uid = speaker.uid;
            if (uid == 0){// uid == 0 代表自己
                String userId = roomContext.currentUserInfo.userId;
                if (roomContext.isRoomOwner(userService.getChannelName())){
                    if (speaker.volume == 0) {
                        micSeatsView.stopRippleAnimation(0);
                    }else {
                        micSeatsView.startRippleAnimation(0);
                    }
                }else {
                    if (null != mVolumeMap && mVolumeMap.containsKey(userId)){
                        if (speaker.volume == 0) {
                            micSeatsView.stopRippleAnimation(mVolumeMap.get(userId));
                        }else {
                            micSeatsView.startRippleAnimation(mVolumeMap.get(userId));
                        }
                    }
                }
            }else {
                if (null != mVolumeMap && mVolumeMap.containsKey(String.valueOf(uid))){
                    if (speaker.volume == 0) {
                        micSeatsView.stopRippleAnimation(mVolumeMap.get(String.valueOf(uid)));
                    }else {
                        micSeatsView.startRippleAnimation(mVolumeMap.get(String.valueOf(uid)));
                    }
                }
            }
        }
    }

}
