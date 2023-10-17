//
//  VoiceChatUIKit.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/4/28.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import AUIKitCore
import AgoraRtcKit
import AgoraRtmKit

public class VoiceChatUIKit: NSObject {
    public static let shared: VoiceChatUIKit = VoiceChatUIKit()
    public var roomConfig: AUICommonConfig?
    private var rtcEngine: AgoraRtcEngineKit?
    private var rtmClient: AgoraRtmClientKit?
    private var service: AUIVoiceChatRoomService?
    private var roomInfo: AUIRoomInfo?
    
    private var roomManager: AUIRoomManagerImpl?
    
    public func setup(roomConfig: AUICommonConfig,
                      rtcEngine: AgoraRtcEngineKit? = nil,
                      rtmClient: AgoraRtmClientKit? = nil) {
        self.roomConfig = roomConfig
        self.rtcEngine = rtcEngine
        self.rtmClient = rtmClient
        self.roomManager = AUIRoomManagerImpl(commonConfig: roomConfig, rtmClient: rtmClient)
    }
    
    public func getRoomInfoList(lastCreateTime: Int64?, pageSize: Int, callback: @escaping AUIRoomListCallback) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize, callback: callback)
    }
    
    public func createRoom(roomInfo: AUICreateRoomInfo,
                           success: ((AUIRoomInfo?)->())?,
                           failure: ((Error)->())?) {
        guard let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        roomManager.createRoom(room: roomInfo) { error, info in
            if let error = error {
                failure?(error)
                return
            }
            
            success?(info)
        }
    }
    
    public func launchRoom(roomInfo: AUIRoomInfo,
                           roomView: AUIVoiceChatRoomView,
                           completion: @escaping ((NSError?)->())) {
        guard /*let rtmClient = self.rtmClient,*/ let roomManager = self.roomManager else {
            assert(false, "please invoke setup first")
            return
        }
        
        generateToken(roomInfo: roomInfo) {[weak self] roomConfig, appId in
            guard let self = self else { return }
            guard appId.count > 0 else {
                completion(NSError(domain: "VoiceChatUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "token generate fail!"]))
                return
            }
            AUIRoomContext.shared.commonConfig?.appId = appId
            let service = AUIVoiceChatRoomService(rtcEngine: rtcEngine,
                                                  roomManager: roomManager,
                                                  roomConfig: roomConfig,
                                                  roomInfo: roomInfo)
            //订阅Token过期回调
            self.subscribeError(delegate: self)
            roomView.bindService(service: service)
            self.service = service
            self.roomInfo = roomInfo
            completion(nil)
        }
    }
    
    public func destoryRoom(roomId: String) {
//        rtmClient?.logout()
        service = nil
    }
    
    
    public func bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        roomManager?.bindRespDelegate(delegate: delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        roomManager?.unbindRespDelegate(delegate: delegate)
    }
}


//MARK: private method
extension VoiceChatUIKit {
    private func subscribeError(delegate: AUIRtmErrorProxyDelegate) {
        roomManager?.rtmManager.subscribeError(channelName: "", delegate: delegate)
    }
    
    private func unsubscribeError(delegate: AUIRtmErrorProxyDelegate) {
        roomManager?.rtmManager.unsubscribeError(channelName: "", delegate: delegate)
    }
    
    private func renew(config: AUIRoomConfig) {
        service?.renew(config: config)
    }
    
    private func generateToken(roomInfo: AUIRoomInfo,
                               completion:@escaping ((AUIRoomConfig, String)->())) {
        let uid = roomConfig?.userId ?? ""
        let channelName = roomInfo.roomId
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

extension VoiceChatUIKit: AUIRtmErrorProxyDelegate {
    @objc public func onTokenPrivilegeWillExpire(channelName: String?) {
        guard let roomInfo = roomInfo else { return }
        generateToken(roomInfo: roomInfo) {[weak self] config, _ in
            self?.renew(config: config)
        }
    }
}
