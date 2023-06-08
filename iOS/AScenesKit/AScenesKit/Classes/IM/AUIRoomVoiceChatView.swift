//
//  AUIRoomVoiceChatView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit

@objc public protocol AUIRoomVoiceChatViewEventsDelegate: NSObjectProtocol {
    
    /// Description 唤起输入键盘
    func raiseKeyboard()
    
    /// Description 发送文本消息
    /// - Parameter text: 文本
    func sendTextMessage(text: String)
    
    /// Description 处理其它底部工具栏事件或者打点统计
    /// - Parameter entity: 底部工具栏实体模型
    func bottomBarEvents(entity: AUIChatFunctionBottomEntity)
}

@objc open class AUIRoomVoiceChatView: UIView {
        
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    public func addActionHandler(actionHandler: AUIRoomVoiceChatViewEventsDelegate) {
        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }

    public func removeEventHandler(actionHandler: AUIRoomVoiceChatViewEventsDelegate) {
        self.eventHandlers.remove(actionHandler)
    }
    
    
    var datas: [AUIChatFunctionBottomEntity] {
        var entities = [AUIChatFunctionBottomEntity]()
        let names = ["ellipsis_vertical","mic_slash","gift_color","thumb_up_color"]
        for i in 0...3 {
            let entity = AUIChatFunctionBottomEntity()
            entity.selected = false
            entity.selectedImage = nil
            entity.normalImage = UIImage.aui_Image(named: names[i])
            entity.index = i
            entities.append(entity)
        }
        return entities
    }
    
    lazy var messageView: AUIRoomChatView = {
        AUIRoomChatView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: self.frame.height-50-CGFloat(ABottomBarHeight)))
    }()
    
    lazy var bottomBar: AUIRoomBottomFunctionBar = {
        AUIRoomBottomFunctionBar(frame: CGRect(x: 0, y: self.frame.height-CGFloat(ABottomBarHeight)-27, width: AScreenWidth, height: 54), datas: self.datas, hiddenChat: false)
    }()
    
    lazy var inputBar: AUIChatInputBar = {
        AUIChatInputBar(frame: CGRect(x: 0, y: AScreenHeight, width: AScreenWidth, height: 60),config: AUIChatInputBarConfig()).backgroundColor(.white)
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(self.messageView)
        self.addSubview(self.bottomBar)
        getWindow()?.addSubview(self.inputBar)
        self.inputBar.isHidden = true
        self.showNewMessage(entity: self.startMessage(nil))
        self.bottomBarEvents()
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bottomBarEvents() {
        self.bottomBar.actionClosure = { [weak self] entity in
            self?.eventHandlers.allObjects.forEach({ handler in
                handler.bottomBarEvents(entity: entity)
            })
            guard let `self` = self,let idx = entity.index else { return }
            switch idx {
            case 3:
                self.messageView.showLikeAnimation()
            default:
                break
            }
        }
        self.bottomBar.raiseKeyboard = { [weak self] in
            self?.eventHandlers.allObjects.forEach({ handler in
                handler.raiseKeyboard()
            })
            self?.inputBar.isHidden = false
            self?.inputBar.inputField.becomeFirstResponder()
        }
        self.inputBar.sendClosure = { [weak self] text in
            self?.eventHandlers.allObjects.forEach({ handler in
                handler.sendTextMessage(text: text)
            })
            self?.messageView.chatView.reloadData()
            self?.inputBar.inputField.text = ""
        }
    }
    
    func showNewMessage(entity: AUIChatEntity) {
        self.messageView.messages?.append(entity)
        self.messageView.chatView.reloadData()
        self.messageView.scrollTableViewToBottom()
    }
    
    func startMessage(_ text: String?) -> AUIChatEntity {
        let entity = AUIChatEntity()
        entity.userName = "owner"
        entity.content = text == nil ? "Welcome to the voice chat room! Pornography, gambling or violence is strictly prohibited in the room.":text
        entity.attributeContent = entity.attributeContent
        entity.chatId = "123"
        entity.width = entity.width
        entity.height = entity.height
        entity.joined = false
        return entity
    }
    
    public func updateBottomBarRedDot(index: Int,show: Bool) {
        self.datas[safe: index]?.selected = show
        self.bottomBar.toolBar.reloadData()
    }
}
