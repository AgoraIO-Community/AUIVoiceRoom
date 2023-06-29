package io.agora.asceneskit.voice.binder

import android.util.Log
import androidx.fragment.app.FragmentActivity
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.service.IAUIChatService
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.imp.AUIChatServiceImpl
import io.agora.auikit.ui.gift.IAUIGiftBarrageView
import io.agora.auikit.ui.gift.impl.AUIGiftBarrageView
import io.agora.auikit.ui.gift.impl.dialog.AUiGiftListView
import io.agora.auikit.utils.ThreadManager

class AUIGiftBarrageBinder constructor(
    activity: FragmentActivity?,
    giftView: IAUIGiftBarrageView,
    data:List<AUIGiftTabEntity>,
    giftService: IAUIGiftsService,
    chatService:IAUIChatService?
):IAUIBindable,IAUIGiftsService.AUIGiftRespDelegate {

    private var auiGiftBarrageView: IAUIGiftBarrageView? =null
    private var mGiftList : List<AUIGiftTabEntity> = mutableListOf()
    private var activity: FragmentActivity?
    private var giftService:IAUIGiftsService
    private var roomContext:AUIRoomContext?
    private var chatService:IAUIChatService?
    private var chatImpl:AUIChatServiceImpl?


    init {
        this.auiGiftBarrageView = giftView
        this.chatService = chatService
        this.giftService = giftService
        this.mGiftList = data
        this.activity = activity
        this.roomContext = AUIRoomContext.shared()
        this.chatImpl = chatService as AUIChatServiceImpl
    }

    override fun bind() {
        giftService.bindRespDelegate(this)
    }

    override fun unBind() {
        giftService.bindRespDelegate(null)
    }

    fun showBottomGiftDialog(){
        activity?.let {
            val dialog = AUiGiftListView(it, mGiftList)
            dialog.setDialogActionListener(object : AUiGiftListView.ActionListener{
                override fun onGiftSend(bean: AUIGiftEntity?) {
                    bean?.let { it1 ->
                        it1.sendUser = roomContext?.currentUserInfo
                        it1.giftCount = 1
                        giftService.sendGift(it1) { error ->
                            if (error == null) {
                                ThreadManager.getInstance().runOnMainThread{
                                    Log.d("AUIGiftViewBinder", "sendGift suc ${giftService.channelName}")
                                    chatImpl?.addGiftList(it1)
                                    auiGiftBarrageView?.refresh(chatImpl?.getGiftList())
                                }
                            } else {
                                Log.e("AUIGiftViewBinder", "sendGift error ${error.code} ${error.message}")
                            }
                        }
                    }
                }
            })
            dialog.show(it.supportFragmentManager, "gift_dialog")
        }

    }

    override fun onReceiveGiftMsg(channel:String) {
        Log.d("AUIGiftViewBinder", "onReceiveGiftMsg ")
        ThreadManager.getInstance().runOnMainThread{
            auiGiftBarrageView?.refresh(chatImpl?.getGiftList())
        }
    }

}