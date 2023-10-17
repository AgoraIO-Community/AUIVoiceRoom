//
//  AUIVoiceChatRoomView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import AUIKitCore
import SwiftTheme

@objc open class AUIVoiceChatRoomView: UIView {
    
    public var service: AUIVoiceChatRoomService?
    
    private var roomInfo: AUIRoomInfo = AUIRoomInfo()
    
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.frame).image(UIImage.aui_Image(named: "voicechat_bg"))
    }()
    
    /// 房间信息UI
    private lazy var roomInfoView: AUIRoomInfoView = AUIRoomInfoView(frame: CGRect(x: 16, y: AStatusBarHeight, width: 185, height: 40),showExtension: true)
    
    // 关闭按钮
    private lazy var closeButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.icon = ThemeAnyPicker(keyPath: "Room.offBtnIcon")
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
    
    private lazy var micSeatView: AUIMicSeatView = AUIMicSeatView(frame: CGRect(x: 16, y: AStatusBarHeight + 74, width: self.bounds.size.width - 16 * 2, height: 324),layout: self.layout(type: AUIMicSeatViewLayoutType(rawValue: self.roomInfo.micSeatStyle) ?? .eight))
    
    private lazy var micSeatBinder: AUIMicSeatViewBinder = AUIMicSeatViewBinder(rtcEngine: self.service!.rtcEngine,roomInfo: self.roomInfo)

    private lazy var chatView: AUIChatBottomBarView = {
        AUIChatBottomBarView(frame: CGRect(x: 0, y: self.micSeatView.frame.maxY+10, width: self.frame.width, height: self.frame.height-self.micSeatView.frame.maxY-CGFloat(ABottomBarHeight)),channelName: self.service?.channelName ?? "")
    }()

    private lazy var receiveGift: AUIGiftBarrageView = {
        AUIGiftBarrageView(frame: CGRect(x: 10, y: self.chatView.frame.minY - (AScreenWidth / 9.0 * 2.5), width: AScreenWidth / 3.0 * 2 + 20, height: AScreenWidth / 9.0 * 2.5),source: nil).backgroundColor(.clear).tag(1111)
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
    
    private lazy var moreActions: AUIMoreOperationView = AUIMoreOperationView(frame: CGRect(x: 0, y: 50, width: AScreenWidth, height: 360), datas: self.moreDatas)
    
    private lazy var membersList: AUIRoomMemberListView = {
        let listView = AUIRoomMemberListView()
        listView.aui_size =  CGSize(width: UIScreen.main.bounds.width, height: 562)
        return listView
    }()
    
    
    private var moreDatas: [AUIMoreOperationCellEntity] {
        AUIRoomContext.shared.isRoomOwner(channelName: self.service?.channelName ?? "") ? [AUIMoreOperationCellEntity()]:[]
    }
        
    public var onClickOffButton: (()->())?

    deinit {
        aui_info("deinit AUIVoiceChatRoomView", tag: "AUIVoiceChatRoomView")
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
    }
    
    @objc public convenience init(frame: CGRect, roomInfo: AUIRoomInfo) {
        self.init(frame: frame)
        self.roomInfo = roomInfo
        aui_info("init AUIVoiceChatRoomView", tag: "AUIVoiceChatRoomView")
        
        //设置皮肤路径
        if let folderPath = Bundle.main.path(forResource: "auiVoiceChatTheme", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        if let folderPath = Bundle.main.path(forResource: "Gift", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        if let folderPath = Bundle.main.path(forResource: "ChatResource", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        if let folderPath = Bundle.main.path(forResource: "Invitation", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func bindService(service: AUIVoiceChatRoomService) {
        self.service = service
        self.roomInfo = AUIRoomContext.shared.roomInfoMap[service.channelName] ?? AUIRoomInfo()
        self.moreActions.datas = self.moreDatas
        self.loadSubviews()
        self.viewBinderConnected()
        
        let channelName:String = service.channelName
        aui_info("enter room: \(channelName)", tag: "AUIVoiceChatRoomView")
        self.service?.roomManagerImpl.enterRoom(roomId: channelName) { error in
            aui_info("enter room success", tag: "AUIVoiceChatRoomView")
        }
        self.service?.joinRtcChannel { error in
            aui_info("joinRtcChannel finished: \(error?.localizedDescription ?? "success")", tag: "AUIVoiceChatRoomView")
        }
    }
    
    private func loadSubviews() {
        aui_info("load voicechat room subview", tag: "AUIVoiceChatRoomView")
        

        self.addSubViews([self.background,self.micSeatView,self.roomInfoView,self.membersView,self.chatView,self.receiveGift,self.closeButton])
        membersView.aui_centerY = self.roomInfoView.aui_centerY
        membersView.aui_right = AScreenWidth - 60
        
        closeButton.aui_centerY = self.roomInfoView.aui_centerY
        closeButton.aui_right = aui_width - 15
        
    }
    
    private func viewBinderConnected() {
        aui_info("viewBinderConnected", tag: "AUIVoiceChatRoomView")
        
        guard let service = service else {
            assert(false, "service is empty")
            aui_error("service is empty", tag: "AUIVoiceChatRoomView")
            return
        }
        service.beKickedClosure = { [weak self] in
            AUIToast.show(text: "您被踢出房间!")
            self?.onBackAction()
        }
        //绑定Service

        micSeatBinder.bindVoiceChat(micSeatView: micSeatView, eventsDelegate: self,
                           micSeatService: service.micSeatImpl,
                                    userService: service.userImpl) { [weak self] onMic,mute in
            self?.chatView.updateBottomBarState(onMic: onMic)
            self?.chatView.updateBottomBarSelected(index: 1, selected: mute)
        }
        micSeatView.uiDelegate = self
        service.reportAudioVolumeIndicationOfSpeakers = { [weak self] speckers, totalVolumes in
            self?.micSeatBinder.speakers = speckers
        }
        
        userBinder.bind(userView: membersView,
                        userService: service.userImpl,
                        micSeatService: service.micSeatImpl)
        
        chatBinder.bind(chat: self.chatView.messageView, chatService: service.chatImplement)
        chatView.addActionHandler(actionHandler: self)
        
        giftBinder.bind(send: self.giftsView, receive: self.receiveGift, giftService: service.giftImplement)
        if let roomInfo = AUIRoomContext.shared.roomInfoMap[service.channelName] {
            self.roomInfoView.updateRoomInfo(withRoomId: roomInfo.roomId, roomName: roomInfo.roomName, ownerHeadImg: roomInfo.owner?.userAvatar)
        }
        
        invitationBinder.bind(inviteView: self.invitationView, applyView: self.applyView, invitationDelegate: service.invitationImplement, roomDelegate: service.roomManagerImpl) { [weak self] in
            self?.requestUsers(users: $0)
        }
        invitationView.addActionHandler(actionHandler: self)
        applyView.addActionHandler(actionHandler: self)
        
        moreActions.addActionHandler(actionHandler: self)
        
        self.membersList.ownerPreview = AUIRoomContext.shared.isRoomOwner(channelName: service.channelName)
        membersView.onClickMoreButtonAction = { [weak self] in
            guard let `self` = self else { return }
            self.membersList.memberList = $0
            AUICommonDialog.show(contentView: self.membersList, theme: AUICommonDialogTheme())
        }
        self.membersList.addActionHandler(actionHandler: self)
    }
    
    
    private func layout(type: AUIMicSeatViewLayoutType) -> UICollectionViewLayout {
        var flow = UICollectionViewLayout()
        switch type {
        case .one,.six:
            let layout = AUIMicSeatCircleLayout()
            layout.dataSource = self
            let width: CGFloat = 80//min(bounds.size.width / 4.0, bounds.size.height / 2)
            let height: CGFloat = 92
            layout.itemSize = CGSize(width: width, height: height)
            layout.minimumLineSpacing = 0
            layout.minimumInteritemSpacing = 0
            flow = layout
        case .eight:
            let flowLayout = UICollectionViewFlowLayout()
            let width: CGFloat = 80//min(bounds.size.width / 4.0, bounds.size.height / 2)
            let height: CGFloat = 92
            let hPadding = Int((self.frame.size.width - 16 * 2 - width * 4) / 3)
            flowLayout.itemSize = CGSize(width: width, height: height)
            flowLayout.minimumLineSpacing = 0
            flowLayout.minimumInteritemSpacing = CGFloat(hPadding)
            flow = flowLayout
        case .nine:
            let layout = AUIMicSeatHostAudienceLayout()
            layout.dataSource = self
            flow = layout
        default:
            break
        }
        return flow
    }



}

extension AUIVoiceChatRoomView: AUIMicSeatCircleLayoutDataSource,AUIMicSeatHostAudienceLayoutDataSource {
    public var radius: CGFloat {
        return min(self.micSeatView.frame.width, self.micSeatView.frame.height)/3.2
    }
    
    public func rowSpace() -> CGFloat {
        10
    }
    
    public func hostSize() -> CGSize {
        CGSize(width: 102, height: 120)
    }
    
    public func otherSize() -> CGSize {
        CGSize(width: 80, height: 92)
    }
}

extension AUIVoiceChatRoomView: AUIRoomMemberListViewEventsDelegate {
    public func kickUser(user: AUIUserCellUserDataProtocol) {
        AUIChatInputBar.hiddenInput()
        AUIAlertView.theme_defaultAlert()
            .contentTextAligment(textAlignment: .center)
            .isShowCloseButton(isShow: true)
            .title(title: "踢出用户")
            .titleColor(color: .white)
            .rightButton(title: "确定").content(content: "确认踢出？踢出后该用户无法再次进入房间")
            .theme_rightButtonBackground(color: "CommonColor.primary")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.kickOut(user: user)
            }).show()
        
    }
    
    private func kickOut(user: AUIUserCellUserDataProtocol) {
        guard let channelName = self.service?.channelName else { return }
        aui_info("kick out channelName:\(channelName)  uid:\(user.userId) op:\(AUIRoomContext.shared.currentUserInfo.userId)")
        self.service?.userImpl.kickUser(roomId: channelName, userId: user.userId, callback: { [weak self] error in
            guard let `self` = self else { return }
            if error == nil {
                self.membersList.memberList = self.membersList.memberList.filter({
                    return $0.userId != user.userId
                })
                self.membersList.refreshView()
                self.membersView.removeMember(member: user)
            }
            AUIToast.show(text: error == nil ? "踢出成功" : "踢出失败")
        })
    }
}

extension AUIVoiceChatRoomView: AUIMicSeatViewDelegate {
    public func seatItems(view: AUIMicSeatView) -> [AUIMicSeatCellDataProtocol] {
        self.micSeatBinder.micSeatArray
    }
    
    public func onItemDidClick(view: AUIMicSeatView, seatIndex: Int) {
        self.micSeatBinder.binderClickItem(seatIndex: seatIndex)
    }
    
    public func onMuteVideo(view: AUIMicSeatView, seatIndex: Int, canvas: UIView, isMuteVideo: Bool) {
        self.micSeatBinder.binderMuteVideo(seatIndex: seatIndex, canvas: canvas, isMuteVideo: isMuteVideo)
    }
    
    
}

extension AUIVoiceChatRoomView: AUIMoreOperationViewEventsDelegate {
    public func onItemSelected(entity: AUIMoreOperationCellDataProtocol) {
        AUICommonDialog.hidden()
        self.applyView.refreshUsers(users: self.filterMicUsers())
        AUICommonDialog.show(contentView: self.applyView,theme: AUICommonDialogTheme())
    }
}

extension AUIVoiceChatRoomView: AUIUserOperationEventsDelegate {
    
    private func requestUsers(users: [String:AUIInvitationCallbackModel]) {
        guard let channelName = self.service?.channelName else { return }
        if !AUIRoomContext.shared.isRoomOwner(channelName: channelName) { return }
        if users.keys.count <= 0 { return }
        self.chatView.updateBottomBarRedDot(index: 0,show: true)
        let userIds = users.keys.map {
            $0
        }
        if userIds.isEmpty { return }
        self.service?.userImpl.getUserInfoList(roomId: channelName, userIdList: userIds, callback: { [weak self] error, userInfos in
            if error == nil,userInfos != nil {
                self?.membersView.members = userInfos!
                if let applyUsers = self?.filterMicUsers().map({
                    if $0.userId == users[$0.userId]?.userId ?? "" {
                        $0.seatIndex = users[$0.userId]?.payload?.seatNo ?? 0
                    }
                    return $0
                }) {
                    self?.applyView.refreshUsers(users:applyUsers)
                }
            } else {
                AUIToast.show(text: "Request application list failed!")
            }
        })
    }
    
    public func operationUser(user: AUIUserCellUserDataProtocol,source: AUIUserOperationEventsSource) {
        switch source {
        case .invite:
            self.service?.invitationImplement.sendInvitation(userId: user.userId, seatIndex: self.invitationView.index, callback: { error in
                if error == nil {
                    self.invitationView.userList.removeAll {
                        $0.userId == user.userId
                    }
                    self.invitationView.filter(userId: user.userId)
                }
                AUIToast.show(text: error == nil ? "邀请成功":"邀请失败")
            })
        case .apply:
            self.service?.invitationImplement.acceptApply(userId: user.userId, seatIndex: user.seatIndex, callback: { error in
                if error == nil {
                    self.applyView.filter(userId: user.userId)
                }
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

extension AUIVoiceChatRoomView: AUIChatBottomBarViewEventsDelegate {
    public func raiseKeyboard() {
        aui_info("chat keyboard raise")
    }
    
    public func sendTextMessage(text: String) {
        self.chatBinder.sendTextMessage(text: text)
    }
    
    public func bottomBarEvents(entity: AUIChatFunctionBottomEntity) {
        switch entity.type {
        case .more : self.showMoreTabs()
        case .mic: self.muteLocal()
        case .gift: self.showGiftTabs()
        default:
            break
        }
    }
    
    func showGiftTabs() {
        AUICommonDialog.hidden()
        let theme = AUICommonDialogTheme()
        AUICommonDialog.show(contentView: self.giftsView,theme: theme)
    }
    
    func showMoreTabs() {
        AUICommonDialog.hidden()
        let theme = AUICommonDialogTheme()
        AUICommonDialog.show(contentView: self.moreActions,theme: theme)
    }
    
    func muteLocal() {
        guard let entity = self.chatView.bottomBar.datas[safe: 1] else {
            return
        }
        let isOwner = AUIRoomContext.shared.isRoomOwner(channelName: self.service?.channelName ?? "")
        let seat = self.micSeatBinder.micSeatArray.first(where: {
            $0.user?.userId ?? "" == AUIRoomContext.shared.currentUserInfo.userId
        })
        if let seat = seat {
            if seat.muteAudio, !isOwner {
                entity.selected = true
                AUIToast.show(text: "当前麦位已被房主静麦")
                return
            }
            self.service?.userImpl.muteUserAudio(isMute: entity.selected, callback: { [weak self] error in
                if error == nil {
                    self?.service?.rtcEngine.muteLocalAudioStream(entity.selected)
                    self?.chatView.updateBottomBarSelected(index: 1, selected: entity.selected)
                }
            })
        } else {
            service?.rtcEngine.muteLocalAudioStream(entity.selected)
            chatView.updateBottomBarSelected(index: 1, selected: entity.selected)
        }
    }
    
}

extension AUIVoiceChatRoomView: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        if user.userId == AUIRoomContext.shared.currentUserInfo.userId {
            AUIRoomContext.shared.currentUserInfo.seatIndex = seatIndex
        }
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        if user.userId == service?.userImpl.getRoomContext().currentUserInfo.userId {
            AUIRoomContext.shared.currentUserInfo.seatIndex = -1
        }
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        if seatIndex == AUIRoomContext.shared.currentUserInfo.seatIndex {
            //refresh tool bar mic icon
        }
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
            AUIChatInputBar.hiddenInput()
            AUIAlertView.theme_defaultAlert()
                .contentTextAligment(textAlignment: .center)
                .isShowCloseButton(isShow: true)
                .title(title: "申请上麦")
                .titleColor(color: .white)
                .rightButton(title: "确定").content(content: "申请上\(index+1)号麦")
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
        AUICommonDialog.hidden()
        AUIToast.hidden()
        service.destory()
        AUIRoomContext.shared.clean(channelName: service.channelName)
//        self.didClickOffButton()
    }
    
    @objc func onSelectedMusic() {
        aui_info("onSelectedMusic", tag: "AUIVoiceChatRoomView")
    }
    
    open override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        for view in subviews.reversed() {
            if view.isKind(of: AUIGiftBarrageView.self),view.frame.contains(point),self.micSeatView.frame.contains(point){
                let childPoint = self.convert(point, to: self.micSeatView)
                let childView = self.micSeatView.hitTest(childPoint, with: event)
                return childView
            }
        }
        return super.hitTest(point, with: event)
    }
}
