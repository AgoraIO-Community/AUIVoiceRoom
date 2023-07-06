package io.agora.asceneskit.voice.binder

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import io.agora.asceneskit.voice.AUIVoiceRoomService
import io.agora.auikit.model.AUIActionModel
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.imp.AUIInvitationServiceImpl
import io.agora.auikit.ui.action.impI.AUIApplyDialog
import io.agora.auikit.ui.action.impI.AUIInvitationDialog
import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.utils.ThreadManager
import java.util.ArrayList

class AUIInvitationBinder constructor(
    activity: FragmentActivity?,
    voiceService:AUIVoiceRoomService,
):
    IAUIBindable, IAUIInvitationService.AUIInvitationRespDelegate,
    IAUIUserService.AUIUserRespDelegate, IAUIMicSeatService.AUIMicSeatRespDelegate {
    private var activity: FragmentActivity?
    private var mVoiceService:AUIVoiceRoomService
    private var invitationImpl:AUIInvitationServiceImpl
    private var invitationService:IAUIInvitationService
    private val applyList = mutableListOf<AUIUserInfo?>()
    private val applyDialog:AUIApplyDialog
    private val invitationDialog:AUIInvitationDialog

    private var mSeatMap = mutableMapOf<Int, String>()
    private var mMemberMap = mutableMapOf<String, AUIUserInfo?>()
    private var currentMemberList:MutableList<AUIUserInfo?> = mutableListOf()
    private var userService:IAUIUserService

    init {
        this.mVoiceService = voiceService
        this.invitationService = voiceService.getInvitationService()
        this.invitationImpl = invitationService as AUIInvitationServiceImpl
        this.activity = activity
        this.applyDialog = AUIApplyDialog()
        this.invitationDialog = AUIInvitationDialog()
        this.userService = voiceService.getUserService()
    }

    override fun bind() {
        invitationImpl.bindRespDelegate(this)
        mVoiceService.getUserService().bindRespDelegate(this)
        mVoiceService.getMicSeatsService().bindRespDelegate(this)
    }

    override fun unBind() {
        invitationImpl.unbindRespDelegate(this)
        mVoiceService.getUserService().unbindRespDelegate(this)
        mVoiceService.getMicSeatsService().unbindRespDelegate(this)
    }

    // 显示申请列表
    fun showApplyDialog(){
        val applyInfo = AUIActionModel(applyList)
        applyDialog.apply {
            arguments = Bundle().apply {
                putSerializable(AUIApplyDialog.KEY_ROOM_APPLY_BEAN, applyInfo)
                putInt(AUIApplyDialog.KEY_CURRENT_ITEM, 0)
            }
            setApplyDialogListener(object : AUIApplyDialogEventListener {
                override fun onApplyItemClick(
                    view: View,
                    applyIndex: Int?,
                    user: AUIUserInfo?,
                    position: Int
                ) {
                    if (user?.userId != null && applyIndex != null){
                        mVoiceService.getInvitationService().acceptApply(
                            user.userId,
                            applyIndex
                        ) {
                            if (it == null){
                                Log.d("apex","房主同意上麦申请 成功")
                                applyList.removeAt(position)
                                applyDialog.refreshApplyData(applyList)
                            }
                        }
                    }
                }
            })
        }
        activity?.supportFragmentManager?.let {
            applyDialog.show(
                it, "AUIApplyDialog"
            )
        }
    }

    override fun onApplyListUpdate(userList: ArrayList<AUIUserInfo>?) {
        ThreadManager.getInstance().runOnMainThread{
            applyList.clear()
            userList?.forEach { it1 ->
                val userInfo = mVoiceService.getUserService().getUserInfo(it1.userId)
                userInfo?.micIndex = it1.micIndex
                userInfo?.let {
                    applyList.add(it)
                }
            }
            applyDialog.refreshApplyData(applyList)
        }
    }

    //显示邀请列表
    fun showInvitationDialog(index:Int){
        val invitationInfo = AUIActionModel()
        invitationInfo.userList = currentMemberList
        invitationInfo.invitedIndex = index
        Log.e("apex","onShowInvited $index $invitationInfo")

        invitationDialog.apply {
            arguments = Bundle().apply {
                putSerializable(AUIInvitationDialog.KEY_ROOM_INVITED_BEAN, invitationInfo)
                putInt(AUIInvitationDialog.KEY_CURRENT_ITEM, 0)
            }
            setInvitationDialogListener(object : AUIInvitationDialogEventListener {
                override fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUIUserInfo?) {
                    if (user != null){
                        mVoiceService.getInvitationService().sendInvitation(
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
            invitationDialog.show(
                it, "AUIInvitationDialog"
            )
        }
    }

    override fun onReceiveInvitation(userId: String, micIndex: Int) {
        // 收到上麦邀请
        activity?.let {
            AUIAlertDialog(it).apply {
                setTitle("收到上麦邀请")
                setMessage("是否同意上麦？")
                setPositiveButton("同意") {
                    mVoiceService.getInvitationService().acceptInvitation(userId,micIndex
                    ) {
                        if (it == null){
                            Log.d("apex","同意上麦邀请成功")
                        }
                    }
                    dismiss()
                }
                setNegativeButton("拒绝") {
                    mVoiceService.getInvitationService().rejectInvitation(userId){
                        if (it == null){
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
        filterCurrentMember()
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.forEach { userInfo ->
            Log.e("apex","onRoomUserSnapshot $userInfo")
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
        filterCurrentMember()
    }

    private fun filterCurrentMember(){
        // 过滤已在麦位的用户
        currentMemberList.clear()
        mMemberMap.values.toList().forEach {  user ->
            // 在麦位数据和所有成员数据中 查找共有的uid
            val uid = mSeatMap.entries.find { it.value == user?.userId }?.value
            if (user?.userId != uid && user?.userId != mVoiceService.getRoomInfo().roomOwner?.userId){
                currentMemberList.add(user)
            }
        }
        invitationDialog.refreshInvitationData(currentMemberList)
    }

}