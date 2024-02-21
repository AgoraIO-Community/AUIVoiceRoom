//
//  AUIMicSeatViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit

import AgoraRtcKit
import AUIKitCore

@objc public protocol AUIMicSeatViewEventsDelegate: NSObjectProtocol {
    func micSeatDidSelectedItem(index: Int)
}

public class AUIMicSeatViewBinder: NSObject {
    
    /// Description 第一个bool值true为上麦  false下麦，第二个bool值更新底部麦位状态 true为选中状态图片，false为未选中状态图片
    private var currentUserMicState: ((Bool,Bool) -> ())?
    
    var speakers: [AgoraRtcAudioVolumeInfo] = [] {
        didSet {
            for (idx,micSeat) in micSeatArray.enumerated() {
                if let userId = micSeat.user?.userId {
                    for speaker in speakers {
                        var index: Int?
                        if speaker.uid == 0 {
                            if AUIRoomContext.shared.currentUserInfo.userId == userId {
                                index = idx
                            }
                        } else {
                            if "\(speaker.uid)" == userId {
                                index = idx
                            }
                        }
                        if index != nil {
                            DispatchQueue.main.async {
                                self.micSeatView?.updateMicVolume(index: index!, volume: Int(speaker.volume))
                            }
                        }
                    }
                }
            }
        }
    }
    public private(set) var micSeatArray: [AUIMicSeatInfo] = []
    public private(set) var userMap: [String: AUIUserInfo] = [:]
    private var rtcEngine: AgoraRtcEngineKit!
    private weak var micSeatView: IAUIMicSeatView?
    
    private weak var eventsDelegate: AUIMicSeatViewEventsDelegate?
    private weak var micSeatDelegate: AUIMicSeatServiceDelegate? {
        didSet {
            micSeatDelegate?.unbindRespDelegate(delegate: self)
            micSeatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var userDelegate: AUIUserServiceDelegate? {
        didSet {
            userDelegate?.unbindRespDelegate(delegate: self)
            userDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    private weak var invitationDelegate: AUIInvitationServiceDelegate?
    
    public convenience init(rtcEngine: AgoraRtcEngineKit,roomInfo: AUIRoomInfo) {
        self.init()
        self.rtcEngine = rtcEngine
        for i in 0...(roomInfo.micSeatCount - 1) {
            let seatInfo = AUIMicSeatInfo()
            seatInfo.seatIndex = UInt(i)
            micSeatArray.append(seatInfo)
        }
    }
    
    public func bind(micSeatView: IAUIMicSeatView,
                     micSeatService: AUIMicSeatServiceDelegate,
                     userService: AUIUserServiceDelegate,
                     invitationDelegate: AUIInvitationServiceDelegate) {
        self.micSeatView = micSeatView
        self.micSeatDelegate = micSeatService
        self.userDelegate = userService
        self.invitationDelegate = invitationDelegate
    }
    
    public func bindVoiceChat(micSeatView: IAUIMicSeatView,
                              eventsDelegate: AUIMicSeatViewEventsDelegate,
                              micSeatService: AUIMicSeatServiceDelegate,
                              userService: AUIUserServiceDelegate,
                              invitationService: AUIInvitationServiceDelegate,
                              currenUserMicStateClosure: @escaping (Bool,Bool) -> ()) {
        self.micSeatView = micSeatView
        self.eventsDelegate = eventsDelegate
        self.micSeatDelegate = micSeatService
        self.userDelegate = userService
        self.invitationDelegate = invitationService
        self.currentUserMicState = currenUserMicStateClosure
    }
    
    private func enterDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("enterSeat")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.enterSeat(seatIndex: Int(seatInfo.seatIndex), callback: { err in
                guard let err = err else {return}
                AUIToast.show(text: err.localizedDescription)
            })
            callback()
        }
        return item
    }
    
    private func kickDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("kickSeat")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.kickSeat(seatIndex: Int(seatInfo.seatIndex),
                                            callback: { error in
                guard let err = error else {return}
                AUIToast.show(text: err.localizedDescription)
            })
            callback()
        }
        return item
    }
    
