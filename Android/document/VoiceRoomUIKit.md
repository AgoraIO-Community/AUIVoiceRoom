# VoiceRoomUIKit

VoiceRoomUIKit是一个语聊房场景组件，提供房间管理和拉起语聊房场景页面的能力。开发者可以使用该组件快速构建一个语聊房应用。

## 快速集成

  > 在集成之前，请确保您已根据此[教程](..) 成功运行项目。

### 1. 准备后台环境
VoiceRoomUIKit依赖后台服务做以下功能：
- 房间管理
- Token生成
- 环信IM聊天房创建
- Rtm踢人

后台服务首先需要获取一个后台服务域名，其获取方式有以下两种：
- 直接使用声网提供的测试域名：https://service.shengwang.cn/uikit
> 测试域名仅供测试使用，不能商用！
- 自己部署后台代码，详见[部署教程](https://github.com/AgoraIO-Community/AUIKit/tree/main/backend)

### 2. 添加源码

**将以下源码复制到自己的项目中：**

- [asceneskit模块](../asceneskit)
- [AUIVoiceRoomUIKit.kt文件](../app-voice/src/main/java/io/agora/app/voice/kit/AUIVoiceRoomUIKit.kt)

> *AUIVoiceRoomUikit*是作为对AScenesKit库的封装类，这个类并不属于AScenesKit的一部分，需要开发者自己手动复制到项目app模块里。
> 设计该类的目的是方便开发者调用AScenesKit的api，而不必直接接触RoomService、RoomManager这些AScenesKit里比较复杂的api。

**在Setting.gradle文件中添加对AScenesKit**

```gradle
  include ':asceneskit'
```

### 3. 初始化VoiceRoomUIKit
```kotlin
val config = AUICommonConfig()
config.context = this
config.appId = BuildConfig.AGORA_APP_ID
config.appCert = BuildConfig.AGORA_APP_CERT
config.basicAuth = BuildConfig.AGORA_BASIC_AUTH
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
AUIVoiceRoomUIKit.init(
  config,
  AUIAPIConfig()
)
```

### 4.获取房间列表
```kotlin
AUIVoiceRoomUIKit.getRoomList(
    lastCreateTime,
    pageSize,
    success = {},
    failure = {}
)
```

### 5.房主创建并进入房间
```kotlin
AUIVoiceRoomUIKit.createRoom(
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

### 6. 观众进入房间
```kotlin
AUIVoiceRoomUIKit.launchRoom(
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

### 7. 退出房间
```kotlin
AUIVoiceRoomUIKit.destroyRoom(roomId)
```

### 8. 异常处理
```kotlin
//订阅 VoiceRoomUIKit 后 AUIRoomManagerRespDelegate 的回调。
AUIVoiceRoomUIKit.registerRespObserver(roomId, this)

//退出房间时取消订阅
AUIVoiceRoomUIKit.unRegisterRespObserver(roomId, this)

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
  AUIVoiceRoomUIKit.generateToken(roomId,
    onSuccess = {
      AUIVoiceRoomUIKit.renewToken(roomId, it)
    },
    onFailure = {
      AUILogger.logger()
        .e("VoiceRoomActivity", "onTokenPrivilegeWillExpire generateToken error $it")
    })
}
```

---
## 功能定制化

VoiceRoomUIKit支持对UI及业务功能做定制化修改，并且由于是依赖AUIKit这个开源组件，不仅能对AScenesKit做基础定制，而且能对AUIKit做深入定制。

代码结构如下图所示，其中可以修改AScenesKit和AUIKit源码来定制功能：

<img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/uikit_structure_chart_voicechat_1.0.0_8.png" width="800" />

### 1. 基础定制

基础定制主要是修改AScenesKit库实现，下面分别从UI和逻辑介绍如何定制。
另外，房间管理定制对于已有后台房间管理功能的用户来说也至关重要，为此也会介绍下如何修改。

#### 1.1 定制UI

  > VoiceRoomUIKit的UI是基于AUIKit的UI组件进行实现，而AUIKit提供了一套UI主题样式，因此VoiceRoomUIKit UI样式是通过扩展AUIKit组件主题来实现的。
  > AUIKit组件的主题样式说明见[AUIKit-UI-README](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/auikit-ui/README.md)。
  >
  > 另外，VoiceRoomUIKit提供了两套默认主题，[Theme.VoiceRoom.Light](../asceneskit/src/main/java/io/agora/asceneskit/voice/res/values/theme.xml)和[Theme.VoiceRoom.Voice](../asceneskit/src/main/java/io/agora/asceneskit/voice/res-voice/values/themes.xml)，
  > `Theme.VoiceRoom.Light`使用的是AUIKit中UI组件的默认亮主题，`Theme.VoiceRoom.Dark`是对默认主题做修改后的暗主题。
  > 
  > 下面以麦位背景图为例来介绍如何做定制：
  >
  > - 定位到在AndroidManifest里配置的主题
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_03.png" width="800" />
  >
  > - 通过上面主题定位打开对应的[themes.xml](../asceneskit/src/main/java/io/agora/asceneskit/voice/res-voice/values/themes.xml)文件
  >
  >      <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_04.png" width="800" />
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

#### 1.2 定制业务逻辑

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

#### 1.3 修改房间管理

  > 在后台服务里提供了一个房间管理，这个房间管理在移动端是由[RoomManager](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/auikit-service/src/main/java/io/agora/auikit/service/room/AUIRoomManager.kt)进行管理。
  > RoomManager提供了创建房间、销毁房间、获取房间列表这三个api，但是这仅能满足简单的房间管理需求，如果有更复杂的需求就需要自行开发房间管理服务。或者您已经有自己的房间管理服务，您也可以使用自己的房间管理服务。
  >
  > 下面说明如何修改房间管理（下面的修改方法只做参考，具体项目里修改方式可以根据您现有的房间接口做出最合适的选择）：
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
  > - 将[AUIVoiceRoomUIKit.kt](../app-voice/src/main/java/io/agora/app/voice/kit/AUIVoiceRoomUIKit.kt)中的RoomManager替换成自己的RoomManager
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_12.png" width="800" />


### 2. 高级定制

高级定制主要是修改AUIKit源码。由于AUKit默认是以maven方式引入到AScenesKit库里，需要先引入源码。
AUIKit主要提供了UI和Service组件，下面介绍如何做定制。

#### 2.1 引入AUIKit源码

  > 本项目默认使用maven引入AUIKit库，但是可以在[setting.gradle](../gradle.properties)里配置AUIKit源码路径。
  > 当AUIKit源码路径存在时，使用Android Studio编译时会将源码导到项目里并能直接修改。
  > 配置方法如下：
  >
  > - 克隆或者直接下载AUIKit源码
  > ```
  > git clone https://github.com/AgoraIO-Community/AUIKit.git
  > ```
  > 
  > - 在setting.gradle里配置AUIKit源码路径，该路径可以是相对于setting.gradle所在目录的相对路径
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_01.png" width="800" />
  >
  > - 点击Android Studio的Sync按钮同步项目文件，然后左侧切到Project模式，即可看到AUIKit源码
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_02.png" width="800" />

#### 2.2 定制UI

  > 高级定制UI是指对AUIKit组件进行属性扩展，详见[AUIKit-UI文档 - 主题的修改](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/auikit-ui/README.md#%E4%B8%BB%E9%A2%98%E7%9A%84%E4%BF%AE%E6%94%B9)

#### 2.3 定制业务功能

  > 高级定制业务功能是基于AUIKit提供service进行修改，具体service的说明见[AUIKit-Service文档](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/auikit-service/README.md)

## License
版权所有 © Agora Corporation。 版权所有。
根据 [MIT 许可证](../LICENSE) 获得许可。

