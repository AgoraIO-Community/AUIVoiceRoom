# AUIVoiceRoom Android

本文档主要介绍如何快速跑通 AUIVoiceRoom 示例工程 及 自定义功能。体验在线语聊房场景，包括麦位管理、用户管理、申请邀请管理、聊天管理、礼物管理等，更详细的介绍，请参考[AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/README.zh.md)

---
## 架构图

<img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/uikit_structure_chart_voicechat_1.0.0.png" width="800" />

---
## 目录结构
```
┌─ app-voice                   // Demo代码集成目录
│  ├─ VoiceRoomListActivity			// 主要提供 VoiceRoom 的房间列表页面
│  └─ VoiceRoomSettingActivity  	// 主要提供 VoiceRoom 的房间设置页面
└─ asceneskit               	// 场景业务组装模块，目前只包含VoiceRoom
   ├─ AUIVoiceRoomView       		// VoiceRoom房间容器View，用于拼接各个基础组件以及基础组件与Service的绑定
   ├─ AUIVoiceRoomService    		// VoiceRoom房间Service，用于创建各个基础Service以及RTC/RTM/IM等的初始化
   └─ Binder                  		// 把UI Components和Service关联起来的业务绑定模块
```

---
## 环境准备

- <mark>最低兼容 Android 5.0</mark>（SDK API Level 21）
- Gradle JDK 11 以上
- Android Studio 3.5及以上版本。
- Android 5.0 及以上的手机设备。

---
## 运行示例
- 克隆或者直接下载项目源码

