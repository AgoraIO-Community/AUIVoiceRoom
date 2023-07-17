package io.agora.asceneskit.voice.binder

import android.util.Log
import android.view.View
import io.agora.asceneskit.voice.AUIVoiceRoomService
import io.agora.auikit.R
import io.agora.auikit.model.*
import io.agora.auikit.service.IAUIChatService
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.imp.AUIChatServiceImpl
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.chatList.IAUIChatListView
import io.agora.auikit.ui.micseats.IMicSeatsView

class AUIChatBottomBarBinder constructor(
    voiceService: AUIVoiceRoomService?,
    chatBottomBarView: IAUIChatBottomBarView,
    chatList:IAUIChatListView,
    micSeatsView: IMicSeatsView,
    event:AUIChatBottomBarEventDelegate?
) : IAUIBindable, AUIMenuItemClickListener, IAUIUserService.AUIUserRespDelegate,
    IAUIMicSeatService.AUIMicSeatRespDelegate, IAUIInvitationService.AUIInvitationRespDelegate {

    private var listener:AUISoftKeyboardHeightChangeListener?=null
    private var chatBottomBarView: IAUIChatBottomBarView?
    private var auiBarrageView: IAUIChatListView?
    private var event:AUIChatBottomBarEventDelegate?
    private var mVoiceService:AUIVoiceRoomService?
    private var chatService:IAUIChatService?
    private var userService:IAUIUserService?
    private var roomContext:AUIRoomContext?
    private var chatImpl:AUIChatServiceImpl?
    private var micSeat:IAUIMicSeatService?
    private var invitationImpl:IAUIInvitationService?
    private var mLocalMute = true
    private var micSeatsView:IMicSeatsView
    private val mVolumeMap: HashMap<String, Int> = HashMap<String, Int>()


    init {
        this.chatBottomBarView = chatBottomBarView
        this.auiBarrageView = chatList
        this.event = event
        this.mVoiceService = voiceService
        this.chatService = voiceService?.getChatService()
        this.userService = voiceService?.getUserService()
        this.micSeat = voiceService?.getMicSeatsService()
        this.invitationImpl = voiceService?.getInvitationService()
        this.roomContext = AUIRoomContext.shared()
        this.micSeatsView = micSeatsView
        val owner = roomContext?.getRoomOwner(chatService?.channelName)
        owner?.let {
            mVolumeMap[it] = 0
        }
        chatImpl = chatService as AUIChatServiceImpl
        if (roomContext?.isRoomOwner(chatImpl?.channelName) != true){
            chatBottomBarView.setShowMic(false)
        }
    }

    override fun bind() {
        userService?.bindRespDelegate(this)
        micSeat?.bindRespDelegate(this)
        chatBottomBarView?.setMenuItemClickListener(this)
        invitationImpl?.bindRespDelegate(this)
        chatBottomBarView?.setSoftKeyListener()
    }

    override fun unBind() {
        chatBottomBarView?.setMenuItemClickListener(null)
        userService?.unbindRespDelegate(this)
        micSeat?.unbindRespDelegate(this)
        invitationImpl?.unbindRespDelegate(this)
    }

    fun setMoreStatus(isOwner:Boolean,status:Boolean){
        chatBottomBarView?.setShowMoreStatus(chatImpl?.isOwner(),status)
    }

    override fun setSoftKeyBoardHeightChangedListener(listener: AUISoftKeyboardHeightChangeListener) {
        this.listener = listener
    }

    fun getSoftKeyboardHeightChangeListener(): AUISoftKeyboardHeightChangeListener?{
        return listener
    }

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
        when (itemId) {
            R.id.voice_extend_item_more -> {
                //自定义预留
                Log.e("apex","more")
                chatBottomBarView?.setShowMoreStatus(chatImpl?.isOwner(),false)
                event?.onClickMore(view)
            }
            R.id.voice_extend_item_mic -> {
                //点击下方麦克风
                Log.e("apex","mic")
                mVolumeMap[roomContext?.currentUserInfo?.userId]?.let {
                    setLocalMute(it,mLocalMute)
                }
                mLocalMute = !mLocalMute
                event?.onClickMic(view)
            }
            R.id.voice_extend_item_gift -> {
                //点击下方礼物按钮 弹出送礼菜单
                event?.onClickGift(view)
            }
            R.id.voice_extend_item_like -> {
                //点击下方点赞按钮
                event?.onClickLike(view)
                Log.e("apex","like")
            }
        }
    }

    override fun onSendMessage(content: String?) {
        chatService?.sendMessage(chatImpl?.getCurrentRoom(),content,roomContext?.currentUserInfo,object: AUIChatMsgCallback{
            override fun onResult(error: AUIException?, message: AgoraChatMessage?) {
                auiBarrageView?.refreshSelectLast(chatImpl?.getMsgList())
            }
        })
    }

    interface AUIChatBottomBarEventDelegate{
        fun onClickGift(view: View?){}
        fun onClickLike(view: View?){}
        fun onClickMore(view: View?){}
        fun onClickMic(view: View?){}
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (localUserId != userId) {
            return
        }
        var localUserSeat: AUIMicSeatInfo? = null
        for (i in 0..7) {
            val seatInfo = mVoiceService?.getMicSeatsService()?.getMicSeatInfo(i)
            if (seatInfo?.user != null && seatInfo.user?.userId == userId) {
                localUserSeat = seatInfo
                break
            }
        }
        if (localUserSeat != null) {
            val userInfo = userService?.getUserInfo(userId)
            val mute = (localUserSeat.muteAudio == 1) || (userInfo?.muteAudio == 1)
            mVolumeMap[userId]?.let { setLocalMute(it,mute) }
        }
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        mVolumeMap[userInfo.userId] = seatIndex
        if (userInfo.userId == localUserId) { // 本地用户上麦
            chatBottomBarView?.setShowMic(true)
            mVoiceService?.setupLocalStreamOn(true)
            val micSeatInfo = mVoiceService?.getMicSeatsService()?.getMicSeatInfo(seatIndex)
            val userInfo = mVoiceService?.getUserService()?.getUserInfo(localUserId)
            val isMute = (micSeatInfo?.muteAudio == 1) || (userInfo?.muteAudio == 1)
            setLocalMute(seatIndex,isMute)
        }
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        mVolumeMap.remove(userInfo.userId)
        if (userInfo.userId == localUserId) { // 本地用户下麦
            chatBottomBarView?.setShowMic(false)
            mVoiceService?.setupLocalStreamOn(false)
        }
    }

    override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
        // 麦位被禁用麦克风
        // 远端用户：关闭对该麦位的音频流订阅
        // 本地用户：保险起见关闭本地用户的麦克风音量
        val micSeatInfo = mVoiceService?.getMicSeatsService()?.getMicSeatInfo(seatIndex)
        val seatUserId = micSeatInfo?.user?.userId
        if (seatUserId == null || seatUserId.isEmpty()) {
            return
        }
        val userInfo = mVoiceService?.getUserService()?.getUserInfo(seatUserId) ?: return
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        val mute = isMute || (userInfo.muteAudio == 1)
        if (seatUserId == localUserId) {
            setLocalMute(seatIndex,mute)
        } else {
            mVoiceService?.setupRemoteAudioMute(seatUserId, mute)
        }
    }


    private fun setLocalMute(seatIndex:Int,isMute: Boolean) {
        Log.d("local_mic","update rtc mute state: $isMute")
        mLocalMute = isMute
        mVoiceService?.setupLocalAudioMute(isMute)
        chatBottomBarView?.setEnableMic(isMute)

        val seatView = micSeatsView.micSeatItemViewList[seatIndex]
        seatView?.let {
            seatView.setAudioMuteVisibility(if (isMute) View.VISIBLE else View.GONE)
        }
    }

    override fun onApplyListUpdate(userList: ArrayList<AUIUserInfo>?) {
        chatBottomBarView?.setShowMoreStatus(chatImpl?.isOwner(),true)
    }





}