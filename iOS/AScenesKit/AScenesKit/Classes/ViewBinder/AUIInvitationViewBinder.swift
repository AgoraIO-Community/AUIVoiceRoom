//
//  AUIInvitationViewBinder.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/6/5.
//

import UIKit
import AUIKitCore

open class AUIInvitationViewBinder: NSObject {
    private var userMap: [String: AUIUserInfo] = [:]
//    private var newApplyClosure: (([String: AUIInvitationInfo]) -> ())?
    
    private weak var inviteView: IAUIListViewBinderRefresh?
    
    private weak var applyView: IAUIListViewBinderRefresh?
    private var seatIndexMap: [Int: String] = [:]
    private var applyInfos: [AUIInvitationInfo] = []
    private var inviteAlertView: AUIAlertView?
    
    public weak var invitationDelegate: AUIInvitationServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            invitationDelegate?.unbindRespDelegate(delegate: self)
        }
    }
    
    public weak var userDelegate: AUIUserServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            userDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public weak var micSeatDelegate: AUIMicSeatServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            micSeatDelegate?.bindRespDelegate(delegate: self)
        }
    }

    public func bind(inviteView: IAUIListViewBinderRefresh,
                     applyView: IAUIListViewBinderRefresh,
                     invitationService: AUIInvitationServiceDelegate,
                     micSeatService: AUIMicSeatServiceDelegate,
                     userService: AUIUserServiceDelegate,
                     receiveApply: @escaping ([AUIInvitationInfo]) -> Void) {
//        self.newApplyClosure = receiveApply
        self.inviteView = inviteView
        self.applyView = applyView
        self.invitationDelegate = invitationService
        self.micSeatDelegate = micSeatService
        self.userDelegate = userService
        self.invitationDelegate?.bindRespDelegate(delegate: self)
    }
    
    func getApplyUsers() -> [AUIUserCellUserData] {
        let userDatas = applyInfos.compactMap({ info in
            var data: AUIUserCellUserData? = nil
            guard let user = self.userMap[info.userId] else { return data }
            data = user.createData(info.seatNo) as? AUIUserCellUserData
            return data
        })
        return userDatas
    }
}

extension AUIInvitationViewBinder: AUIInvitationRespDelegate {
    public func onReceiveApplyUsersUpdate(applyList: [AUIInvitationInfo]) {
        //TODO: - 全量更新申请列表
//        self.newApplyClosure?(users)
        applyInfos = applyList
//        self.applyView.refreshUsers(users:applyUsers)
    }
    
    public func onInviteeListUpdate(inviteeList: [AUIInvitationInfo]) {
        self.inviteView?.refreshUsers(users: [])
    }
    
    public func onReceiveNewInvitation(userId: String, seatIndex: Int) {
        inviteAlertView =
        AUIAlertView
            .theme_defaultAlert()
            .contentTextAligment(textAlignment: .center)
            .isShowCloseButton(isShow: true)
            .title(title: "邀请上麦").content(content: seatIndex != -1 ? "房主邀请您上\(seatIndex+1)号麦": "房主邀请您上麦")
            .titleColor(color: .white)
            .rightButton(title: aui_localized("confirm")).leftButton(title: "拒绝")
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
        inviteAlertView?.hidden()
    }
    
    public func onReceiveNewApply(userId: String, seatIndex: Int) {
        //TODO: - 替换为更新更多功能小红点 提示用户有新申请
//        AUIAlertView()
//            .theme_background(color: "CommonColor.black")
//            .isShowCloseButton(isShow: true)
//            .title(title: aui_localized("Apply to mic")).content(content: seatIndex != -1 ? "用户\(userId)申请上\(seatIndex)号麦": "用户\(userId)申请上麦")
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
    
    public func onInviteWillAccept(userId: String, 
                                   seatIndex: Int,
                                   metaData: NSMutableDictionary) -> NSError? {
        //先查询是否可以上麦
        if let _ = seatIndexMap[seatIndex] {
            return AUICommonError.micSeatNotIdle.toNSError()
        }
        
        if let userInfo = userMap[userId] {
            micSeatDelegate?.pickSeat(seatIndex: seatIndex,
                                      user: userInfo) { err in
            }
        }
        return nil
    }
    
    public func onApplyWillAccept(userId: String, 
                                  seatIndex: Int,
                                  metaData: NSMutableDictionary) -> NSError? {
        //先查询是否可以上麦
        if let _ = seatIndexMap[seatIndex] {
            return AUICommonError.micSeatNotIdle.toNSError()
        }
        
        if let userInfo = userMap[userId] {
            micSeatDelegate?.pickSeat(seatIndex: seatIndex,
                                      user: userInfo) { err in
            }
        }
        return nil
    }
}

extension AUIInvitationViewBinder: AUIUserRespDelegate {
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.inviteView?.refreshUsers(users: userList.map({ $0.createData(-1)}))
        userMap.removeAll()
        userList.forEach { userMap[$0.userId] = $0 }
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.filter(userId: userInfo.userId)
        userMap[userInfo.userId] = userInfo
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.filter(userId: userInfo.userId)
        userMap[userInfo.userId] = nil
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        self.inviteView?.filter(userId: userInfo.userId)
        userMap[userInfo.userId] = userInfo
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
}

extension AUIInvitationViewBinder: AUIMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        seatIndexMap[seatIndex] = user.userId
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {
        seatIndexMap[seatIndex] = nil
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        
    }
    
    
}
