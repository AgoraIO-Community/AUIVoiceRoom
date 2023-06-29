package io.agora.asceneskit.voice.binder

import android.content.Context
import android.util.Log
import android.view.View
import io.agora.asceneskit.voice.AUIVoiceRoomService
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.ui.basic.AUIBottomDialog
import io.agora.auikit.ui.member.IAUIRoomMembersView
import io.agora.auikit.ui.member.impl.AUIRoomMemberListView
import io.agora.auikit.ui.member.listener.AUIRoomMembersActionListener
import java.lang.Exception

class AUIRoomMembersBinder constructor(
    context: Context,
    memberView: IAUIRoomMembersView,
    voiceService:AUIVoiceRoomService,
    listener:AUIRoomMemberEvent?,
):
    IAUIBindable, IAUIUserService.AUIUserRespDelegate, AUIRoomMembersActionListener,
    IAUIMicSeatService.AUIMicSeatRespDelegate {

    private var userService:IAUIUserService?
    private var micSeats:IAUIMicSeatService?
    private var memberView:IAUIRoomMembersView?
    private var roomInfo:AUIRoomInfo?
    private var context:Context?=null
    private var roomContext:AUIRoomContext
    private var roomManager:IAUIRoomManager
    private var mMemberMap = mutableMapOf<String, AUIUserInfo>()
    private var mSeatMap = mutableMapOf<Int, String>()
    private var isOwner:Boolean = false
    private var listener:AUIRoomMemberEvent?
    private var dialogMemberView:AUIRoomMemberListView?=null


    init {
        this.userService = voiceService.getUserService()
        this.micSeats = voiceService.getMicSeatsService()
        this.roomManager = voiceService.getRoomManager()
        this.roomInfo = voiceService.getRoomInfo()
        this.roomContext = AUIRoomContext.shared()
        this.memberView = memberView
        this.context = context
        this.listener = listener
        isOwner = roomContext.currentUserInfo.userId == roomInfo?.roomOwner?.userId
    }

    override fun bind() {
        micSeats?.bindRespDelegate(this)
        userService?.bindRespDelegate(this)
        memberView?.setMemberActionListener(this)
    }

    override fun unBind() {
        userService?.unbindRespDelegate(this)
        micSeats?.unbindRespDelegate(this)
        memberView?.setMemberActionListener(null)
    }

    override fun onMemberRankClickListener(view: View) {

    }

    override fun onMemberRightShutDownClickListener(view: View) {
        listener?.onCloseClickListener(view)
    }

    override fun onMemberRightUserMoreClickListener(view: View) {
        context?.let { showUserListDialog(it) }
    }

    /**
     * 显示用户列表
     */
    private fun showUserListDialog(context: Context) {
        dialogMemberView = AUIRoomMemberListView(context)
        dialogMemberView?.setMembers(mMemberMap.values.toList(), mSeatMap)
        dialogMemberView?.setIsOwner(isOwner, roomInfo?.roomOwner?.userId)
        dialogMemberView?.setMemberActionListener(object : AUIRoomMemberListView.ActionListener{
            override fun onKickClick(view: View, position: Int, user: AUIUserInfo) {
                try {
                    val userId = user.userId.toInt()
                    if (isOwner){
                        roomManager.kickUser(roomInfo?.roomId,userId
                        ) {
                            if (it == null){
                                Log.d("AUIRoomMembersBinder","onKickClick suc")
                            }else{
                                Log.d("AUIRoomMembersBinder","onKickClick fail ${it.message}")
                            }
                        }
                    }
                }catch (e:Exception){
                    Log.d("AUIRoomMembersBinder","Conversion exception")
                }
            }
        })

        dialogMemberView.let {
            AUIBottomDialog(context).apply {
                setBackground(null)
                it?.let { it1 -> setCustomView(it1) }
                show()
            }
        }

    }

    /** IAUiUserService.AUiUserRespDelegate */
    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        memberView?.setMemberData(mMemberMap.values.toList())
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap.remove(userInfo.userId)
        memberView?.setMemberData(mMemberMap.values.toList())
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        memberView?.setMemberData(mMemberMap.values.toList())
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.forEach { userInfo ->
            mMemberMap[userInfo.userId] = userInfo
        }
        memberView?.setMemberData(mMemberMap.values.toList())
    }


    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        mSeatMap[seatIndex] = userInfo.userId
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
        }
    }

    interface AUIRoomMemberEvent{
        fun onCloseClickListener(view: View){}
    }

}