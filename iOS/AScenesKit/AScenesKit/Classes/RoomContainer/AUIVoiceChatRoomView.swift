//
//  AUIVoiceChatRoomView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKit

@objc open class AUIVoiceChatRoomView: UIView {
    
    private var service: AUIVoiceChatRoomService?
    
    @AUserDefault("MicSeatType",defaultValue: 2) var seatType
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.frame).image(UIImage.aui_Image(named: "voicechat_bg@3x"))
    }()
    
    /// 房间信息UI
    private lazy var roomInfoView: AUIRoomInfoView = AUIRoomInfoView(frame: CGRect(x: 16, y: AStatusBarHeight, width: 185, height: 40))
    
    // 关闭按钮
    private lazy var closeButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.icon = auiThemeImage("Room.offBtnIcon")
        theme.iconWidth = "Room.offBtnIconWidth"
        theme.iconHeight = "Room.offBtnIconHeight"
        theme.buttonWidth = "Room.offBtnWidth"
        theme.buttonHeight = "Room.offBtnHeight"
        theme.backgroundColor = "Room.offBtnBgColor"
        theme.cornerRadius = "Room.offBtnCornerRadius"
        
        let button = AUIButton()
        button.style = theme
        button.addTarget(self, action: #selector(didClickOffButton), for: .touchUpInside)
        
        return button
    }()

    private lazy var micSeatView: AUIMicSeatView = AUIMicSeatView(frame: CGRect(x: 16, y: ANavigationHeight+60, width: self.bounds.size.width - 16 * 2, height: 324),style: AUIMicSeatViewLayoutType(rawValue: self.seatType) ?? .eight)
    
    private lazy var micSeatBinder: AUIMicSeatViewBinder = AUIMicSeatViewBinder(rtcEngine: self.service!.rtcEngine)

    private lazy var chatView: AUIRoomVoiceChatView = {
        AUIRoomVoiceChatView(frame: CGRect(x: 0, y: self.micSeatView.frame.maxY+10, width: self.frame.width, height: self.frame.height-self.micSeatView.frame.maxY-CGFloat(ABottomBarHeight)))
    }()

    private lazy var receiveGift: AUIReceiveGiftsView = {
        AUIReceiveGiftsView(frame: CGRect(x: 10, y: self.chatView.frame.minY - (AScreenWidth / 9.0 * 2), width: AScreenWidth / 3.0 * 2 + 20, height: AScreenWidth / 9.0 * 1.8)).backgroundColor(.clear).tag(1111)
    }()

    private lazy var giftsView: AUIRoomGiftDialog = {
        AUIRoomGiftDialog(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 390), tabs: [AUIGiftTabEntity]())
    }()

    private lazy var giftBinder: AUIRoomGiftBinder = AUIRoomGiftBinder()

    private lazy var chatBinder: AUIIMViewBinder = AUIIMViewBinder()
    
    /// 用户列表UI
    private lazy var membersView: AUIRoomMembersView = AUIRoomMembersView()
    
    private lazy var userBinder: AUIUserViewBinder = AUIUserViewBinder()
    
    private lazy var invitationView: AUIInvitationView = AUIInvitationView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 380))
    
    private lazy var invitationBinder: AUIInvitationViewBinder = AUIInvitationViewBinder()
        
    public var onClickOffButton: (()->())?

    deinit {
        giftsView.removeFromSuperview()
        aui_info("deinit AUiKaraokeRoomView", tag: "AUiKaraokeRoomView")
    }
    
    public override init(frame: CGRect) {
        aui_info("init AUiKaraokeRoomView", tag: "AUiKaraokeRoomView")
        super.init(frame: frame)
        
        //设置皮肤路径
        if let folderPath = Bundle.main.path(forResource: "auiVoiceChatTheme", ofType: "bundle") {
            AUIRoomContext.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bindService(service: AUIVoiceChatRoomService) {
        self.service = service
        self.loadSubviews()
        self.viewBinderConnected()
        
        let channelName:String = service.channelName
        aui_info("enter room: \(channelName)", tag: "AUiKaraokeRoomView")
        self.service?.roomManagerImpl.enterRoom(roomId: channelName) { error in
            aui_info("enter room success", tag: "AUiKaraokeRoomView")
        }
        self.service?.joinRtcChannel { error in
            aui_info("joinRtcChannel finished: \(error?.localizedDescription ?? "success")", tag: "AUiKaraokeRoomView")
        }
    }
    
    private func loadSubviews() {
        aui_info("load karaoke room subview", tag: "AUiKaraokeRoomView")
        

        self.addSubViews([self.background,self.micSeatView,self.roomInfoView,self.membersView,self.chatView,self.receiveGift,self.closeButton])
        membersView.aui_centerY = self.roomInfoView.aui_centerY
        membersView.aui_right = AScreenWidth - 60
        
        closeButton.aui_centerY = self.roomInfoView.aui_centerY
        closeButton.aui_right = aui_width - 15
        
    }
    
    private func viewBinderConnected() {
        aui_info("viewBinderConnected", tag: "AUiKaraokeRoomView")
        
        guard let service = service else {
            assert(false, "service is empty")
            aui_error("service is empty", tag: "AUiKaraokeRoomView")
            return
        }
        
        //绑定Service
        micSeatBinder.bindVoiceChat(micSeatView: micSeatView, eventsDelegate: self,
                           micSeatService: service.micSeatImpl,
                           userService: service.userImpl)
        
        
        userBinder.bind(userView: membersView,
                        userService: service.userImpl,
                        micSeatService: service.micSeatImpl)
        
        chatBinder.bind(chat: self.chatView, chatService: service.chatImplement)
        chatView.addActionHandler(actionHandler: self)
        
        giftBinder.bind(send: self.giftsView, receive: self.receiveGift, giftService: service.giftImplement)
        giftsView.addActionHandler(actionHandler: self)
        if let roomInfo = AUIRoomContext.shared.roomInfoMap[service.channelName] {
            self.roomInfoView.updateRoomInfo(withRoomId: roomInfo.roomId, roomName: roomInfo.roomName, ownerHeadImg: roomInfo.owner?.userAvatar)
        }
        
        invitationBinder.bind(inviteView: self.invitationView, invitationDelegate: service.invitationImplement, roomDelegate: service.roomManagerImpl)
        invitationView.addActionHandler(actionHandler: self)
    }

}

extension AUIVoiceChatRoomView: AUIInvitationViewEventsDelegate {
    public func inviteUser(user: AUIUserCellUserDataProtocol) {
        self.service?.invitationImplement.sendInvitation(userId: user.userId, seatIndex: nil, callback: { [weak self ] error in
            AUIToast.show(text: error == nil ? "邀请成功":"邀请失败")
        })
    }
}

extension AUIVoiceChatRoomView: AUIRoomGiftDialogEventsDelegate {
    
    public func sendGiftAction(gift: AUIGiftEntity) {
        self.giftBinder.sendGift(gift: gift) { error in
            AUIToast.show(text: error == nil ? "Sent successful!":"Sent failed!")
            let sent = gift
            sent.sendUser = AUIRoomContext.shared.currentUserInfo
            self.receiveGift.gifts.append(sent)
        }
    }
    
    
}

extension AUIVoiceChatRoomView: AUIRoomVoiceChatViewEventsDelegate {
    public func raiseKeyboard() {
        aui_info("chat keyboard raise")
    }
    
    public func sendTextMessage(text: String) {
        self.chatBinder.sendTextMessage(text: text)
    }
    
    public func bottomBarEvents(entity: AUIChatFunctionBottomEntity) {
        guard let index = entity.index else { return }
        switch index {
        case 2: self.showGiftTabs()
        default:
            break
        }
    }
    
    func showGiftTabs() {
        let theme = AUICommonDialogTheme()
        theme.contentControlColor = .pickerWithUIColors([UIColor.white])
        AUICommonDialog.show(contentView: self.giftsView,theme: theme)
    }
    
}

extension AUIVoiceChatRoomView: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        if user.userId == service?.userImpl.getRoomContext().currentUserInfo.userId {
//            microphoneButton.isHidden = false
        }
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        if user.userId == service?.userImpl.getRoomContext().currentUserInfo.userId {
//            microphoneButton.isHidden = true
        }
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        
    }
    
    
}


