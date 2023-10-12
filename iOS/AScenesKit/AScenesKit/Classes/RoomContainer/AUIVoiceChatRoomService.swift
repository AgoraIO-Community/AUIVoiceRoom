//
//  AUIVoiceChatRoomService.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit

import AgoraRtcKit
import AUIKitCore
import AVFAudio
import AgoraRtmKit

/// 语聊房房间Service，内部负责初始化房间内需要的Service组件，包括房间Service，邀请Service，麦位Service，...
///  初始化时候需要注意先初始化rtc 然后rtm 先加入rtc后加入rtm，避免发生加入channel用户uid不一致问题
open class AUIVoiceChatRoomService: NSObject {
    lazy var micSeatImpl: AUIMicSeatServiceDelegate = AUIMicSeatServiceImpl(channelName: channelName,
                                                                            rtmManager: rtmManager,
                                                                            roomManager: roomManagerImpl)
    
    lazy var userImpl: AUIUserServiceDelegate = AUIUserServiceImpl(channelName: channelName,
                                                                   rtmManager: rtmManager,
                                                                   roomManager: roomManagerImpl)
    
    lazy var chatImplement: AUIMManagerServiceDelegate = AUIIMManagerServiceImplement(channelName: channelName, rtmManager: rtmManager)
    
    lazy var giftImplement: AUIGiftServiceImplement = AUIGiftServiceImplement(channelName: channelName, rtmManager: rtmManager)
    
    lazy var invitationImplement: AUIInvitationServiceImpl = AUIInvitationServiceImpl(channelName: channelName, rtmManager: rtmManager)
    
    var roomManagerImpl: AUIRoomManagerImpl!
    public private(set) var channelName: String!
    private var roomConfig: AUIRoomConfig!
    private(set) var rtcEngine: AgoraRtcEngineKit!
    private var rtmManager: AUIRtmManager!
    private var rtcEngineCreateBySercice = false
    
    private var rtcJoinClousure: ((Error?)->())?
    
    public var beKickedClosure: (() -> ())?
    
    public var reportAudioVolumeIndicationOfSpeakers:(([AgoraRtcAudioVolumeInfo], Int)->())?
    
    deinit {
        aui_info("deinit AUIVoiceChatRoomService", tag: "AUIVoiceChatRoomService")
    }
    
    public init(rtcEngine: AgoraRtcEngineKit?,
                roomManager: AUIRoomManagerImpl,
                roomConfig: AUIRoomConfig,
                roomInfo: AUIRoomInfo) {
        aui_info("init AUIVoiceChatRoomService", tag: "AUIVoiceChatRoomService")
        super.init()
        self.channelName = roomInfo.roomId
        self.roomConfig = roomConfig
        if let rtcEngine = rtcEngine {
            self.rtcEngine = rtcEngine
        } else {
            self.rtcEngine = self._createRtcEngine(commonConfig: roomManager.commonConfig)
            rtcEngineCreateBySercice = true
        }
        rtcEngine?.enableAudioVolumeIndication(350, smooth: 2, reportVad: false)
        self.roomManagerImpl = roomManager
        self.rtmManager = roomManager.rtmManager
        self.userImpl.bindRespDelegate(delegate: self)
        let userId = Int(roomManager.commonConfig.userId) ?? 0
        
        AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
        AUIRoomContext.shared.roomInfoMap[channelName] = roomInfo
    }
    
    //token过期之后调用该方法更新所有token
    public func renew(config: AUIRoomConfig) {
        roomConfig = config
        AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
        
        //rtm renew
        rtmManager.renew(token: roomConfig.rtmToken007)
        rtmManager.renewChannel(channelName: channelName, token: roomConfig.rtcToken007)
        
        //rtc renew
        rtcEngine.renewToken(roomConfig.rtcRtcToken)
    }
    
    func joinRtcChannel(completion: ((Error?)->())?) {
        guard let commonConfig = AUIRoomContext.shared.commonConfig,
              let uid = UInt(commonConfig.userId) else {
            aui_error("joinRtcChannel fail, commonConfig is empty", tag: "AUIVoiceChatRoomService")
            completion?(nil)
            return
        }
        
        setEngineConfig(with: uid)
        
        let ret =
        self.rtcEngine.joinChannel(byToken: roomConfig.rtcRtcToken,
                                   channelId: roomConfig.rtcChannelName,
                                   uid: uid,
                                   mediaOptions: channelMediaOptions())
#if DEBUG
        aui_info("joinChannel channelName ret: \(ret) channelName:\(roomConfig.rtcChannelName), uid: \(uid) token: \(roomConfig.rtcRtcToken)", tag: "AUIVoiceChatRoomService")
#endif
        
        if ret != 0 {
            completion?(AUICommonError.rtcError(ret).toNSError())
            return
        }
        rtcJoinClousure = completion
    }
    
    func leaveRtcChannel() {
        self.rtcEngine.leaveChannel()
        if rtcEngineCreateBySercice {
            AgoraRtcEngineKit.destroy()
        }
        aui_error("leaveRtcChannel", tag: "AUIVoiceChatRoomService")
    }
    
    func destory() {
        leaveRtcChannel()
    }
    
