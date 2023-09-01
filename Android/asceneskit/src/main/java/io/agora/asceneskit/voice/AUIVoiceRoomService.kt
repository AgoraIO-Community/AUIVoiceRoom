package io.agora.asceneskit.voice

import android.util.Log
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.service.IAUIChorusService
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.IAUIIMManagerService
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIJukeboxService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIMusicPlayerService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.imp.AUIChorusServiceImpl
import io.agora.auikit.service.imp.AUIGiftServiceImpl
import io.agora.auikit.service.imp.AUIIMManagerServiceImpl
import io.agora.auikit.service.imp.AUIInvitationServiceImpl
import io.agora.auikit.service.imp.AUIJukeboxServiceImpl
import io.agora.auikit.service.imp.AUIMicSeatServiceImpl
import io.agora.auikit.service.imp.AUIMusicPlayerServiceImpl
import io.agora.auikit.service.imp.AUIRoomManagerImpl
import io.agora.auikit.service.imp.AUIUserServiceImpl
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVApiConfig
import io.agora.auikit.service.ktv.KTVApiImpl
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine

class AUIVoiceRoomService constructor(
    private val rtcEngine: RtcEngine?,
    private val ktvApi: KTVApi?,
    private val roomManager: AUIRoomManagerImpl,
    private val roomConfig: AUIRoomConfig,
    private val roomInfo: AUIRoomInfo,
) : IAUIUserService.AUIUserRespDelegate {

    private val TAG = "AUIVoiceRoomService"

    private val channelName: String
        get() { return  roomInfo.roomId }

    val mRtcEngine: RtcEngine = rtcEngine ?: AgoraEngineCreator.createRtcEngine(
        AUIRoomContext.shared().commonConfig.context,
        AUIRoomContext.shared().commonConfig.appId
    )

    private val rtmManager: AUIRtmManager = roomManager.rtmManager

    private val chatManager = AUIChatManager(roomInfo.roomId, AUIRoomContext.shared())

    private val userImpl: IAUIUserService by lazy {
        val user = AUIUserServiceImpl(roomInfo.roomId, rtmManager)
        user.bindRespDelegate(this)
        user
    }

    private val chatImpl: IAUIIMManagerService by lazy { AUIIMManagerServiceImpl(roomInfo.roomId, rtmManager, chatManager) }

    private val micSeatImpl: IAUIMicSeatService by lazy { AUIMicSeatServiceImpl(roomInfo.roomId, rtmManager) }

    private val playerImpl: IAUIMusicPlayerService by lazy { AUIMusicPlayerServiceImpl(mRtcEngine, roomInfo.roomId, mKtvApi) }

    private val chorusImpl: IAUIChorusService by lazy { AUIChorusServiceImpl(roomInfo.roomId, mKtvApi, rtmManager) }

    private val jukeboxImpl: IAUIJukeboxService by lazy { AUIJukeboxServiceImpl(roomInfo.roomId, rtmManager, mKtvApi) }

    private val invitationImpl: IAUIInvitationService by lazy { AUIInvitationServiceImpl(roomInfo.roomId, rtmManager) }

    private val giftImpl:IAUIGiftsService by lazy { AUIGiftServiceImpl(roomInfo.roomId, rtmManager, chatManager) }

    private val mKtvApi: KTVApi = ktvApi ?: run {
        val config = KTVApiConfig(
            AUIRoomContext.shared().commonConfig.appId,
            roomConfig.rtcRtmToken,
            mRtcEngine,
            roomConfig.rtcChannelName,
            AUIRoomContext.shared().commonConfig.userId.toInt(),
            roomConfig.rtcChorusChannelName,
            roomConfig.rtcChorusRtcToken
        )
        val impl = KTVApiImpl()
        impl.initialize(config)
        return@run impl
    }

    fun getRoomManager() = roomManager
    fun getUserService() = userImpl
    fun getMicSeatsService() = micSeatImpl
    fun getJukeboxService() = jukeboxImpl
    fun getChorusService() = chorusImpl
    fun getMusicPlayerService() = playerImpl
    fun getIMManagerService() = chatImpl
    fun getInvitationService() = invitationImpl
    fun getRoomInfo() = roomInfo
    fun getGiftService() = giftImpl
    fun getChatManager() = chatManager

    fun enterRoom(failure: (AUIException) -> Unit) {
        AUILogger.logger().d(TAG, "enterRoom start ...")
        roomManager.enterRoom(channelName, roomConfig.rtcToken) { error ->
            AUILogger.logger().d(TAG, "enterRoom result : $error")
            if (error != null) {
                // failure
                failure.invoke(error)
                AUILogger.logger().e(TAG, "roomManager enterRoom failure $error")
            } else {
                // success
                AUILogger.logger().d(TAG, "roomManager enterRoom success ...")
            }
            AUILogger.logger().d(TAG, "enterRoom end ...")
        }
    }

    fun destroyRoom() {
        roomManager.destroyRoom(channelName) {}
        mKtvApi.release()
        mRtcEngine.leaveChannel()
    }

    fun setupLocalStreamOn(isOn: Boolean) {
        Log.d("rtc_publish_state", "isOn: $isOn")
        if (isOn) {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = true
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        } else {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = false
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        }
    }

    fun setupLocalAudioMute(isMute: Boolean) {
        if (isMute) {
            mKtvApi.setMicStatus(false)
            mRtcEngine.adjustRecordingSignalVolume(0)
        } else {
            mKtvApi.setMicStatus(true)
            mRtcEngine.adjustRecordingSignalVolume(100)
        }
    }

    fun setupRemoteAudioMute(userId: String, isMute: Boolean) {
        mRtcEngine.muteRemoteAudioStream(userId.toInt(), isMute)
    }

    fun joinRtcRoom(failure: (AUIException) -> Unit) {
        AUILogger.logger().d(TAG, "joinRtcRoom start ...")
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine.enableVideo()
        mRtcEngine.enableLocalVideo(false)
        mRtcEngine.enableAudio()
        mRtcEngine.setAudioProfile(
            Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
            Constants.AUDIO_SCENARIO_GAME_STREAMING
        )
        mRtcEngine.enableAudioVolumeIndication(350, 2, true)
        mRtcEngine.setClientRole(if (AUIRoomContext.shared().isRoomOwner(channelName)) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE)

        AUILogger.logger().d("RtcEngineEx", "joinChannel uid:${AUIRoomContext.shared().currentUserInfo.userId.toInt()}  rtcChannelName=${roomConfig.rtcChannelName}  rtcRtcToken=${roomConfig.rtcToken}")
        val ret: Int = mRtcEngine.joinChannel(
            roomConfig.rtcRtcToken,
            roomConfig.rtcChannelName,
            null,
            AUIRoomContext.shared().commonConfig.userId.toInt()
        )

        if (ret == Constants.ERR_OK) {
            AUILogger.logger().d(TAG, "join rtc room success")
        }else{
            AUILogger.logger().e(TAG, "join rtc room failed $ret")
            failure.invoke(AUIException(ret,"join rtc room failed"))
        }
        AUILogger.logger().d(TAG, "joinRtcRoom end ...")
    }

    /** AUIUserRespDelegate */
    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.firstOrNull { it.userId == AUIRoomContext.shared().currentUserInfo.userId }?.let { user ->
            onUserAudioMute(user.userId, (user.muteAudio == 1))
            onUserVideoMute(user.userId, (user.muteVideo == 1))
        }
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        if (userId != AUIRoomContext.shared().currentUserInfo.userId) {
            return
        }
        rtcEngine?.adjustRecordingSignalVolume(if (mute) 0 else 100)
    }

    override fun onUserVideoMute(userId: String, mute: Boolean) {
        if (userId != AUIRoomContext.shared().currentUserInfo.userId) {
            return
        }
        rtcEngine?.enableLocalVideo(!mute)
        val option = ChannelMediaOptions()
        option.publishCameraTrack = !mute
        rtcEngine?.updateChannelMediaOptions(option)
    }

    //token过期之后调用该方法更新所有token
    fun renew(config: AUIRoomConfig){
        AUIRoomContext.shared().roomConfig = config

        //rtm renew
        rtmManager.renew(config.rtmToken)
        rtmManager.renewChannel(config.channelName,config.rtcToken)

        //rtc renew
        mRtcEngine.renewToken(config.rtcToken)
        AUILogger.logger().d(TAG, "renew token ...")
    }

}