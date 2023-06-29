package io.agora.asceneskit.voice.binder

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.ui.roomInfo.IAUIRoomInfoView
import io.agora.auikit.ui.roomInfo.impl.AUIRoomInfoView
import io.agora.auikit.ui.roomInfo.listener.AUIRoomInfoActionListener

class AUIRoomInfoBinder constructor(
    roomInfoView: IAUIRoomInfoView,
    auiRoomInfo: AUIRoomInfo,
    listener:AUIRoomInfoEvent?
):
    IAUIBindable, AUIRoomInfoActionListener {
    private var auiRoomInfoView:AUIRoomInfoView? = null
    private var roomInfoView:IAUIRoomInfoView? = null
    private var mMainHandler:Handler? = null
    private var auiRoomInfo:AUIRoomInfo? = null
    private var listener:AUIRoomInfoEvent?

    init {
        mMainHandler = Handler(Looper.getMainLooper())
        this.roomInfoView = roomInfoView
        this.auiRoomInfoView = roomInfoView as AUIRoomInfoView
        this.auiRoomInfo = auiRoomInfo
        this.listener = listener

        // 缺少房间信息server 设置房主头像、房间标题和子标题
        auiRoomInfo.let {
            it.roomOwner?.userAvatar?.let { it1 -> auiRoomInfoView?.setMemberAvatar(it1) }
            it.roomName.let { it1 -> auiRoomInfoView?.setVoiceTitle(it1) }
            auiRoomInfoView?.setVoiceSubTitle(it.roomId)
        }
    }

    override fun bind() {
        auiRoomInfoView?.setRoomInfoActionListener(this)
    }

    override fun unBind() {
        mMainHandler?.removeCallbacksAndMessages(null)
        mMainHandler = null
        roomInfoView?.setRoomInfoActionListener(null)
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (mMainHandler != null) {
            if (mMainHandler?.looper?.thread === Thread.currentThread()) {
                runnable.run()
            } else {
                mMainHandler?.post(runnable)
            }
        }
    }

    override fun onClickUpperLeftAvatar(view: View) {
        Log.d("AUIRoomInfoBinder","onClickUpperLeftAvatar")
    }

    override fun onLongClickUpperLeftAvatar(view: View): Boolean {
        Log.d("AUIRoomInfoBinder","onLongClickUpperLeftAvatar")
        return true
    }

    override fun onUpperLeftRightIconClickListener(view: View) {
        Log.d("AUIRoomInfoBinder","onUpperLeftRightIconClickListener")
    }

    override fun onBackClickListener(view: View) {
        listener?.onBackClickListener(view)
    }

    interface AUIRoomInfoEvent{
        fun onBackClickListener(view: View){}
    }

}