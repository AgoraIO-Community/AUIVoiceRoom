package io.agora.asceneskit.voice

import android.util.Log
import io.agora.CallBack
import io.agora.auikit.model.*
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.callback.AUICreateChatRoomCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.imp.*
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUIRtmErrorProxyDelegate
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.ThreadManager
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient

object AUIVoiceRoomUikit {
    private val notInitException =
        RuntimeException("The VoiceServiceManager has not been initialized!")
    private val initedException =
        RuntimeException("The VoiceServiceManager has been initialized!")

    private var mRoomManager: AUIRoomManagerImpl? = null
    private var shouldReleaseRtc = true
    private var mRtcEngineEx: RtcEngineEx? = null
    private var mKTVApi: KTVApi? = null
    private var mService: AUIVoiceRoomService? = null

    /**
     * 初始化。
     * 对于rtmClient、rtcEngineEx、ktvApi：
     *      当外部没传时内部会自行创建，并在release方法调用时销毁；
     *      当外部传入时在release时不会销毁
     */
    fun init(
        config: AUICommonConfig,
        serverHost: String,
        ktvApi: KTVApi? = null,
        rtcEngineEx: RtcEngineEx? = null,
        rtmClient: RtmClient? = null
    ) {
        if (mRoomManager != null) {
            throw initedException
        }

        HttpManager.setBaseURL(serverHost)
        AUIRoomContext.shared().commonConfig = config
        mKTVApi = ktvApi

        if (rtcEngineEx != null) { // 用户塞进来的engine由用户自己管理生命周期
            mRtcEngineEx = rtcEngineEx
            shouldReleaseRtc = false
        }

        mRoomManager = AUIRoomManagerImpl(config, rtmClient)

        AUILogger.initLogger(AUILogger.Config(AUIRoomContext.shared().commonConfig.context, "Voice"))
    }

    /**
     * 释放资源
     */
    fun release() {
        if (shouldReleaseRtc) {
            RtcEngine.destroy()
        }
        mRtcEngineEx = null
        mRoomManager = null
        mKTVApi = null
    }

    fun destroyRoom(roomId: String?) {
        (mService?.getChatService() as AUIChatServiceImpl).logoutChat()
        (mService?.getChatService() as AUIChatServiceImpl).userQuitRoom(object : CallBack{
            override fun onSuccess() {

            }

            override fun onError(code: Int, error: String?) {
                Log.e("apex","userQuitRoom $code $error")
            }
        })
        mService?.destroyRoom()
        mService = null
    }

    fun subscribeError(roomId: String, delegate: AUIRtmErrorProxyDelegate) {
        mRoomManager?.rtmManager?.proxy?.subscribeError(roomId, delegate)
    }

    fun unsubscribeError(roomId: String, delegate: AUIRtmErrorProxyDelegate) {
        mRoomManager?.rtmManager?.proxy?.unsubscribeError(roomId, delegate)
    }

    fun bindRespDelegate(delegate: IAUIRoomManager.AUIRoomManagerRespDelegate) {
        mRoomManager?.bindRespDelegate(delegate)
    }

    fun unbindRespDelegate(delegate: IAUIRoomManager.AUIRoomManagerRespDelegate) {
        mRoomManager?.unbindRespDelegate(delegate)
    }

    /**
     * 获取房间列表
     */
    fun getRoomList(
        startTime: Long?,
        pageSize: Int,
        success: (List<AUIRoomInfo>) -> Unit,
        failure: (AUIException) -> Unit
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.getRoomInfoList(
            startTime, pageSize
        ) { error, roomList ->
            if (error == null) {
                success.invoke(roomList ?: emptyList())
            } else {
                failure.invoke(error)
            }
        }
    }

    /**
     * 创建房间
     */
    fun createRoom(
        createRoomInfo: AUICreateRoomInfo,
        success: (AUIRoomInfo) -> Unit,
        failure: (AUIException) -> Unit
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.createRoom(
            createRoomInfo
        ) { error, roomInfo ->
            if (error == null && roomInfo != null) {
                success.invoke(roomInfo)
            } else {
                failure.invoke(error ?: AUIException(-999, "RoomInfo return null"))
            }
        }

    }

    /**
     * 拉起并跳转的房间页面
     */
    fun launchRoom(
        lunchType: LaunchType,
        roomInfo: AUIRoomInfo,
        config: AUIRoomConfig,
        voiceRoom:AUIVoiceRoomView,
        eventHandler: RoomEventHandler? = null,
    ) {
        AUIRoomContext.shared().roomConfig = config
        val roomManager = mRoomManager ?: throw notInitException
        val roomService = AUIVoiceRoomService(
            mRtcEngineEx,
            mKTVApi,
            roomManager,
            config,
            roomInfo
        )

        mService = roomService

        val auiChatServiceImpl = roomService.getChatService() as AUIChatServiceImpl
        auiChatServiceImpl.getChatUser {

            auiChatServiceImpl.initChatService()

            if (!auiChatServiceImpl.isLoggedIn()){
                auiChatServiceImpl.loginChat(object : CallBack{
                    override fun onSuccess() {
                        Log.d("VoiceRoomUikit","loginChat suc")
                        if (it == null && lunchType == LaunchType.CREATE){
                            auiChatServiceImpl.createChatRoom(roomInfo.roomId,object :
                                AUICreateChatRoomCallback{
                                override fun onResult(error: AUIException?, chatRoomId: String?) {
                                    if (error == null){
                                        chatRoomId.let {
                                            voiceRoom.bindService(roomService)
                                            Log.d("VoiceRoomUikit","setChatRoomId suc")
                                        }
                                    }else{
                                        Log.e("VoiceRoomUikit","createChatRoom fail ${error.message}")
                                    }
                                }
                            })
                        }else{
                            ThreadManager.getInstance().runOnMainThread {
                                voiceRoom.bindService(roomService)
                            }
                        }
                    }

                    override fun onError(code: Int, error: String?) {
                        Log.e("VoiceRoomUikit","loginChat error $code  $error")
                    }
                })
            }
        }

        eventHandler?.onRoomLaunchSuccess
    }

    enum class ErrorCode(val value: Int, val message: String) {
        RTM_LOGIN_FAILURE(100, "Rtm login failed!"),
        ROOM_PERMISSIONS_LEAK(101, "The room leak required permissions!"),
        ROOM_DESTROYED(102, "The room has been destroyed!"),
    }

    data class RoomEventHandler(
        val onRoomLaunchSuccess: (() -> Unit)? = null,
        val onRoomLaunchFailure: ((ErrorCode) -> Unit)? = null,
    )

    enum class LaunchType{
        UNKNOWN,
        CREATE,
        JOIN
    }

}