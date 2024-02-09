package io.agora.asceneskit.voice.binder

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import io.agora.asceneskit.R
import io.agora.asceneskit.voice.AUIVoiceRoomService
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.ui.action.AUIActionUserInfo
import io.agora.auikit.ui.action.AUIActionUserInfoList
import io.agora.auikit.ui.action.impI.AUIApplyDialog
import io.agora.auikit.ui.action.impI.AUIInvitationDialog
import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener
import io.agora.auikit.ui.basic.AUIAlertDialog

class AUIInvitationBinder constructor(
    activity: FragmentActivity?,
    voiceService:AUIVoiceRoomService,
):
    IAUIBindable, IAUIInvitationService.AUIInvitationRespObserver,
    IAUIUserService.AUIUserRespObserver, IAUIMicSeatService.AUIMicSeatRespObserver {
    private var activity: FragmentActivity?
    private var mVoiceService:AUIVoiceRoomService
    private var invitationService:IAUIInvitationService
    private val applyList = mutableListOf<AUIUserInfo?>()
    private var applyDialog:AUIApplyDialog?=null
    private var invitationDialog:AUIInvitationDialog?=null

    private var mSeatMap = mutableMapOf<Int, String>()
    private var mMemberMap = mutableMapOf<String?, AUIUserInfo?>()
    private var currentMemberList:MutableList<AUIUserInfo?> = mutableListOf()
    private var userService:IAUIUserService
    private var applyUserList = ArrayList<AUIUserInfo?>()

    init {
        this.mVoiceService = voiceService
        this.invitationService = voiceService.invitationService
        this.activity = activity
        this.userService = voiceService.userService

        val roomOwner = voiceService.roomInfo?.owner
        val owner = AUIUserInfo()
        owner.userName = roomOwner?.userName.toString()
        owner.userId = roomOwner?.userId.toString()
        owner.userAvatar = roomOwner?.userAvatar.toString()
        mMemberMap[roomOwner?.userId] = owner
        mSeatMap[0] = owner.userId
    }

    override fun bind() {
        invitationService.registerRespObserver(this)
        mVoiceService.userService.registerRespObserver(this)
        mVoiceService.micSeatService.registerRespObserver(this)
    }

    override fun unBind() {
        invitationService.unRegisterRespObserver(this)
        mVoiceService.userService.unRegisterRespObserver(this)
        mVoiceService.micSeatService.unRegisterRespObserver(this)
    }

    // 显示申请列表
    fun showApplyDialog(){
        var dialog = applyDialog
        if (dialog == null){
            dialog = AUIApplyDialog()
            dialog.refreshApplyData(applyList.map { userInfo ->
                AUIActionUserInfo(
                    userInfo?.userId ?: "",
                    userInfo?.userName ?: "",
                    userInfo?.userAvatar ?: "",
                    userInfo?.micIndex ?: 0
                )
            })
            applyDialog = dialog
        }
        dialog.setApplyDialogListener(object : AUIApplyDialogEventListener {
            override fun onApplyItemClick(
                view: View,
                applyIndex: Int?,
                user: AUIActionUserInfo?,
                position: Int
            ) {
                if (user?.userId != null && applyIndex != null){
                    mVoiceService.invitationService.acceptApply(
                        user.userId,
                        applyIndex
                    ) {
                        if (it == null){
                            Log.d("apex","房主同意上麦申请 成功")
                            Toast.makeText(view.context, "同意上麦申请成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(view.context, "同意上麦申请失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
        activity?.supportFragmentManager?.let {
            applyDialog?.show(
                it, "AUIApplyDialog"
            )
        }
    }

    override fun onApplyListUpdate(userList: List<AUIUserInfo>?) {
        super.onApplyListUpdate(userList)
        applyUserList = ArrayList(userList ?: emptyList())
        applyList.clear()
        userList?.forEach { user ->
            val userInfo = mMemberMap[user.userId]
            userInfo?.micIndex = user.micIndex
            userInfo?.let {
                applyList.add(it)
            }
        }
        applyDialog?.refreshApplyData(applyList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
    }

    override fun onApplyWillAccept(userId: String, seatIndex: Int): AUIException? {
        if (mSeatMap[seatIndex]?.isNotEmpty() == true) {
            return AUIException(AUIException.ERROR_CODE_SEAT_NOT_IDLE, "")
        }
        userService.getUserInfo(userId)?.let {
            mVoiceService.micSeatService.pickSeat(seatIndex, it) {}
        }
        return super.onApplyWillAccept(userId, seatIndex)
    }

    override fun onInviteWillAccept(userId: String, seatIndex: Int): AUIException? {
        if (mSeatMap[seatIndex]?.isNotEmpty() == true) {
            return AUIException(AUIException.ERROR_CODE_SEAT_NOT_IDLE, "")
        }
        userService.getUserInfo(userId)?.let {
            mVoiceService.micSeatService.pickSeat(seatIndex, it) {}
        }

        return super.onInviteWillAccept(userId, seatIndex)
    }


    //显示邀请列表
    fun showInvitationDialog(index:Int){
        val invitationInfo = AUIActionUserInfoList(currentMemberList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
        invitationInfo.invitedIndex = index
        Log.e("apex","onShowInvited $index $invitationInfo")
        invitationDialog = AUIInvitationDialog()
        invitationDialog?.apply {
            arguments = Bundle().apply {
                putSerializable(AUIInvitationDialog.KEY_ROOM_INVITED_BEAN, invitationInfo)
                putInt(AUIInvitationDialog.KEY_CURRENT_ITEM, 0)
            }
            setInvitationDialogListener(object : AUIInvitationDialogEventListener {
                override fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUIActionUserInfo?) {
                    if (user != null){
                        mVoiceService.invitationService.sendInvitation(
                            user.userId,
                            invitedIndex
                        ) {
                            if (it == null){
                                Log.d("apex","邀请${user.userId}上麦成功 $invitedIndex 成功")
                            }
                        }
                    }
                }
            })
        }
        activity?.supportFragmentManager?.let {
            invitationDialog?.show(
                it, "AUIInvitationDialog"
            )
        }
    }

    override fun onReceiveInvitation(userId: String, micIndex: Int) {
        if(userId != mVoiceService.micSeatService.roomContext.currentUserInfo.userId){
            return
        }
        // 收到上麦邀请
        activity?.let {
            AUIAlertDialog(it).apply {
                setTitle(context.getString(R.string.voice_room_invited_action))
                setMessage(
                    if (micIndex == -1){
                        context.getString(R.string.voice_room_receive_invitation_content)
                    }else{
                        context.getString(R.string.voice_room_receive_invitation_title,micIndex+1)
                    }
                )
                setPositiveButton(context.getString(R.string.voice_room_confirm)) {
                    mVoiceService.invitationService.acceptInvitation(userId,micIndex
                    ) { e->
                        if (e == null){
                            Log.d("apex","同意上麦邀请成功")
                        }
                    }
                    dismiss()
                }
                setNegativeButton( context.getString(R.string.voice_room_reject)) {
                    mVoiceService.invitationService.rejectInvitation(userId){ e ->
                        if (e == null){
                            Log.d("apex","拒绝上麦邀请成功")
                        }
                    }
                    dismiss()
                }
                show()
            }
        }
    }


    /** IAUiUserService.AUiUserRespDelegate */
    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        Log.d("apex","onRoomUserEnter ${userInfo.userId}")
        val auiUserInfo = userService.getUserInfo(userInfo.userId)
        mMemberMap[userInfo.userId] = auiUserInfo
        filterCurrentMember()
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap.remove(userInfo.userId)
        filterCurrentMember()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        updateApplyUserInfo()
        filterCurrentMember()
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.forEach { userInfo ->
            Log.e("apex","onRoomUserSnapshot ${userInfo.userName} - ${userInfo.userAvatar}")
            mMemberMap[userInfo.userId] = userInfo
        }
        filterCurrentMember()
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        mSeatMap[seatIndex] = userInfo.userId
        filterCurrentMember()
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
        }
        invitationService.cleanUserInfo(userInfo.userId){}
        filterCurrentMember()
    }

    private fun filterCurrentMember(){
        // 过滤已在麦位的用户
        currentMemberList.clear()
        mMemberMap.values.toList().forEach {  user ->
            // 在麦位数据和所有成员数据中 查找共有的uid
            val uid = mSeatMap.entries.find { it.value == user?.userId }?.value
            if (user?.userId != uid && user?.userId != mVoiceService.roomInfo?.owner?.userId){
                currentMemberList.add(user)
            }
        }
        invitationDialog?.refreshInvitationData(currentMemberList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
    }

    private fun updateApplyUserInfo(){
        applyList.clear()
        applyUserList.forEach { it1 ->
            val userInfo = mMemberMap[it1?.userId]
            userInfo?.micIndex = it1?.micIndex
            userInfo?.let {
                applyList.add(it)
            }
        }
        applyDialog?.refreshApplyData(applyList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
    }

}