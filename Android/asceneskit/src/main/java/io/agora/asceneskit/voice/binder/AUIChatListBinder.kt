package io.agora.asceneskit.voice.binder

import android.util.Log
import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AgoraChatMessage
import io.agora.auikit.service.IAUIChatService
import io.agora.auikit.service.im.AUIChatSubscribeDelegate
import io.agora.auikit.service.imp.AUIChatServiceImpl
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatList.IAUIChatListView
import io.agora.auikit.ui.chatList.listener.AUIChatListItemClickListener
import io.agora.auikit.utils.ThreadManager

class AUIChatListBinder constructor(
    chatList:IAUIChatListView,
    chatBottomBar:IAUIChatBottomBarView,
    chatService:IAUIChatService,
    roomInfo:AUIRoomInfo
): IAUIBindable, AUIChatListItemClickListener, AUIChatSubscribeDelegate {
    private val chatListImpl:IAUIChatListView
    private val chatBottomBarImpl:IAUIChatBottomBarView
    private val chatServiceImpl:AUIChatServiceImpl

    init {
        this.chatListImpl = chatList
        this.chatBottomBarImpl = chatBottomBar
        this.chatServiceImpl = chatService as AUIChatServiceImpl
        chatList.initView(roomInfo.roomOwner?.userId)
    }

    override fun bind() {
        chatListImpl.setChatListItemClickListener(this)
        chatServiceImpl.subscribeChatMsg(this)
    }

    override fun unBind() {
        chatListImpl.setChatListItemClickListener(null)
        chatServiceImpl.unsubscribeChatMsg(this)
    }

    override fun onChatListViewClickListener() {
        chatBottomBarImpl.hideKeyboard()
    }

    override fun onItemClickListener(message: AUIChatEntity?) {
        Log.d("AUIChatListBinder","onItemClickListener")
    }

    override fun onReceiveTextMsg(roomId: String?, message: AgoraChatMessage?) {
        ThreadManager.getInstance().runOnMainThread{
            chatListImpl.refreshSelectLast(chatServiceImpl.getMsgList())
        }
    }

    override fun onReceiveMemberJoinedMsg(roomId: String?, message: AgoraChatMessage?) {
        ThreadManager.getInstance().runOnMainThread{
            chatListImpl.refreshSelectLast(chatServiceImpl.getMsgList())
        }
    }
}