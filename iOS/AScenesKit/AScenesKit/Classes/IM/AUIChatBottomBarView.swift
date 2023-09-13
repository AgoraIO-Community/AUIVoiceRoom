//
//  AUIRoomVoiceChatView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKitCore

@objc public protocol AUIChatBottomBarViewEventsDelegate: NSObjectProtocol {
    
    /// Description 唤起输入键盘
    func raiseKeyboard()
    
    /// Description 发送文本消息
    /// - Parameter text: 文本
    func sendTextMessage(text: String)
    
    /// Description 处理其它底部工具栏事件或者打点统计
    /// - Parameter entity: 底部工具栏实体模型
    func bottomBarEvents(entity: AUIChatFunctionBottomEntity)
}

/// Description 弹幕区域点赞动画区域底部功能区域的集合体
@objc open class AUIChatBottomBarView: UIView {
        
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    private var channelName = ""
    
    public func addActionHandler(actionHandler: AUIChatBottomBarViewEventsDelegate) {
        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }

    public func removeEventHandler(actionHandler: AUIChatBottomBarViewEventsDelegate) {
        self.eventHandlers.remove(actionHandler)
    }
    
    lazy var messageView: AUIChatListView = {
        AUIChatListView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: self.frame.height-65))
    }()
    
    lazy var emitter: AUIPraiseEffectView = {
        AUIPraiseEffectView(frame: CGRect(x: AScreenWidth - 80, y: 0, width: 80, height: self.frame.height - 70),images: []).backgroundColor(.clear)
    }()
    
    lazy var bottomBar: AUIRoomBottomFunctionBar = {
        AUIRoomBottomFunctionBar(frame: CGRect(x: 0, y: self.frame.height-70, width: AScreenWidth, height: 54), datas: self.updateBottomBarDatas(onMic: AUIRoomContext.shared.isRoomOwner(channelName: self.channelName)), hiddenChat: false)
    }()
    
    lazy var inputBar: AUIChatInputBar = {
        AUIChatInputBar(frame: CGRect(x: 0, y: AScreenHeight, width: AScreenWidth, height: 60),config: AUIChatInputBarConfig()).theme_backgroundColor(color: "InputBar.backgroundColor")
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    deinit {
        inputBar.removeFromSuperview()
        aui_info("AUIRoomVoiceChatView deinit")
    }
    
    @objc public convenience init(frame: CGRect,channelName: String) {
        self.init(frame: frame)
        self.channelName = channelName
        self.addSubViews([self.messageView,self.bottomBar,self.emitter])
        getWindow()?.addSubview(self.inputBar)
        self.inputBar.isHidden = true
        self.messageView.showNewMessage(entity: self.startMessage(nil))
        self.bottomBarEvents()
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func bottomBarEvents() {
        self.bottomBar.actionClosure = { [weak self] entity in
            self?.eventHandlers.allObjects.forEach({ handler in
                handler.bottomBarEvents(entity: entity)
            })
            guard let `self` = self else { return }
            switch entity.type {
            case .like:
                self.emitter.setupEmitter()
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
    
    @objc public func dismissKeyboard() {
        self.inputBar.hiddenInputBar()
        self.window?.endEditing(true)
    }
    
    
    @objc func startMessage(_ text: String?) -> AUIChatEntity {
        let entity = AUIChatEntity()
        let user = AUIRoomContext.shared.roomInfoMap[self.channelName]?.owner ?? AUIUserThumbnailInfo()
        entity.user = user
        entity.content = text == nil ? aui_localized("startMessage", bundleName: "auiVoiceChatLocalizable"):text
        entity.attributeContent = entity.attributeContent
        entity.width = entity.width
        entity.height = entity.height
        entity.joined = false
        return entity
    }
    
    @objc public func updateBottomBarRedDot(index: Int,show: Bool) {
        self.bottomBar.datas[safe: index]?.showRedDot = show
        self.bottomBar.toolBar.reloadItems(at: [IndexPath(item: index, section: 0)])
    }
    
    @objc public func updateBottomBarState(onMic: Bool) {
        self.bottomBar.refreshToolBar(datas: self.updateBottomBarDatas(onMic: onMic))
    }
    
    /// Description 更新底部选中状态图片
    /// - Parameters:
    ///   - index: index从左到右 0~
    ///   - selected: 'true' or 'false'
    @objc public func updateBottomBarSelected(index: Int,selected: Bool) {
        self.bottomBar.datas[safe: index]?.selected = selected
        self.bottomBar.toolBar.reloadItems(at: [IndexPath(item: index, section: 0)])
    }
    
    @objc func updateBottomBarDatas(onMic: Bool) -> [AUIChatFunctionBottomEntity] {
        var entities = [AUIChatFunctionBottomEntity]()
        var names = ["ellipsis_vertical","mic","gift_color","thumb_up_color"]
        var selectedNames = ["ellipsis_vertical","unmic","gift_color","thumb_up_color"]
        if onMic == false {
            names = ["ellipsis_vertical","gift_color","thumb_up_color"]
            selectedNames = ["ellipsis_vertical","gift_color","thumb_up_color"]
        }
        
        for i in 0...names.count-1 {
            let entity = AUIChatFunctionBottomEntity()
            entity.selected = false
            entity.selectedImage = UIImage.aui_Image(named: selectedNames[i])
            entity.normalImage = UIImage.aui_Image(named: names[i])
            switch names[i] {
            case "ellipsis_vertical":
                entity.type = .more
            case "mic","unmic":
                entity.type = .mic
            case "gift_color":
                entity.type = .gift
            case "thumb_up_color":
                entity.type = .like
            default:
                entity.type = .unknown
                break
            }
            entities.append(entity)
        }
        return entities
    }
}
