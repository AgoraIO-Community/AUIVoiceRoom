# VoiceRoomUIKit


VoiceChatUIKit 是一个语聊房场景组件，提供房间管理和拉起语聊房场景页面的能力。 开发者可以使用该组件快速构建一个语聊房应用。

## 快速集成

 > 在集成之前，请确保您已根据此[教程](../AUIVoiceRoom/) 成功运行项目。

### 1. 准备后台环境
VoiceRoomUIKit依赖后台服务做以下功能：
- 房间管理
- Rtc/Rtm Token生成
- 环信IM聊天房创建
- 踢人

后台服务首先需要获取一个后台服务域名，其获取方式有以下两种：
- 直接使用声网提供的测试域名：https://service.shengwang.cn/uikit
> 测试域名仅供测试使用，不能商用！
- 自己部署后台代码，详见[部署教程](https://github.com/AgoraIO-Community/AUIKit/tree/main/backend)

### 2. 添加源码

**将以下源码复制到自己的项目中：**

- [AScenesKit](../AScenesKit)
- [KeyCenter.swift](../AUIVoiceRoom/AUIVoiceRoom/KeyCenter.swift)
- [VoiceChatUIKit.swift](../AUIVoiceRoom/AUIVoiceRoom/VoiceChatUIKit.swift)

**在Podfile文件中添加对AScenesKit的依赖（例如AScenesKit与Podfile放在同级目录下时）**

```
  pod 'AScenesKit', :path => './AScenesKit'
```

**将 KeyCenter.swift 和 VoiceChatUIKit.swift 拖到项目中**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/add_keycenter_to_voiceroom_1.0.1.jpg)

**在Info.plist里配置麦克风和摄像头权限**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/WeChatWorkScreenshot_c9c309c0-731c-4964-8ef3-1e60ab6b9241.png)


### 3. 初始化VoiceRoomUIKit
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

### 4.获取房间列表
```swift
VoiceRoomUIKit.shared.getRoomInfoList(lastCreateTime: 0,
                                      pageSize: 20,
                                      callback: { error, list in
    //更新UI
    ...
})
```

### 5.房主创建并进入房间
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

### 6. 观众进入房间
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

### 7. 退出房间
#### 7.1 主动退出
```swift
//AUIVoiceChatRoomView 提供一个关闭的闭包
voiceRoomView.onClickOffButton = { [weak self] in
    VoiceRoomUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "")

    ...
}
```

