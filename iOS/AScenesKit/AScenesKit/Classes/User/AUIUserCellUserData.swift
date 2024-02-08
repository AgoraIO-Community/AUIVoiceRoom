//
//  AUIUserCellUserData.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/11.
//

import AUIKitCore

class AUIUserCellUserData: AUIUserThumbnailInfo, AUIUserCellUserDataProtocol {
    var seatIndex: Int = -1
    var isOwner: Bool = false
}

extension AUIUserThumbnailInfo {
    func createData(_ seatIndex: Int, _ isOwner: Bool = false) -> AUIUserCellUserDataProtocol {
        let userData = AUIUserCellUserData()
        userData.userAvatar = userAvatar
        userData.userId = userId
        userData.userName = userName
        userData.seatIndex = seatIndex
        userData.isOwner = isOwner
        return userData
    }
    
     static func createUser(data: AUIUserCellUserDataProtocol) -> AUIUserThumbnailInfo {
         let userInfo = AUIUserThumbnailInfo()
         userInfo.userId = data.userId
         userInfo.userAvatar = data.userAvatar
         userInfo.userName = data.userName
         return userInfo
    }
}
