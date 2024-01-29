package io.agora.asceneskit.voice

import android.util.Log
import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.IAUIIMManagerService
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.arbiter.AUIArbiter
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.imp.AUIGiftServiceImpl
import io.agora.auikit.service.imp.AUIIMManagerServiceImpl
import io.agora.auikit.service.imp.AUIInvitationServiceImpl
import io.agora.auikit.service.imp.AUIMicSeatServiceImpl
import io.agora.auikit.service.imp.AUIUserServiceImpl
import io.agora.auikit.service.rtm.AUIRtmErrorRespObserver
import io.agora.auikit.service.rtm.AUIRtmLockRespObserver
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmPayload
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import io.agora.auikit.utils.ThreadManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtm.LockEvent
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmEventListener


val kRoomInfoAttrKey = "basic"

class AUIVoiceRoomService constructor(
    apiConfig: AUIAPIConfig,
    private var roomConfig: AUIRoomConfig,
) {

    private val TAG = "AUIVoiceRoomService"

    private val rtcEngineCreateByService = apiConfig.rtcEngineEx == null
    private val rtmCreateCreateByService = apiConfig.rtmClient == null

    private var isRoomDestroyed = false

    private val userRespObserver = object : IAUIUserService.AUIUserRespObserver {

        override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
            userSnapshotList = userList
            userList?.firstOrNull { it.userId == AUIRoomContext.shared().currentUserInfo.userId }
                ?.let { user ->
                    onUserAudioMute(user.userId, (user.muteAudio == 1))
                    onUserVideoMute(user.userId, (user.muteVideo == 1))
                }
        }

        override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
            super.onRoomUserLeave(roomId, userInfo)
            if (AUIRoomContext.shared().getRoomOwner(channelName) == userInfo.userId) {
                cleanRoomInfo(roomId)
            } else {
                cleanUserInfo(roomId, userInfo.userId)
            }
        }

        override fun onUserAudioMute(userId: String, mute: Boolean) {
            if (userId != AUIRoomContext.shared().currentUserInfo.userId) {
                return
            }
            rtcEngine.adjustRecordingSignalVolume(if (mute) 0 else 100)
        }

        override fun onUserVideoMute(userId: String, mute: Boolean) {
            if (userId != AUIRoomContext.shared().currentUserInfo.userId) {
                return
            }
            rtcEngine.enableLocalVideo(!mute)
            val option = ChannelMediaOptions()
            option.publishCameraTrack = !mute
            rtcEngine.updateChannelMediaOptions(option)
        }
    }

    private val rtmErrorRespObserver = object : AUIRtmErrorRespObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            channelName ?: return
            observableHelper.notifyEventHandlers {
                it.onTokenPrivilegeWillExpire(channelName)
            }
        }

        override fun onMsgReceiveEmpty(channelName: String) {
            super.onMsgReceiveEmpty(channelName)
            if (this@AUIVoiceRoomService.channelName != channelName) {
                return
            }
            isRoomDestroyed = true
            observableHelper.notifyEventHandlers {
                it.onRoomDestroy(channelName)
            }
        }

        override fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {
            super.onConnectionStateChanged(channelName, state, reason)
            if (reason == RtmConstants.RtmConnectionChangeReason.getValue(RtmConstants.RtmConnectionChangeReason.REJOIN_SUCCESS)) {
                AUIRoomContext.shared().getArbiter(this@AUIVoiceRoomService.channelName)?.acquire()
            }
            if (state == RtmConstants.RtmConnectionState.getValue(RtmConstants.RtmConnectionState.FAILED)
                && reason == RtmConstants.RtmConnectionChangeReason.getValue(RtmConstants.RtmConnectionChangeReason.BANNED_BY_SERVER)
            ) {
                observableHelper.notifyEventHandlers {
                    it.onRoomUserBeKicked(
                        this@AUIVoiceRoomService.channelName,
                        AUIRoomContext.shared().currentUserInfo.userId
                    )
                }
            }
        }
    }

    private val rtmLockRespObserver = object : AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            lockRetrived = true
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String) {

        }
    }

    private var enterRoomCompletion: ((AUIRoomInfo?) -> Unit)? = null

    private var subscribeSuccess = false
        set(value) {
            if (field != value) {
                field = value
                checkRoomValid()
            }
        }

    private var lockRetrived = false
        set(value) {
            if (field != value) {
                field = value
                checkRoomValid()
            }
        }

    private var userSnapshotList: List<AUIUserInfo>? = null
        set(value) {
            if (field != value) {
                field = value
                checkRoomValid()
            }
        }

    val channelName: String
        get() {
            return roomConfig.channelName
        }

    val rtcEngine: RtcEngine = apiConfig.rtcEngineEx ?: AgoraEngineCreator.createRtcEngine(
        AUIRoomContext.shared().requireCommonConfig().context,
        AUIRoomContext.shared().requireCommonConfig().appId
    )

    val rtmClient = apiConfig.rtmClient ?: RtmClient.create(
        RtmConfig.Builder(
            AUIRoomContext.shared().requireCommonConfig().appId,
            AUIRoomContext.shared().currentUserInfo.userId
        ).presenceTimeout(60).eventListener(object : RtmEventListener {
            override fun onLockEvent(event: LockEvent?) {
                super.onLockEvent(event)
                Log.d(TAG, "onLockEvent event: $event")
            }
        }).build()
    )

    val rtmManager: AUIRtmManager = AUIRtmManager(
        AUIRoomContext.shared().requireCommonConfig().context,
        rtmClient,
        apiConfig.rtmClient != null
    )

    val observableHelper = ObservableHelper<AUIVoiceRoomObserver>()

    val chatManager = AUIChatManager(channelName, AUIRoomContext.shared())

    val userService: IAUIUserService = AUIUserServiceImpl(channelName, rtmManager).apply {
        registerRespObserver(userRespObserver)
    }

    val imManagerService: IAUIIMManagerService = AUIIMManagerServiceImpl(
        channelName,
        rtmManager,
        chatManager
    )

    val micSeatService: IAUIMicSeatService = AUIMicSeatServiceImpl(
        channelName,
        rtmManager
    )

    val invitationService: IAUIInvitationService = AUIInvitationServiceImpl(
        channelName,
        rtmManager
    )

    val giftService: IAUIGiftsService = AUIGiftServiceImpl(
        channelName,
        rtmManager,
        chatManager
    )

    var roomInfo: AUIRoomInfo? = null
        set(value) {
            if (field != value) {
                field = value
                AUIRoomContext.shared().insertRoomInfo(value)
                checkRoomValid()
            }
        }

    init {
        AUIRoomContext.shared().roomConfigMap[channelName] = roomConfig
        AUIRoomContext.shared().roomArbiterMap[channelName] = AUIArbiter(
            channelName,
            rtmManager,
            AUIRoomContext.shared().currentUserInfo.userId
        )
    }

    fun create(
        roomInfo: AUIRoomInfo,
        completion: (AUIException?) -> Unit
    ) {
        this.roomInfo = roomInfo

        rtmManager.login(
            roomConfig.rtmToken
        ) { error ->
            if (error != null) {
                completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                return@login
            }
            AUIRoomContext.shared().getArbiter(channelName)?.create()
            initRoom { initError ->
                if (initError != null) {
                    completion.invoke(initError)
                } else {
                    enter(completion)
                }
            }
        }
    }

    fun enter(completion: (AUIException?) -> Unit) {
        val roomId = channelName
        rtmManager.login(
            roomConfig.rtmToken
        ) { error ->
            if (error != null) {
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                }
                return@login
            }
            enterRoomCompletion = {
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(null)
                }
            }

            if (roomInfo == null) {
                rtmManager.getMetadata(roomId) { _, metadata ->
                    val payloadInfo = GsonTools.toBean<AUIRtmPayload<AUIRoomInfo>>(
                        metadata?.metadataItems?.find { it.key == kRoomInfoAttrKey }?.value,
                        object : TypeToken<AUIRtmPayload<AUIRoomInfo>>() {}.type
                    )
                    payloadInfo?.payload?.roomId = payloadInfo?.roomId ?: ""
                    roomInfo = payloadInfo?.payload ?: AUIRoomInfo()
                }
            }

            AUIRoomContext.shared().getArbiter(channelName)?.acquire(){ lockError ->
                if(lockError != null && enterRoomCompletion != null){
                    enterRoomCompletion = null
                    isRoomDestroyed = true
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error: $lockError"))
                    }
                    return@acquire
                }
            }

            rtmManager.subscribeError(rtmErrorRespObserver)
            rtmManager.subscribeLock(channelName, observer = rtmLockRespObserver)
            rtmManager.subscribe(channelName) { subscribeError ->
                if (subscribeError != null) {
                    completion.invoke(
                        AUIException(
                            AUIException.ERROR_CODE_RTM,
                            "error : $subscribeError"
                        )
                    )
                    return@subscribe
                }
                subscribeSuccess = true
            }

            joinRtcChannel { joinError ->
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(AUIException(AUIException.ERROR_CODE_RTM, "error: $joinError"))
                }
            }
        }
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
            rtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        } else {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = false
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            rtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        }
    }

    fun setupLocalAudioMute(isMute: Boolean) {
        if (isMute) {
            rtcEngine.adjustRecordingSignalVolume(0)
        } else {
            rtcEngine.adjustRecordingSignalVolume(100)
        }
    }

    fun setupRemoteAudioMute(userId: String, isMute: Boolean) {
        rtcEngine.muteRemoteAudioStream(userId.toInt(), isMute)
    }

    fun joinRtcChannel(failure: (AUIException) -> Unit) {
        AUILogger.logger().d(TAG, "joinRtcRoom start ...")
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        rtcEngine.enableVideo()
        rtcEngine.enableLocalVideo(false)
        rtcEngine.enableAudio()
        rtcEngine.setAudioProfile(
            Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
            Constants.AUDIO_SCENARIO_GAME_STREAMING
        )
        rtcEngine.enableAudioVolumeIndication(350, 2, true)
        rtcEngine.setClientRole(
            if (AUIRoomContext.shared()
                    .isRoomOwner(channelName)
            ) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        )

        AUILogger.logger().d(
            "RtcEngineEx",
            "joinChannel uid:${AUIRoomContext.shared().currentUserInfo.userId.toInt()}  rtcChannelName=${roomConfig.channelName}  rtcRtcToken=${roomConfig.rtcToken}"
        )
        val ret: Int = rtcEngine.joinChannel(
            roomConfig.rtcToken,
            roomConfig.channelName,
            null,
            AUIRoomContext.shared().currentUserInfo.userId.toInt()
        )

        if (ret == Constants.ERR_OK) {
            AUILogger.logger().d(TAG, "join rtc room success")
        } else {
            AUILogger.logger().e(TAG, "join rtc room failed $ret")
            failure.invoke(AUIException(ret, "join rtc room failed"))
        }
        AUILogger.logger().d(TAG, "joinRtcRoom end ...")
    }


    //token过期之后调用该方法更新所有token
    fun renew(config: AUIRoomConfig) {
        AUIRoomContext.shared().roomConfigMap[config.channelName] = config

        //rtm renew
        rtmManager.renew(config.rtmToken)
        rtmManager.renewStreamChannelToken(config.channelName, config.rtcToken)

        //rtc renew
        rtcEngine.renewToken(config.rtcToken)
        AUILogger.logger().d(TAG, "renew token ...")
    }

    fun destroy() {
        cleanRoomInfo(channelName)
        cleanRoom()
    }

    fun exit(): Boolean {
        cleanUserInfo(channelName, AUIRoomContext.shared().currentUserInfo.userId)
        cleanRoom()
        AUIRoomContext.shared().getArbiter(channelName)?.release()
        return isRoomDestroyed
    }

    private fun initRoom(completion: (AUIException?) -> Unit) {
        val basicInfo = AUIRtmPayload<AUIRoomInfo>(
            channelName,
            payload = roomInfo
        )

        val basicInfoStr = GsonTools.beanToString(basicInfo)
        if (basicInfoStr == null) {
            completion.invoke(
                AUIException(
                    AUIException.ERROR_CODE_NETWORK_PARSE,
                    "initRoom >> bean to string failed!"
                )
            )
            return
        }
        rtmManager.setBatchMetadata(
            channelName,
            lockName = "",
            metadata = mapOf(Pair(kRoomInfoAttrKey, basicInfoStr)),
            fetchImmediately = true
        ) { err ->
            if (err == null) {
                completion.invoke(null)
            } else {
                completion.invoke(
                    AUIException(
                        AUIException.ERROR_CODE_RTM,
                        "initRoom >> setBatchMetadata failed : $err"
                    )
                )
            }
        }
    }

    private fun checkRoomValid() {
        if (subscribeSuccess && roomInfo != null && lockRetrived && userSnapshotList != null) {

            micSeatService.initService{}

            if (enterRoomCompletion != null) {
                enterRoomCompletion?.invoke(roomInfo)
                enterRoomCompletion = null
                observableHelper.notifyEventHandlers {
                    it.onRoomInfoChange(channelName, roomInfo!!)
                }
            }

            // room owner not found, clean room
            val snapShotOwnerId = userSnapshotList?.find {
                it.userId == AUIRoomContext.shared().getRoomOwner(channelName)
            }
            if (snapShotOwnerId == null) {
                cleanRoomInfo(channelName)
            }

            imManagerService.serviceDidLoad()
        }
    }

    private fun cleanRoomInfo(roomId: String) {
        if (AUIRoomContext.shared().getArbiter(roomId)?.isArbiter() != true) {
            return
        }
        micSeatService.deInitService { }
        imManagerService.deInitService { }
        invitationService.deInitService { }
        rtmManager.cleanBatchMetadata(
            channelName,
            remoteKeys = listOf(kRoomInfoAttrKey),
            fetchImmediately = true
        ) {}
        AUIRoomContext.shared().getArbiter(channelName)?.destroy()
    }

    private fun cleanRoom() {
        logoutRtm()
        leaveRtcChannel()
        AUIRoomContext.shared().cleanRoom(channelName)
    }

    private fun logoutRtm() {
        rtmManager.deInit()
        rtmManager.unSubscribe(channelName)
        rtmManager.unSubscribeError(rtmErrorRespObserver)
        rtmManager.unsubscribeLock(rtmLockRespObserver)
        rtmManager.logout()
        if (rtmCreateCreateByService) {
            RtmClient.release()
        }
    }

    private fun leaveRtcChannel() {
        rtcEngine.leaveChannel()
        if (rtcEngineCreateByService) {
            RtcEngine.destroy()
        }
    }

    private fun cleanUserInfo(roomId: String, userId: String) {
        if (AUIRoomContext.shared().getArbiter(roomId)?.isArbiter() != true) {
            return
        }
        val index = micSeatService.getMicSeatIndex(userId)
        if (index >= 0) {
            micSeatService.kickSeat(index) {}
        }
    }



}