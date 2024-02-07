# VoiceRoomUIKit

VoiceRoomUIKit 是一个语聊房场景组件，提供房间管理和拉起语聊房场景页面的能力。 开发者可以使用该组件快速构建一个语聊房应用。

## 快速集成
  
  > 在集成之前，请确保您已根据此[教程](..) 成功运行项目。

### 1. 添加源码

**将以下源码复制到自己的项目中：**

- [asceneskit](../asceneskit)


**在Setting.gradle文件中添加对AScenesKit**

```gradle
  include ':asceneskit'
```


### 2. 初始化 VoiceRoomUIKit
```kotlin
val config = AUICommonConfig()
config.context = this
config.appId = BuildConfig.AGORA_APP_ID
config.appCert = BuildConfig.AGORA_APP_CERT
config.host = BuildConfig.SERVER_HOST
config.imAppKey = BuildConfig.IM_APP_KEY
config.imClientId = BuildConfig.IM_CLIENT_ID
config.imClientSecret = BuildConfig.IM_CLIENT_SECRET
// Randomly generate local user information
config.owner = AUIUserThumbnailInfo().apply {
  userId = RandomUtils.randomUserId()
  userName = RandomUtils.randomUserName()
  userAvatar = RandomUtils.randomAvatar()
}
AUIVoiceRoomUikit.init(
  config,
  AUIAPIConfig()
)
```

### 3.获取房间列表
```kotlin
AUIVoiceRoomUikit.getRoomList(
    lastCreateTime,
    pageSize,
    success = {},
    failure = {}
)
```

### 4.房主创建并进入房间
```kotlin
AUIVoiceRoomUikit.createRoom(
  roomInfo,
  roomConfig,
  mViewBinding.VoiceRoomView,
  completion = { error, _ ->
    if (error != null) {
      shutDownRoom()
    }
  }
)
```

### 5. 观众进入房间
```kotlin
AUIVoiceRoomUikit.launchRoom(
  roomInfo,
  roomConfig,
  mViewBinding.VoiceRoomView,
  completion = { error, _ ->
    if (error != null) {
      shutDownRoom()
    }
  }
)
```

### 6. 退出房间
```kotlin
AUIVoiceRoomUikit.destroyRoom(roomId)
```

### 7. 异常处理
```kotlin
//订阅 VoiceRoomUIKit 后 AUIRoomManagerRespDelegate 的回调。
mVoiceService.getRoomManager().registerRespObserver(this)

//退出房间时取消订阅
mVoiceService?.getRoomManager()?.unRegisterRespObserver(this)

//通过AUIRoomManagerRespDelegate回调方法中的onRoomDestroy处理房间销毁
override fun onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}

//用户被踢出房间的回调
override fun onRoomUserBeKicked(roomId: String?, userId: String?) {
    if (roomId == mVoiceService?.getRoomInfo()?.roomId){
        AUIAlertDialog(context).apply {
            setTitle("您已被踢出房间")
            setPositiveButton("确认") {
                dismiss()
                mOnRoomDestroyEvent?.invoke()
            }
            show()
        }
    }
}

// Token即将过期回调
override fun onTokenPrivilegeWillExpire(roomId: String) {
  super.onTokenPrivilegeWillExpire(roomId)
  AUIVoiceRoomUikit.generateToken(roomId,
    onSuccess = {
      AUIVoiceRoomUikit.renewToken(roomId, it)
    },
    onFailure = {
      AUILogger.logger()
        .e("VoiceRoomActivity", "onTokenPrivilegeWillExpire generateToken error $it")
    })
}
```

