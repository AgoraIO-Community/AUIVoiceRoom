//
//  AUIInvitationViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/6/5.
//

import UIKit
import AUIKit

open class AUIInvitationViewBinder: NSObject {
    
    private var newApplyClosure: (([String:Int]) -> ())?
    
    private weak var inviteView: AUIInvitationView?
    
    private weak var applyView: AUIApplyView?
    
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

    public func bind(inviteView: AUIInvitationView,applyView: AUIApplyView, invitationDelegate: AUIInvitationServiceDelegate, roomDelegate: AUIRoomManagerDelegate,receiveApply: @escaping ([String:Int]) -> Void) {
        self.newApplyClosure = receiveApply
        self.inviteView = inviteView
        self.applyView = applyView
        self.invitationDelegate = invitationDelegate
        self.roomDelegate = roomDelegate
        self.invitationDelegate?.bindRespDelegate(delegate: self)
    }
}

extension AUIInvitationViewBinder: AUIInvitationRespDelegate {
    
    public func onReceiveApplyUsersUpdate(users: [String:Int]) {
        //TODO: - 全量更新申请列表
        self.newApplyClosure?(users)
    }
    
    public func onInviteeListUpdate(inviteeList: [String:Int]) {
        self.inviteView?.userList.removeAll()
//        self.inviteView?.userList = inviteeList
        self.inviteView?.tableView.reloadData()
    }
    
    public func onReceiveNewInvitation(userId: String, seatIndex: Int) {
        AUIAlertView
            .theme_defaultAlert()
            .contentTextAligment(textAlignment: .center)
            .isShowCloseButton(isShow: true)
            .title(title: "邀请上麦").content(content: seatIndex != -1 ? "房主邀请您上\(seatIndex)号麦": "房主邀请您上麦")
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
        self.applyView?.userList = self.applyView?.userList.filter({
            $0.userId != userId
        }) ?? []
        self.inviteView?.userList = self.inviteView?.userList.filter({
            $0.userId != userId
        }) ?? []
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
        self.inviteView?.userList = self.inviteView?.userList.filter({
            $0.userId != AUIRoomContext.shared.currentUserInfo.userId
        }) ?? []
        self.applyView?.userList = self.applyView?.userList.filter({
            $0.userId != AUIRoomContext.shared.currentUserInfo.userId
        }) ?? []
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
