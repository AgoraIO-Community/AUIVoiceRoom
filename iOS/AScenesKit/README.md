# ASceneKit-iOS

*English | [中文](README_zh.md)*
##  Overview
AScenesKit is a container component for voicechat rooms that integrates UI and services provided by AUIKit, providing developers with a solution to quickly build voicechat scenes.

### Directory
```
AScenesKit  
├─ Binder                          // Binder
│  ├─ AUIUserViewBinder            // Binder for user management
│  ├─ AUIMicSeatViewBinder         // Binder  for mic seat management
│  ├─ AUIInvitationViewBinder      // Binder for invitation management 
│  ├─ AUIIMViewBinder              // Binder for chat management
|  └─ AUIRoomGiftBinder            // Binder for gift management
│
└─ RoomContainer                   // VoiceRoom integration container
   ├─ AUIVoiceChatRoomView         // VoiceRoom View, responsible for creating, assembling, and binding components 
   └─ AUIVoiceChatRoomService      // VoiceRoom Service, responsible for creating basic services
```
###  Relationship between component and service dependencies
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/VoiceRoom_en.png)

### Scheduling process between Binder and View and Service

![](https://fullapp.oss-cn-beijing.aliyuncs.com/pic/pako_eNo9UD1vwjAQ_SunmwMqbaDEQyUCKxNVh2IGK76ApcROHbuUxvnvNQ7qTad7H_f0BqyMJGRYN-ZaXYR18L7jGuJsjh-KrqXSkuwJZrO34DspHIFXAcoEniZmeUcheAWicsroAJuHRZL1pCVY-vLUuwDb44Hst6roId5OYkt9B5IaOscX0QAww5ZsK5SM6YY7l6O7UEscWVwl1cI3jiPXY6QK78zhpitktWh6ynDKulP.png)

## Quick Started
> Please make sure you have successfully run the project according to this [tutorial](../Example/AUIVoiceRoom/README.md) before integrating。

### 1. Add Source Code

**Copy the following source code into your own project：**

- [AScenesKit](../AScenesKit)
- [KeyCenter.swift](../Example/AUIVoiceRoom/AUIVoiceRoom/KeyCenter.swift)

**Add dependencies on AScenesKit in the Podfile file (for example, when AScenesKit are placed in the same level directory as the Podfile)**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUIKitCore'
```

**Drag VoiceChatUIKit.swift into the project**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_keycenter_ios.png) 

**Configure microphone and camera permissions**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)

### 2.Creating room management objects
```swift
let commonConfig = AUICommonConfig()
commonConfig.host = KeyCenter.HostUrl
commonConfig.userId = userInfo.userId  
commonConfig.userName = userInfo.userName
commonConfig.userAvatar = userInfo.userAvatar
//rtmClient can be empty
let roomManager = AUIRoomManagerImpl(commonConfig: roomConfig, rtmClient: rtmClient)
```

### 3.Get room list
```swift
roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize, callback: callback)
```

### 4.Create room
```swift
let room = AUICreateRoomInfo()
room.roomName = text
room.thumbnail = userInfo.userAvatar
room.seatCount = 8
roomManager.createRoom(room: roomInfo) { error, info in
}
```

### 5.Enter room
####  5.1 Obtain token and appid based on room id
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
#### 5.2 Setup appId
When obtaining a token, obtain the appID from the interface and set it in UIKit. If there is no appID, it will cause room creation exceptions.
```swift
AUIRoomContext.shared.commonConfig?.appId = appId
```
####  5.3 Create room view
```swift
//create voicechat room view
let voiceChatView = AUIVoiceChatRoomView(frame: self.view.bounds)  
self.view.addSubview(voiceChatView)
voiceChatView.onClickOffButton = { [weak self] in
    //click exit button
}

//create a room token configuration according to 5.1 and pass it in to AUIRoomConfig
let roomConfig = AUIRoomConfig()
...

//rtcEngine and ktvApi can be empty
let service = AUIVoiceChatRoomService(rtcEngine: rtcEngine,
                                    roomManager: roomManager,
                                    roomConfig: roomConfig,
                                    roomInfo: roomInfo)
voiceChatView.bindService(service: service)
```
After the above five steps, you can create a room with a voicechat scene.

## 6.Subscription room changes
Subscribe to changes through the bindRespDelegate method of AUIRoomManagerImpl
```swift
roomManager.bindRespDelegate(delegate: delegate)
```
Then implement the method in the callback AUiRoomManagerRespDelegate
```swift
func onRoomDestroy(roomId: String) {
    //Received message that the room has been destroyed
}
```

## 7.Subscription exception handling
Subscribe to changes through the subscribeError method of AUIRoomManagerImpl
```swift
roomManager.rtmManager.subscribeError(channelName: roomId, delegate: delegate)
```
Then implement the method in the callback AUiRtmErrorProxyDelegate
```swift
@objc func onTokenPrivilegeWillExpire(channelName: String?) {
    //Received token expired
}

@objc func onMsgRecvEmpty(channelName: String) {
    //Received room data is empty, currently only the room has been destroyed and cleared
}

@objc func onConnectionStateChanged(channelName: String,
                                    connectionStateChanged state: AgoraRtmClientConnectionState,
                                    result reason: AgoraRtmClientConnectionChangeReason) {
    //Received status change
}
```
