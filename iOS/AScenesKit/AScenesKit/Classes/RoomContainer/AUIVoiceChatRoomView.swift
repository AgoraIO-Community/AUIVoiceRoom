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
        AUIRoomVoiceChatView(frame: CGRect(x: 0, y: self.micSeatView.frame.maxY+10, width: self.frame.width, height: self.frame.height-self.micSeatView.frame.maxY-CGFloat(ABottomBarHeight)),channelName: self.service?.channelName ?? "")
    }()

    private lazy var receiveGift: AUIReceiveGiftsView = {
        AUIReceiveGiftsView(frame: CGRect(x: 10, y: self.chatView.frame.minY - (AScreenWidth / 9.0 * 2), width: AScreenWidth / 3.0 * 2 + 20, height: AScreenWidth / 9.0 * 2),source: nil).backgroundColor(.clear).tag(1111)
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
    
    private lazy var applyView: AUIApplyView = AUIApplyView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 380))
    
    private lazy var invitationBinder: AUIInvitationViewBinder = AUIInvitationViewBinder()
    
    private lazy var moreActions: AUIMoreOperationView = AUIMoreOperationView(frame: CGRect(x: 0, y: 50, width: AScreenWidth, height: 360), datas: AUIRoomContext.shared.isRoomOwner(channelName: self.service?.channelName ?? "") ? [AUIMoreOperationCellEntity()]:[])
    
    private lazy var membersList: AUIRoomMemberListView = {
        let listView = AUIRoomMemberListView()
        listView.aui_size =  CGSize(width: UIScreen.main.bounds.width, height: 562)
        return listView
    }()
        
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
        service.beKickedClosure = { [weak self] in
            AUIToast.show(text: "You were kicked!")
            self?.onBackAction()
        }
        //绑定Service
        micSeatBinder.bindVoiceChat(micSeatView: micSeatView, eventsDelegate: self,
                           micSeatService: service.micSeatImpl,
                           userService: service.userImpl)
        service.reportAudioVolumeIndicationOfSpeakers = { [weak self] speckers, totalVolumes in
            self?.micSeatBinder.speakers = speckers
        }
        
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
        
        invitationBinder.bind(inviteView: self.invitationView, applyView: self.applyView, invitationDelegate: service.invitationImplement, roomDelegate: service.roomManagerImpl) { [weak self] in
            self?.requestUsers(users: $0)
        }
        invitationView.addActionHandler(actionHandler: self)
        applyView.addActionHandler(actionHandler: self)
        
        moreActions.addActionHandler(actionHandler: self)
        
        membersView.onClickMoreButtonAction = { [weak self] in
            guard let `self` = self else { return }
            self.membersList.memberList = $0
            AUICommonDialog.show(contentView: self.membersList, theme: AUICommonDialogTheme())
        }
        self.membersList.addActionHandler(actionHandler: self)
    }

}

extension AUIVoiceChatRoomView: AUIRoomMemberListViewEventsDelegate {
    public func kickUser(user: AUIUserCellUserDataProtocol) {
        guard let channelName = self.service?.channelName else { return }
        self.service?.userImpl.kickUser(roomId: channelName, userId: user.userId, callback: { [weak self] error in
            guard let `self` = self else { return }
            if error == nil {
                self.membersList.memberList = self.membersList.memberList.filter({
                    $0.userId != user.userId
                })
                self.membersList.refreshView()
            }
            AUIToast.show(text: error == nil ? "踢出成功" : "踢出失败")
        })
    }
}

extension AUIVoiceChatRoomView: AUIMoreOperationViewEventsDelegate {
    public func onItemSelected(entity: AUIMoreOperationCellDataProtocol) {
        AUICommonDialog.hidden()
        
        AUICommonDialog.show(contentView: self.applyView,theme: AUICommonDialogTheme())
    }
}

extension AUIVoiceChatRoomView: AUIUserOperationEventsDelegate {
    
