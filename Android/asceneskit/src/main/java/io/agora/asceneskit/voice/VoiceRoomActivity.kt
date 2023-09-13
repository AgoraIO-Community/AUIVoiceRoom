package io.agora.asceneskit.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.asceneskit.R
import io.agora.asceneskit.databinding.VoiceRoomActivityBinding
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.application.ApplicationInterface
import io.agora.auikit.service.http.application.TokenGenerateReq
import io.agora.auikit.service.http.application.TokenGenerateResp
import io.agora.auikit.service.rtm.AUIRtmErrorProxyDelegate
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.PermissionHelp
import retrofit2.Response

class VoiceRoomActivity : AppCompatActivity(), AUIRtmErrorProxyDelegate,
    IAUIRoomManager.AUIRoomManagerRespDelegate {

    private val mViewBinding by lazy { VoiceRoomActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mPermissionHelp = PermissionHelp(this)
    private var service:AUIVoiceRoomService? = null

    companion object {
        private var roomInfo: AUIRoomInfo = AUIRoomInfo()
        private var themeId: Int = View.NO_ID

        fun launch(context: Context, roomInfo: AUIRoomInfo, themeId: Int = R.style.Theme_VoiceRoom) {
            Companion.roomInfo = roomInfo
            VoiceRoomActivity.themeId = themeId

            val intent = Intent(context, VoiceRoomActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            themeId = savedInstanceState.getInt("themeId")
            roomInfo.roomId = savedInstanceState.getString("roomId").toString()
            val owner = AUIUserThumbnailInfo()
            owner.userId = savedInstanceState.getString("userId").toString()
            owner.userName = savedInstanceState.getString("userName").toString()
            owner.userAvatar = savedInstanceState.getString("userAvatar").toString()
            roomInfo.roomOwner = owner
            roomInfo.onlineUsers = savedInstanceState.getInt("onlineUsers")
            roomInfo.createTime = savedInstanceState.getLong("createTime")
            roomInfo.roomName = savedInstanceState.getString("roomName").toString()
            roomInfo.thumbnail = savedInstanceState.getString("thumbnail").toString()
            roomInfo.micSeatCount = savedInstanceState.getInt("micSeatCount")
            roomInfo.password = savedInstanceState.getString("password").toString()
            roomInfo.micSeatStyle = savedInstanceState.getString("micSeatStyle").toString()
        }

        if (themeId != View.NO_ID) {
            setTheme(themeId)
        }
        setContentView(mViewBinding.root)
        mViewBinding.VoiceRoomView.setFragmentActivity(this)

        mViewBinding.VoiceRoomView.setOnShutDownClick {
            onUserLeaveRoom()
        }
        mViewBinding.VoiceRoomView.setOnRoomDestroyEvent{
            shutDownRoom()
        }
        mPermissionHelp.checkMicPerm(
            {
                roomInfo.let {
                    generateToken(roomInfo.roomId) { config ->
                        AUIVoiceRoomUikit.launchRoom(
                            it,
                            config,
                            mViewBinding.VoiceRoomView,
                            AUIVoiceRoomUikit.RoomEventHandler(
                                onRoomLaunchSuccess = {
                                    this.service = it
                                },
                                onRoomLaunchFailure = {

                                }
                            ))
                        AUIVoiceRoomUikit.subscribeError(it.roomId, this)
                        AUIVoiceRoomUikit.bindRespDelegate(this)
                    }
                }
            },
            {
                finish()
            },
            true
        )

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        roomInfo.let {
            outState.putInt("themeId",themeId)
            outState.putString("roomId", it.roomId)
            outState.putString("userId", it.roomOwner?.userId)
            outState.putString("userName", it.roomOwner?.userName)
            outState.putString("userAvatar", it.roomOwner?.userAvatar)
            outState.putInt("onlineUsers",it.onlineUsers)
            outState.putLong("createTime",it.createTime)
            outState.putString("roomName",it.roomName)
            outState.putString("thumbnail",it.thumbnail)
            outState.putInt("micSeatCount",it.micSeatCount)
            outState.putString("password",it.password)
            outState.putString("micSeatStyle",it.micSeatStyle)
        }
    }

    override fun onBackPressed() {
        AUILogger.logger().d("VoiceRoomActivity", "onBackPressed ...")
        onUserLeaveRoom()
    }

    private fun generateToken(roomId:String?,onSuccess: (AUIRoomConfig) -> Unit) {
        val config = AUIRoomConfig( roomId ?: "")
        var response = 3
        val trySuccess = {
            response -= 1;
            if (response == 0) {
                onSuccess.invoke(config)
            }
        }
        val userId = AUIRoomContext.shared().currentUserInfo.userId
        AUILogger.logger().d("VoiceRoomActivity", "generateToken start channelName=${config.channelName} userId=$userId...")
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.channelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken = rspObj.rtcToken
                        config.rtmToken = rspObj.rtmToken
                        AUIRoomContext.shared()?.commonConfig?.appId = rspObj.appId
                        AUILogger.logger().d("VoiceRoomActivity", "generateToken update rtcToken/rtmToken...")
                    }
                    AUILogger.logger().d("VoiceRoomActivity", "generateToken success...")
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    AUILogger.logger().e("VoiceRoomActivity", "generateToken onFailure ${t.message}...")
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.rtcChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcRtcToken = rspObj.rtcToken
                        config.rtcRtmToken = rspObj.rtmToken
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.rtcChorusChannelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        // rtcChorusRtcToken007
                        config.rtcChorusRtcToken = rspObj.rtcToken
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        AUILogger.logger().d("VoiceRoomActivity", "onTokenPrivilegeWillExpire $channelName ...")
        generateToken(channelName, onSuccess = {
            service?.renew(it)
        })
    }

    private fun onUserLeaveRoom() {
        val owner = (roomInfo.roomOwner?.userId == AUIRoomContext.shared().currentUserInfo.userId)
        AUIAlertDialog(this).apply {
            setTitle("Tip")
            if (owner) {
                setMessage("是否离开并销毁房间？")
            } else {
                setMessage("是否离开房间？")
            }
            setPositiveButton("确认") {
                dismiss()
                shutDownRoom()
            }
            setNegativeButton("取消") {
                dismiss()
            }
            show()
        }
    }

    private fun shutDownRoom() {
        AUILogger.logger().d("VoiceRoomActivity", "shutDownRoom ...")
        roomInfo.roomId.let { roomId ->
            AUIVoiceRoomUikit.destroyRoom(roomId)
            AUIVoiceRoomUikit.unsubscribeError(roomId, this@VoiceRoomActivity)
            AUIVoiceRoomUikit.unbindRespDelegate(this@VoiceRoomActivity)
        }
        finish()
    }

}