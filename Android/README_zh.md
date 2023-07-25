# AUIKitVoiceRoom Android 示例工程快速跑通

*[English](README.md) | 中文*

本文档主要介绍如何快速跑通 AUIKitVoiceRoom 示例工程，体验在线语聊房场景，包括麦位管理、用户管理、申请邀请管理、聊天管理、礼物管理等，更详细的介绍，请参考[AUIScenesKit](../AScenesKit/README_zh.md)和[AUIKit](https://github.com/AgoraIO-Community/AUIKit/blob/main/iOS/README_zh.md)

## 架构图
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/uikit_structure_chart_voicechat_0.2.0.png.png)


## 目录结构
```
┌─ Example                    // Demo代码集成目录
│  ├─ VoiceRoomListActivity			// 主要提供 VoiceRoom 的房间列表页面
│  └─ VoiceRoomSettingActivity  	// 主要提供 VoiceRoom 的房间设置页面
├─ AUIScenesKit               	// 场景业务组装模块，目前只包含VoiceRoom
│  ├─ AUIVoiceRoomView       		// VoiceRoom房间容器View，用于拼接各个基础组件以及基础组件与Service的绑定
│  ├─ AUIVoiceRoomService    		// VoiceRoom房间Service，用于创建各个基础Service以及RTC/RTM/IM等的初始化
│  └─ Binder                  		// 把UI Components和Service关联起来的业务绑定模块
└─ AUiKit                       // 包含基础组件和基础服务
   ├─ Service                   	// 相关基础组件服务类，包括麦位以及申请邀请上麦、点歌器、聊天服务、送礼服务、用户管理、合唱等
   ├─ UI Widgets                	// 基础UI组件，支持通过配置文件进行一键换肤
   └─ UI Components             	// 相关基础业务UI模块，包括麦位、申请邀请、聊天、送礼等，这些UI模块不包含任何业务逻辑，是纯UI模块
```

## 环境准备

- <mark>最低兼容 Android 5.0</mark>（SDK API Level 21）
- Gradle JDK 11 以上
- Android Studio 3.5及以上版本。
- Android 5.0 及以上的手机设备。

## 运行示例


### 1. 一键部署VoiceRoom后端服务

[如何部署VoiceRoom后端服务](../../backend/README_zh.md)

### 2. 项目运行
- 克隆或者直接下载项目源码
- 打开终端，在根目录执行以下命令
```
git submodule init
git submodule update
```

- 获取声网sdk
  下载[包含RTM 2.0的RTC SDK最新版本](https://download.agora.io/null/Agora_Native_SDK_for_Android_rel.v4.1.1.30_49294_FULL_20230512_1606_264137.zip)并将文件解压到以下目录
  [AUIKit/Android/auikit/libs](../AUIKit/Android/auikit/libs) : agora-rtc-sdk.jar
  [AUIKit/Android/src/main/jniLibs](../AUIKit/Android/auikit/src/main/jniLibs) : so(arm64-v8a/armeabi-v7a/x86/x86_64)

- 在项目的[**local.properties**](/local.properties)里配置业务服务器域名

  ![图片](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 

- 在项目的[**local.properties**](/local.properties)里配置业务服务器域名
```
SERVER_HOST== “Your SERVER_HOST”
```

> 声网测试域名： https://service.agora.io/uikit-voiceroom


- 用 Android Studio 运行项目即可开始您的体验

## 常见问题

- [常见问题](VoiceRoomFAQ_zh.md)
- 如有其他问题请反馈至 [开发者社区](https://www.rtcdeveloper.cn/cn/community/discussion/0)

## 许可证

版权所有 Agora, Inc. 保留所有权利。 使用 [MIT 许可证](https://bitbucket.agoralab.co/projects/ADUC/repos/uikit/browse/Android/LICENSE?at=refs%2Fheads%2Fdev%2Fandroid%2Ftheme)