    private func requestUsers(users: [String:Int]) {
        guard let channelName = self.service?.channelName else { return }
        if !AUIRoomContext.shared.isRoomOwner(channelName: channelName) { return }
        self.chatView.updateBottomBarRedDot(index: 0,show: true)
        let userIds = users.keys.map {
            $0
        }
        if userIds.isEmpty { return }
        self.service?.userImpl.getUserInfoList(roomId: channelName, userIdList: userIds, callback: { [weak self] error, userInfos in
            if error == nil,userInfos != nil {
                self?.membersView.members = userInfos!
                self?.applyView.refreshUsers(users: self?.filterMicUsers() ?? [])
            } else {
                AUIToast.show(text: "Request application list failed!")
            }
        })
    }
    
    public func operationUser(user: AUIUserCellUserDataProtocol,source: AUIUserOperationEventsSource) {
        switch source {
        case .invite:
            self.service?.invitationImplement.sendInvitation(userId: user.userId, seatIndex: self.invitationView.index, callback: { error in
                AUIToast.show(text: error == nil ? "邀请成功":"邀请失败")
            })
        case .apply:
            self.service?.invitationImplement.acceptApply(userId: user.userId, seatIndex: user.seatIndex, callback: { error in
                AUIToast.show(text: error == nil ? "同意上麦申请成功":"同意上麦申请失败")
            })
        default:
            break
        }
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
        switch entity.index {
        case 0: self.showMoreTabs()
        case 1: self.muteLocal()
        case 2: self.showGiftTabs()
        default:
            break
        }
    }
    
    func showGiftTabs() {
        AUICommonDialog.hidden()
        let theme = AUICommonDialogTheme()
        theme.contentControlColor = .pickerWithUIColors([UIColor.white])
        AUICommonDialog.show(contentView: self.giftsView,theme: theme)
    }
    
    func showMoreTabs() {
        AUICommonDialog.hidden()
        let theme = AUICommonDialogTheme()
        theme.contentControlColor = .pickerWithUIColors([UIColor.white])
        AUICommonDialog.show(contentView: self.moreActions,theme: theme)
    }
    
    func muteLocal() {
        guard let entity = self.chatView.datas[safe: 1] else {
            return
        }
        entity.showRedDot = false
        entity.selected = !entity.selected
        self.service?.userImpl.muteUserAudio(isMute: entity.selected, callback: { [weak self] error in
            if error == nil {
                self?.chatView.updateBottomBarRedDot(index: 1, show: false)
            }
        })
        
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
            self.invitationView.index = index
            self.invitationView.refreshUsers(users: self.filterMicUsers())
            AUICommonDialog.show(contentView: self.invitationView, theme: AUICommonDialogTheme())
        } else {
            AUIAlertView.theme_defaultAlert()
                .contentTextAligment(textAlignment: .center)
                .isShowCloseButton(isShow: true)
                .title(title: "申请上麦")
                .titleColor(color: .white)
                .rightButton(title: "确定").content(content: "申请上\(index)号麦")
                .theme_rightButtonBackground(color: "CommonColor.primary")
                .rightButtonTapClosure(onTap: {[weak self] text in
                    guard let self = self else { return }
                    self.service?.invitationImplement.sendApply(seatIndex: index, callback: { error in
                        AUIToast.show(text: error == nil ? "申请成功":"申请失败")
                    })
                }).show()
        }
    }
    
    private func filterMicUsers() -> [AUIUserCellUserDataProtocol] {
        let members = self.membersView.members.filter({
            $0.userId != AUIRoomContext.shared.currentUserInfo.userId
        })
        var users = [AUIUserCellUserDataProtocol]()
        var onMicUserIds = [String]()
        for mic in self.micSeatBinder.micSeatArray {
            if let userId = mic.user?.userId {
                onMicUserIds.append(userId)
            }
        }
        for user in members {
            if !onMicUserIds.contains(user.userId) {
                users.append(user)
            }
        }
        return users
    }
    
    
    @objc private func didClickOffButton(){
        self.onClickOffButton?()
    }
    
    @objc public func onBackAction() {
        guard let service = service else {return}
        if AUIRoomContext.shared.isRoomOwner(channelName: service.channelName) {
            service.chatImplement.userDestroyedChatroom()
        } else {
            service.chatImplement.userQuitRoom(completion: nil)
        }
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
