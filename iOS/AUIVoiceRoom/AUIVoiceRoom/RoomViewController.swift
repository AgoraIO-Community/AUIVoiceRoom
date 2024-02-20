//
//  RoomViewController.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/3/9.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import UIKit
import AScenesKit
import AUIKitCore

class RoomViewController: UIViewController {
    var roomInfo: AUIRoomInfo?
    var isCreate: Bool = false
    var themeIdx = 0
    private var voiceRoomView: AUIVoiceChatRoomView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = .white
        guard let room = roomInfo else { return }
        self.navigationItem.title = roomInfo?.roomName
        
        let uid = VoiceChatUIKit.shared.commonConfig?.owner?.userId ?? ""
        //创建房间容器
        let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds)
        let isOwner = roomInfo?.owner?.userId == uid ? true : false
        voiceRoomView.onClickOffButton = { [weak self] in
            aui_info("onClickOffButton", tag: "RoomViewController")
            AUIChatInputBar.hiddenInput()
            AUIAlertView.theme_defaultAlert()
                .contentTextAligment(textAlignment: .center)
                .title(title: isOwner ? "解散房间" : "离开房间")
                .content(content: isOwner ? "确定解散该房间吗?" : "确定离开该房间吗？")
                .leftButton(title: "取消")
                .rightButton(title: "确定")
                .rightButtonTapClosure {
                    guard let self = self else {return}
                    AUIToast.hidden(delay:0)
                    AUICommonDialog.hidden()
                    self.voiceRoomView?.onBackAction()
                    self.navigationController?.popViewController(animated: true)
                    aui_info("rightButtonTapClosure", tag: "RoomViewController")
                }.leftButtonTapClosure {
                    aui_info("leftButtonTapClosure", tag: "RoomViewController")
                }
                .show()
        }
        self.view.addSubview(voiceRoomView)
        self.voiceRoomView = voiceRoomView
        
        let roomConfig = AUIRoomConfig()
        if isOwner, isCreate {
            let roomId = roomInfo?.roomId ?? ""
            generateToken(channelName: roomId,
                          roomConfig: roomConfig,
                          completion: {[weak self] error in
                guard let self = self else {return}
                if let error = error {
                    self.navigationController?.popViewController(animated: true)
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
                VoiceChatUIKit.shared.createRoom(roomInfo: room,
                                                 roomConfig: roomConfig,
                                                 chatView: voiceRoomView) {[weak self] error in
                    guard let self = self else {return}
                    if let error = error {
                        self.navigationController?.popViewController(animated: true)
                        AUIToast.show(text: error.localizedDescription)
                        return
                    }
                    self.roomInfo = roomInfo
                }
                
                // 订阅房间被销毁回调
                VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
            })
        } else {
            let roomId = roomInfo?.roomId ?? ""
            generateToken(channelName: roomId,
                          roomConfig: roomConfig) {[weak self] err  in
                guard let self = self else {return}
                VoiceChatUIKit.shared.enterRoom(roomId: roomId,
                                                roomConfig: roomConfig,
                                                chatView: voiceRoomView) {[weak self] roomInfo, error in
                    guard let self = self else {return}
                    if let error = error {
                        self.navigationController?.popViewController(animated: true)
                        AUIToast.show(text: error.localizedDescription)
                        return
                    }
                }
                
                // 订阅房间被销毁回调
                VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.isNavigationBarHidden = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        VoiceChatUIKit.shared.leaveRoom(roomId: roomInfo?.roomId ?? "")
        VoiceChatUIKit.shared.unbindRespDelegate(delegate: self)
        AUIToast.hidden(delay:0)
        AUICommonDialog.hidden()
    }
    
    override func willMove(toParent parent: UIViewController?) {
        super.willMove(toParent: parent)
        if parent == nil {
            navigationController?.isNavigationBarHidden = false
        }
    }
    
    private func generateToken(channelName: String,
                               roomConfig: AUIRoomConfig,
                               completion: @escaping ((Error?) -> Void)) {
        let uid = VoiceChatUIKit.shared.commonConfig?.owner?.userId ?? ""
        let rtcChorusChannelName = "\(channelName)_rtc_ex"
        roomConfig.channelName = channelName
        roomConfig.rtcChorusChannelName = rtcChorusChannelName
        print("generateTokens: \(uid)")

        let group = DispatchGroup()

        var err: Error?
        group.enter()
        let tokenModel1 = AUITokenGenerateNetworkModel()
        tokenModel1.channelName = channelName
        tokenModel1.userId = uid
        tokenModel1.request { error, result in
            defer {
                if err == nil {
                    err = error
                }
                group.leave()
            }
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            roomConfig.rtcToken = tokenMap["rtcToken"] ?? ""
            roomConfig.rtmToken = tokenMap["rtmToken"] ?? ""
        }

        group.enter()
        let tokenModel2 = AUITokenGenerateNetworkModel()
        tokenModel2.channelName = rtcChorusChannelName
        tokenModel2.userId = uid
        tokenModel2.request { error, result in
            defer {
                if err == nil {
                    err = error
                }
                group.leave()
            }

            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}

            roomConfig.rtcChorusRtcToken = tokenMap["rtcToken"] ?? ""
        }

        group.notify(queue: DispatchQueue.main) {
            completion(err)
        }
    }
}

extension RoomViewController: AUIVoiceChatRoomServiceRespDelegate {
    private func showLeaveAlert(alertText: String, subText: String, confirmText: String) {
        AUIChatInputBar.hiddenInput()
        
        AUIAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: true)
            .title(title: alertText)
            .content(content: subText)
            .contentTextAligment(textAlignment: .center)
            .rightButton(title: confirmText)
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                AUICommonDialog.hidden()
                self.voiceRoomView?.onBackAction()
                self.navigationController?.popViewController(animated: true)
            })
            .isShowCloseButton(isShow: false)
            .show(fromVC: self)
    }
    
    func onRoomUserBeKicked(roomId: String, userId: String) {
        showLeaveAlert(alertText: "您已被踢出房间", subText: "", confirmText: aui_localized("confirm"))
    }
    
    func onRoomDestroy(roomId: String) {
        showLeaveAlert(alertText: "房间已销毁", subText: "返回房间列表", confirmText: "我知道了")
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
    }
}