extension AUIVoiceChatRoomView: AUIMicSeatViewEventsDelegate {
    
    public func micSeatDidSelectedItem(index: Int) {
        guard let channelName = self.service?.channelName else { return }
        if AUIRoomContext.shared.isRoomOwner(channelName: channelName) {
//            let listView = AUIRoomMemberListView()
//            listView.aui_size =  CGSize(width: UIScreen.main.bounds.width, height: 562)
//            listView.memberList = members
//            listView.seatMap = seatMap
            
            AUICommonDialog.show(contentView: self.invitationView, theme: AUICommonDialogTheme())
//            self.memberListView = listView
        } else {
            AUIAlertView()
                .theme_background(color: "CommonColor.black")
                .isShowCloseButton(isShow: true)
                .title(title: "申请上麦")
                .titleColor(color: .white)
                .rightButton(title: "确定")
                .theme_rightButtonBackground(color: "CommonColor.primary")
                .rightButtonTapClosure(onTap: {[weak self] text in
                    self?.service?.invitationImplement.sendApply(seatIndex: index, callback: { error in
                        if error == nil {
                            self?.micSeatBinder.enterMic(seatIndex: index)
                        } else {
                            AUIToast.show(text: "Apply failed!")
                        }
                    })
                }).show()
            
        }
    }
    
    @objc private func didClickOffButton(){
        self.onClickOffButton?()
    }
    
    @objc public func onBackAction() {
        guard let service = service else {return}
        if AUIRoomContext.shared.isRoomOwner(channelName: service.channelName) {
            service.roomManagerImpl.destroyRoom(roomId: service.channelName) { err in
            }
        } else {
            service.roomManagerImpl.exitRoom(roomId: service.channelName) { err in
            }
        }
        service.destory()
        AUIRoomContext.shared.clean(channelName: service.channelName)
    }
    
    @objc func onSelectedMusic() {
        aui_info("onSelectedMusic", tag: "AUiKaraokeRoomView")
    }
}
