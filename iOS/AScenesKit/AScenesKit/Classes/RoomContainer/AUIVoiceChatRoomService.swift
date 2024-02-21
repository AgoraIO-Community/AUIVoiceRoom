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

private let kSertviceTag = "AUIVoiceChatRoomService"
private let kRoomInfoAttrKry = "basic"

/// 语聊房房间Service，内部负责初始化房间内需要的Service组件，包括房间Service，邀请Service，麦位Service，...
///  初始化时候需要注意先初始化rtc 然后rtm 先加入rtc后加入rtm，避免发生加入channel用户uid不一致问题
open class AUIVoiceChatRoomService: NSObject {
    lazy var micSeatImpl: AUIMicSeatServiceDelegate = AUIMicSeatServiceImpl(channelName: channelName,
                                                                            rtmManager: rtmManager)
    
    lazy var userImpl: AUIUserServiceDelegate = AUIUserServiceImpl(channelName: channelName,
                                                                   rtmManager: rtmManager)
    
    lazy var chatImplement: AUIMManagerServiceDelegate = AUIIMManagerServiceImplement(channelName: channelName, 
                                                                                      rtmManager: rtmManager)
    
    lazy var giftImplement: AUIGiftServiceImplement = AUIGiftServiceImplement(channelName: channelName, 
                                                                              rtmManager: rtmManager)
    
    lazy var invitationImplement: AUIInvitationServiceImpl = AUIInvitationServiceImpl(channelName: channelName, 
                                                                                      rtmManager: rtmManager)
    
    public private(set) var channelName: String!
    private var roomConfig: AUIRoomConfig!
    private(set) var rtcEngine: AgoraRtcEngineKit!
    private var rtmClient: AgoraRtmClientKit!
    public private(set) lazy var rtmManager: AUIRtmManager = {
        return AUIRtmManager(rtmClient: self.rtmClient, rtmChannelType: .message, isExternalLogin: !rtmClientCreateBySercice)
    }()
    private var rtcEngineCreateBySercice = false
    private var rtmClientCreateBySercice = false
    
    private var rtcJoinClousure: ((Error?)->())?
        
    public var reportAudioVolumeIndicationOfSpeakers:(([AgoraRtcAudioVolumeInfo], Int)->())?
    
    private var subscribeDate: Date?
    private var lockRetrived: Bool = false {
        didSet {
            checkRoomValid()
        }
    }
    
    private var subscribeSuccess: Bool = false {
        didSet {
            checkRoomValid()
        }
    }
    private var userSnapshotList: [AUIUserInfo]? {
        didSet {
            checkRoomValid()
        }
    }
    private(set) var roomInfo: AUIRoomInfo? {
        didSet {
            if let info = roomInfo {
                AUIRoomContext.shared.roomInfoMap[info.roomId] = info
            }
            checkRoomValid()
        }
    }
    private var enterRoomCompletion: ((AUIRoomInfo?)-> ())?
    
    private var respDelegates: NSHashTable<AUIVoiceChatRoomServiceRespDelegate> = NSHashTable<AUIVoiceChatRoomServiceRespDelegate>.weakObjects()
    
    deinit {
        aui_info("deinit AUIVoiceChatRoomService", tag: kSertviceTag)
    }
    
    public init(apiConfig: AUIAPIConfig?,
                roomConfig: AUIRoomConfig) {
        aui_info("init AUIKaraokeRoomService", tag: kSertviceTag)
        super.init()
        self.channelName = roomConfig.channelName
        self.roomConfig = roomConfig
        if let rtcEngine = apiConfig?.rtcEngine {
            self.rtcEngine = rtcEngine
        } else {
            self.rtcEngine = self._createRtcEngine(commonConfig: AUIRoomContext.shared.commonConfig!)
            rtcEngineCreateBySercice = true
        }
        
        if let rtmClient = apiConfig?.rtmClient {
            self.rtmClient = rtmClient
        } else {
            rtmClientCreateBySercice = true
            self.rtmClient = createRtmClient()
        }
        
        self.userImpl.bindRespDelegate(delegate: self)
        
        AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
        AUIRoomContext.shared.roomArbiterMap[channelName] = AUIArbiter(channelName: channelName, rtmManager: rtmManager, userInfo: AUIRoomContext.shared.currentUserInfo)
    }
    
