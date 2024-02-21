//
//  AUIVoiceChatRoomServiceRespDelegate.swift
//  AScenesKit
//
//  Created by wushengtao on 2024/2/2.
//

import Foundation
import AUIKitCore

/// 房间操作对应的响应
@objc public protocol AUIVoiceChatRoomServiceRespDelegate: NSObjectProtocol {
    
    /// 房间即将过期
    /// - Parameter roomId: 房间id
    @objc optional func onTokenPrivilegeWillExpire(roomId: String?)

    /// 房间被销毁的回调
    /// - Parameter roomId: 房间id
    @objc optional func onRoomDestroy(roomId: String)
    
    /// 房间信息变更回调
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - roomInfo: 房间信息
    @objc optional func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo)
    
    /// Description 房间用户被踢出房间
    ///
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userId: 用户id
    @objc optional func onRoomUserBeKicked(roomId: String,userId: String)
}
