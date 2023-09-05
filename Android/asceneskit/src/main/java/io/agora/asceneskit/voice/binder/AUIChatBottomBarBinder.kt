package io.agora.asceneskit.voice.binder

import android.util.Log
import android.view.View
import android.widget.Toast
import io.agora.asceneskit.voice.AUIVoiceRoomService
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.ui.R
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.chatList.AUIChatInfo
import io.agora.auikit.ui.chatList.IAUIChatListView
import io.agora.auikit.ui.micseats.IMicSeatsView
import io.agora.auikit.utils.FastClickTools

class AUIChatBottomBarBinder constructor(
    private val voiceService: AUIVoiceRoomService,
    private val chatBottomBarView: IAUIChatBottomBarView,
    private val chatList: IAUIChatListView,
    private val micSeatsView: IMicSeatsView,
    private val event: AUIChatBottomBarEventDelegate?
) : IAUIBindable,
    AUIMenuItemClickListener,
    IAUIUserService.AUIUserRespDelegate,
    IAUIMicSeatService.AUIMicSeatRespDelegate,
    IAUIInvitationService.AUIInvitationRespDelegate {

    private val roomInfo = voiceService.getRoomInfo()
    private val userService = voiceService.getUserService()
    private val micSeatsService = voiceService.getMicSeatsService()
    private val invitationService = voiceService.getInvitationService()
    private val imManagerService = voiceService.getIMManagerService()
    private val chatManager = voiceService.getChatManager()

    private var listener: AUISoftKeyboardHeightChangeListener? = null
    private var roomContext = AUIRoomContext.shared()
    private var mLocalMute = true
    private val mVolumeMap: HashMap<String, Int> = HashMap()
    private val isRoomOwner = roomContext.isRoomOwner(roomInfo.roomId)

    init {
        val owner = roomContext?.getRoomOwner(roomInfo.roomId)
        owner?.let {
            mVolumeMap[it] = 0
        }
        if (!isRoomOwner) {
            chatBottomBarView.setShowMic(false)
        }
    }

    override fun bind() {
        userService.bindRespDelegate(this)
        micSeatsService.bindRespDelegate(this)
        invitationService.bindRespDelegate(this)

        chatBottomBarView.setMenuItemClickListener(this)
        chatBottomBarView.setSoftKeyListener()
    }

    override fun unBind() {
        userService.unbindRespDelegate(this)
        micSeatsService.unbindRespDelegate(this)
        invitationService.unbindRespDelegate(this)

        chatBottomBarView.setMenuItemClickListener(null)
    }

    fun setMoreStatus(isOwner: Boolean, status: Boolean) {
        chatBottomBarView.setShowMoreStatus(isOwner, status)
    }

    override fun setSoftKeyBoardHeightChangedListener(listener: AUISoftKeyboardHeightChangeListener) {
        this.listener = listener
    }

    fun getSoftKeyboardHeightChangeListener(): AUISoftKeyboardHeightChangeListener? {
        return listener
    }

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
        when (itemId) {
            R.id.voice_extend_item_more -> {
                //自定义预留
                if (view?.let { FastClickTools.isFastClick(it) } == true) return
                chatBottomBarView.setShowMoreStatus(isRoomOwner, false)
                event?.onClickMore(view)
            }

            R.id.voice_extend_item_mic -> {
                //点击下方麦克风
                mLocalMute = !mLocalMute
                userService.muteUserAudio(mLocalMute, null)
                event?.onClickMic(view)
            }

            R.id.voice_extend_item_gift -> {
                if (view?.let { FastClickTools.isFastClick(it) } == true) return
                //点击下方礼物按钮 弹出送礼菜单
                event?.onClickGift(view)
            }

            R.id.voice_extend_item_like -> {
                //点击下方点赞按钮
                event?.onClickLike(view)
            }
        }
    }

    override fun onSendMessage(content: String?) {
        content?: return
        imManagerService.sendMessage(
            roomInfo.roomId,
            content
        ) { _, error ->
            if(error == null){
                chatList.refreshSelectLast(chatManager.getMsgList().map {
                    AUIChatInfo(
                        it.user?.userId ?: "", it.user?.userName ?: "",
                        it.content, it.joined
                    )
                })
            }
        }
    }

    interface AUIChatBottomBarEventDelegate {
        fun onClickGift(view: View?) {}
        fun onClickLike(view: View?) {}
        fun onClickMore(view: View?) {}
        fun onClickMic(view: View?) {}
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (localUserId != userId) {
            return
        }
        var localUserSeat: AUIMicSeatInfo? = null
        for (i in 0..7) {
            val seatInfo = micSeatsService.getMicSeatInfo(i)
            if (seatInfo?.user != null && seatInfo.user?.userId == userId) {
                localUserSeat = seatInfo
                break
            }
        }
        if (localUserSeat != null) {
            if (localUserSeat.muteAudio == 1){
                Toast.makeText(
                    roomContext.commonConfig.context,
                    roomContext.commonConfig.context.getString(io.agora.asceneskit.R.string.voice_room_owner_mute_seat)
                    ,Toast.LENGTH_SHORT
                ).show()
            }
            val userInfo = userService.getUserInfo(userId)
            val mMute = (localUserSeat.muteAudio == 1) || (userInfo?.muteAudio == 1)
            mVolumeMap[userId]?.let { setLocalMute(it, mMute) }
        }
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        mVolumeMap[userInfo.userId] = seatIndex
        if (userInfo.userId == localUserId) { // 本地用户上麦
            chatBottomBarView.setShowMic(true)
            voiceService.setupLocalStreamOn(true)
            val micSeatInfo = micSeatsService.getMicSeatInfo(seatIndex)
            val mUserInfo = userService.getUserInfo(localUserId)
            val isMute = (micSeatInfo?.muteAudio == 1) || (mUserInfo?.muteAudio == 1)
            setLocalMute(seatIndex, isMute)
        }
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        mVolumeMap.remove(userInfo.userId)
        if (userInfo.userId == localUserId) { // 本地用户下麦
            chatBottomBarView.setShowMic(false)
            voiceService.setupLocalStreamOn(false)
        }
    }

    override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
        // 麦位被禁用麦克风
        // 远端用户：关闭对该麦位的音频流订阅
        // 本地用户：保险起见关闭本地用户的麦克风音量
        val micSeatInfo = micSeatsService.getMicSeatInfo(seatIndex)
        val seatUserId = micSeatInfo?.user?.userId
        if (seatUserId.isNullOrEmpty()) {
            return
        }
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (seatUserId == localUserId) {
            userService.muteUserAudio(isMute, null)
        } else {
            voiceService.setupRemoteAudioMute(seatUserId, isMute)
        }
        micSeatsView.stopRippleAnimation(seatIndex)
    }


    private fun setLocalMute(seatIndex: Int, isMute: Boolean) {
        Log.d("local_mic", "update rtc mute state: $isMute")
        mLocalMute = isMute
        voiceService.setupLocalAudioMute(isMute)
        chatBottomBarView.setEnableMic(isMute)

        val seatView = micSeatsView.micSeatItemViewList[seatIndex]
        seatView?.let {
            seatView.setAudioMuteVisibility(if (isMute) View.VISIBLE else View.GONE)
        }
    }

    override fun onApplyListUpdate(userList: ArrayList<AUIUserInfo>?) {
        chatBottomBarView.setShowMoreStatus(isRoomOwner, true)
    }


}