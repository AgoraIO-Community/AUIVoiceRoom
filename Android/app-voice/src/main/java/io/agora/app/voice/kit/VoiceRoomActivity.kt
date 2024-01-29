package io.agora.app.voice.kit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.asceneskit.R
import io.agora.asceneskit.databinding.VoiceRoomActivityBinding
import io.agora.asceneskit.voice.AUIVoiceRoomObserver
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.PermissionHelp

class VoiceRoomActivity : AppCompatActivity(), AUIVoiceRoomObserver {

    private val mViewBinding by lazy { VoiceRoomActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mPermissionHelp = PermissionHelp(this)

    companion object {
        private val EXTRA_IS_CREATE_ROOM = "isCreateRoom"
        private val EXTRA_ROOM_INFO = "roomInfo"
        private val EXTRA_THEME_ID = "themeId"

        fun launch(
            context: Context,
            isCreateRoom: Boolean,
            roomInfo: AUIRoomInfo,
            themeId: Int = R.style.Theme_VoiceRoom
        ) {
            val intent = Intent(context, VoiceRoomActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_IS_CREATE_ROOM, isCreateRoom)
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            intent.putExtra(EXTRA_THEME_ID, themeId)
            context.startActivity(intent)
        }
    }

    private val themeId by lazy { intent.getIntExtra(EXTRA_THEME_ID, View.NO_ID) }
    private val isCreateRoom by lazy { intent.getBooleanExtra(EXTRA_IS_CREATE_ROOM, false) }
    private val roomInfo by lazy {
        intent.getSerializableExtra(EXTRA_ROOM_INFO) as AUIRoomInfo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            roomInfo.roomId = savedInstanceState.getString("roomId").toString()
            val owner = AUIUserThumbnailInfo()
            owner.userId = savedInstanceState.getString("userId").toString()
            owner.userName = savedInstanceState.getString("userName").toString()
            owner.userAvatar = savedInstanceState.getString("userAvatar").toString()
            roomInfo.owner = owner
            roomInfo.memberCount = savedInstanceState.getInt("onlineUsers")
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
        mViewBinding.VoiceRoomView.setOnRoomDestroyEvent {
            shutDownRoom()
        }
        mPermissionHelp.checkMicPerm(
            {
                AUIVoiceRoomUikit.generateToken(
                    roomInfo.roomId,
                    onSuccess = { roomConfig ->
                        if (isCreateRoom) {
                            AUIVoiceRoomUikit.createRoom(
                                roomInfo,
                                roomConfig,
                                mViewBinding.VoiceRoomView,
                                completion = { error, _ ->
                                    if (error != null) {
                                        shutDownRoom()
                                    }
                                }
                            )
                        } else {
                            AUIVoiceRoomUikit.launchRoom(
                                roomInfo,
                                roomConfig,
                                mViewBinding.VoiceRoomView,
                                completion = { error, _ ->
                                    if (error != null) {
                                        shutDownRoom()
                                    }
                                }
                            )
                        }
                        AUIVoiceRoomUikit.registerRespObserver(roomId = roomInfo.roomId, this)
                    },
                    onFailure = {
                        shutDownRoom()
                    }
                )
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
            outState.putInt("themeId", themeId)
            outState.putString("roomId", it.roomId)
            outState.putString("userId", it.owner?.userId)
            outState.putString("userName", it.owner?.userName)
            outState.putString("userAvatar", it.owner?.userAvatar)
            outState.putInt("onlineUsers", it.memberCount)
            outState.putLong("createTime", it.createTime)
            outState.putString("roomName", it.roomName)
            outState.putString("thumbnail", it.thumbnail)
            outState.putInt("micSeatCount", it.micSeatCount)
            outState.putString("password", it.password)
            outState.putString("micSeatStyle", it.micSeatStyle)
        }
    }

    override fun onBackPressed() {
        AUILogger.logger().d("VoiceRoomActivity", "onBackPressed ...")
        onUserLeaveRoom()
    }

    private fun onUserLeaveRoom() {
        val owner = (roomInfo.owner?.userId == AUIRoomContext.shared().currentUserInfo.userId)
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
        val roomId = roomInfo.roomId
        AUIVoiceRoomUikit.destroyRoom(roomId)
        AUIVoiceRoomUikit.unRegisterRespObserver(roomId, this@VoiceRoomActivity)
        finish()
    }

}