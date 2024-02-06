# AUIVoiceRoom Android

本文档主要介绍如何快速跑通 AUIVoiceRoom 示例工程 及 自定义功能。体验在线语聊房场景，包括麦位管理、用户管理、申请邀请管理、聊天管理、礼物管理等，更详细的介绍，请参考[AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/Android/README.zh.md)

---
## 架构图

<img src="https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/uikit_structure_chart_voicechat_1.0.0_2.png" width="800" />

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
## 快速集成及自定义功能

  > 如果需要在自己的项目里集成VoiceRoomUIKit或者自定义功能，请查看[VoiceRoomUIKit](document/VoiceRoomUIKit.md)

---
## 常见问题

- [常见问题](VoiceRoomFAQ.md)
- 如有其他问题请反馈至 [开发者社区](https://www.rtcdeveloper.cn/cn/community/discussion/0)

---
## 许可证

版权所有 Agora, Inc. 保留所有权利。 使用 [MIT 许可证](../LICENSE)
