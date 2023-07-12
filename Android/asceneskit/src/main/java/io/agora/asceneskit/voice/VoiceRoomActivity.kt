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
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.application.ApplicationInterface
import io.agora.auikit.service.http.application.TokenGenerateReq
import io.agora.auikit.service.http.application.TokenGenerateResp
import io.agora.auikit.service.rtm.AUIRtmErrorProxyDelegate
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.scene.show.utils.PermissionHelp
import retrofit2.Response

class VoiceRoomActivity : AppCompatActivity(), AUIRtmErrorProxyDelegate,
    IAUIRoomManager.AUIRoomManagerRespDelegate {

    private val mViewBinding by lazy { VoiceRoomActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mPermissionHelp = PermissionHelp(this)

    companion object {
        private var roomInfo: AUIRoomInfo? = null
        private var themeId: Int = View.NO_ID
        private var lunchType:VoiceRoomUikit.LaunchType = VoiceRoomUikit.LaunchType.UNKNOWN

        fun launch(lunchType: VoiceRoomUikit.LaunchType,context: Context, roomInfo: AUIRoomInfo, themeId: Int = R.style.Theme_VoiceRoom) {
            Companion.roomInfo = roomInfo
            VoiceRoomActivity.themeId = themeId
            Companion.lunchType = lunchType

            val intent = Intent(context, VoiceRoomActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (themeId != View.NO_ID) {
            setTheme(themeId)
        }
        setContentView(mViewBinding.root)
        mViewBinding.VoiceRoomView.setFragmentActivity(this)

        val roomInfo = roomInfo ?: return
        mViewBinding.VoiceRoomView.setOnShutDownClick {
            onUserLeaveRoom()
        }
        mViewBinding.VoiceRoomView.setOnRoomDestroyEvent{
            shutDownRoom()
        }
        mPermissionHelp.checkMicPerm(
            {
                generateToken { config ->
                    VoiceRoomUikit.launchRoom(
                        lunchType,
                        roomInfo,
                        config,
                        mViewBinding.VoiceRoomView,
                        VoiceRoomUikit.RoomEventHandler {

                        })
                    VoiceRoomUikit.subscribeError(roomInfo.roomId, this)
                    VoiceRoomUikit.bindRespDelegate(this)
                }
            },
            {
                finish()
            },
            true
        )

    }

    override fun onBackPressed() {
        onUserLeaveRoom()
    }

    private fun generateToken(onSuccess: (AUIRoomConfig) -> Unit) {
        val config = AUIRoomConfig( roomInfo?.roomId ?: "")
        config.themeId = themeId
        var response = 3
        val trySuccess = {
            response -= 1;
            if (response == 0) {
                onSuccess.invoke(config)
            }
        }
        val userId = AUIRoomContext.shared().currentUserInfo.userId
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate(TokenGenerateReq(config.channelName, userId))
            .enqueue(object : retrofit2.Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, response: Response<CommonResp<TokenGenerateResp>>) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken007 = rspObj.rtcToken
                        config.rtmToken007 = rspObj.rtmToken
                        AUIRoomContext.shared()?.commonConfig?.appId = rspObj.appId
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
        HttpManager
            .getService(ApplicationInterface::class.java)
            .tokenGenerate006(TokenGenerateReq(config.rtcChannelName, userId))
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
                        config.rtcChorusRtcToken007 = rspObj.rtcToken
                    }
                    trySuccess.invoke()
                }
                override fun onFailure(call: retrofit2.Call<CommonResp<TokenGenerateResp>>, t: Throwable) {
                    trySuccess.invoke()
                }
            })
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {

    }

    private fun onUserLeaveRoom() {
        val owner = (roomInfo?.roomOwner?.userId == AUIRoomContext.shared().currentUserInfo.userId)
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
        roomInfo?.roomId?.let { roomId ->
            VoiceRoomUikit.destroyRoom(roomId)
            VoiceRoomUikit.unsubscribeError(roomId, this@VoiceRoomActivity)
            VoiceRoomUikit.unbindRespDelegate(this@VoiceRoomActivity)
        }
        finish()
    }

}