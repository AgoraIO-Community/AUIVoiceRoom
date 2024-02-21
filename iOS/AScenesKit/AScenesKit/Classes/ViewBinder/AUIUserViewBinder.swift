//
//  AUIUserViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import AUIKitCore

open class AUIUserViewBinder: NSObject {
    private weak var userView: IAUIRoomMembersView?
    private weak var userDelegate: AUIUserServiceDelegate? {
        didSet {
            userDelegate?.unbindRespDelegate(delegate: self)
            userDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var micSeatDelegate: AUIMicSeatServiceDelegate? {
        didSet {
            micSeatDelegate?.unbindRespDelegate(delegate: self)
            micSeatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var invitationDelegate: AUIInvitationServiceDelegate?
    private var seatIndexMap: [String: Int] = [:]
    
    public func bind(userView: IAUIRoomMembersView, 
                     userService: AUIUserServiceDelegate,
                     micSeatService: AUIMicSeatServiceDelegate,
                     invitationDelegate: AUIInvitationServiceDelegate) {
        self.userView = userView
        self.userDelegate = userService
        self.micSeatDelegate = micSeatService
        self.invitationDelegate = invitationDelegate
    }
}

extension AUIUserViewBinder: AUIUserRespDelegate {
    private func cleanUserIdfNeed(roomId: String, userId: String) {
        guard AUIRoomContext.shared.getArbiter(channelName: roomId)?.isArbiter() ?? false else { return }
        _ = micSeatDelegate?.cleanUserInfo?(userId: userId, completion: { err in
        })
        _ = invitationDelegate?.cleanUserInfo?(userId: userId, completion: { err in
        })
    }
    
    private func convertUserData(roomId: String, userInfo: AUIUserInfo) -> AUIUserCellUserDataProtocol {
        let isOwner = AUIRoomContext.shared.isRoomOwner(channelName: roomId, userId: userInfo.userId)
        return userInfo.createData(seatIndexMap[userInfo.userId] ?? -1, isOwner)
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "AUIUserViewBinder")
        let members = userList.map({self.convertUserData(roomId: roomId, userInfo: $0)})
        userView?.updateMembers(members: members, channelName: roomId)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserEnter \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.appendMember(member: convertUserData(roomId: roomId, userInfo: userInfo))
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserLeave \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.removeMember(userId: userInfo.userId)
        cleanUserIdfNeed(roomId: roomId, userId: userInfo.userId)
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserUpdate \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.updateMember(member: convertUserData(roomId: roomId, userInfo: userInfo))
    }
}

extension AUIUserViewBinder: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        seatIndexMap[user.userId] = seatIndex
        userView?.updateSeatInfo(userId: user.userId, seatIndex: seatIndex)
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        seatIndexMap[user.userId] = -1
        userView?.updateSeatInfo(userId: user.userId, seatIndex: -1)
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onUserMicrophoneMute(userId: String, mute: Bool) {
        
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        
    }
}


