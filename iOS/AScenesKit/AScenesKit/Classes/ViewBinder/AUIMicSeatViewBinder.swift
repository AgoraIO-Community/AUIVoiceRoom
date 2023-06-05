//
//  AUIMicSeatViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit

import AgoraRtcKit
import AUIKit

@objc public protocol AUIMicSeatViewEventsDelegate: NSObjectProtocol {
    func micSeatDidSelectedItem(index: Int)
}

public class AUIMicSeatViewBinder: NSObject {
    
    @AUserDefault("MicSeatCount",defaultValue: 8) var seatCount
    
    private var micSeatArray: [AUIMicSeatInfo] = []
    private var userMap: [String: AUIUserInfo] = [:]
    private var rtcEngine: AgoraRtcEngineKit!
    private weak var micSeatView: AUIMicSeatView?
    
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
    private weak var musicDelegate: AUIMusicServiceDelegate? {
        didSet {
            musicDelegate?.unbindRespDelegate(delegate: self)
            musicDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var chorusDelegate: AUIChorusServiceDelegate? {
        didSet {
            chorusDelegate?.unbindRespDelegate(delegate: self)
            chorusDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public convenience init(rtcEngine: AgoraRtcEngineKit) {
        self.init()
        self.rtcEngine = rtcEngine
        for i in 0...(seatCount - 1) {
            let seatInfo = AUIMicSeatInfo()
            seatInfo.seatIndex = UInt(i)
            micSeatArray.append(seatInfo)
        }
    }
    
    public func bind(micSeatView: AUIMicSeatView,
                     micSeatService: AUIMicSeatServiceDelegate,
                     userService: AUIUserServiceDelegate,
                     musicSeatService: AUIMusicServiceDelegate,
                     chorusService: AUIChorusServiceDelegate) {
        self.micSeatView = micSeatView
        micSeatView.uiDelegate = self
        self.micSeatDelegate = micSeatService
        self.userDelegate = userService
        self.musicDelegate = musicSeatService
        self.chorusDelegate = chorusService
    }
    
    public func bindVoiceChat(micSeatView: AUIMicSeatView,eventsDelegate: AUIMicSeatViewEventsDelegate,
                     micSeatService: AUIMicSeatServiceDelegate,
                     userService: AUIUserServiceDelegate) {
        self.micSeatView = micSeatView
        self.eventsDelegate = eventsDelegate
        micSeatView.uiDelegate = self
        self.micSeatDelegate = micSeatService
        self.userDelegate = userService
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
    
    private func closeDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = seatInfo.lockSeat == .locked ? aui_localized("closeSeat") : aui_localized("openSeat")
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
                items.append(lockDialogItem(seatInfo: seatInfo, callback:callback))
            } else {
                if isCurrentUser {
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                } else {  //other user
                    items.append(kickDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(closeDialogItem(seatInfo: seatInfo, callback:callback))
                }
            }
        } else {
            if isEmptySeat {
                if currentUserAlreadyEnterSeat {
                } else {
                    items.append(enterDialogItem(seatInfo: seatInfo, callback:callback))
                }
            } else {
                if isCurrentUser {
                    items.append(leaveDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                } else {  //other user
                }
            }
        }
        
        return items
    }
}

extension AUIMicSeatViewBinder: AUIMicSeatRespDelegate {
    
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        aui_info("onAnchorEnterSeat seat: \(seatIndex)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        if let fullUser = userMap[user.userId] {
            micSeat.user = fullUser
        } else {
            micSeat.user = user
        }
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
  
        updateMic(with: seatIndex, role: .onlineAudience)

        //current user enter seat
        if user.userId == micSeatDelegate?.getRoomContext().commonConfig?.userId {
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.clientRoleType = .broadcaster
            mediaOption.publishMicrophoneTrack = true
            rtcEngine.updateChannel(with: mediaOption)
            aui_info("update clientRoleType: \(mediaOption.clientRoleType.rawValue)", tag: "AUIMicSeatViewBinder")
            return
        }
        
        guard let uid = UInt(micSeat.user?.userId ?? "") else {
            return
        }
        aui_info("mute audio uid: \(uid) isMute: \(micSeat.muteAudio)", tag: "AUIMicSeatViewBinder")
        self.rtcEngine.muteRemoteAudioStream(uid, mute: micSeat.muteAudio)
        aui_info("mute video uid: \(uid) isMute: \(micSeat.muteVideo)", tag: "AUIMicSeatViewBinder")
        self.rtcEngine.muteRemoteVideoStream(uid, mute: micSeat.muteVideo)
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        aui_info("onAnchorLeaveSeat seat: \(seatIndex)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.user = nil
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])

        updateMic(with: seatIndex, role: .offlineAudience)
 
        //current user enter seat
        guard user.userId == micSeatDelegate?.getRoomContext().commonConfig?.userId else {
            return
        }
        
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
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
        
        //TODO: 麦位静音表示不听远端用户的声音，目前是mute remote audio
        guard let uid = UInt(micSeat.user?.userId ?? ""),
              micSeat.user?.userId != micSeatDelegate?.getRoomContext().currentUserInfo.userId else {
            return
        }
        aui_info("mute audio uid: \(uid) isMute: \(isMute)", tag: "AUIMicSeatViewBinder")
        self.rtcEngine.muteRemoteAudioStream(uid, mute: isMute)
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        aui_info("onSeatVideoMute  seat: \(seatIndex) isMute: \(isMute)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.muteVideo = isMute
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
        
        guard let uid = UInt(micSeat.user?.userId ?? ""),
              micSeat.user?.userId != micSeatDelegate?.getRoomContext().currentUserInfo.userId else {
            return
        }
        aui_info("mute video uid: \(uid) isMute: \(isMute)", tag: "AUIMicSeatViewBinder")
        self.rtcEngine.muteRemoteVideoStream(UInt(micSeat.user?.userId ?? "") ?? 0, mute: isMute)
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        aui_info("onSeatClose seat:\(seatIndex) isClose: \(isClose)", tag: "AUIMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.lockSeat = isClose ? AUILockSeatStatus.locked : AUILockSeatStatus.idle
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
    }
}

//MARK: AUIMicSeatViewDelegate
extension AUIMicSeatViewBinder: AUIMicSeatViewDelegate {
    public func seatItems(view: AUIMicSeatView) -> [AUIMicSeatCellDataProtocol] {
        return micSeatArray
    }
    
    public func onItemDidClick(view: AUIMicSeatView, seatIndex: Int) {
        self.eventsDelegate?.micSeatDidSelectedItem(index: seatIndex)
    }
    
    public func onMuteVideo(view: AUIMicSeatView, seatIndex: Int, canvas: UIView, isMuteVideo: Bool) {
        aui_info("onMuteVideo  seatIdx: \(seatIndex) mute: \(isMuteVideo)", tag: "AUIMicSeatViewBinder")
        let videoCanvas = AgoraRtcVideoCanvas()
        let micSeat = micSeatArray[seatIndex]
        if let userId = micSeat.user?.userId, let uid = UInt(userId), !isMuteVideo {
            videoCanvas.uid = uid
            videoCanvas.view = canvas
            videoCanvas.renderMode = .hidden
            if userId == self.micSeatDelegate?.getRoomContext().commonConfig?.userId {
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
    public func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
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
        micSeatView?.collectionView.reloadData()
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
        
        for (seatIndex, micSeat) in micSeatArray.enumerated() {
            if let user = userMap[userId], user.userId == micSeat.user?.userId {
                micSeat.user = user
                micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
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
                micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
                break
            }
        }
    }
}

//MARK: AUIMusicRespDelegate
extension AUIMicSeatViewBinder: AUIMusicRespDelegate {
    public func onAddChooseSong(song: AUIChooseMusicModel) {
        
    }
    
    public func onRemoveChooseSong(song: AUIChooseMusicModel) {
        
    }
    
    public func onUpdateChooseSong(song: AUIChooseMusicModel) {
        
    }
    
    public func onUpdateAllChooseSongs(songs: [AUIChooseMusicModel]) {
        guard let topSong = songs.first else {
            //没有歌曲的话 在麦的用户都要变成onlineAudience
            let _ = micSeatArray.map {[weak self] in
                if ($0.user != nil) {
                    self?.updateMic(with: Int($0.seatIndex), role: .onlineAudience)
                }
            }
            return
        }
        guard let index = getMicIndex(with: topSong.userId ?? "") else {return}
        updateMic(with: index, role: .mainSinger)
    }
    
    
}

//MARK: AUIChorusRespDelegate
extension AUIMicSeatViewBinder: AUIChorusRespDelegate {
    public func onChoristerDidEnter(chorister: AUIChoristerModel) {
        //获取需要更新的麦位UI
        guard let index =  getMicIndex(with: chorister.userId) else {return}
        updateMic(with: index, role: .coSinger)
    }
    
    public func onChoristerDidLeave(chorister: AUIChoristerModel) {
        //获取需要更新的麦位UI
        guard let index =  getMicIndex(with: chorister.userId) else {return}
        updateMic(with: index, role: .onlineAudience)
    }
    
    private func getMicIndex(with userId: String) -> Int? {
        return micSeatArray
            .filter { $0.user?.userId == userId }
            .map { Int($0.seatIndex) }
            .first
    }
    
    private func getLocalUserId() -> String? {
        guard let commonConfig = AUIRoomContext.shared.commonConfig else {return nil}
        return commonConfig.userId
    }
    
    private func updateMic(with index: Int, role: MicRole) {
        let micSeat = micSeatArray[index]
        micSeat.micRole = role
        micSeatArray[index] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: index, section: 0)])
    }

}