    //token过期之后调用该方法更新所有token
    public func renew(config: AUIRoomConfig) {
        roomConfig = config
        AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
        
        //rtm renew
        rtmManager.renew(token: config.rtmToken)
        
        //rtc renew
        rtcEngine.renewToken(roomConfig.rtcToken)
    }
    
    func joinRtcChannel(completion: ((Error?)->())?) {
        let currentUserInfo = AUIRoomContext.shared.currentUserInfo
        guard let uid = UInt(currentUserInfo.userId) else {
            aui_error("joinRtcChannel fail, commonConfig is empty", tag: kSertviceTag)
            completion?(nil)
            return
        }
        
        setEngineConfig(with: uid)
        let ret = self.rtcEngine.joinChannel(byToken: roomConfig.rtcToken,
                                             channelId: roomConfig.channelName,
                                             uid: uid,
                                             mediaOptions: channelMediaOptions())
        aui_info("joinChannel channelName ret: \(ret) channelName:\(roomConfig.channelName), uid: \(uid)", tag: kSertviceTag)
        
        guard ret == 0 else {
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
        aui_error("leaveRtcChannel", tag: kSertviceTag)
    }
    
    func destroy() {
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
         aui_error("config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'", tag: kSertviceTag)
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
    
    private func createRtmClient() -> AgoraRtmClientKit {
        let commonConfig = AUIRoomContext.shared.commonConfig!
        let userInfo = AUIRoomContext.shared.currentUserInfo
        let rtmConfig = AgoraRtmClientConfig(appId: commonConfig.appId, userId: userInfo.userId)
        rtmConfig.presenceTimeout = 60
        if rtmConfig.userId.count == 0 {
            aui_error("userId is empty")
            assert(false, "userId is empty")
        }
        if rtmConfig.appId.count == 0 {
            aui_error("appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
            assert(false, "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
        }
        let rtmClient = try? AgoraRtmClientKit(rtmConfig, delegate: nil)
        return rtmClient!
    }
    
    private func checkRoomValid() {
        guard subscribeSuccess, let roomInfo = roomInfo, lockRetrived else { return }
        if let completion = self.enterRoomCompletion {
            completion(roomInfo)
            self.enterRoomCompletion = nil
            for obj in self.respDelegates.allObjects {
                obj.onRoomInfoChange?(roomId: roomInfo.roomId, roomInfo: roomInfo)
            }
            
            //TODO: add more service.sereviceDidLoad
            chatImplement.sereviceDidLoad?()
        }
        
        guard let userList = userSnapshotList else { return }
        guard roomInfo.roomId.count > 0,
              let _ = userList.filter({ AUIRoomContext.shared.isRoomOwner(channelName: channelName, userId: $0.userId)}).first else {
            //room owner not found, clean room
            cleanRoomInfo(channelName: channelName)
            return
        }
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
        
        guard errorCode == .clientIsBannedByServer else {
            return
        }
        notifyBeKicked()
        
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
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, connectionChangedTo state: AgoraConnectionState, reason: AgoraConnectionChangedReason) {
        guard state == .failed, reason == .reasonBannedByServer else { return }
        
        notifyBeKicked()
    }
}

// room manager handler
extension AUIVoiceChatRoomService {
    private func cleanUserInfo(channelName: String, userId: String) {
        if userId == AUIRoomContext.shared.currentUserInfo.userId {
            AUIRoomContext.shared.getArbiter(channelName: channelName)?.release()
        }
        //TODO: 仲裁者暂无
//        guard AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false else {return}
//        guard let idx = micSeatImpl.getMicSeatIndex?(userId: userId), idx >= 0 else {return}
//        micSeatImpl.kickSeat(seatIndex: idx) { err in }
    }
    
    
    /// 清理房间，仲裁者才有权限操作
    /// - Parameter channelName: <#channelName description#>
    private func cleanRoomInfo(channelName: String) {
        guard let arbiter = AUIRoomContext.shared.getArbiter(channelName: channelName),
              arbiter.isArbiter() else {return}

        micSeatImpl.deinitService? { err in }
        chatImplement.deinitService? { err in }
        invitationImplement.deinitService { err in }
        
        rtmManager.cleanBatchMetadata(channelName: channelName,
                                      lockName: kRTM_Referee_LockName,
                                      removeKeys: [kRoomInfoAttrKry],
                                      fetchImmediately: true,
                                      completion: { err in
        })
        
        arbiter.destroy()
    }
    
    public func create(roomInfo: AUIRoomInfo, completion:@escaping (NSError?)->()) {
        guard let rtmToken = AUIRoomContext.shared.roomConfigMap[roomInfo.roomId]?.rtmToken else {
            assert(false)
            return
        }
        self.roomInfo = roomInfo
        guard rtmManager.isLogin else {
            let date = Date()
            rtmManager.login(token: rtmToken) {[weak self] err in
                aui_info("[Benchmark]rtm login: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
                if let err = err {
                    completion(err as NSError)
                    return
                }
                self?.create(roomInfo: roomInfo, completion: completion)
            }
            return
        }
        
        AUIRoomContext.shared.getArbiter(channelName: roomInfo.roomId)?.create()
        initRoom(roomInfo: roomInfo) {[weak self] err in
            if let err = err {
                completion(err)
                return
            }
            self?.enter(completion: { _, err in
                completion(err)
            })
        }
    }
    
    public func enter(completion:@escaping (AUIRoomInfo?, NSError?)->()) {
        let roomId = channelName!
        guard let config = AUIRoomContext.shared.roomConfigMap[roomId], config.rtmToken.count > 0 else {
            assert(false)
            aui_info("enterRoom: \(roomId) fail", tag: kSertviceTag)
            completion(nil, AUICommonError.missmatchRoomConfig.toNSError())
            return
        }
        
        guard rtmManager.isLogin else {
            let date = Date()
            rtmManager.login(token: config.rtmToken) {[weak self] err in
                aui_info("[Benchmark]rtm login: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
                if let err = err {
                    completion(nil, err)
                    return
                }
                self?.enter(completion: completion)
            }
            return
        }
        
        aui_info("enterRoom subscribe: \(roomId)", tag: kSertviceTag)
        let date = Date()
        self.enterRoomCompletion = { roomInfo in
            aui_info("[Benchmark]enterRoomCompletion: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
            completion(roomInfo, nil)
        }
        
        if self.roomInfo == nil {
            rtmManager.getMetadata(channelName: roomId) { err, metadata in
                guard let value = metadata?[kRoomInfoAttrKry], let roomInfo = AUIRoomInfo.yy_model(withJSON: value) else {
                    self.roomInfo = AUIRoomInfo()
                    return
                }
                
                self.roomInfo = roomInfo
            }
        }
        subscribeDate = Date()
        //TODO: create有bad case导致锁没有创建
        AUIRoomContext.shared.getArbiter(channelName: roomId)?.create()
        AUIRoomContext.shared.getArbiter(channelName: roomId)?.acquire()
        rtmManager.subscribeError(channelName: roomId, delegate: self)
        rtmManager.subscribeLock(channelName: roomId, lockName: kRTM_Referee_LockName, delegate: self)
        rtmManager.subscribe(channelName: roomId) {[weak self] error in
            guard let self = self else { return }
            if let error = error {
                completion(nil, error)
                return
            }
            self.subscribeSuccess = true
            aui_info("[Benchmark]rtm manager subscribe: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
            aui_info("enterRoom subscribe finished \(roomId) \(error?.localizedDescription ?? "")", tag: kSertviceTag)
        }
        
        //join rtc
        joinRtcChannel { error in
            aui_info("joinRtcChannel finished: \(error?.localizedDescription ?? "success")", tag: kSertviceTag)
        }
    }
    
    public func exit(callback: @escaping (NSError?) -> ()) {
        let roomId = channelName!
        aui_info("exitRoom: \(roomId)", tag: kSertviceTag)
        cleanUserInfo(channelName: roomId, userId: AUIRoomContext.shared.currentUserInfo.userId)
        cleanSDK()
        callback(nil)
    }
    
    public func destroy(callback: @escaping (NSError?) -> ()) {
        let roomId = channelName!
        aui_info("destroyRoom: \(roomId)", tag: kSertviceTag)
        cleanRoomInfo(channelName: roomId)
        cleanSDK()
    }
    
    private func initRoom(roomInfo: AUIRoomInfo, completion:@escaping (NSError?)->()) {
        guard let roomInfoStr = roomInfo.yy_modelToJSONString() else {
            assert(false)
            completion(nil)
            return
        }
        
        _ = micSeatImpl.initService?(completion: { err in
        })
        
        let date = Date()
        rtmManager.setBatchMetadata(channelName: roomInfo.roomId,
                                    lockName: "",
                                    metadata: [kRoomInfoAttrKry: roomInfoStr],
                                    fetchImmediately: true) { err in
            aui_info("[Benchmark]rtm setMetaData: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSertviceTag)
            if let err = err {
                completion(err)
                return
            }
            completion(nil)
        }
    }
    
    private func cleanSDK() {
        let roomId = channelName!
        self.rtmManager.unSubscribe(channelName: roomId)
        rtmManager.unsubscribeError(channelName: roomId, delegate: self)
        rtmManager.unsubscribeLock(channelName: roomId, lockName: kRTM_Referee_LockName, delegate: self)
        rtmManager.logout()
        leaveRtcChannel()
    }
    
    private func notifyBeKicked() {
        for obj in self.respDelegates.allObjects {
            obj.onRoomUserBeKicked?(roomId: channelName, userId: AUIRoomContext.shared.currentUserInfo.userId)
        }
    }
}

extension AUIVoiceChatRoomService: AUIUserRespDelegate {    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.userSnapshotList = userList
        
        guard let user = userList.filter({$0.userId == AUIRoomContext.shared.currentUserInfo.userId }).first else {return}
        aui_info("onRoomUserSnapshot", tag: kSertviceTag)
        onUserAudioMute(userId: user.userId, mute: user.muteAudio)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        guard AUIRoomContext.shared.isRoomOwner(channelName: roomId, userId: userInfo.userId) else {
            cleanUserInfo(channelName: roomId, userId: userInfo.userId)
            return
        }
        cleanRoomInfo(channelName: roomId)
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
        aui_info("onUserAudioMute mute current user: \(mute)", tag: kSertviceTag)
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

extension AUIVoiceChatRoomService: AUIRtmLockProxyDelegate {
    public func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        aui_benchmark("onReceiveLockDetail", cost: -(subscribeDate?.timeIntervalSinceNow ?? 0), tag: kSertviceTag)
        self.lockRetrived = true
    }
    
    public func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
    }
}

extension AUIVoiceChatRoomService: AUIRtmErrorProxyDelegate {
    public func onTokenPrivilegeWillExpire(channelName: String?) {
        aui_info("onTokenPrivilegeWillExpire: \(channelName ?? "")", tag: kSertviceTag)
        for obj in self.respDelegates.allObjects {
            obj.onTokenPrivilegeWillExpire?(roomId: channelName)
        }
    }
    
    public func bindRespDelegate(delegate: AUIVoiceChatRoomServiceRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIVoiceChatRoomServiceRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    @objc public func onMsgRecvEmpty(channelName: String) {
        self.respDelegates.allObjects.forEach { obj in
            obj.onRoomDestroy?(roomId: channelName)
        }
    }
    
    @objc public func onConnectionStateChanged(channelName: String,
                                               connectionStateChanged state: AgoraRtmClientConnectionState,
                                               result reason: AgoraRtmClientConnectionChangeReason) {
        if reason == .changedRejoinSuccess {
            AUIRoomContext.shared.getArbiter(channelName: channelName)?.acquire()
        }
        guard state == .failed, reason == .changedBannedByServer, channelName == self.channelName else { return }
        
        notifyBeKicked()
    }
}
