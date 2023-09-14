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
    var themeIdx = 0
    private var voiceRoomView: AUIVoiceChatRoomView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = .white
        guard let info = roomInfo else { return }
        self.navigationItem.title = roomInfo?.roomName
        
        let uid = VoiceChatUIKit.shared.roomConfig?.userId ?? ""
        //创建房间容器
        let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds,roomInfo: info)
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
        
        //通过generateToken方法获取到必须的token和appid
        generateToken {[weak self] roomConfig, appId in
            guard let self = self else {return}
            VoiceChatUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                                           appId: appId,
                                           config: roomConfig,
                                           roomView: voiceRoomView) {_ in
            }
            //订阅Token过期回调
            VoiceChatUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)
            //订阅房间被销毁回调
            VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.isNavigationBarHidden = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        VoiceChatUIKit.shared.destoryRoom(roomId: roomInfo?.roomId ?? "")
        VoiceChatUIKit.shared.unsubscribeError(roomId: roomInfo?.roomId ?? "", delegate: self)
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
    
    private func generateToken(completion:@escaping ((AUIRoomConfig, String)->())) {
        let uid = VoiceChatUIKit.shared.roomConfig?.userId ?? ""
        let channelName = roomInfo?.roomId ?? ""
        let rtcChannelName = "\(channelName)_rtc"
        let rtcChorusChannelName = "\(channelName)_rtc_ex"
        let roomConfig = AUIRoomConfig()
        roomConfig.channelName = channelName
        roomConfig.rtcChannelName = rtcChannelName
        roomConfig.rtcChorusChannelName = rtcChorusChannelName
        print("generateTokens: \(uid)")
        
        var appId = ""
        
        let group = DispatchGroup()
        
        group.enter()
        let tokenModel1 = AUITokenGenerateNetworkModel()
        tokenModel1.channelName = channelName
        tokenModel1.userId = uid
        tokenModel1.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcToken007 = tokenMap["rtcToken"] ?? ""
            roomConfig.rtmToken007 = tokenMap["rtmToken"] ?? ""
            appId = tokenMap["appId"] ?? ""
        }
        
        group.enter()
        let tokenModel2 = AUITokenGenerateNetworkModel()
        tokenModel2.channelName = rtcChannelName
        tokenModel2.userId = uid
        tokenModel2.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcRtcToken = tokenMap["rtcToken"] ?? ""
            roomConfig.rtcRtmToken = tokenMap["rtmToken"] ?? ""
        }
        
        group.enter()
        let tokenModel3 = AUITokenGenerateNetworkModel()
        tokenModel3.channelName = rtcChorusChannelName
        tokenModel3.userId = uid
        tokenModel3.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcChorusRtcToken = tokenMap["rtcToken"] ?? ""
        }
        
        group.notify(queue: DispatchQueue.main) {
            completion(roomConfig, appId)
        }
    }
}

extension RoomViewController: AUIRoomManagerRespDelegate {
    
    func onRoomUserBeKicked(roomId: String, userId: String) {
        AUIToast.show(text: "您被踢出房间!")
        VoiceChatUIKit.shared.unsubscribeError(roomId: roomInfo?.roomId ?? "", delegate: self)
        VoiceChatUIKit.shared.unbindRespDelegate(delegate: self)
        AUIToast.hidden(delay:1.5)
        AUICommonDialog.hidden()
        self.voiceRoomView?.onBackAction()
        self.navigationController?.popViewController(animated: true)
    }
    
    func onRoomAnnouncementChange(roomId: String, announcement: String) {
        
    }
    
    func onRoomDestroy(roomId: String) {
        AUIChatInputBar.hiddenInput()
        
        AUIAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: true)
            .title(title: "房间已销毁")
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                AUICommonDialog.hidden()
                self.voiceRoomView?.onBackAction()
                self.navigationController?.popViewController(animated: true)
            })
            .show(fromVC: self)
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
        
    }
}


extension RoomViewController: AUIRtmErrorProxyDelegate {
    @objc func onTokenPrivilegeWillExpire(channelName: String?) {
        generateToken { config, _ in
            VoiceChatUIKit.shared.renew(config: config)
        }
    }
}