    private func leaveDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("leaveSeat")
        item.icon = "ActionSheetCell.normalIcon"
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.leaveSeat(callback: { error in
                guard let err = error else {return}
                AUIToast.show(text: err.localizedDescription)
            })
            callback()
        }
        return item
    }
    
    private func muteAudioDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) ->AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = seatInfo.muteAudio ? aui_localized("unmuteAudio") : aui_localized("muteAudio")
//        item.icon = "ActionSheetCell.warnIcon"
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.muteAudioSeat(seatIndex: Int(seatInfo.seatIndex),
                                                 isMute: !seatInfo.muteAudio,
                                                 callback: { error in
                guard let err = error else {return}
                AUIToast.show(text: err.localizedDescription)
            })
            callback()
        }
        
        return item
    }
    
    private func lockDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = seatInfo.lockSeat == .locked ? aui_localized("unlockSeat") : aui_localized("lockSeat")
        item.titleColor = "CommonColor.danger"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.closeSeat(seatIndex: Int(seatInfo.seatIndex),
                                             isClose: seatInfo.lockSeat != .locked,
                                             callback: { error in
                guard let err = error else {return}
                AUIToast.show(text: err.localizedDescription)
            })
            callback()
        }
        
        return item
    }
    
    private func inviteDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("Invite to mic")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = {
            self.eventsDelegate?.micSeatDidSelectedItem(index: Int(seatInfo.seatIndex))
            callback()
        }
        return item
    }
    
    private func applyDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("Apply to mic")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = {
            self.eventsDelegate?.micSeatDidSelectedItem(index: Int(seatInfo.seatIndex))
            callback()
        }
        return item
    }
    
    private func lookupUserInfoDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = "UserInfo"
        item.titleColor = "CommonColor.danger"
        item.callback = {
            callback()
        }
        return item
    }
    
    public func getDialogItems(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) ->[AUIActionSheetItem] {
        var items = [AUIActionSheetItem]()
        
        let channelName: String = micSeatDelegate?.getChannelName() ?? ""
        let currentUserId: String = micSeatDelegate?.getRoomContext().currentUserInfo.userId ?? ""
        //当前麦位用户是否自己
        let isCurrentUser: Bool = seatInfo.user?.userId == currentUserId
        //是否空麦位
        let isEmptySeat: Bool = seatInfo.user == nil || seatInfo.user?.userId.count == 0
        //是否房主
        let isRoomOwner: Bool = micSeatDelegate?.getRoomContext().isRoomOwner(channelName: channelName) ?? false
        //当前用户是否在麦位上
        let currentUserAlreadyEnterSeat: Bool = micSeatArray.filter {$0.user?.userId == currentUserId}.count > 0 ? true : false
        if isRoomOwner {
            if isEmptySeat {
                items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                if !seatInfo.isLock {
                    items.append(inviteDialogItem(seatInfo: seatInfo, callback: callback))
                }
                items.append(lockDialogItem(seatInfo: seatInfo, callback:callback))
            } else {
                if isCurrentUser {
                } else {  //other user
                    items.append(kickDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(lockDialogItem(seatInfo: seatInfo, callback:callback))
                }
            }
        } else {
            if isEmptySeat {
                if currentUserAlreadyEnterSeat {
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                } else {
                    if !seatInfo.isLock {
                        items.append(applyDialogItem(seatInfo: seatInfo, callback: callback))
                    }
                }
            } else {
                if isCurrentUser {
                    items.append(leaveDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                } else {  //other user
                    items.append(lookupUserInfoDialogItem(seatInfo: seatInfo, callback: callback))
                }
            }
        }
        
        return items
    }
    
    private func getLocalUserId() -> String? {
        return AUIRoomContext.shared.currentUserInfo.userId
    }
}

extension AUIMicSeatViewBinder: AUIMicSeatRespDelegate {
    public func onSeatWillLeave(userId: String, metaData: NSMutableDictionary) -> NSError? {
//        if let err = micSeatDelegate?.cleanUserInfo?(userId: userId, metaData: metaData) {
//            return err
//        }
        invitationDelegate?.cleanUserInfo?(userId: userId, completion: { err in
        })
        
        return nil
    }
    
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        aui_info("onAnchorEnterSeat seat: \(seatIndex)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        if let fullUser = userMap[user.userId] {
            micSeat.user = fullUser
        } else {
            micSeat.user = user
        }
        micSeatArray[seatIndex] = micSeat
        micSeatView?.refresh(index: seatIndex)
  
        //current user enter seat
        if user.userId == getLocalUserId() {
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.clientRoleType = .broadcaster
            mediaOption.publishMicrophoneTrack = true
            rtcEngine.updateChannel(with: mediaOption)
            aui_info("update clientRoleType: \(mediaOption.clientRoleType.rawValue)", tag: "AUIMicSeatViewBinder")
            self.currentUserMicState?(true,micSeat.isMuteAudio)
            return
        }
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        aui_info("onAnchorLeaveSeat seat: \(seatIndex)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.user = nil
        micSeatArray[seatIndex] = micSeat
        micSeatView?.refresh(index: seatIndex)
 
        //current user enter seat
        guard user.userId == getLocalUserId() else {
            return
        }
        
        self.currentUserMicState?(false,false)
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.clientRoleType = .audience
        rtcEngine.updateChannel(with: mediaOption)
        
        aui_info("update clientRoleType: \(mediaOption.clientRoleType.rawValue)", tag: "AUIMicSeatViewBinder")
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        aui_info("onSeatAudioMute seat: \(seatIndex) isMute: \(isMute)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.muteAudio = isMute
        micSeatArray[seatIndex] = micSeat
        if let fullUser = micSeat.user as? AUIUserInfo {
            fullUser.muteAudio = isMute
            micSeatView?.refresh(index: seatIndex)
        }
        micSeatView?.refresh(index: seatIndex)
        
        if micSeat.user?.userId == micSeatDelegate?.getRoomContext().currentUserInfo.userId {
            self.currentUserMicState?(true,isMute)
            userDelegate?.muteUserAudio(isMute: isMute, callback: { err in
            })
        }
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        aui_info("onSeatVideoMute  seat: \(seatIndex) isMute: \(isMute)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.muteVideo = isMute
        micSeatArray[seatIndex] = micSeat
        if let fullUser = micSeat.user as? AUIUserInfo {
            fullUser.muteVideo = isMute
            micSeatView?.refresh(index: seatIndex)
        }
        
        if micSeat.user?.userId == micSeatDelegate?.getRoomContext().currentUserInfo.userId {
            self.currentUserMicState?(true,isMute)
        }
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        aui_info("onSeatClose seat:\(seatIndex) isClose: \(isClose)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.lockSeat = isClose ? AUILockSeatStatus.locked : AUILockSeatStatus.idle
        micSeatArray[seatIndex] = micSeat
        micSeatView?.refresh(index: seatIndex)
    }
}

//MARK: AUIMicSeatViewDelegate
extension AUIMicSeatViewBinder {
    public func binderClickItem(seatIndex: Int) {
        let micSeat = micSeatArray[seatIndex]

        let dialogItems = getDialogItems(seatInfo: micSeat) {
            AUICommonDialog.hidden()
        }
        guard dialogItems.count > 0 else {return}
        var headerInfo: AUIActionSheetHeaderInfo? = nil
        if let user = micSeat.user, user.userId.count > 0 {
            headerInfo = AUIActionSheetHeaderInfo()
            headerInfo?.avatar = user.userAvatar
            headerInfo?.title = user.userName
            headerInfo?.subTitle = micSeat.seatIndexDesc()
        }
        let dialogView = AUIActionSheet(title: aui_localized("managerSeat"),
                                        items: dialogItems,
                                        headerInfo: headerInfo)
        dialogView.setTheme(theme: AUIActionSheetTheme())
        AUICommonDialog.show(contentView: dialogView, theme: AUICommonDialogTheme())
    }
    
    public func binderMuteVideo(seatIndex: Int, canvas: UIView, isMuteVideo: Bool) {
        aui_info("onMuteVideo  seatIdx: \(seatIndex) mute: \(isMuteVideo)", tag: "AUIMicSeatViewBinder")
        let videoCanvas = AgoraRtcVideoCanvas()
        let micSeat = micSeatArray[seatIndex]
        if let userId = micSeat.user?.userId, let uid = UInt(userId), !isMuteVideo {
            videoCanvas.uid = uid
            videoCanvas.view = canvas
            videoCanvas.renderMode = .hidden
            if userId == getLocalUserId() {
                rtcEngine.setupLocalVideo(videoCanvas)
            } else {
                rtcEngine.setupRemoteVideo(videoCanvas)
            }
            aui_info("onMuteVideo user[\(userId)] seatIdx: \(seatIndex) mute: \(isMuteVideo)", tag: "AUIMicSeatViewBinder")
        } else {
            self.rtcEngine.setupRemoteVideo(videoCanvas)
        }
    }
    
    public func enterMic(seatIndex: Int) {
        let micSeat = micSeatArray[seatIndex]

        let dialogItems = getDialogItems(seatInfo: micSeat) {
            AUICommonDialog.hidden()
        }
        guard dialogItems.count > 0 else {return}
        var headerInfo: AUIActionSheetHeaderInfo? = nil
        if let user = micSeat.user, user.userId.count > 0 {
            headerInfo = AUIActionSheetHeaderInfo()
            headerInfo?.avatar = user.userAvatar
            headerInfo?.title = user.userName
            headerInfo?.subTitle = micSeat.seatIndexDesc()
        }
        let dialogView = AUIActionSheet(title: aui_localized("managerSeat"),
                                        items: dialogItems,
                                        headerInfo: headerInfo)
        dialogView.setTheme(theme: AUIActionSheetTheme())
        AUICommonDialog.show(contentView: dialogView, theme: AUICommonDialogTheme())
    }
}

//MARK: AUIUserRespDelegate
extension AUIMicSeatViewBinder: AUIUserRespDelegate {
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "AUIMicSeatViewBinder")
        userMap.removeAll()
        userList.forEach { user in
            self.userMap[user.userId] = user
        }
        
        for micSeat in micSeatArray {
            if let userId = micSeat.user?.userId, userId.count > 0, let user = userMap[userId] {
                micSeat.user = user
            }
        }
        micSeatView?.refresh(index: -1)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserEnter: \(userInfo.userId)", tag: "AUIMicSeatViewBinder")
        userMap[userInfo.userId] = userInfo
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserLeave: \(userInfo.userId)", tag: "AUIMicSeatViewBinder")
        userMap[userInfo.userId] = nil
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserUpdate: \(userInfo.userId)", tag: "AUIMicSeatViewBinder")
        userMap[userInfo.userId] = userInfo
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        aui_info("onUserAudioMute userId: \(userId) mute: \(mute)", tag: "AUIMicSeatViewBinder")
        userMap[userId]?.muteAudio = mute
        let micSeat = micSeatArray.first { $0.user?.userId ?? "" == userId
        }
        if userId == getLocalUserId() {
            if let micSeatMute = micSeat?.muteAudio,micSeatMute {
                self.rtcEngine.muteLocalAudioStream(true)
            } else {
                self.rtcEngine.muteLocalAudioStream(mute)
            }
        }
        
        for (seatIndex, micSeat) in micSeatArray.enumerated() {
            if let user = userMap[userId], user.userId == micSeat.user?.userId {
                micSeat.user = user
                micSeatView?.refresh(index: seatIndex)
                break
            }
        }
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        aui_info("onUserVideoMute userId: \(userId) mute: \(mute)", tag: "AUIMicSeatViewBinder")
        userMap[userId]?.muteVideo = mute
        
        for (seatIndex, micSeat) in micSeatArray.enumerated() {
            if let user = userMap[userId], user.userId == micSeat.user?.userId {
                micSeat.user = userMap[userId]
                micSeatView?.refresh(index: seatIndex)
                break
            }
        }
    }
}
