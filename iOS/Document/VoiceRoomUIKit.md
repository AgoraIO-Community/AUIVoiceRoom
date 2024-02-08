# VoiceRoomUIKit


VoiceChatUIKit 是一个语聊房场景组件，提供房间管理和拉起语聊房场景页面的能力。 开发者可以使用该组件快速构建一个语聊房应用。

## 快速集成

 > 在集成之前，请确保您已根据此[教程](../AUIVoiceRoom/) 成功运行项目。

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
commonConfig.appId = KeyCenter.AppId
commonConfig.appCert = KeyCenter.AppCertificate
commonConfig.basicAuth = KeyCenter.AppBasicAuth
commonConfig.imAppKey = KeyCenter.IMAppKey
commonConfig.imClientId = KeyCenter.IMClientId
commonConfig.imClientSecret = KeyCenter.IMClientSecret
commonConfig.host = KeyCenter.HostUrl
let ownerInfo = AUIUserThumbnailInfo()
ownerInfo.userId = userInfo.userId
ownerInfo.userName = userInfo.userName
ownerInfo.userAvatar = userInfo.userAvatar
commonConfig.owner = ownerInfo
VoiceChatUIKit.shared.setup(commonConfig: commonConfig,
                            apiConfig: nil)
```

### 3.获取房间列表
```swift
VoiceRoomUIKit.shared.getRoomInfoList(lastCreateTime: 0,
                                      pageSize: 20,
                                      callback: { error, list in
    //更新UI
    ...
})
```

### 4.房主创建并进入房间
```swift

    //生成token
    let roomConfig = AUIRoomConfig()
    ...

    //创建房间信息
    let room = AUICreateRoomInfo()
    room.roomName = "room name"
    room.thumbnail = self.userInfo.userAvatar
    room.micSeatCount = UInt(AUIRoomContext.shared.seatCount)
    room.micSeatStyle = UInt(AUIRoomContext.shared.seatType.rawValue)

    //创建房间容器
    let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds)
    voiceRoomView.onClickOffButton = { [weak self] in
      //退出房间的回调
    }

    //创建
    VoiceChatUIKit.shared.createRoom(roomInfo: room,
                                     roomConfig: roomConfig,
                                     chatView: voiceRoomView) { roomInfo in
        ...
    } failure: { error in
        //错误提示
    }


    // 订阅房间被销毁回调
    VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
```

### 5. 观众进入房间
```swift
//生成token
let roomConfig = AUIRoomConfig()
...

//创建房间容器视图
let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds,roomInfo: info)
voiceRoomView.onClickOffButton = { [weak self] in
  //退出房间的回调
}

//加入
VoiceChatUIKit.shared.enterRoom(roomId: roomId,
                                roomConfig: roomConfig,
                                chatView: voiceRoomView) {roomInfo, error in
    ...
}

// 订阅房间被销毁回调
VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
```

### 6. 退出房间
#### 6.1 主动退出
```swift
//AUIVoiceChatRoomView 提供一个关闭的闭包
voiceRoomView.onClickOffButton = { [weak self] in
    VoiceRoomUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "")

    ...
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
- 更多换皮问题请参考 [皮肤设置](./VoiceRoomTheme.md)

# API参考
## setup
初始化
```swift
func setup(commonConfig: AUICommonConfig,
           apiConfig: AUIAPIConfig? = nil)
```

| 参数        | 类型            | 含义                                                         |
| ----------- | --------------- | ------------------------------------------------------------ |
| commonConfig      | AUICommonConfig | 通用配置，包含用户信息和域名等                              |
| apiConfig | AUIAPIConfig     | （可选）声网相关SDK的引擎设置，为nil表示内部隐式创建该实例。 |


## createRoom
创建房间

```swift
func createRoom(roomInfo: AUIRoomInfo,
                roomConfig: AUIRoomConfig,
                chatView: AUIVoiceChatRoomView,
                completion: @escaping (NSError?) -> Void)
```


参数如下表所示：

| 参数           | 类型              | 含义                             |
| -------------- | ----------------- | -------------------------------- |
| roomInfo | AUICreateRoomInfo | 创建房间所需的信息               |
| roomConfig        | AUIRoomConfig          | 房间token配置 |
| chatView        | AUIVoiceChatRoomView          | 语聊房容器View                    |
| completion        | Closure          | 完成回调                         |



### getRoomInfoList

获取房间列表

```swift
func getRoomInfoList(lastCreateTime: Int64, 
                     pageSize: Int, 
                     callback: @escaping AUIRoomListCallback)
```

参数如下表所示：

| 参数      | 类型     | 含义                                 |
| --------- | -------- | ------------------------------------ |
| lastCreateTime | Int64     | 起始时间，与1970-01-01:00:00:00的差值，单位：毫秒，例如:1681879844085，默认为0，表示最新                         |
| pageSize  | Int      | 页数                                 |
| callback   | Closure | 完成回调 |

### enterRoom

```swift
func enterRoom(roomId: String,
               roomConfig: AUIRoomConfig,
               chatView: AUIVoiceChatRoomView,
               completion: @escaping (AUIRoomInfo?, NSError?) -> Void) 
```

参数如下表所示：

| 参数        | 类型            | 含义                                  |
| ----------- | --------------- | ------------------------------------- |
| roomId    | String     | 房间id                              |
| roomConfig        | AUIRoomConfig          | 房间token配置 |
| chatView | AUIVoiceChatRoomView | 语聊房容器View                          |
| completion | Closure | 加入房间完成回调                           |

### leaveRoom

离开房间

```swift
func leaveRoom(roomId: String)
```

参数如下表所示：

| 参数   | 类型   | 含义           |
| ------ | ------ | -------------- |
| roomId | String | 要离开的房间ID |


## 数据模型

### AUICommonConfig

| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| host       | String  | 后端服务域名          |
| appId     | String  | 声网AppId               |
| appCert   | String  | (可选)声网App证书，没有使用后端token生成服务可不填               |
| basicAuth   | String  | (可选)声网basicAuth，没有用到后端踢人服务可以不设置             |
| imAppKey   | String  | (可选)环信AppKey，没有用到后端IM服务可以不设置              |
| imClientId   | String  | (可选)环信ClientId，没有用到后端IM服务可以不设置              |
| imClientSecret   | String  | (可选)环信ClientSecret，没有用到后端IM服务可以不设置               |
| owner | AUIUserThumbnailInfo  | 用户信息             |

### AUIAPIConfig
| 参数       | 类型    | 含义                 |
| ---------- | ------- | -------------------- |
| rtcEngine       | AgoraRtcEngineKit  | (可选)rtc实例对象,为nil内部在使用到时会自行创建         |
| rtmClient       | AgoraRtmClientKit  | (可选)rtm实例对象,为nil内部在使用到时会自行创建          |
| ktvApi       | KTVApiDelegate  | (可选)KTVApi实例，用户KTV场景，如果非K歌场景可以不设置，  为nil内部在使用到时会自行创建        |

### AUIRoomInfo

| 参数        | 类型                 | 含义         |
| ----------- | -------------------- | ------------ |
| roomId      | String               | 房间id       |
| roomName      | String               | 房间名称       |
| owner   | AUIUserThumbnailInfo | 房主信息     |
| memberCount | Int                  | 房间人数     |
| micSeatCount | UInt                  | 麦位个数，默认为8     |
| micSeatStyle | UInt                  | 麦位类型     |
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

## 自定义功能

## License
版权所有 © Agora Corporation。 版权所有。
根据 [MIT 许可证](../LICENSE) 获得许可。

