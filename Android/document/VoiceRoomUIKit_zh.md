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
        config.appId = BuildConfig.AGORA_APP_ID
        config.userId = mUserId
        config.userName = "user_$mUserId"
        config.userAvatar = randomAvatar()

    // init AUiKit
    AUIVoiceRoomUikit.init(
        config = config, // must
        rtmClient = null, // option
        rtcEngineEx = null, // option
        ktvApi = null,// option
        serverHost = BuildConfig.SERVER_HOST
    )
```

### 3.获取房间列表
```kotlin

    AUIVoiceRoomUikit.getRoomList(
        lastCreateTime: Long?,
        pageSize: Int,
        success: (List<AUIRoomInfo>) -> Unit,
        failure: (AUIException) -> Unit
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
                generateToken { config ->
                    AUIVoiceRoomUikit.launchRoom(
                        lunchType,
                        roomInfo,
                        config,
                        mViewBinding.VoiceRoomView,
                        AUIVoiceRoomUikit.RoomEventHandler {

                        })
                    AUIVoiceRoomUikit.subscribeError(roomInfo.roomId, this)
                    AUIVoiceRoomUikit.bindRespDelegate(this)
                }
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
            AUIVoiceRoomUikit.unsubscribeError(roomId, this@VoiceRoomActivity)
            AUIVoiceRoomUikit.unbindRespDelegate(this@VoiceRoomActivity)
        }
        finish()
    }
```

#### 6.2 房间销毁与自动退出
Please refer to [Room Destruction] (# 7.2-Room-Destruction)


### 7. 异常处理
#### 7.1 Token过期处理
```kotlin
//订阅 AUIVoiceRoomUikit.subscribeError 后 AUIRtmErrorProxyDelegate 的回调
AUIVoiceRoomUikit.subscribeError(roomInfo.roomId, this)

//退出房间时取消订阅
AUIVoiceRoomUikit.unsubscribeError(roomId, this@VoiceRoomActivity)

//然后使用AUIRtmErrorProxyDelegate回调中的onTokenPrivilegeWillExpire回调方法更新所有token
override fun onTokenPrivilegeWillExpire(channelName: String?) {
        generateToken(channelName, onSuccess = {
            AUIRoomContext.shared().roomConfig = it
        })
    }
```

#### 7.2 Room destruction
```kotlin
//订阅 VoiceRoomUIKit 后 AUIRoomManagerRespDelegate 的回调。
mVoiceService.getRoomManager().bindRespDelegate(this)

//退出房间时取消订阅
mVoiceService?.getRoomManager()?.unbindRespDelegate(this)

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

