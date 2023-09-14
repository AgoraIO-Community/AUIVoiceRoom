# VoiceRoomUIKit

*English | [英文](VoiceRoomUIKit.md)*

VoiceRoomUIKit 是一个语聊房场景组件，提供房间管理和拉起语聊房场景页面的能力。 开发者可以使用该组件快速构建一个语聊房应用。

## Quick Started
> 在集成之前，请确保您已根据此[教程](../Example/AUIVoiceRoom/README.md) 成功运行项目。

### 1. Add Source Code

**将以下源码复制到自己的项目中：**

- [AUIKit](../AUIKit)
- [AScenesKit](../AScenesKit)
- [VoiceRoomUIKit.swift](../AUIVoiceRoom/iOS/AUIVoiceRoom/VoiceRoomUIKit.swift)
- [KeyCenter.swift](../AUIVoiceRoom/AUIVoiceRoom/KeyCenter.swift)

**在Podfile文件中添加对AScenesKit和AUIKit的依赖（比如AUIKit和AScenesKit与Podfile放在同级目录下）**

```
  pod 'AScenesKit', :path => './AScenesKit'
  pod 'AUIKit', :path => './AUIKit'
```

**将 VoiceRoomUIKit.swift 拖到项目中**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_keycenter_ios.png)

**配置iOS系统麦克风权限**

![](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_app_privacy_ios.png)


### 2. Initialize VoiceRoomUIKit
```swift
//为VoiceRoomUIKit设置基本信息
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

### 3.获取房间列表
```swift
VoiceRoomUIKit.shared.getRoomInfoList(lastCreateTime: nil,
                                    pageSize: kListCountPerPage,
                                    callback: { error, list in
    //更新UI
})
```

### 4.创建房间
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

### 5. 加载房间
```swift
let uid = VoiceRoomUIKit.shared.roomConfig?.userId ?? ""
//创建房间容器视图
let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds,roomInfo: info)
//通过generateToken方法获取必要的token和appid
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

### 6. 退出房间
#### 6.1 Proactively exiting
```swift
//AUIVoiceChatRoomView 提供一个关闭的闭包
voiceRoomView.onClickOffButton = { [weak self] in
    self.navigationController?.popViewController(animated: true)
    VoiceRoomUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "")
}
```

#### 6.2 房间销毁与自动退出
Please refer to [Room Destruction] (# 7.2-Room-Destruction)


### 7. 异常处理
#### 7.1 Token过期处理
```swift
//订阅 VoiceRoomUIKit.shared.launchRoom 后 AUIRtmErrorProxyDelegate 的回调
VoiceRoomUIKit.shared.subscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//退出房间时取消订阅
VoiceRoomUIKit.shared.unsubscribeError(roomId: self.roomInfo?.roomId ?? "", delegate: self)

//然后使用AUIRtmErrorProxyDelegate回调中的onTokenPrivilegeWillExpire回调方法更新所有token
@objc func onTokenPrivilegeWillExpire(channelName: String?) {
    generatorToken { config, _ in
        VoiceRoomUIKit.shared.renew(config: config)
    }
}
```

#### 7.2 Room destruction
```swift
//订阅 VoiceRoomUIKit 后 AUIRoomManagerRespDelegate 的回调。 共享。 发射室
VoiceRoomUIKit.shared.bindRespDelegate(delegate: self)

//退出房间时取消订阅
VoiceRoomUIKit.shared.unbindRespDelegate(delegate: self)

//通过AUIRoomManagerRespDelegate回调方法中的onRoomDestroy处理房间销毁
func onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}
//用户被踢出房间的回调
 func onRoomUserBeKicked(roomId: String, userId: String) {
        AUIToast.show(text: "You were kicked out!")
        self.navigationController?.popViewController(animated: true)
 }
```

### 8.更换皮肤
- AUIKit支持一键换肤，可以通过以下方法设置皮肤
```swift
//重新设置默认皮肤
AUIRoomContext.shared.resetTheme()
```
```swift
//切换到下一个主题皮肤
AUIRoomContext.shared.switchThemeToNext()
```

```swift
//指定特殊皮肤
AUIRoomContext.shared.switchTheme(themeName: "UIKit")
```
- 您还可以通过修改 [configuration file](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/theme) or replacing the [resource file](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/resource)
- 更多换皮问题请参考 [Skin Settings](./VoiceRoomTheme.md)

## License
版权所有 © Agora Corporation。 版权所有。
根据 [MIT 许可证](../LICENSE) 获得许可。

