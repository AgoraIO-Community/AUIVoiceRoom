# AUIVoiceRoom iOS 示例工程快速跑通


本文档主要介绍如何快速跑通 AUIVoiceRoom 示例工程，体验在线语聊房场景，包括麦位管理、用户管理、申请邀请管理、聊天管理、礼物管理等，更详细的介绍，请参考[AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS)

## 架构图
<img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/voiceroom_uikit_structure.png" width="800" />

AUIVoiceRoom 依赖于 ASceneKit，ASceneKit 依赖于底层的 AUIKit。详细说明如下：
- AUIVoiceRoom：代表语聊房 App（开发者自行开发维护的部分）。
  - ViewController：用于管理语聊 App 中房间列表页面和单个房间的详情页面。
  - VoiceChatUIKit：负责统一调度 VoiceChatRoomView 和 VoiceChatRoomService，并管理房间。
- AScenesKit：为语聊场景提供业务逻辑的组装模块（由声网提供并维护）。
  - VoiceChatRoomView：语聊房的容器 View。用于管理 AUIKit 提供的 UI。
  - VoiceChatRoomService：语聊房的 Service。用于管理 AUIKit 提供的 Service。
  - ViewBinder：用于将 VoiceChatRoomView 和 VoiceChatRoomService 绑定。
- AUIKit：基础库（由声网提供并维护） 。
  - UI：基础 UI 组件。
  - Service：上麦、聊天、送礼物等业务能力。
  - Manager：RTM管理（AUIRtmManager）、房间管理（AUIRoomMananager）等

---

## 目录结构
```
┌─ AUIVoiceRoom                           // Demo代码集成目录
├─ AScenesKit                             // 场景业务组装模块，目前只包含VoiceRoom
│  ├─ AScenesKit
│  │  ├─ Classes
│  │  │  └─ RoomContainer
│  │  │     ├─ AUIVoiceChatRoomView       // VoiceRoom房间容器View，用于拼接各个基础组件以及基础组件与Service的绑定
│  │  │     ├─ AUIVoiceChatRoomService    // VoiceRoom房间Service，用于创建各个基础Service以及RTC/RTM/IM等的初始化
│  │  │     └─ ViewBinder                 // 把UI Components和Service关联起来的业务绑定模块
│  │  └─ Resources                        // 房间容器资源，包括国际化文件和主题
│  └─ AScenesKit.podspec                  // 本地依赖的podspec文件
└─ Document                               // 文档
```
---

## 环境准备

- Xcode 13.0及以上版本
- 最低支持系统：iOS 13.0
- 请确保您的项目已设置有效的开发者签名
  
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

- 获取RESTful HTTP基本认证 BasicAuth

  > 在用户列表的踢人功能里，用到了[RESTful服务](https://doc.shengwang.cn/doc/rtc/restful/best-practice/user-privilege#%E5%B0%86%E7%94%A8%E6%88%B7%E4%B8%80%E6%AC%A1%E6%80%A7%E8%B8%A2%E5%87%BA%E9%A2%91%E9%81%93)将用户踢出房间，因此这里需要获取RESTful基本认证BasicAuth。
  > 
  > HTTP基本认证获取方法见[官方文档](https://doc.shengwang.cn/doc/rtc/restful/get-started/http-authentication)。
  
- 获取后台服务域名

  > 本项目依赖一个后台服务，该后台服务主要提供下面几个功能：
  > - 房间管理
  > - Rtc/Rtm Token生成
  > - 环信IM聊天房创建
  > - 踢人
  >
  > 后台代码完全开源，部署教程见[后台部署](../backend)，部署完即可拿到后台服务域名。
  > 
  > **如果开发者不想或者不熟悉怎么部署后台服务，在已经获取到上面配置的App ID、App 证书等后，可以使用声网提供的测试域名：https://service.shengwang.cn/uikit**
  > 
  > **但是注意这个域名仅供测试，不能商用！**

- 在项目的[KeyCenter.swift](AUIVoiceRoom/KeyCenter.swift) 里配置上面获取到的 App ID、App 证书等
  ```
  static var HostUrl: String = <#您的后台服务域名#>
  static var AppId: String = <#您的声网AppID#>
  static var AppCertificate: String = <#您的声网App证书#>
  static var AppBasicAuth: String = <#您的声网认证Basic Auth#>
  static var IMAppKey: String = <#您的环信IM AppKey#>
  static var IMClientId: String = <#您的环信IM Client ID#>
  static var IMClientSecret: String = <#您的环信IM Client Secret#>
  ```


- 打开终端，进入到[Podfile](Podfile)目录下，执行`pod update`命令
  - 建议cocoapods升级到1.12.0以上，如果您的cocoapods版本低于1.12.0，可能会遇到如下错误
  ```
  the version of cocoapods to generate the lockfile(1.12.0) is higher than the version of the current executable(1.11.2). 
  ```
  ```
  can't modify frozen string: "[Xcodeproject] unknown object version (56).
  ```
  请打开[AUIVoiceRoom.xcodeproj](AUIVoiceRoom.xcodeproj)并按照下图修改为"Xcode 13.0-compatible"
  ![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/1691738494762.jpg)
- 最后打开AUIVoiceRoom.xcworkspace，运行即可开始您的体验
  - 如果您的cocoapods版本低于1.12.0，会遇到如下错误，请在"Team"里手动设置签名
  ![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/1691739881708.jpg)
---

## 快速集成及自定义功能
如果需要在自己的项目里集成VoiceRoomUIKit或者自定义功能，请查看[VoiceRoomUIKit](../Document/VoiceRoomUIKit.md)

---

## 常见问题

- [常见问题](VoiceRoomFAQ.md)
- 如有其他问题请反馈至 [开发者社区](https://www.rtcdeveloper.cn/cn/community/discussion/0)

---

## 许可证

版权所有 Agora, Inc. 保留所有权利。 使用 [MIT 许可证](https://bitbucket.agoralab.co/projects/ADUC/repos/uikit/browse/Android/LICENSE?at=refs%2Fheads%2Fdev%2Fandroid%2Ftheme)