---
## 自定义功能
VoiceRoomUIKit支持对UI及业务功能做定制化修改，其实现放在asceneskit库里，该库通过maven引入已有的[AUKit](https://github.com/AgoraIO-Community/AUIKit)组件做定制，因此自定义需要先了解AUIKit的接口。

AUIKit的接口可以通过AUIKit的[README](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/README.md)文档进行查看。

为了更方便地介绍如何做基础及高阶定制，下面先介绍如何引入AUIKit源码，然后再从UI和业务逻辑分别说明如何做定制。

### 引入AUIKit源码

  > 本项目默认使用maven引入AUIKit库，但是可以在[setting.gradle](../gradle.properties)里配置AUIKit源码路径。
  > 当AUIKit源码路径存在时，使用Android Studio编译时会将源码导到项目里并能直接修改。
  > 配置方法如下：
  >
  > - 克隆或者直接下载AUIKit源码
  >
  > - 在setting.gradle里配置AUIKit源码路径，该路径可以是相对于setting.gradle所在目录的相对路径
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_01.png" width="800" />
  >
  > - 点击Android Studio的Sync按钮同步项目文件，然后左侧切到Project模式，即可看到AUIKit源码
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_02.png" width="800" />

### UI定制
VoiceRoomUIKit的UI是基于AUIKit的UI组件进行实现，而AIKit提供了一套UI主题样式，因此VoiceRoomUIKit UI样式是通过扩展AUIKit组件主题来实现的。

AUIKit组件的主题样式说明见[README](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/doc/AUIKit-UI.md)。

另外，VoiceRoomUIKit提供了两套默认主题，[Theme.VoiceRoom](../asceneskit/src/main/java/io/agora/asceneskit/voice/res/values/theme.xml)和[Theme.VoiceRoom.Voice](../asceneskit/src/main/java/io/agora/asceneskit/voice/res-voice/values/themes.xml)，
`Theme.VoiceRoom`使用的是AUIKit中UI组件的默认主题，`Theme.VoiceRoom.Voice`是对默认主题做修改后的主题。

下面介绍`Theme.VoiceRoom.Voice`是如何定制主题的，然后再进阶介绍如何自定义新的主题属性

#### 基础定制

  > 基础定制主要是介绍如何在主题里对特定ui组件的属性进行修改以达到所要的效果，其中ui组件的可修改主题属性见[AUIKit文档](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/doc/AUIKit-UI.md)。
  >
  > 下面以麦位背景图为例来介绍如何做定制：
  >
  > - 定位到在AndroidManifest里配置的主题
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_03.png" width="800" />
  >
  > - 通过上面主题定位打开对应的[themes.xml](../asceneskit/src/main/java/io/agora/asceneskit/voice/res-voice/values/themes.xml)文件
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_04.png" width="800" />
  >
  > - 定义好麦位组件的style
  >
  > ~~~xml
  > <!-- 继承默认麦位style(AUIMicSeatItem.Appearance)修改 -->
  > <style name="AUIMicSeatItem.Appearance.My">
  >     <!-- 自己的麦位背景图 -->
  >     <item name="aui_micSeatItem_seatBackground">@drawable/ktv_ic_seat</item>
  > </style>
  > ~~~
  >
  > - 在主题里配置麦位样式
  >
  > ~~~xml
  > <resources>
  >  <style name="Theme.VoiceRoom.Voice" parent="Theme.AUIKit.Dark">
  >      ...
  >      <!-- 麦位组件 -->
  >      <item name="aui_micSeatItem_appearance">@style/AUIMicSeatItem.Appearance.My</item>
  >  </style>
  > </resources>
  > ~~~
  >
  > - 配置好运行项目即可看到效果

#### 高级定制

  > 高级定制主要适用于主题属性无法满足UI定制化需求，此时需要自己对AUIKit组件进行属性扩展。
  > 要对AUIKit ui组件添加属性，需要先参考前面章节引入AUIKit源码，然后直接在AUIKit源码上进行修改。
  >
  > 下面以麦位背景色来介绍如何添加新属性，以及如何在代码里获取到主题属性值并调整ui:
  >
  > - 找到麦位的attrs.xml，在里面添加背景色属性
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_05.png" width="800" />
  >
  > - 找到麦位自定义View及其布局
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_06.png" width="800" />
  >
  > - 在麦位view布局里使用上面定义的背景色属性
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_07.png" width="800" />
  >
  > - 主题属性自定义完成后即可按基础定制的步骤来使用这个新增属性

### 业务功能定制
VoiceRoomUIKit的业务服务是基于AUIKit的Service组件进行实现。AUIKit提供了一系列Service组件供上层使用，具体可以[AUIKit Service文档](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/doc/AUIKit-Service.md)。下面介绍VoiceRoomUIKit如何做基础定制，以及如何实现自己的房间管理。

#### binder 和 service 使用

  > 在做自定义前，需要知道几点：
  >   1. 组件通过[Binder](../asceneskit/src/main/java/io/agora/asceneskit/voice/binder)将AUIKit提供的UI组件及Service组件绑定起来以实现业务交互
  >   2. [AUIVoiceRoomService](../asceneskit/src/main/java/io/agora/asceneskit/voice/AUIVoiceRoomService.kt)管理着所有业务service
  >   3. [AUIVoiceRoomView](../asceneskit/src/main/java/io/agora/asceneskit/voice/AUIVoiceRoomView.kt)作为房间总ui入口，管理所有Binder及AUIVoiceRoomService
  >
  > 自定义功能核心是修改Binder及AUIVoiceRoomView。
  >
  > 下面是自定义麦位的参考步骤：
  >
  >- 查看[voice_room_view.xml](../asceneskit/src/main/java/io/agora/asceneskit/voice/res/layout/voice_room_view.xml)布局找到麦位控件
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_08.png" width="800" />
  >
  > - 根据麦位控件ID在AUIVoiceRoomView里找到[对应的Binder实现](../asceneskit/src/main/java/io/agora/asceneskit/voice/binder/AUIMicSeatsBinder.java)。
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_09.png" width="800" />
  >
  > - 将麦位相关的AUIKit ui组件实例及service组件实例通过构造方法传入麦位Binder里
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_10.png" width="800" />
  >
  > - 在麦位Binder的bind方法里设置service事件监听、获取service数据及初始化ui等初始化操作
  >
  > - 在麦位Binder的unBind方法里取消service事件监听等释放操作
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_11.png" width="800" />

#### 自定义房间管理

  > 在后台服务里提供了一个房间管理，这个房间管理在移动端是由[RoomManager](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/auikit-service/src/main/java/io/agora/auikit/service/room/AUIRoomManager.kt)进行管理。
  > RoomManager提供了创建房间、销毁房间、获取房间列表这三个api，但是这仅能满足简单的房间管理需求，如果有更复杂的需求就需要自行开发房间管理服务。或者您已经有自己的房间管理服务，您也可以使用自己的房间管理服务。
  >
  > 下面说明如何自定义房间管理：
  >
  >- 确认后台有独立的三个后台接口：创建房间、销毁房间 以及 获取房间列表。
     >   并且房间信息里必须包含房主的用户信息：用户名、用户ID 和 用户头像。
  >
  > - 实现您的RoomManager，并包含以下三个接口
  >
  > ~~~kotlin
  > // 创建房间
  > fun createRoom(
  >      roomInfo: AUIRoomInfo,
  >      callback: AUICreateRoomCallback?
  >  )
  > // 销毁房间
  > fun destroyRoom(
  >      roomId: String,
  >      callback: AUICallback?
  >  )
  > // 获取房间列表
  > fun getRoomInfoList(lastCreateTime: Long?, pageSize: Int, callback: AUIRoomListCallback?)
  > ~~~
  >
  > - 将[AUIVoiceRoomUikit.kt](../app-voice/src/main/java/io/agora/app/voice/kit/AUIVoiceRoomUikit.kt)中的RoomManager替换成自己的RoomManager
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_12.png" width="800" />


## License
版权所有 © Agora Corporation。 版权所有。
根据 [MIT 许可证](../LICENSE) 获得许可。