    private func setEngineConfig(with uid:UInt) {
        //todo 因为sdk的问题需要在加入频道前修改audioSession权限 退出频道去掉这个
        rtcEngine.setAudioSessionOperationRestriction(.all)
        try? AVAudioSession.sharedInstance().setCategory(.playAndRecord, options: [.defaultToSpeaker,.mixWithOthers,.allowBluetoothA2DP])
        
        rtcEngine.setDefaultAudioRouteToSpeakerphone(true)
        rtcEngine.enableLocalAudio(true)
//        rtcEngine.setAudioScenario(.gameStreaming)
        rtcEngine.setAudioProfile(.musicHighQuality)
//        rtcEngine.setChannelProfile(.liveBroadcasting)
        rtcEngine.setParameters("{\"rtc.enable_nasa2\": false}")
        rtcEngine.setParameters("{\"rtc.ntp_delay_drop_threshold\": 1000}")
//        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp\": true}")
        rtcEngine.setParameters("{\"rtc.net.maxS2LDelay\": 800}")
//        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\": true}")
        rtcEngine.setParameters("{\"rtc.net.maxS2LDelayBroadcast\": 400}")
        rtcEngine.setParameters("{\"che.audio.neteq.prebuffer\": true}")
        rtcEngine.setParameters("{\"che.audio.neteq.prebuffer_max_delay\": 600}")
        rtcEngine.setParameters("{\"che.audio.max_mixed_participants\": 8}")
//        rtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast_dynamic\": true}")
        rtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        
        //开启唱歌评分功能
        rtcEngine.enableAudioVolumeIndication(50, smooth: 3, reportVad: true)
        rtcEngine.enableAudio()
    }
    
    private func channelMediaOptions() -> AgoraRtcChannelMediaOptions {
        let isRoomOwner = AUIRoomContext.shared.isRoomOwner(channelName: channelName)
        let option = AgoraRtcChannelMediaOptions()
        option.clientRoleType = isRoomOwner ? .broadcaster : .audience
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = option.clientRoleType == .broadcaster ? true : false
        option.publishCustomAudioTrack = false
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true
        option.enableAudioRecordingOrPlayout = true
        
        aui_info("update clientRoleType: \(option.clientRoleType.rawValue)", tag: "AUIVoiceChatRoomService")
        return option
    }
}

//private method
extension AUIVoiceChatRoomService {
    private func _rtcEngineConfig(commonConfig: AUICommonConfig) -> AgoraRtcEngineConfig {
       let config = AgoraRtcEngineConfig()
        config.appId = commonConfig.appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        
        if config.appId?.count ?? 0 == 0 {
            aui_error("config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'", tag: "AUIVoiceChatRoomService")
            assert(false, "config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'")
        }
        return config
    }
    
    private func _createRtcEngine(commonConfig: AUICommonConfig) ->AgoraRtcEngineKit {
        let engine = AgoraRtcEngineKit.sharedEngine(with: _rtcEngineConfig(commonConfig: commonConfig),
                                                    delegate: self)
        engine.delegate = self
        return engine
    }
}

extension AUIVoiceChatRoomService: AgoraRtcEngineDelegate {
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        aui_info("didJoinChannel channel:\(channel) uid: \(uid)", tag: "AUIVoiceChatRoomService")
        
        guard uid == UInt(AUIRoomContext.shared.currentUserInfo.userId) else {
            return
        }
        
        aui_info("joinChannel  channelName success channelName:\(channel), uid: \(uid)", tag: "AUIVoiceChatRoomService")
//        self.rtcEngine.setAudioSessionOperationRestriction(.deactivateSession)
        rtcJoinClousure?(nil)
        rtcJoinClousure = nil
    }
   
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        aui_error("didOccurError: \(errorCode.rawValue)", tag: "AUIVoiceChatRoomService")
        rtcJoinClousure?(AUICommonError.rtcError(Int32(errorCode.rawValue)).toNSError())
        rtcJoinClousure = nil
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        reportAudioVolumeIndicationOfSpeakers?(speakers, totalVolume)
    }
}

extension AUIVoiceChatRoomService: AgoraRtmClientDelegate {
    
}

extension AUIVoiceChatRoomService: AUIUserRespDelegate {
    public func onUserBeKicked(roomId: String, userId: String) {
        self.beKickedClosure?()
    }
    
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        guard let user = userList.filter({$0.userId == AUIRoomContext.shared.currentUserInfo.userId }).first else {return}
        aui_info("onRoomUserSnapshot", tag: "AUIVoiceChatRoomService")
        onUserAudioMute(userId: user.userId, mute: user.muteAudio)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
        aui_info("onUserAudioMute mute current user: \(mute)", tag: "AUIVoiceChatRoomService")
        rtcEngine.adjustRecordingSignalVolume(mute ? 0 : 100)
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
//        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
//        aui_info("onMuteVideo onUserVideoMute [\(userId)]: \(mute)", tag: "AUIVoiceChatRoomService")
//        rtcEngine.enableLocalVideo(!mute)
//        let option = AgoraRtcChannelMediaOptions()
//        option.publishCameraTrack = !mute
//        rtcEngine.updateChannel(with: option)
    }
}