#### 7.2 房间销毁与自动退出
详见[房间销毁](#81-房间销毁)

### 8. 异常处理

#### 8.1 房间销毁
```swift
//订阅 VoiceRoomUIKit 后 AUIVoiceChatRoomServiceRespDelegate 的回调
VoiceRoomUIKit.shared.bindRespDelegate(delegate: self)

//退出房间时取消订阅
VoiceRoomUIKit.shared.unbindRespDelegate(delegate: self)

//通过AUIVoiceChatRoomServiceRespDelegate回调方法中的onRoomDestroy处理房间销毁
func onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}
//用户被踢出房间的回调
func onRoomUserBeKicked(roomId: String, userId: String) {
    AUIToast.show(text: "You were kicked out!")
    self.navigationController?.popViewController(animated: true)
}
```

### 9.更换皮肤
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
| micSeatCount | UInt                  | 麦位个数，默认为8     |
| micSeatStyle | UInt                  | 麦位类型     |
| customPayload  | [String: Any]       | 扩展信息 |

### AUIUserThumbnailInfo

| 参数       | 类型   | 含义     |
| ---------- | ------ | -------- |
| userId     | String | 用户Id   |
| userName   | String | 用户名   |
| userAvatar | String | 用户头像 |

### AUIVoiceChatRoomServiceRespDelegate
```AUIVoiceChatRoomServiceRespDelegate``` 协议用于处理与房间操作相关的各种响应事件。它提供了以下方法，可以由遵循此协议的类来实现，以响应特定的事件。

#### 方法
  - `onTokenPrivilegeWillExpire(roomId: String?)`
    房间token即将过期的回调方法
    - 参数：
      - ```roomId```: 房间ID。
    >
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

- ```func onRoomUserBeKicked(roomId: String, userId: String)```
    房间用户被踢出房间时调用的方法。
    - 参数：
      - ```roomId```: 房间ID。
      - ```userId```: 用户ID。

## 功能定制化

VoiceRoomUIKit支持对UI及业务功能做定制化修改，并且由于是依赖AUIKit这个开源组件，不仅能对AScenesKit做基础定制，而且能对AUIKit做深入定制。

代码结构如下图所示，其中可以修改AScenesKit和AUIKit源码来定制功能：

<img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_architecture_diagram_1.0.0.png" width="800" />


### 1. 基础定制

基础定制主要是修改AScenesKit库实现，下面分别从UI和逻辑介绍如何定制。
另外，房间管理定制对于已有后台房间管理功能的用户来说也至关重要，为此也会介绍下如何修改。

#### 1.1 定制UI
> VoiceRoomUIKit的UI是基于AUIKit的UI组件进行实现，而AIKit提供了一套UI主题样式，因此VoiceRoomUIKit UI样式是通过扩展AUIKit组件主题来实现的。
> AUIKit组件的主题样式说明见[AUIKit属性](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/README.md#widget)。
>
>
> 另外，VoiceRoomUIKit提供了两套默认主题，[暗色(Dark)](https://github.com/AgoraIO-Community/AUIKit/tree/main/iOS/AUIKitCore/Resource/auiTheme.bundle/Dark) 和 [亮色(Light)](https://github.com/AgoraIO-Community/AUIKit/tree/main/iOS/AUIKitCore/Resource/auiTheme.bundle/Light)，
>
>
> 下面介绍 `Light` 是如何定制主题的，然后再进阶介绍如何自定义新的主题属性
>
> 下面以麦位背景图为例来介绍如何做定制：
>
> - 定位打开对应的[micSeat.json](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/AUIKitCore/Resource/auiTheme.bundle/Dark/theme/micSeat.json)文件
>
>    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_theme_01.png" width="800" />
>
> - 拷贝该麦位主题json文件到项目里，例如拷贝到scenekit的对应主题bundle里
>
>    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_theme_02.png" width="800" />
>
> - 修改麦位属性，例如
>
> ~~~json
> "SeatItem": {
>   ...
>   "backgroundColor": "#ff0000",
>   ...
> }
> ~~~
>
> - 配置好运行项目即可看到效果

#### 1.2 定制业务逻辑
  > 在做自定义前，需要知道几点：
  >   1. 组件通过[Binder](../AScenesKit/AScenesKit/Classes/ViewBinder/)将AUIKit提供的UI组件及Service组件绑定起来以实现业务交互
  >   2. [AUIVoiceChatRoomService](../AScenesKit/AScenesKit/Classes/RoomContainer/AUIVoiceChatRoomService.swift)管理着所有业务service
  >   3. [AUIVoiceChatRoomView](../AScenesKit/AScenesKit/Classes/RoomContainer/AUIVoiceChatRoomView.swift)作为房间总ui入口，管理所有Binder及AUIVoiceRoomService
  >
  > 自定义功能核心是修改Binder及AUIVoiceChatRoomView。
  >
  > 下面是自定义麦位的参考步骤：
  >
  >- 查看 AUIVoiceChatRoomView 找到麦位控件
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_03.png" width="800" />
  >
  > - 在AUIVoiceRoomView里找到[对应的Binder实现](../AScenesKit/AScenesKit/Classes/ViewBinder/AUIMicSeatViewBinder.swift)。
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_04.png" width="800" />
  >
  > - 将麦位相关的AUIKit ui组件实例及service组件实例通过与麦位Binder进行绑定
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_05.png" width="800" />
  >
  > - 在麦位Binder的bind方法里设置service事件监听、获取service数据及初始化ui等初始化操作
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_06.png" width="800" />


#### 1.3修改房间管理

  > 在后台服务里提供了一个房间管理，这个房间管理在移动端是由[RoomManager](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/AUIKitCore/Sources/Service/Impl/AUIRoomManagerImpl.swift)进行管理。
  > RoomManager提供了创建房间、销毁房间、获取房间列表这三个api，但是这仅能满足简单的房间管理需求，如果有更复杂的需求就需要自行开发房间管理服务。或者您已经有自己的房间管理服务，您也可以使用自己的房间管理服务。
  >
   > 下面说明如何修改房间管理（下面的修改方法只做参考，具体项目里修改方式可以根据您现有的房间接口做出最合适的选择）：
  >
  >- 确认后台有独立的三个后台接口：创建房间、销毁房间 以及 获取房间列表。
     >   并且房间信息里必须包含房主的用户信息：用户名、用户ID 和 用户头像。
  >
  > - 实现您的RoomManager，并包含以下三个接口
  >
  > ~~~swift
  > // 创建房间
  > public func createRoom(room: AUIRoomInfo,
  >                        callback: @escaping (NSError?, AUIRoomInfo?) -> ())
  >  
  > // 销毁房间
  > public func destroyRoom(roomId: String,
  >                         callback: @escaping (NSError?) -> ())
  > 
  > // 获取房间列表
  > public func getRoomInfoList(lastCreateTime: Int64,
  >                             pageSize: Int,
  >                             callback: @escaping AUIRoomListCallback)
  > ~~~
  >
  > - 将[VoiceChatUIKit](../AUIVoiceRoom/AUIVoiceRoom/VoiceChatUIKit.swift)中的RoomManager替换成自己的RoomManager
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_07.png" width="800" />


### 2. 高级定制
高级定制主要是修改AUIKit源码。由于AUKit默认是以CocoaPods方式引入到AScenesKit库里，需要先引入源码。
AUIKit主要提供了UI和Service组件，下面介绍如何做定制。

#### 2.1 引入AUIKit源码
  > 本项目默认使用CocoaPods引入AUIKit库，但是可以在[Podfile](../AUIVoiceRoom/Podfile)里配置AUIKit源码路径。
  > 当AUIKit源码路径存在时，使用Xcode编译时会将源码导到项目里并能直接修改。
  > 配置方法如下：
  >
  > - 克隆或者直接下载AUIKit源码
  > ```
  > git clone https://github.com/AgoraIO-Community/AUIKit.git
  > ```
  > - 在Podfile里配置AUIKit源码路径，该路径可以是相对于Podfile所在目录的相对路径
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_dependency.png" width="800" />
  >
  > - 执行`pod install`后，即可看到AUIKit源码
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_02.png" width="800" />
  >
  > 
#### 2.2 定制UI
  > 
  > 如何为一组新UI扩展主题请参考[VoiceRoom Theme](./VoiceRoomTheme.md)。
  >
  > 下面以麦位背景色来介绍如何添加新属性，以及如何在代码里获取到主题属性值并调整ui:
  >
  > - 找到麦位的json文件，在里面添加背景色属性
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_theme_03.png" width="800" />
  >
  > - 找到麦位自定义View，在麦位view里使用上面定义的背景色属性
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/voicechat_custom_theme_04.png" width="800" />
  >
  > - 主题属性自定义完成后即可按基础定制的步骤来使用这个新增属性
  > 

#### 2.3 定制业务功能

  > 高级定制业务功能是基于AUIKit提供service进行修改，具体service的说明见[AUIKit-Service文档](https://github.com/AgoraIO-Community/AUIKit/tree/main/iOS#service)

## License
版权所有 © Agora Corporation。 版权所有。
根据 [MIT 许可证](../LICENSE) 获得许可。

