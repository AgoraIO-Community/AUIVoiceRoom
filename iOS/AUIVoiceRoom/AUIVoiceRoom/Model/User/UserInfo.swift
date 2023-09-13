//
//  UserInfo.swift
//  AScenesKit_Example
//
//  Created by wushengtao on 2023/4/18.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation

private let kUserId = "userId"
private let kUserName = "userName"
private let kUserAvatar = "userAvatar"

private let userNames = [
    "安迪",
    "路易",
    "汤姆",
    "杰瑞",
    "杰森",
    "布朗",
    "吉姆",
    "露西",
    "莉莉",
    "韩梅梅",
    "李雷",
    "张三",
    "李四",
    "小红",
    "小明",
    "小刚",
    "小霞",
    "小智",
    ]

private let userAvatars = [
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_2.png",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_3.png",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_4.png",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_5.png",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_6.png",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_7.png"
]

class UserInfo: NSObject {
    public var userId: String {
        get {
            var uid = UserDefaults.standard.string(forKey: kUserId)
            if let uid = uid, uid.count > 0 {
                return uid
            }
            uid = "\(arc4random_uniform(999999999))"
            UserDefaults.standard.set(uid, forKey: kUserId)
            return uid!
        } set {
            UserDefaults.standard.set(newValue, forKey: kUserId)
        }
    }
    
    public var userAvatar: String {
        get {
            var uAvatar = UserDefaults.standard.string(forKey: kUserAvatar)
            if let uAvatar = uAvatar, uAvatar.count > 0 {
                return uAvatar
            }
            
            let idx = arc4random_uniform(UInt32(userAvatars.count - 1))
            uAvatar = userAvatars[Int(idx)]
            UserDefaults.standard.set(uAvatar, forKey: kUserAvatar)
            return uAvatar!
        } set {
            UserDefaults.standard.set(newValue, forKey: kUserAvatar)
        }
    }
    
    public var userName: String {
        get {
            var uName = UserDefaults.standard.string(forKey: kUserName)
            if let uName = uName, uName.count > 0 {
                return uName
            }
            
            let idx = arc4random_uniform(UInt32(userNames.count - 1))
            uName = userNames[Int(idx)]
            UserDefaults.standard.set(uName, forKey: kUserName)
            return uName!
        } set {
            UserDefaults.standard.set(newValue, forKey: kUserName)
        }
    }
}
