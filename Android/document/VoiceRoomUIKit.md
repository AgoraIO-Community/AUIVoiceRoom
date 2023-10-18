# VoiceRoomUIKit

*English | [中文](VoiceRoomUIKit_zh.md)*

VoiceRoomUIKit is a voice chat room scene component, which provides room management and the ability to pull up the voice chat room scene page. Developers can use this component to quickly build a chat room application.


## Quick Started
> Please make sure you have successfully run the project according to this [tutorial](../Example/AUIKitVoiceRoom/README.md) before integrating.。

### 1. Add Source Code

**Copy the following source code into your own project：**

- [AUIKit](../AUIKit)
- [AScenesKit](../AScenesKit)

**Add dependencies on AScenesKit and AUIKit in the Setting.gradle file**

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
    //VoiceRoomUIKit Set basic information

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
        ktvApi = null,// option
        serverHost = BuildConfig.SERVER_HOST
    )
```

### 3. Get room list
```kotlin
    AUIVoiceRoomUikit.getRoomList(
        lastCreateTime: Long?,
        pageSize: Int,
        success: (List<AUIRoomInfo>) -> Unit,
        failure: (AUIException) -> Unit
    )
```

### 4. Create room
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

### 5. Check permissions and Launch room
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
                    AUIVoiceRoomUikit.registerRespObserver(this)
                }
            },
            {
                finish()
            },
            true
        )
```

### 6. Exit the room
#### 6.1 Proactively exiting
```kotlin
//AUIVoiceChatRoomView provides a closure for onClickOffButton
 private fun shutDownRoom() {
        roomInfo?.roomId?.let { roomId ->
            AUIVoiceRoomUikit.destroyRoom(roomId)
            AUIVoiceRoomUikit.unRegisterRespObserver(this@VoiceRoomActivity)
        }
        finish()
    }
```

#### 6.2 Room destruction passive exit
Please refer to [Room Destruction] (# 7.1-Room-Destruction)


### 7. Exception handling
#### 7.1 Room destruction
```kotlin
//Subscribe to the callback for AUIRoomManagerRespDelegate after VoiceRoomUIKit. shared. launchRoom
mVoiceService.getRoomManager().registerRespObserver(this)

//Unsubscribe when exiting the room
mVoiceService?.getRoomManager()?.unRegisterRespObserver(this)

//Process room destruction through onRoomDestroy in the AUIRoomManagerRespDelegate callback method
override fun onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}

override fun onRoomUserBeKicked(roomId: String?, userId: String?) {
        if (roomId == mVoiceService?.getRoomInfo()?.roomId){
            AUIAlertDialog(context).apply {
                setTitle("You have been kicked out of the room")
                setPositiveButton("confirm") {
                    dismiss()
                    mOnRoomDestroyEvent?.invoke()
                }
                show()
            }
        }
    }
```

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).