- 获取声网 App ID 和 App 证书 -------- [声网 Docs - 解决方案 - 声动语聊 - 快速开始 - 开通服务](https://doc.shengwang.cn/doc/chatroom/android/integration-with-ui/get-started/enable-service)
  
  > - 登录[声网控制台](https://console.shengwang.cn/)，如果没有账号则注册一个
  > 
  > - 点击创建应用
  >   
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_01.png" width="800" />
  > 
  > - 选择语聊场景
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_02_voice.png" width="800" />
  > 
  > - 保存 App ID 和 App 证书
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_03.png" width="800" />
  
- 配置实时消息RTM
  
  > - 进入[声网控制台](https://console.shengwang.cn/)
  > 
  > - 启用实时消息RTM -------- 进入控制台 -> 选择项目 -> 全部产品 -> 基础能力 -> 实时消息 —> 功能配置 -> 启用
  >  
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_04.png" width="800" />
  > 
  > - 启用 Storage 和 Lock
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_05.png" width="800" />
  
- 获取环信 AppKey、Client ID和Client Secret
  
  > - 登录[环信通讯云控制台](https://console.easemob.com/)，如果没有账号则创建一个
  > 
  > - 创建项目
  > 
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_easemob_01.png" width="800" />
  > 
  > - 复制AppKey、Client ID和Client Secret
  > 
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_easemob_02.png" width="800" />
  
- 获取后台服务域名

  > 本项目依赖一个后台服务，该后台服务主要提供下面几个功能：
  > - 房间管理
  > - Token生成
  > - 环信IM聊天房创建
  > - Rtm踢人
  >
  > 后台代码完全开源，部署教程见[后台部署](../backend)，部署完即可拿到后台服务域名。
  > 
  > **如果开发者不想或者不熟悉怎么部署后台服务，在已经获取到上面配置的App ID、App 证书等后，可以使用声网提供的测试域名：https://service.shengwang.cn/uikit**
  > 
  > **但是注意这个域名仅供测试，不能商用！**
  
- 在项目里配置上面获取到的 App ID、App 证书等
  
  > - 在项目根目录下创建local.properties文件
  >
  > - 将上面步骤获得的配置填写在local.properties文件里
  >
  >   ~~~xml
  >   # 注意：声网测试域名https://service.shengwang.cn/uikit仅供测试验证AppID等配置使用，不能商用！
  >   SERVER_HOST=<=您的后台服务域名 或者 https://service.shengwang.cn/uikit=>
  >   AGORA_APP_ID=<=您的声网AppID=>
  >   AGORA_APP_CERT=<=您的声网App证书=>
  >   IM_APP_KEY=<=您的环信IM AppKey=>
  >   IM_CLIENT_ID=<=您的环信IM Client ID=>
  >   IM_CLIENT_SECRET=<=您的环信IM Client Secret=>
  >   ~~~
  > 
  > - 如果没有按上面步骤获取声网AppID等，可使用 **声网测试域名** 和 **测试App ID：925dd81d763a42919862fee9f3f204a7**，如下图所示配置：
  >   
  >   注意：测试域名和测试AppID都仅供测试使用，不能商用！
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_07.png" width="800" />

- 用 Android Studio 运行项目即可开始您的体验

---
## 快速集成

  > 如果需要在自己的项目里集成VoiceRoomUIKit，请查看[VoiceRoomUIKit](document/VoiceRoomUIKit.md)

---
## 自定义功能
VoiceRoomUIKit支持对UI及业务功能做定制化修改，其实现放在asceneskit库里，该库通过maven引入已有的[AUKit](https://github.com/AgoraIO-Community/AUIKit)组件做定制，因此自定义需要先了解AUIKit的接口。

AUIKit的接口可以通过AUIKit的[README](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/README.md)文档进行查看。

为了更方便地介绍如何做基础及高阶定制，下面先介绍如何引入AUIKit源码，然后再从UI和业务逻辑分别说明如何做定制。

### 引入AUIKit源码

  > 本项目默认使用maven引入AUIKit库，但是可以在[setting.gradle](./gradle.properties)里配置AUIKit源码路径。
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

另外，VoiceRoomUIKit提供了两套默认主题，[Theme.VoiceRoom](./asceneskit/src/main/java/io/agora/asceneskit/voice/res/values/theme.xml)和[Theme.VoiceRoom.Voice](./asceneskit/src/main/java/io/agora/asceneskit/voice/res-voice/values/themes.xml)，
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
  > - 通过上面主题定位打开对应的[themes.xml](./asceneskit/src/main/java/io/agora/asceneskit/voice/res-voice/values/themes.xml)文件
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
  >   1. 组件通过[Binder](./asceneskit/src/main/java/io/agora/asceneskit/voice/binder)将AUIKit提供的UI组件及Service组件绑定起来以实现业务交互
  >   2. [AUIVoiceRoomService](./asceneskit/src/main/java/io/agora/asceneskit/voice/AUIVoiceRoomService.kt)管理着所有业务service
  >   3. [AUIVoiceRoomView](./asceneskit/src/main/java/io/agora/asceneskit/voice/AUIVoiceRoomView.kt)作为房间总ui入口，管理所有Binder及AUIVoiceRoomService
  >
  > 自定义功能核心是修改Binder及AUIVoiceRoomView。
  >
  > 下面是自定义麦位的参考步骤：
  >
  >- 查看[voice_room_view.xml](./asceneskit/src/main/java/io/agora/asceneskit/voice/res/layout/voice_room_view.xml)布局找到麦位控件
  >
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_08.png" width="800" />
  >
  > - 根据麦位控件ID在AUIVoiceRoomView里找到[对应的Binder实现](./asceneskit/src/main/java/io/agora/asceneskit/voice/binder/AUIMicSeatsBinder.java)。
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
  > - 将[AUIVoiceRoomUikit.kt](./app-voice/src/main/java/io/agora/app/voice/kit/AUIVoiceRoomUikit.kt)中的RoomManager替换成自己的RoomManager
  > 
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/voicechat_android_custom_12.png" width="800" />

---
## 常见问题

- [常见问题](VoiceRoomFAQ.md)
- 如有其他问题请反馈至 [开发者社区](https://www.rtcdeveloper.cn/cn/community/discussion/0)

---
## 许可证

版权所有 Agora, Inc. 保留所有权利。 使用 [MIT 许可证](../LICENSE)
