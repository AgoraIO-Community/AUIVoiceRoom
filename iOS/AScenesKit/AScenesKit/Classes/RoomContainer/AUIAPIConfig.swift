//
//  AUIAPIConfig.swift
//  AScenesKit
//
//  Created by wushengtao on 2023/11/15.
//

import Foundation
import AUIKitCore
import AgoraRtcKit
import AgoraRtmKit

@objcMembers
public class AUIAPIConfig: NSObject {
    var rtcEngine: AgoraRtcEngineKit? = nil
    var ktvApi: KTVApiDelegate? = nil
    var rtmClient: AgoraRtmClientKit? = nil
}
