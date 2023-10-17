# VoiceRoomUIKit

*English | [英文](VoiceRoomUIKit.md)*

VoiceChatUIKit 是一个语聊房场景组件，提供房间管理和拉起语聊房场景页面的能力。 开发者可以使用该组件快速构建一个语聊房应用。

## 快速集成
### 1. 添加源码

**将以下源码复制到自己的项目中：**

- [AScenesKit](../AScenesKit)
- [KeyCenter.swift](../AUIVoiceRoom/AUIVoiceRoom/KeyCenter.swift)

**在Podfile文件中添加对AScenesKit的依赖（例如AScenesKit与Podfile放在同级目录下时）**

```
  pod 'AScenesKit', :path => './AScenesKit'
```

**将 KeyCenter.swift 拖到项目中**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/add_keycenter_to_voiceroom.jpg)

**配置iOS系统麦克风权限**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/WeChatWorkScreenshot_c9c309c0-731c-4964-8ef3-1e60ab6b9241.png)


### 2. 初始化VoiceRoomUIKit
```swift
//为VoiceRoomUIKit设置基本信息
let commonConfig = AUICommonConfig()
commonConfig.host = KeyCenter.HostUrl
commonConfig.userId = userInfo.userId
commonConfig.userName = userInfo.userName
commonConfig.userAvatar = userInfo.userAvatar
VoiceRoomUIKit.shared.setup(roomConfig: commonConfig,
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
    room.roomName = "room name"
    room.thumbnail = self.userInfo.userAvatar
    room.micSeatCount = UInt(AUIRoomContext.shared.seatCount)
    room.micSeatStyle = UInt(AUIRoomContext.shared.seatType.rawValue)
    VoiceChatUIKit.shared.createRoom(roomInfo: room) { roomInfo in
        let vc = RoomViewController()
        vc.roomInfo = roomInfo
        self.navigationController?.pushViewController(vc, animated: true)
    } failure: { error in
        //错误提示
    }
```

### 5. 加载房间
```swift
//创建房间容器视图
let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds,roomInfo: info)
voiceRoomView.onClickOffButton = { [weak self] in
  //退出房间的回调
}

VoiceChatUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                                 roomView: voiceRoomView) {[weak self] error in
    guard let self = self else {return}
    if let _ = error { return }
    //订阅房间被销毁回调
    VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
}
```

### 6. 退出房间
#### 6.1 主动退出
```swift
//AUIVoiceChatRoomView 提供一个关闭的闭包
voiceRoomView.onClickOffButton = { [weak self] in
    self.navigationController?.popViewController(animated: true)
    VoiceRoomUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "")
}
```

#### 6.2 房间销毁与自动退出
详见[房间销毁](#71-房间销毁)

### 7. 异常处理

#### 7.1 房间销毁
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
- 您还可以通过修改 [theme](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/theme)或替换[resource](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/resource)更新皮肤资源
- 更多换皮问题请参考 [皮肤设置](./VoiceRoomTheme_zh.md)

# API参考
## setup
初始化
```swift
func setup(roomConfig: AUICommonConfig,
           rtcEngine: AgoraRtcEngineKit? = nil,
           rtmClient: AgoraRtmClientKit? = nil) 
```

| 参数        | 类型            | 含义                                                         |
| ----------- | --------------- | ------------------------------------------------------------ |
| config      | AUICommonConfig | 通用配置，包含用户信息和域名等                              |
| rtcEngineEx | AgoraRtcEngineKit     | （可选）声网RTC引擎。当项目里已集成Agora RTC可以传入，否则传空由内部自动创建。 |
| rtmClient   | AgoraRtmClientKit       | （可选）声网RTM引擎。当项目里已集成Agora RTM可以传入，否则传空由内部自动创建。 |

## createRoom
创建房间

```swift
func createRoom(roomInfo: AUICreateRoomInfo,
                success: ((AUIRoomInfo?)->())?,
                failure: ((Error)->())?)
```


参数如下表所示：

| 参数           | 类型              | 含义                             |
| -------------- | ----------------- | -------------------------------- |
| roomInfo | AUICreateRoomInfo | 创建房间所需的信息               |
| success        | Closure          | 成功回调，成功会返回一个房间信息 |
| failure        | Closure          | 失败回调                         |



### getRoomInfoList

获取房间列表

```swift
func getRoomInfoList(lastCreateTime: Int64?, 
                     pageSize: Int, 
                     callback: @escaping AUIRoomListCallback)
```

参数如下表所示：

| 参数      | 类型     | 含义                                 |
| --------- | -------- | ------------------------------------ |
| lastCreateTime | Int64     | 起始时间，与1970-01-01:00:00:00的差值，单位：毫秒，例如:1681879844085                         |
| pageSize  | Int      | 页数                                 |
| callback   | Closure | 完成回调 |

### launchRoom

```swift
func launchRoom(roomInfo: AUIRoomInfo,
                voiceChatView: AUIVoiceChatRoomView) 
```

参数如下表所示：

| 参数        | 类型            | 含义                                  |
| ----------- | --------------- | ------------------------------------- |
| roomInfo    | AUIRoomInfo     | 房间信息                              |
| voiceChatView | AUIVoiceChatRoomView | 房间UI View                           |
| completion | Closure | 加入房间完成回调                           |

### destroyRoom

销毁房间

```swift
func destoryRoom(roomId: String)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| roomId | String | 要销毁的房间ID |


## 数据模型

### AUICommonConfig

| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| host       | String  | 后端服务域名          |
| userId     | String  | 用户ID               |
| userName   | String  | 用户名               |
| userAvatar | String  | 用户头像             |

### AUIRoomInfo

| 参数        | 类型                 | 含义         |
| ----------- | -------------------- | ------------ |
| roomId      | String               | 房间id       |
| roomOwner   | AUIUserThumbnailInfo | 房主信息     |
| memberCount | Int                  | 房间人数     |
| createTime  | Int64                 | 房间创建时间，与1970-01-01:00:00:00的差值，单位：毫秒，例如:1681879844085 |

### AUIUserThumbnailInfo

| 参数       | 类型   | 含义     |
| ---------- | ------ | -------- |
| userId     | String | 用户Id   |
| userName   | String | 用户名   |
| userAvatar | String | 用户头像 |

### AUIRoomManagerRespDelegate
```AUIRoomManagerRespDelegate``` 协议用于处理与房间操作相关的各种响应事件。它提供了以下方法，可以由遵循此协议的类来实现，以响应特定的事件。

#### 方法
  - ```func onRoomDestroy(roomId: String)```
    房间被销毁时调用的回调方法。
    - 参数：
      - ```roomId```: 房间ID。
    >
  - ```func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo)```
    房间信息发生变更时调用的回调方法。
    - 参数：
      - ```roomId```:房间ID。
      - ```roomInfo```:房间信息。
    >
  - ```func onRoomAnnouncementChange(roomId: String, announcement: String)```
    房间公告发生变更时调用的方法。
    - 参数：
      - ```roomId```: 房间ID。
      - ```announcement```: 公告变更内容。
    >
- ```func onRoomUserBeKicked(roomId: String, userId: String)```
    房间用户被踢出房间时调用的方法。
    - 参数：
      - ```roomId```: 房间ID。
      - ```userId```: 用户ID。

## License
版权所有 © Agora Corporation。 版权所有。
根据 [MIT 许可证](../LICENSE) 获得许可。

