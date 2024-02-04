//
//  AUIUserCellUserData.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/11.
//

import AUIKitCore

class AUIUserCellUserData: AUIUserThumbnailInfo, AUIUserCellUserDataProtocol {
    var seatIndex: Int = -1
}

extension AUIUserThumbnailInfo {
    func createData(_ seatIndex: Int) -> AUIUserCellUserDataProtocol {
        let userInfo = AUIUserCellUserData()
        userInfo.userAvatar = userAvatar
        userInfo.userId = userId
        userInfo.userName = userName
        userInfo.seatIndex = seatIndex
        return userInfo
    }
}
