# VoiceRoomUIKit

*English | [中文](VoiceRoomUIKit_zh.md)*

VoiceRoomUIKit is a voice chat room scene component, which provides room management and the ability to pull up the voice chat room scene page. Developers can use this component to quickly build a chat room application.


## Quick Started
> Please make sure you have successfully run the project according to this [tutorial](../Example/AUIVoiceRoom/README.md) before integrating.。

### 1. Add Source Code

**Copy the following source code into your own project：**

- [AUIKit](../AUIKit)
- [AScenesKit](../AScenesKit)
- [VoiceRoomUIKit.swift](../AUIVoiceRoom/iOS/AUIVoiceRoom/VoiceRoomUIKit.swift)
- [KeyCenter.swift](../AUIVoiceRoom/AUIVoiceRoom/KeyCenter.swift)

**Add dependencies on AScenesKit and AUIKit in the Podfile file (for example, when AUIKit and AScenesKit are placed in the same level directory as the Podfile)**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUIKit', :path => './AUIKit'
```

**Drag VoiceRoomUIKit.swift into the project**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_keycenter_ios.png)

**Configure microphone and camera permissions**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)


### 2. Initialize VoiceRoomUIKit
```swift
//Set basic information to VoiceRoomUIKit
let commonConfig = AUICommonConfig()
commonConfig.host = KeyCenter.HostUrl
commonConfig.userId = userInfo.userId
commonConfig.userName = userInfo.userName
commonConfig.userAvatar = userInfo.userAvatar
VoiceRoomUIKit.shared.setup(roomConfig: commonConfig,
                          ktvApi: nil,      //If there is an externally initialized KTV API
                          rtcEngine: nil,   //If there is an externally initialized rtc engine
                          rtmClient: nil)   //If there is an externally initialized rtm client
```

### 3. Get room list
```swift
VoiceRoomUIKit.shared.getRoomInfoList(lastCreateTime: nil,
                                    pageSize: kListCountPerPage,
                                    callback: { error, list in
    //Update UI
})
```

### 4. Create room
```swift
                let room = AUICreateRoomInfo()
                room.roomName = text
                room.thumbnail = self.userInfo.userAvatar
                room.micSeatCount = UInt(AUIRoomContext.shared.seatCount)
                room.micSeatStyle = UInt(AUIRoomContext.shared.seatType.rawValue)
                VoiceChatUIKit.shared.createRoom(roomInfo: room) { roomInfo in
                    let vc = RoomViewController()
                    roomInfo?.micSeatCount = UInt(AUIRoomContext.shared.seatCount)
                    roomInfo?.micSeatStyle = UInt(AUIRoomContext.shared.seatType.rawValue)
                    vc.roomInfo = roomInfo
                    self.navigationController?.pushViewController(vc, animated: true)
                } failure: { error in
                    AUIToast.show(text: error.localizedDescription)
                }
```

### 5. Launch room
```swift
let uid = VoiceRoomUIKit.shared.roomConfig?.userId ?? ""
//Creating Room Containers
let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds,roomInfo: info)
//Obtain the necessary token and appid through the generateToken method
generateToken {[weak self] roomConfig, appId in
        guard let self = self else {return}
        VoiceChatUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                                           appId: appId,
                                           config: roomConfig,
                                           roomView: voiceRoomView) {_ in
        }
        //订阅Token过期回调
        VoiceChatUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)
        //订阅房间被销毁回调
        VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
}
```

### 6. Exit the room
#### 6.1 Proactively exiting
```swift
//AUIVoiceChatRoomView provides a closure for onClickOffButton
voiceRoomView.onClickOffButton = { [weak self] in
    self.navigationController?.popViewController(animated: true)
    VoiceRoomUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "")
}
```

#### 6.2 Room destruction passive exit
Please refer to [Room Destruction] (# 7.2-Room-Destruction)


### 7. Exception handling
#### 7.1 Token expiration processing
```swift
//Subscribe to the callback for AUIRtmErrorProxyDelegate after VoiceRoomUIKit.shared.launchRoom
VoiceRoomUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//Unsubscribe when exiting the room
VoiceRoomUIKit.shared.unsubscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//Then use the onTokenPrivilegeWillExpire callback method in the AUIRtmErrorProxyDelegate callback to renew all tokens
@objc func onTokenPrivilegeWillExpire(channelName: String?) {
    generatorToken { config, _ in
        VoiceRoomUIKit.shared.renew(config: config)
    }
}
```

#### 7.2 Room destruction
```swift
//Subscribe to the callback for AUIRoomManagerRespDelegate after VoiceRoomUIKit. shared. launchRoom
VoiceRoomUIKit.shared.bindRespDelegate(delegate: self)

//Unsubscribe when exiting the room
VoiceRoomUIKit.shared.unbindRespDelegate(delegate: self)

//Process room destruction through onRoomDestroy in the AUIRoomManagerRespDelegate callback method
func onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}

 func onRoomUserBeKicked(roomId: String, userId: String) {
        AUIToast.show(text: "You were kicked out!")
        self.navigationController?.popViewController(animated: true)
 }
```

### 8 Skin changing
- AUIKit supports one click skin changing, and you can set the skin using the following methods
```swift
//Reset to default theme
AUIRoomContext.shared.resetTheme()
```
```swift
//Switch to the next theme
AUIRoomContext.shared.switchThemeToNext()
```

```swift
//Specify a theme
AUIRoomContext.shared.switchTheme(themeName: "UIKit")
```
- You can also change the skin of the component by modifying the [configuration file](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/theme) or replacing the [resource file](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/resource)
- For more skin changing issues, please refer to [Skin Settings](./VoiceRoomTheme.md)

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).

