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
    
    public func bind(userView: IAUIRoomMembersView, userService: AUIUserServiceDelegate, micSeatService: AUIMicSeatServiceDelegate) {
        self.userView = userView
        self.userDelegate = userService
        self.micSeatDelegate = micSeatService
    }
}

extension AUIUserViewBinder: AUIUserRespDelegate {
    public func onUserBeKicked(roomId: String, userId: String) {
        let user = AUIUserThumbnailInfo()
        user.userId = userId
        self.userView?.removeMember(member: user)
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "AUIUserViewBinder")
        userView?.updateMembers(members: userList, channelName: self.micSeatDelegate?.getChannelName() ?? "")
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserEnter \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.appendMember(member: userInfo)
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserLeave \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.removeMember(member: userInfo)
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserUpdate \(userInfo.userId) \(userInfo.userName)", tag: "AUIUserViewBinder")
        userView?.updateMember(member: userInfo)
    }
}

extension AUIUserViewBinder: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        userView?.updateSeatInfo(member: user, seatIndex: seatIndex)
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        userView?.updateSeatInfo(member: user, seatIndex: seatIndex)
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


