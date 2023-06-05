//
//  AUIInvitationViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/6/5.
//

import UIKit
import AUIKit

open class AUIInvitationViewBinder: NSObject {
    
    private weak var inviteView: AUIInvitationView?
    
    public weak var invitationDelegate: AUIInvitationServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            invitationDelegate?.unbindRespDelegate(delegate: self)
        }
    }
    
    
    public weak var roomDelegate: AUIRoomManagerDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            roomDelegate?.bindRespDelegate(delegate: self)
        }
    }

    public func bind(inviteView: AUIInvitationView, invitationDelegate: AUIInvitationServiceDelegate, roomDelegate: AUIRoomManagerDelegate) {
        self.inviteView = inviteView
        self.invitationDelegate = invitationDelegate
        self.roomDelegate = roomDelegate
    }
}

extension AUIInvitationViewBinder: AUIInvitationRespDelegate {
    
    public func onReceiveNewInvitation(userId: String, seatIndex: Int?) {
        AUIAlertView()
            .theme_background(color: "CommonColor.black")
            .isShowCloseButton(isShow: true)
            .title(title: "邀请上麦").content(content: seatIndex != nil ? "房主邀请您上\(seatIndex!)号麦": "房主邀请您上麦")
            .titleColor(color: .white)
            .rightButton(title: "确定")
            .theme_rightButtonBackground(color: "CommonColor.primary")
            .rightButtonTapClosure(onTap: {[weak self] text in
                self?.invitationDelegate?.acceptInvitation(userId: AUIRoomContext.shared.currentUserInfo.userId, seatIndex: seatIndex, callback: { error in
                    AUIToast.show(text: error == nil ? "同意邀请成功":"同意邀请失败")
                })
            }).show()
    
    }
    
    public func onInviteeAccepted(userId: String) {
        AUIToast.show(text: "用户\(userId)已同意邀请！")
    }
    
    public func onInviteeRejected(userId: String) {
        AUIToast.show(text: "用户\(userId)已拒绝邀请！")
    }
    
    public func onInvitationCancelled(userId: String) {
        AUIToast.show(text: "用户\(userId)的邀请已取消！")
    }
    
    public func onReceiveNewApply(userId: String, seatIndex: Int?) {
        AUIAlertView()
            .theme_background(color: "CommonColor.black")
            .isShowCloseButton(isShow: true)
            .title(title: "申请上麦").content(content: seatIndex != nil ? "用户\(userId)申请上\(seatIndex!)号麦": "用户\(userId)申请上麦")
            .titleColor(color: .white)
            .rightButton(title: "同意")
            .theme_rightButtonBackground(color: "CommonColor.primary")
            .rightButtonTapClosure(onTap: {[weak self] text in
                self?.invitationDelegate?.acceptApply(userId: AUIRoomContext.shared.currentUserInfo.userId, seatIndex: seatIndex, callback: { error in
                    AUIToast.show(text: error == nil ? "同意申请成功":"同意申请失败")
                })
            }).show()
    }
    
    public func onApplyAccepted(userId: String) {
        AUIToast.show(text: "房主已接受您的申请！")
    }
    
    public func onApplyRejected(userId: String) {
        AUIToast.show(text: "房主已拒绝您的申请！")
    }
    
    public func onApplyCanceled(userId: String) {
        AUIToast.show(text: "房主已取消您的申请！")
    }
    
    
}

extension AUIInvitationViewBinder: AUIRoomManagerRespDelegate {
    public func onRoomAnnouncementChange(roomId: String, announcement: String) {
        //TODO: - update room announcement
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.inviteView?.userList = userList
        self.inviteView?.tableView.reloadData()
    }
    
    public func onRoomDestroy(roomId: String) {
        
    }
    
    public func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
        
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.userList = self.inviteView?.userList.filter({$0.userId != userInfo.userId}) ?? []
        self.inviteView?.userList.append(userInfo)
        self.inviteView?.tableView.reloadData()
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.userList = self.inviteView?.userList.filter({$0.userId != userInfo.userId}) ?? []
        self.inviteView?.tableView.reloadData()
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.userList = self.inviteView?.userList.filter({$0.userId != userInfo.userId}) ?? []
        self.inviteView?.userList.append(userInfo)
        self.inviteView?.tableView.reloadData()
    }
    
}
