# AUIVoiceRoom Android 示例工程快速跑通

本文档主要介绍如何快速跑通 AUIVoiceRoom 示例工程，体验在线语聊房场景，包括麦位管理、用户管理、申请邀请管理、聊天管理、礼物管理等，更详细的介绍，请参考[AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/README.zh.md)

---
## 架构图

<img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/uikit_structure_chart_voicechat_0.2.0.png.png" width="800" />

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
  
- 在项目里配置上面获取到的 App ID、App 证书等
  
  > - 在项目根目录下创建local.properties文件
  >
  > - 填写配置
  >
  >   ~~~xml
  >   # Backend service host
  >   SERVER_HOST=https://service.shengwang.cn/uikit
  >     
  >   AGORA_APP_ID=<=Your App ID=>
  >   AGORA_APP_CERT=<=Your App Certifaction=>
  >   IM_APP_KEY=<=Your IM App Key=>
  >   IM_CLIENT_ID=<=Your IM Client ID=>
  >   IM_CLIENT_SECRET=<=Your IM Client Secret=>
  >   ~~~
  > 
  > - 如果没有按上面步骤获取声网AppID等，可按下图配置使用声网提供的测试App ID：925dd81d763a42919862fee9f3f204a7
  >
  >    <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/createaccount/uikit_agora_07.png" width="800" />

- 用 Android Studio 运行项目即可开始您的体验

---
## 部署后台服务（可选）

  > 声网提供公用的后台服务：https://service.shengwang.cn/uikit
  > 这个服务如果使用自己的AppId可能容易暴露自己的证书，适用于测试环境以及用户量小、安全性要求低的客户。
  >
  > **如果有更高的要求，可以自己购买服务器并部署后台服务代码，具体教程见 [后台部署](../backend)。**
  >
  > 在部署完自己服务后，将自己后台服务域名配置在项目根目录下的local.properties文件里:
  >
  > ~~~xml
  > # Backend service host
  > SERVER_HOST=<=Youe backen service host=>
  > ~~~

---
## 快速集成

  > 如果需要在自己的项目里集成VoiceRoomUIKit，请查看[VoiceRoomUIKit](document/VoiceRoomUIKit.md)

---
## 自定义功能
VoiceRoomUIKit支持对业务功能做定制化修改，其实现放在asceneskit库里，该库通过maven引入已有的[AUKit](https://github.com/AgoraIO-Community/AUIKit)组件做定制，
因此自定义需要先了解AUIKit的接口。AUIKit的接口可以通过AUIKit的[README](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/README.zh.md)文档进行查看。

下面以麦位为例说明如何自定义asceneskit功能，然后简单说明如何引入AUIKit源码做更深层次的定制。

### 自定义asceneskit功能（以麦位为例）

  > 在做自定义前，需要知道几点：
  >   1. 组件通过[Binder](./asceneskit/src/main/java/io/agora/asceneskit/voice/binder)将AUIKit提供的UI组件及Service组件绑定起来以实现业务交互
  >   2. [AUIVoiceRoomService](./asceneskit/src/main/java/io/agora/asceneskit/voice/AUIVoiceRoomService.kt)管理着所有业务service
  >   3. [AUIVoiceRoomView](./asceneskit/src/main/java/io/agora/asceneskit/voice/AUIVoiceRoomView.kt)作为房间总ui入口，管理所有Binder及AUIVoiceRoomService
  > 自定义功能核心是修改Binder及AUIVoiceRoomView。
  > 
  > 下面是自定义麦位的参考步骤：
  >
  > - 查看[voice_room_view.xml](./asceneskit/src/main/java/io/agora/asceneskit/voice/res/layout/voice_room_view.xml)布局找到麦位控件
  >   
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_01.png" width="800" />
  >
  > - 根据麦位控件ID在AUIVoiceRoomView里找到[对应的Binder实现](./asceneskit/src/main/java/io/agora/asceneskit/voice/binder/AUIMicSeatsBinder.java)。
  >   
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_02.png" width="800" />
  > 
  > - 将麦位相关的AUIKit ui组件实例及service组件实例通过构造方法传入麦位Binder里
  >   
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_03.png" width="800" />
  > 
  > - 在麦位Binder的bind方法里设置service事件监听、获取service数据及初始化ui等初始化操作
  > 
  > - 在麦位Binder的unBind方法里取消service事件监听等释放操作
  > 
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_04.png" width="800" />

### 引入AUIKit源码

  > 本项目默认使用maven引入AUIKit库，但是可以在[setting.gradle](./gradle.properties)里配置AUIKit源码路径。
  > 当AUIKit源码路径存在时，使用Android Studio编译时会将源码导到项目里并能直接修改。
  > 配置方法如下：
  > 
  > - 克隆或者直接下载AUIKit源码
  > 
  > - 在setting.gradle里配置AUIKit源码路径，该路径可以是相对于setting.gradle所在目录的相对路径
  >   
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_05.png" width="800" />
  > 
  > - 点击Android Studio的Sync按钮同步项目文件，然后左侧切到Project模式，即可看到AUIKit源码
  > 
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_06.png" width="800" />
  >
  > - 选择要自定义的service接口，如麦位接口，自己实现这个接口然后替换已有的麦位实现类
  > 
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_07.png" width="800" />
  >
  > - 在AUIVoiceRoomService里将service替换成自己的麦位实现类
  > 
  >   <img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/karaoke/karaoke_android_custome_08.png" width="800" />

---
## 常见问题

- [常见问题](VoiceRoomFAQ.md)
- 如有其他问题请反馈至 [开发者社区](https://www.rtcdeveloper.cn/cn/community/discussion/0)

---
## 许可证

版权所有 Agora, Inc. 保留所有权利。 使用 [MIT 许可证](../LICENSE)
