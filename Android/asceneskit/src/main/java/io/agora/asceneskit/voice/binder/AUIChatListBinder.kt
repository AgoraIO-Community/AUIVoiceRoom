package io.agora.asceneskit.voice.binder

import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIIMManagerService
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatList.AUIChatInfo
import io.agora.auikit.ui.chatList.IAUIChatListView
import io.agora.auikit.ui.chatList.impl.AUIChatListView
import io.agora.auikit.ui.chatList.listener.AUIChatListItemClickListener
import io.agora.auikit.utils.ThreadManager

class AUIChatListBinder constructor(
    roomInfo:AUIRoomInfo,
    private val chatList:IAUIChatListView,
    private val chatBottomBar:IAUIChatBottomBarView,
    private val chatManager: AUIChatManager,
    private val imManagerService:IAUIIMManagerService,
): IAUIBindable, AUIChatListItemClickListener, IAUIIMManagerService.AUIIMManagerRespDelegate {

    init {
        (chatList as? AUIChatListView)?.setOwnerId(roomInfo.roomOwner?.userId ?: "")
    }

    override fun bind() {
        chatList.setChatListItemClickListener(this)
        imManagerService.bindRespDelegate(this)
    }

    override fun unBind() {
        chatList.setChatListItemClickListener(null)
        imManagerService.unbindRespDelegate(this)
    }

    override fun onChatListViewClickListener() {
        chatBottomBar.hideKeyboard()
    }


    override fun messageDidReceive(
        roomId: String,
        message: IAUIIMManagerService.AgoraChatTextMessage
    ) {
        ThreadManager.getInstance().runOnMainThread{
            chatList.refreshSelectLast(chatManager.getMsgList().map {
                AUIChatInfo(
                    it.user?.userId ?: "", it.user?.userName ?: "",
                    it.content, it.joined
                )
            })
        }
    }

    override fun onUserDidJoinRoom(
        roomId: String,
        message: IAUIIMManagerService.AgoraChatTextMessage
    ) {
        ThreadManager.getInstance().runOnMainThread{
            chatList.refreshSelectLast(chatManager.getMsgList().map {
                AUIChatInfo(
                    it.user?.userId ?: "", it.user?.userName ?: "",
                    it.content, it.joined
                )
            })
        }
    }

}