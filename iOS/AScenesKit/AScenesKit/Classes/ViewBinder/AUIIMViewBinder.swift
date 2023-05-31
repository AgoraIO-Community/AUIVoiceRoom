//
//  AUIIMViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit

@objcMembers open class AUIIMViewBinder: NSObject {
    
    private weak var chatView: AUIRoomVoiceChatView?
    
    private weak var chatDelegate: AUIMManagerServiceDelegate? {
        didSet {
            chatDelegate?.unbindRespDelegate(delegate: self)
            chatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public func bind(chat: AUIRoomVoiceChatView, chatService: AUIMManagerServiceDelegate) {
        self.chatView = chat
        self.chatDelegate = chatService
        self.chatDelegate?.configIM(appKey: "", user: AUIRoomContext.shared.currentUserInfo, completion: { error in
            AUIToast.show(text: error != nil ? "IM initialize failed!":"IM initialize successful!")
            if error == nil {
                let channelName = chatService.getChannelName()
                if AUIRoomContext.shared.isRoomOwner(channelName: channelName) {
                    self.chatDelegate?.createChatRoom(roomId: channelName) { id, error in
                        AUIToast.show(text: error == nil ? "Create chatroom successful!":"Create chatroom failed!")
                    }
                }
            }
        })
    }

}

@objc extension AUIIMViewBinder {
    func sendTextMessage(text: String) {
        guard let channelName = self.chatDelegate?.getChannelName() else {
            AUIToast.show(text: "ChatroomId can't be empty,when you send message.")
            return
        }
        self.chatDelegate?.sendMessage(roomId: channelName, text: text, userInfo: AUIRoomContext.shared.currentUserInfo, completion: { message, error in
            if error == nil,message != nil {
                self.chatView?.showNewMessage(entity: self.convertTextMessageToRenderEntity(message: message!))
            } else {
                AUIToast.show(text: "Send message failed!")
            }
        })
    }
}

@objc extension AUIIMViewBinder: AUIMManagerRespDelegate {
    
    public func messageDidReceive(roomId: String, message: AgoraChatTextMessage) {
        self.chatView?.showNewMessage(entity: self.convertTextMessageToRenderEntity(message: message))
    }
    
    public func onUserDidJoinRoom(roomId: String, user: AUIUserThumbnailInfo) {
        self.chatView?.showNewMessage(entity: self.convertJoinMessageToRenderEntity(user: user))
    }
    
    private func convertTextMessageToRenderEntity(message: AgoraChatTextMessage) -> AUIChatEntity {
        let entity = AUIChatEntity()
        entity.userName = message.user?.userName
        entity.content = message.content
        entity.chatId = message.user?.userId
        entity.joined = false
        entity.attributeContent = entity.attributeContent
        entity.width = entity.width
        entity.height = entity.height
        return entity
    }
    
    private func convertJoinMessageToRenderEntity(user: AUIUserThumbnailInfo) -> AUIChatEntity {
        let entity = AUIChatEntity()
        entity.userName = user.userName
        entity.content = "Joined"
        entity.chatId = user.userId
        entity.joined = true
        entity.attributeContent = entity.attributeContent
        entity.width = entity.width
        entity.height = entity.height
        return entity
    }
}

