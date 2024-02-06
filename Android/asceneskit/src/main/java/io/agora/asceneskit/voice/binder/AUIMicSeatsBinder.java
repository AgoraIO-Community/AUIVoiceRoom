package io.agora.asceneskit.voice.binder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.asceneskit.R;
import io.agora.asceneskit.voice.AUIVoiceRoomService;
import io.agora.auikit.model.AUIMicSeatInfo;
import io.agora.auikit.model.AUIMicSeatStatus;
import io.agora.auikit.model.AUIRoomContext;
import io.agora.auikit.model.AUIUserInfo;
import io.agora.auikit.model.AUIUserThumbnailInfo;
import io.agora.auikit.service.IAUIInvitationService;
import io.agora.auikit.service.IAUIMicSeatService;
import io.agora.auikit.service.IAUIUserService;
import io.agora.auikit.service.callback.AUICallback;
import io.agora.auikit.service.callback.AUIException;
import io.agora.auikit.ui.basic.AUIAlertDialog;
import io.agora.auikit.ui.micseats.IMicSeatDialogView;
import io.agora.auikit.ui.micseats.IMicSeatItemView;
import io.agora.auikit.ui.micseats.IMicSeatsView;
import io.agora.auikit.ui.micseats.MicSeatStatus;
import io.agora.auikit.utils.AUILogger;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;

public class AUIMicSeatsBinder extends IRtcEngineEventHandler implements
        IAUIBindable,
        IMicSeatsView.ActionDelegate,
        IAUIMicSeatService.AUIMicSeatRespObserver,
        IAUIUserService.AUIUserRespObserver {
    private final IMicSeatsView micSeatsView;
    private final IAUIUserService userService;
    private final IAUIMicSeatService micSeatService;
    private final IAUIInvitationService invitationService;
    private AUIRoomContext roomContext;
    private Handler mMainHandler;
    private RtcEngine mRtcEngine;
    private Map<Integer, String> mSeatMap = new HashMap();
    private Map<String,Integer> mVolumeMap = new HashMap();
    private Context context;
    private AUIAlertDialog auiAlertDialog;


    public AUIMicSeatsBinder(
            Context context,
            IMicSeatsView micSeatsView,
            AUIVoiceRoomService voiceService) {
        this.userService = voiceService.getUserService();
        this.micSeatsView = micSeatsView;
        this.micSeatService = voiceService.getMicSeatService();
        this.invitationService = voiceService.getInvitationService();
        this.mRtcEngine = voiceService.getRtcEngine();
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
        userService.registerRespObserver(this);
        micSeatService.registerRespObserver(this);
        micSeatsView.setMicSeatActionDelegate(this);
        try {
            mRtcEngine.addHandler(this);
        } catch (Exception e) {
            // try : NullPointerException: Attempt to invoke virtual method
            // 'void io.agora.rtc2.internal.RtcEngineImpl.addHandler(io.agora.rtc2.IAgoraEventHandler)'
            // on a null object reference
        }
    }

    @Override
    public void unBind() {
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        userService.unRegisterRespObserver(this);
        micSeatService.unRegisterRespObserver(this);

        try {
            mRtcEngine.removeHandler(this);
        } catch (Exception e) {
            // do nothing
        }
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

        boolean isAudioMute = micSeatInfo.muteAudio;
        if (userInfo != null) {
            isAudioMute = isAudioMute || (userInfo.muteAudio == 1);
        }
        seatView.setAudioMuteVisibility(isAudioMute ? View.VISIBLE : View.GONE);

        boolean isVideoMute = micSeatInfo.muteVideo;
        seatView.setVideoMuteVisibility(isVideoMute ? View.VISIBLE : View.GONE);

        if (micSeatInfo.user != null) {
            seatView.setTitleText(micSeatInfo.user.userName);
            seatView.setUserAvatarImageUrl(micSeatInfo.user.userAvatar);
            seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
        }
    }

    /** AUIMicSeatDialogView.ActionDelegate implements. */
    @Override
    public boolean onClickSeat(int index, IMicSeatDialogView dialogView) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(index);
        if (seatInfo == null ||  seatInfo.user == null){ return false;}
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
                dialogView.addMuteAudio(seatInfo.muteAudio);
                dialogView.addCloseSeat((seatInfo.seatStatus == AUIMicSeatStatus.locked));
            } else {
                if (isCurrentUser) {
                    dialogView.addMuteAudio(seatInfo.muteAudio);
                } else {
                    dialogView.addKickSeat();
                    dialogView.addMuteAudio(seatInfo.muteAudio);
                    dialogView.addCloseSeat((seatInfo.seatStatus == AUIMicSeatStatus.locked));
                }
            }
        } else {
            if (isEmptySeat) {
                if (inSeat) {
                    return false;
                } else {
                    boolean seatUnLocked = seatInfo.seatStatus != AUIMicSeatStatus.locked;
                    if (seatUnLocked) {
                        dialogView.addEnterSeat(true);
                    } else {
                        return false;
                    }
                }
            } else {
                if (isCurrentUser) {
                    dialogView.addLeaveSeat();
                    dialogView.addMuteAudio(seatInfo.muteAudio);
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
    public void onUserOffline(int uid, int reason) {
        super.onUserOffline(uid, reason);
        AUILogger.Companion.logger().d("AUIMicSeatsBinder", "onUserOffline >> uid=" + uid + ", reason=" + reason);
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
