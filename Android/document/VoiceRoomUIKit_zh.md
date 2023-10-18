# VoiceRoomUIKit

*English | [英文](VoiceRoomUIKit.md)*

VoiceRoomUIKit 是一个语聊房场景组件，提供房间管理和拉起语聊房场景页面的能力。 开发者可以使用该组件快速构建一个语聊房应用。

## Quick Started
> 在集成之前，请确保您已根据此[教程](../Example/AUIKitVoiceRoom/README.md) 成功运行项目。

### 1. Add Source Code

**将以下源码复制到自己的项目中：**

- [AUIKit](../AUIKit)
- [AScenesKit](../AScenesKit)


**在Setting.gradle文件中添加对AScenesKit和AUIKit的依赖**

```gradle

  rootProject.name = "AUIKitVoiceRoom"
  def uiKitPath = new File(settingsDir, '../AUIKit/Android/auikit')
  if(!uiKitPath.exists()){
    throw new RuntimeException("Please run `git submodule update --init` in AUIKitVoiceRoom root direction.")
  }

  include ':auikit'
  project(':auikit').projectDir = uiKitPath
  include ':asceneskit'
  
```

### 2. Initialize VoiceRoomUIKit
```kotlin
//VoiceRoomUIKit设置基本信息

// Create Common Config
val config = AUICommonConfig()
config.context = application
config.userId = mUserId
config.userName = "user_$mUserId"
config.userAvatar = randomAvatar()
config.host = BuildConfig.SERVER_HOST

// init AUiKit
AUIVoiceRoomUikit.init(
    config = config, // must
    rtmClient = null, // option
    rtcEngineEx = null, // option
    ktvApi = null// option
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

### 4.创建房间
```kotlin
val createRoomInfo = AUICreateRoomInfo()
createRoomInfo.roomName = roomName
createRoomInfo.micSeatCount = seatCount
createRoomInfo.micSeatStyle = seatStyle
AUIVoiceRoomUikit.createRoom(
    createRoomInfo,
    success = { roomInfo ->
        gotoRoomDetailPage(AUIVoiceRoomUikit.LaunchType.CREATE,roomInfo)
    },
    failure = {
        Toast.makeText(this@VoiceRoomListActivity, "Create room failed!", Toast.LENGTH_SHORT)
            .show()
    }
)
```

### 5. 检查权限 拉起并跳转的房间页面
```kotlin
mPermissionHelp.checkMicPerm(
        {
            AUIVoiceRoomUikit.launchRoom(
                roomInfo,
                mViewBinding.VoiceRoomView,
                AUIVoiceRoomUikit.RoomEventHandler {

                })
            AUIVoiceRoomUikit.registerRespObserver(this)
        },
        {
            finish()
        },
        true
    )
```

### 6. 退出房间
#### 6.1 Proactively exiting
```kotlin
//AUIVoiceChatRoomView 提供一个关闭的闭包
private fun shutDownRoom() {
    roomInfo?.roomId?.let { roomId ->
        AUIVoiceRoomUikit.destroyRoom(roomId)
        AUIVoiceRoomUikit.unRegisterRespObserver(this@VoiceRoomActivity)
    }
    finish()
}
```

#### 6.2 房间销毁与自动退出
Please refer to [Room Destruction] (# 7.1-Room-Destruction)


### 7. 异常处理
#### 7.1 Room destruction
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
```

## License
版权所有 © Agora Corporation。 版权所有。
根据 [MIT 许可证](../LICENSE) 获得许可。

