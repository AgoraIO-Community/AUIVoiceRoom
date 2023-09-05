//
//  AUIInvitationViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/6/5.
//

import UIKit
import AUIKitCore

open class AUIInvitationViewBinder: NSObject {
    
    private var newApplyClosure: (([String:AUIInvitationCallbackModel]) -> ())?
    
    private weak var inviteView: IAUIListViewBinderRefresh?
    
    private weak var applyView: IAUIListViewBinderRefresh?
    
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

    public func bind(inviteView: IAUIListViewBinderRefresh,applyView: IAUIListViewBinderRefresh, invitationDelegate: AUIInvitationServiceDelegate, roomDelegate: AUIRoomManagerDelegate,receiveApply: @escaping ([String:AUIInvitationCallbackModel]) -> Void) {
        self.newApplyClosure = receiveApply
        self.inviteView = inviteView
        self.applyView = applyView
        self.invitationDelegate = invitationDelegate
        self.roomDelegate = roomDelegate
        self.invitationDelegate?.bindRespDelegate(delegate: self)
    }
}

extension AUIInvitationViewBinder: AUIInvitationRespDelegate {
    
    public func onApplyAcceptedButFailed(userId: String) {
        AUIToast.show(text: "\(userId) apply to mic failed!")
    }
    
    public func onInviteeAcceptedButFailed(userId: String) {
        AUIToast.show(text: "\(userId) agree invitation to mic failed!")
    }
    
    public func onReceiveApplyUsersUpdate(users: [String:AUIInvitationCallbackModel]) {
        //TODO: - 全量更新申请列表
        self.newApplyClosure?(users)
    }
    
    public func onInviteeListUpdate(inviteeList: [String:AUIInvitationCallbackModel]) {
        self.inviteView?.refreshUsers(users: [])
    }
    
    public func onReceiveNewInvitation(userId: String, seatIndex: Int) {
        AUIAlertView
            .theme_defaultAlert()
            .contentTextAligment(textAlignment: .center)
            .isShowCloseButton(isShow: true)
            .title(title: "邀请上麦").content(content: seatIndex != -1 ? "房主邀请您上\(seatIndex+1)号麦": "房主邀请您上麦")
            .titleColor(color: .white)
            .rightButton(title: "确定").leftButton(title: "拒绝")
            .theme_leftButtonBackground(color: "Alert.leftBackgroundColor")
            .theme_rightButtonBackground(color: "CommonColor.primary")
            .rightButtonTapClosure(onTap: {[weak self] text in
                self?.invitationDelegate?.acceptInvitation(userId: AUIRoomContext.shared.currentUserInfo.userId, seatIndex: seatIndex, callback: { error in
                    AUIToast.show(text: error == nil ? "同意邀请成功":"同意邀请失败")
                })
            }).leftButtonTapClosure(onTap: { [weak self] in
                self?.invitationDelegate?.rejectInvitation(userId: AUIRoomContext.shared.currentUserInfo.userId, callback: { error in
                    AUIToast.show(text: error == nil ? "拒绝邀请成功":"拒绝邀请失败")
                })
            }).show()
    
    }
    
    public func onInviteeAccepted(userId: String) {
        AUIToast.show(text: "用户\(userId)已同意邀请！")
        self.applyView?.filter(userId: userId)
        self.inviteView?.filter(userId: userId)
    }
    
    public func onInviteeRejected(userId: String) {
        AUIToast.show(text: "用户\(userId)已拒绝邀请！")
    }
    
    public func onInvitationCancelled(userId: String) {
        AUIToast.show(text: "用户\(userId)的邀请已取消！")
    }
    
    public func onReceiveNewApply(userId: String, seatIndex: Int) {
        //TODO: - 替换为更新更多功能小红点 提示用户有新申请
//        AUIAlertView()
//            .theme_background(color: "CommonColor.black")
//            .isShowCloseButton(isShow: true)
//            .title(title: "申请上麦").content(content: seatIndex != -1 ? "用户\(userId)申请上\(seatIndex)号麦": "用户\(userId)申请上麦")
//            .titleColor(color: .white)
//            .rightButton(title: "同意")
//            .theme_rightButtonBackground(color: "CommonColor.primary")
//            .rightButtonTapClosure(onTap: {[weak self] text in
//                self?.invitationDelegate?.acceptApply(userId: AUIRoomContext.shared.currentUserInfo.userId, seatIndex: seatIndex, callback: { error in
//                    AUIToast.show(text: error == nil ? "同意申请成功":"同意申请失败")
//                })
//            }).show()
    }
    
    public func onApplyAccepted(userId: String) {
        AUIToast.show(text: "房主已接受您的申请！")
        self.inviteView?.filter(userId: AUIRoomContext.shared.currentUserInfo.userId)
        self.applyView?.filter(userId: AUIRoomContext.shared.currentUserInfo.userId)
    }
    
    public func onApplyRejected(userId: String) {
        AUIToast.show(text: "房主已拒绝您的申请！")
    }
    
    public func onApplyCanceled(userId: String) {
        AUIToast.show(text: "房主已取消您的申请！")
    }
    
    
}

extension AUIInvitationViewBinder: AUIRoomManagerRespDelegate {
    public func onRoomUserBeKicked(roomId: String, userId: String) {
        
    }
    
    public func onRoomAnnouncementChange(roomId: String, announcement: String) {
        //TODO: - update room announcement
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.inviteView?.refreshUsers(users: userList)
    }
    
    public func onRoomDestroy(roomId: String) {
        
    }
    
    public func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
        
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.filter(userId: userInfo.userId)
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.filter(userId: userInfo.userId)
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.filter(userId: userInfo.userId)
    }
    
}
