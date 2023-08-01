# AUIKitVoiceRoom-Android Quick Start

*English | [中文](README_zh.md)

This document mainly introduces how to quickly run through the AUIKitVoiceRoom example  and experience online VoiceRoom scenarios, including micseat service, invite&apply service, user service, chat service, gift service, etc. For a more detailed introduction, please refer to [AUIScenesKit](../AScenesKit/README.md) and [AUIKit](https://github.com/AgoraIO-Community/AUIKit/tree/main/Android)

## Architecture
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/uikit_structure_chart_voicechat_0.2.0.png.png)

## Directory
```
┌─ Example                    	// Demo Code Integration Catalog
│  ├─ VoiceRoomListActivity           	// Provide VoiceRoom's list page
│  └─ VoiceRoomSettingActivity			// Provide VoiceRoom's Settings page
├─ AUIScenesKit                		// Scenario business assembly module, currently only including VoiceRoom
│  ├─ AUIVoiceRoomView      			// VoiceRoom room container view, used to splice various basic components and bind them to services
│  ├─ AUIVoiceRoomService    			// VoiceRoom Room Service, used to create various basic services and initialize RTC/RTM/IM, etc
│  └─ Binder                   			// Business binding module that associates UI Components with Service
└─ AUIKit                      		// Including basic components and services
   ├─ Service                  			// Related basic component services, including micseat, jukebox, user, choir, etc
   ├─ UI Widgets               			// Basic UI component, supporting one click skin changing through configuration files
   └─ UI Components       				// Related basic business UI modules, including micseat,chat,gift etc. These UI modules do not contain any business 											logic and are pure UI modules
   
```

## Requirements

- <mark>Minimum Compatibility with Android 5.0</mark>（SDK API Level 21）

- Gradle JDK 11 UP

- Android Studio 3.5 and above versions.

- Mobile devices with Android 5.0 and above.


## Getting Started

### 1. Deployment backend services

[How to deploy VoiceRoom backend services](../../backend/README.md)

### 2. Running the Example
- Clone or download  source code
- Open the terminal and execute the following command in the root directory
```
git submodule init
git submodule update
```
- Obtain Agora SDK
  Download [the rtc sdk with rtm 2.0](https://download.agora.io/null/Agora_Native_SDK_for_Android_rel.v4.1.1.30_49294_FULL_20230512_1606_264137.zip) and then unzip it to the directions belows:
  [AUIKit/Android/auikit/libs](../AUIKit/Android/auikit/libs) : agora-rtc-sdk.jar
  [AUIKit/Android/auikit/src/main/jniLibs](../AUIKit/Android/auikit/src/main/jniLibs) : so(arm64-v8a/armeabi-v7a/x86/x86_64)

- Please fill in the domain name of the business server in the [**local.properties**](/local.properties) file of the project

  ![PIC](https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/github_readme/uikit/config_serverhost_android.png)

``` 
SERVER_HOST= （Domain name of the business server）
```

> Agora Test Domain: https://service.agora.io/uikit-voiceroom

- Run the project with Android Studio to begin your experience.

## FAQ

- [FAQ](VoiceRoomFAQ.md)

- If you have any other questions, please feedback to the [developer community](https://www.rtcdeveloper.cn/cn/community/discussion/0)


## License

Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](LICENSE).
