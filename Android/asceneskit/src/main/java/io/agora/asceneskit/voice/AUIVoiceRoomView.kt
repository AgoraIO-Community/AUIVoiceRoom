package io.agora.asceneskit.voice

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import io.agora.asceneskit.R
import io.agora.asceneskit.databinding.VoiceRoomViewBinding
import io.agora.asceneskit.voice.binder.*
import io.agora.asceneskit.voice.dialog.*
import io.agora.auikit.model.*
import io.agora.auikit.service.*
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIGiftListCallback
import io.agora.auikit.service.imp.AUIIMManagerServiceImpl
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.ui.chatBottomBar.impl.AUIKeyboardStatusWatcher
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.micseats.MicSeatOption
import io.agora.auikit.ui.micseats.MicSeatType
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.ThreadManager

class AUIVoiceRoomView : FrameLayout,
    AUIVoiceRoomObserver,
    IAUIMicSeatService.AUIMicSeatRespObserver {
    private val mRoomViewBinding = VoiceRoomViewBinding.inflate(LayoutInflater.from(context))
    private val mBinders = mutableListOf<IAUIBindable>()
    private var mVoiceService: AUIVoiceRoomService? = null
    private var listener: AUISoftKeyboardHeightChangeListener? = null
    private var activity:FragmentActivity?= null
    private var moreDialog:VoiceRoomMoreDialog?=null
    private var micType: MicSeatType?=null
    private var mOnClickShutDown: (() -> Unit)? = null
    private var mOnRoomDestroyEvent: (() -> Unit)? = null
    private var imManagerService:IAUIIMManagerService?=null
    private var invitationBinder:AUIInvitationBinder?=null
    private var auiGiftBarrageBinder :AUIGiftBarrageBinder?=null
    private val mDestroyDialog by lazy {
        AUIAlertDialog(context).apply {
            setTitle("房间已销毁")
            setMessage("请返回房间列表 ")
            setPositiveButton("我知道了") {
                mVoiceService?.exit()
                mOnRoomDestroyEvent?.invoke()
                dismiss()
            }
            setCancelable(false)
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mRoomViewBinding.root)
    }

    fun bindService(service: AUIVoiceRoomService) {
        mVoiceService = service
        imManagerService = service.imManagerService as AUIIMManagerServiceImpl
        service.observableHelper.subscribeEvent(this)
        service.micSeatService.registerRespObserver(this)

        val roomInfo = service.roomInfo ?: throw RuntimeException("RoomInfo is null")
        setUpRoomStyle(roomInfo)
        viewBinderConnected(service, roomInfo)
        getSoftKeyboardHeight()
    }

    private fun viewBinderConnected(service:AUIVoiceRoomService, roomInfo: AUIRoomInfo){
        /** 获取礼物列表 初始化礼物Binder */
        service.giftService.getGiftsFromService(object : AUIGiftListCallback{
            override fun onResult(error: AUIException?, giftList: List<AUIGiftTabEntity>) {
                auiGiftBarrageBinder = AUIGiftBarrageBinder(
                    activity,
                    mRoomViewBinding.giftView,
                    giftList,
                    service.giftService,
                    service.chatManager
                )
                auiGiftBarrageBinder?.let {
                    it.bind()
                    mBinders.add(it)
                }
            }
        })

        service.chatManager.saveWelcomeMsg(context.getString(R.string.voice_room_welcome))

        val chatListBinder = AUIChatListBinder(
            roomInfo,
            mRoomViewBinding.chatListView,
            mRoomViewBinding.chatBottomBar,
            service.chatManager,
            service.imManagerService,
        )
        chatListBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val roomInfoBinder = AUIRoomInfoBinder(
            mRoomViewBinding.leftView,
            roomInfo,
            listener = object : AUIRoomInfoBinder.AUIRoomInfoEvent {
                override fun onBackClickListener(view: View) {
                    AUILogger.logger().d("AUIVoiceRoomView", "AUIRoomInfoBinder onBackClick ...")
                    mOnClickShutDown?.invoke()
                }
            })

        roomInfoBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val roomMemberBinder = AUIRoomMembersBinder(
            context,
            mRoomViewBinding.rightView,
            service,
            listener = object :AUIRoomMembersBinder.AUIRoomMemberEvent{
                override fun onCloseClickListener(view: View) {
                    AUILogger.logger().d("AUIVoiceRoomView", "AUIRoomMembersBinder onCloseClick ...")
                    mOnClickShutDown?.invoke()
                }
            }
        )

        roomMemberBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val micSeatsBinder = AUIMicSeatsBinder(
            context,
            when (micType) {
                MicSeatType.EightTag -> {
                    mRoomViewBinding.micSeatsView
                }

                MicSeatType.NineTag -> {
                    mRoomViewBinding.micSeatsHostView
                }

                else -> {
                    mRoomViewBinding.micSeatsCircleView
                }
            }, service
        )

        micSeatsBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val chatBottomBarBinder = AUIChatBottomBarBinder(
            service,
            mRoomViewBinding.chatBottomBar,
            mRoomViewBinding.chatListView,
            when (micType) {
                MicSeatType.EightTag -> {
                    mRoomViewBinding.micSeatsView
                }
                MicSeatType.NineTag -> {
                    mRoomViewBinding.micSeatsHostView
                }
                else -> {
                    mRoomViewBinding.micSeatsCircleView
                }
            },
            object : AUIChatBottomBarBinder.AUIChatBottomBarEventDelegate{
                override fun onClickGift(view: View?) {
                    auiGiftBarrageBinder?.showBottomGiftDialog()
                }

                override fun onClickLike(view: View?) {
                    mRoomViewBinding.likeView.addFavor()
                }

                override fun onClickMore(view: View?) {
                    showMoreDialog()
                }

                override fun onClickMic(view: View?) {

                }
            })

        chatBottomBarBinder.let {
            it.bind()
            mBinders.add(it)
            listener = it.getSoftKeyboardHeightChangeListener()
        }

        invitationBinder = AUIInvitationBinder(activity, service)
        invitationBinder?.let {
            it.bind()
            mBinders.add(it)
        }
    }


    fun setFragmentActivity(activity: FragmentActivity){
        this.activity = activity
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        AUILogger.logger().d("AUIVoiceRoomView", "onDetachedFromWindow")

        mVoiceService?.observableHelper?.unSubscribeEvent(this)
        mVoiceService?.micSeatService?.unRegisterRespObserver(this)

        mBinders.forEach {
            it.unBind()
        }
        if(isRoomOwner()){
            imManagerService?.userDestroyedChatroom()
        }else {
            imManagerService?.userQuitRoom()
        }
    }

    private fun isRoomOwner() = AUIRoomContext.shared().isRoomOwner(mVoiceService?.roomInfo?.roomId)

    private fun getSoftKeyboardHeight(){
        ThreadManager.getInstance().runOnMainThreadDelay({
            activity?.let {
                AUIKeyboardStatusWatcher(
                    it, activity!!
                ) { isKeyboardShowed: Boolean, keyboardHeight: Int? ->
                    Log.e("apex", "isKeyboardShowed: $isKeyboardShowed $keyboardHeight")
                    listener?.onSoftKeyboardHeightChanged(isKeyboardShowed,keyboardHeight)
                }
            }
        },200)
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showMoreDialog(){
        val bean = VoiceMoreItemBean()
        val list = mutableListOf<VoiceMoreItemBean>()
        if (isRoomOwner()){ // 房主显示申请列表
            bean.ItemIcon = context.getDrawable(R.drawable.voice_icon_more_hands)
            bean.ItemTitle = "Application list"
            list.add(bean)
        }
        moreDialog = VoiceRoomMoreDialog(context,list,object :
            VoiceRoomMoreDialog.GridViewItemClickListener{
                override fun onItemClickListener(position: Int) {
                    if (position == 0){
                        if (isRoomOwner()){
                            // 显示申请列表
                            invitationBinder?.showApplyDialog()
                        }
                    }
                }
            })
        if (isRoomOwner()){
            activity?.supportFragmentManager?.let { moreDialog?.show(it,"more_dialog") }
        }
    }

    override fun onShowInvited(index:Int) {
        // 显示邀请dialog
        invitationBinder?.showInvitationDialog(index)
    }

    override fun onRoomDestroy(roomId: String) {
        AUILogger.logger().d("AUIVoiceRoomView", "onRoomDestroy $roomId")
        if (roomId == mVoiceService?.roomInfo?.roomId){
            mDestroyDialog.show()
        }
    }


    override fun onRoomUserBeKicked(roomId: String, userId: String) {
        super.onRoomUserBeKicked(roomId, userId)
        AUILogger.logger().d("AUIVoiceRoomView", "onRoomUserBeKicked $roomId $userId")
        if (roomId == mVoiceService?.roomInfo?.roomId){
            AUIAlertDialog(context).apply {
                setTitle("您已被踢出房间")
                setPositiveButton("确认") {
                    dismiss()
                    mOnRoomDestroyEvent?.invoke()
                }
                show()
            }
        }
    }

    fun setOnShutDownClick(action: () -> Unit) {
        mOnClickShutDown = action
    }

    fun setOnRoomDestroyEvent(action: () -> Unit){
        mOnRoomDestroyEvent = action
    }

    private fun setUpRoomStyle(roomInfo:AUIRoomInfo){
        val seatStyle = roomInfo.micSeatStyle
        micType = MicSeatType.fromString(seatStyle)
        when( micType ){
            MicSeatType.OneTag ->{
                mRoomViewBinding.micSeatsView.visibility = View.GONE
                mRoomViewBinding.micSeatsHostView.visibility = View.GONE
                mRoomViewBinding.micSeatsCircleView.visibility = View.VISIBLE
                mRoomViewBinding.micSeatsCircleView.setOptions(MicSeatOption(
                    roomInfo.micSeatCount,MicSeatType.OneTag
                ))
            }
            MicSeatType.SixTag ->{
                mRoomViewBinding.micSeatsView.visibility = View.GONE
                mRoomViewBinding.micSeatsHostView.visibility = View.GONE
                mRoomViewBinding.micSeatsCircleView.visibility = View.VISIBLE
                mRoomViewBinding.micSeatsCircleView.setOptions(
                    MicSeatOption(
                    roomInfo.micSeatCount,MicSeatType.SixTag,if (
                    roomInfo.micSeatCount == 3 || roomInfo.micSeatCount == 5) 90 else 0
                )
                )
            }
            MicSeatType.EightTag ->{
                mRoomViewBinding.micSeatsView.visibility = View.VISIBLE
                mRoomViewBinding.micSeatsCircleView.visibility = View.GONE
                mRoomViewBinding.micSeatsHostView.visibility = View.GONE
            }
            MicSeatType.NineTag ->{
                mRoomViewBinding.micSeatsView.visibility = View.GONE
                mRoomViewBinding.micSeatsCircleView.visibility = View.GONE
                mRoomViewBinding.micSeatsHostView.visibility = View.VISIBLE
            }
            else -> {}
        }
    }
}