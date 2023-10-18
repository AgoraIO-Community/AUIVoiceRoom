# AScenesKit

*English | [中文](README.zh.md)*

##  概述
AScenesKit是基于AUIKit提供的UI和Service整合成的K歌房容器组件，为开发者提供快速搭建K歌场景的方案。

### 目录结构
```
AScenesKit  
├─ Binder                       // Binder
│  ├─ AUIUserViewBinder         // 用户管理Binder
│  ├─ AUIMicSeatViewBinder      // 麦位管理Binder  
│  ├─ AUIInvitationViewBinder   // 邀请管理Binder  
│  ├─ AUIIMViewBinder           // 聊天管理Binder
|  └─ AUIRoomGiftBinder         // 礼物管理Binder
│
└─ RoomContainer                // K歌整合容器
   ├─ AUIVoiceChatRoomView        // K歌容器View，负责组件的创建、拼装和绑定   
   └─ AUIVoiceChatRoomService     // K歌容器Service，负责基础service的创建
```
###  组件和Service依赖的关系
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/VoiceRoom_zh.png)

### Binder与View和Service之间调度流程

![](https://fullapp.oss-cn-beijing.aliyuncs.com/pic/pako_eNo9UD1vwjAQ_SunmwMqbaDEQyUCKxNVh2IGK76ApcROHbuUxvnvNQ7qTad7H_f0BqyMJGRYN-ZaXYR18L7jGuJsjh-KrqXSkuwJZrO34DspHIFXAcoEniZmeUcheAWicsroAJuHRZL1pCVY-vLUuwDb44Hst6roId5OYkt9B5IaOscX0QAww5ZsK5SM6YY7l6O7UEscWVwl1cI3jiPXY6QK78zhpitktWh6ynDKulP.png)

## 快速集成

### 1. 添加源码

**将以下源码复制到自己项目里：**

- [AScenesKit](../AScenesKit)
- [KeyCenter.swift](../Example/AUIVoiceRoom/KeyCenter.swift)

**在Podfile文件里添加依赖AScenesKit(例如AScenesKit放置在Podfile同一级目录下时)**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUIKitCore'
```

**把KeyCenter.swift拖进工程里**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_keycenter_ios.png) 

**在Info.plist里配置麦克风和摄像头权限**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)

### 2.创建房间管理对象
```swift
let commonConfig = AUICommonConfig()
commonConfig.host = KeyCenter.HostUrl
commonConfig.userId = userInfo.userId  
commonConfig.userName = userInfo.userName
commonConfig.userAvatar = userInfo.userAvatar
//rtmClient可空
let roomManager = AUIRoomManagerImpl(commonConfig: roomConfig, rtmClient: rtmClient)
```

### 3.拉取房间列表
```swift
roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize, callback: callback)
```

### 4.创建房间
```swift
let room = AUICreateRoomInfo()
room.roomName = text
room.thumbnail = userInfo.userAvatar
room.seatCount = 8
roomManager.createRoom(room: roomInfo) { error, info in
}
```

### 5.进入房间
####  5.1根据房间id获取token和appid
```swift
let tokenModel1 = AUITokenGenerateNetworkModel()
tokenModel1.channelName = channelName
tokenModel1.userId = uid
tokenModel1.request { error, result in
  guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
  let rtcToken007 = tokenMap["rtcToken"] ?? ""
  let rtmToken007 = tokenMap["rtmToken"] ?? ""
  let appId = tokenMap["appId"] ?? ""
}
```
#### 5.2设置appId
在获取token时从接口拿到appid并设置进UIKit中，如果没有appId会造成房间创建异常
```swift
AUIRoomContext.shared.commonConfig?.appId = appId
```
####  5.3生成房间容器
```swift
//创建房间容器
let voiceChatView = AUIVoiceChatRoomView(frame: self.view.bounds)  
self.view.addSubview(voiceChatView)
voiceChatView.onClickOffButton = { [weak self] in
    //点击退出按钮后
}

//根据5.1创建房间token配置传入AUIRoomConfig
let roomConfig = AUIRoomConfig()
...

//rtcEngine和ktvApi可空
let service = AUIVoiceChatRoomService(rtcEngine: rtcEngine,
                                      roomManager: roomManager,
                                      roomConfig: roomConfig,
                                      roomInfo: roomInfo)
voiceChatView.bindService(service: service)
```
经过上述五步，即可创建一个语聊场景的房间

## 6.订阅房间变化
通过AUIRoomManagerImpl的bindRespDelegate方法订阅变化
```swift
roomManager.bindRespDelegate(delegate: delegate)
```
然后在回调AUIRoomManagerRespDelegate里实现方法
```swift
func onRoomDestroy(roomId: String) {
    //收到房间销毁消息 
}
```

## 7.订阅异常处理
通过AUIRoomManagerImpl的subscribeError方法订阅变化
```swift
roomManager.rtmManager.subscribeError(channelName: roomId, delegate: delegate)
```
然后在回调AUIRtmErrorProxyDelegate里实现方法
```swift
@objc func onTokenPrivilegeWillExpire(channelName: String?) {
    //收到token过期
}

@objc func onMsgRecvEmpty(channelName: String) {
    //收到房间数据为空，目前只有房间被销毁之后被清理
}

@objc func onConnectionStateChanged(channelName: String,
                                    connectionStateChanged state: AgoraRtmClientConnectionState,
                                    result reason: AgoraRtmClientConnectionChangeReason) {
    //收到状态变化
}
```