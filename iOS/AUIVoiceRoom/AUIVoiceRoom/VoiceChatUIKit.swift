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
import AScenesKit

private let kSceneId = "VoiceRoomUIKit"
@objcMembers
public class VoiceChatUIKit: NSObject {
    public static let shared: VoiceChatUIKit = VoiceChatUIKit()
    public var commonConfig: AUICommonConfig?
    private var apiConfig: AUIAPIConfig?
    private var service: AUIVoiceChatRoomService?
    private var roomId: String?
    private var isRoomOwner: Bool = false
    private lazy var roomManager: AUIRoomManagerImpl = AUIRoomManagerImpl(sceneId: kSceneId)
    private var isRoomDestroy: Bool = false
    
    public func setup(commonConfig: AUICommonConfig,
                      apiConfig: AUIAPIConfig? = nil) {
        AUIRoomContext.shared.commonConfig = commonConfig
        self.commonConfig = commonConfig
        self.apiConfig = apiConfig
    }
    
    public func getRoomInfoList(lastCreateTime: Int64, 
                                pageSize: Int,
                                callback: @escaping AUIRoomListCallback) {
        checkSetupAndCommonConfig()
        roomManager.getRoomInfoList(lastCreateTime: lastCreateTime,
                                    pageSize: pageSize,
                                    callback: callback)
    }
    
    public func createRoom(roomInfo: AUIRoomInfo,
                           roomConfig: AUIRoomConfig,
                           chatView: AUIVoiceChatRoomView,
                           completion: @escaping (NSError?) -> Void) {
        checkSetupAndCommonConfig()
        
        var roomError: NSError?
        var serviceError: NSError?
        let dispatchGroup = DispatchGroup()
        
        let date = Date()
        isRoomDestroy = false
        dispatchGroup.enter()
        roomManager.createRoom(room: roomInfo) {error, info in
            roomError = error
            aui_info("(roomManager)createRoom: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            dispatchGroup.leave()
        }
        
        dispatchGroup.enter()
        let service = AUIVoiceChatRoomService(apiConfig: self.apiConfig, roomConfig: roomConfig)
        self.bindRespDelegate(delegate: self)
        self.service = service
        self.roomId = roomInfo.roomId
        self.isRoomOwner = true
        service.create(roomInfo: roomInfo) { error in
            serviceError = error
            aui_info("(service)createRoom: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            dispatchGroup.leave()
        }
        chatView.bindService(service: service)
        
        dispatchGroup.notify(queue: .main) {
            aui_info("createRoom total cost: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            if let err = roomError ?? serviceError {
                completion(err)
                return
            }
            completion(nil)
        }
    }
    
    public func enterRoom(roomId: String,
                          roomConfig: AUIRoomConfig,
                          chatView: AUIVoiceChatRoomView,
                          completion: @escaping (AUIRoomInfo?, NSError?) -> Void) {
        checkSetupAndCommonConfig()
        isRoomDestroy = false
        let date = Date()
        let service = AUIVoiceChatRoomService(apiConfig: self.apiConfig,
                                            roomConfig: roomConfig)
        self.service = service
        self.roomId = roomId
        self.bindRespDelegate(delegate: self)
        service.enter {[weak self, weak chatView] roomInfo, err in
            aui_info("service enterRoom2: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: "Benchmark")
            chatView?.bindService(service: service)
            if let _ = roomInfo?.owner?.userId {
                self?.isRoomOwner = roomInfo?.owner?.userId == AUIRoomContext.shared.currentUserInfo.userId
            } else {
                self?.isRoomOwner = true
            }
            completion(roomInfo, err as NSError?)
        }
    }
    
    public func leaveRoom(roomId: String) {
        checkSetupAndCommonConfig()
        self.unbindRespDelegate(delegate: self)
        if isRoomOwner || isRoomDestroy {
            roomManager.destroyRoom(roomId: roomId,
                                    callback: { _ in
            })
            service?.destroy(callback: { _ in })
        } else {
            service?.exit { _ in }
        }
        isRoomOwner = false
//        rtmClient?.logout()
        self.service = nil
        self.roomId = nil
    }

    public func renew(config: AUIRoomConfig) {
        service?.renew(config: config)
    }

    public func bindRespDelegate(delegate: AUIVoiceChatRoomServiceRespDelegate) {
        service?.bindRespDelegate(delegate: delegate)
    }

    public func unbindRespDelegate(delegate: AUIVoiceChatRoomServiceRespDelegate) {
        service?.unbindRespDelegate(delegate: delegate)
    }
}

extension VoiceChatUIKit: AUIVoiceChatRoomServiceRespDelegate {
    public func onRoomDestroy(roomId: String) {
        isRoomDestroy = true
    }
}

// MARK: private method
extension VoiceChatUIKit {
    private func checkSetupAndCommonConfig() {
        assert(AUIRoomContext.shared.commonConfig?.isValidate() ?? false, "make sure invoke setup first")
    }
}
