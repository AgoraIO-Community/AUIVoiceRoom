package io.agora.asceneskit.voice

import android.util.Log
import io.agora.auikit.model.AUICreateRoomInfo
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.TokenGenerator.generateToken
import io.agora.auikit.service.http.application.ApplicationInterface
import io.agora.auikit.service.http.application.TokenGenerateReq
import io.agora.auikit.service.http.application.TokenGenerateResp
import io.agora.auikit.service.imp.AUIRoomManagerImplRespResp
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUIRtmErrorRespObserver
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm2.RtmClient
import retrofit2.Response

object AUIVoiceRoomUikit {
    private val notInitException =
        RuntimeException("The VoiceServiceManager has not been initialized!")
    private val initedException =
        RuntimeException("The VoiceServiceManager has been initialized!")

    private var mRoomManager: AUIRoomManagerImplRespResp? = null
    private var shouldReleaseRtc = true
    private var mRtcEngineEx: RtcEngineEx? = null
    private var mKTVApi: KTVApi? = null
    private var mService: AUIVoiceRoomService? = null
    private val mErrorObservers = mutableListOf<AUIRtmErrorRespObserverImp>()

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

        mRoomManager = AUIRoomManagerImplRespResp(AUIRoomContext.shared().commonConfig, rtmClient)

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

    fun destroyRoom(roomId: String) {
        if (AUIRoomContext.shared().isRoomOwner(roomId)) {
            mService?.getRoomManager()?.destroyRoom(roomId) {}
        } else {
            mService?.getRoomManager()?.exitRoom(roomId) {}
        }
        mErrorObservers.filter { it.roomId == roomId }.forEach {
            mRoomManager?.rtmManager?.proxy?.unRegisterErrorRespObserver(it)
        }
        mService?.destroyRoom()
        AUIRoomContext.shared().cleanRoom(roomId)
        mService = null
    }

    fun registerRespObserver(delegate: IAUIRoomManager.AUIRoomManagerRespObserver) {
        mRoomManager?.registerRespObserver(delegate)
    }

    fun unRegisterRespObserver(delegate: IAUIRoomManager.AUIRoomManagerRespObserver) {
        mRoomManager?.unRegisterRespObserver(delegate)
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
        val roomManager = mRoomManager ?: AUIRoomManagerImplRespResp(AUIRoomContext.shared().commonConfig, null)
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
        AUIRoomContext.shared().roomConfigMap[roomInfo.roomId] = config
        val roomManager = mRoomManager ?: AUIRoomManagerImplRespResp(AUIRoomContext.shared().commonConfig, null)
        val roomService = AUIVoiceRoomService(
            mRtcEngineEx,
            mKTVApi,
            roomManager,
            config,
            roomInfo
        )

        mService = roomService
        voiceRoom.bindService(roomService)

        val observer = AUIRtmErrorRespObserverImp(roomInfo.roomId)
        mErrorObservers.add(observer)
        mRoomManager?.rtmManager?.proxy?.registerErrorRespObserver(observer)
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

    private fun generateToken(
        roomId: String,
        onSuccess: (AUIRoomConfig) -> Unit,
        onFailure: (AUIException) -> Unit
    ) {
        val config = AUIRoomConfig(roomId)
        var response = 3
        var isFailure = false
        val trySuccess = {
            response -= 1
            if (response == 0 && !isFailure) {
                onSuccess.invoke(config)
            }
        }
        val failure = { ex: AUIException ->
            if (!isFailure) {
                isFailure = true
                onFailure.invoke(ex)
            }
        }

        val userId = AUIRoomContext.shared().currentUserInfo.userId
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.channelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken = rspObj.rtcToken
                        config.rtmToken = rspObj.rtmToken
                        AUIRoomContext.shared().appId = rspObj.appId
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-1, t.message))
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.rtcChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcRtcToken = rspObj.rtcToken
                        config.rtcRtmToken = rspObj.rtmToken
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-2, t.message))
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.rtcChorusChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        // rtcChorusRtcToken007
                        config.rtcChorusRtcToken = rspObj.rtcToken
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: retrofit2.Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-3, t.message))
                }
            })
    }

    private class AUIRtmErrorRespObserverImp(val roomId: String) : AUIRtmErrorRespObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            if (roomId != channelName) {
                return
            }
            generateToken(channelName,
                { mService?.renew(it) },
                {
                    Log.e("AUIVoiceRoomUikit", "onTokenPrivilegeWillExpire >> renew token failed -- $it")
                })
        }
    }
}