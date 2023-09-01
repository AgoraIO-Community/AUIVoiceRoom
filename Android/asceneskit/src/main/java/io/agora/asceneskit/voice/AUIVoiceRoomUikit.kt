package io.agora.asceneskit.voice

import io.agora.auikit.model.AUICreateRoomInfo
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.imp.AUIRoomManagerImpl
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUIRtmErrorProxyDelegate
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm2.RtmClient

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
        ktvApi: KTVApi? = null,
        rtcEngineEx: RtcEngineEx? = null,
        rtmClient: RtmClient? = null
    ) {
        if (mRoomManager != null) {
            throw initedException
        }

        mKTVApi = ktvApi

        if (rtcEngineEx != null) { // 用户塞进来的engine由用户自己管理生命周期
            mRtcEngineEx = rtcEngineEx
            shouldReleaseRtc = false
        }

        mRoomManager = AUIRoomManagerImpl(AUIRoomContext.shared().commonConfig, rtmClient)

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
        val roomManager = mRoomManager ?: AUIRoomManagerImpl(AUIRoomContext.shared().commonConfig, null)
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
        roomInfo: AUIRoomInfo,
        config: AUIRoomConfig,
        voiceRoom:AUIVoiceRoomView,
        eventHandler: RoomEventHandler? = null,
    ) {
        RtcEngine.destroy()
        AUIRoomContext.shared().roomConfig = config
        val roomManager = mRoomManager ?: AUIRoomManagerImpl(AUIRoomContext.shared().commonConfig, null)
        val roomService = AUIVoiceRoomService(
            mRtcEngineEx,
            mKTVApi,
            roomManager,
            config,
            roomInfo
        )

        mService = roomService
        voiceRoom.bindService(roomService)
        eventHandler?.onRoomLaunchSuccess?.invoke(roomService)
    }

    enum class ErrorCode(val value: Int, val message: String) {
        RTM_LOGIN_FAILURE(100, "Rtm login failed!"),
        ROOM_PERMISSIONS_LEAK(101, "The room leak required permissions!"),
        ROOM_DESTROYED(102, "The room has been destroyed!"),
    }

    data class RoomEventHandler(
        val onRoomLaunchSuccess: ((AUIVoiceRoomService) -> Unit)? = null,
        val onRoomLaunchFailure: ((ErrorCode) -> Unit)? = null,
    )

}