# VoiceRoomUIKit

*English | [中文](VoiceRoomUIKit_zh.md)*

VoiceRoomUIKit is a voice chat room scene component, which provides room management and the ability to pull up the voice chat room scene page. Developers can use this component to quickly build a chat room application.


## Quick Started
> Please make sure you have successfully run the project according to this.After successful operation, the AUIKit folder will appear in the iOS folder level directory. [tutorial](../AUIVoiceRoom/README.md) before integrating.。

### 1. Add Source Code

**Copy the following source code into your own project：**

- [AScenesKit](../AScenesKit)
- [KeyCenter.swift](../AUIVoiceRoom/AUIVoiceRoom/KeyCenter.swift)

**Add dependencies on AScenesKit in the Podfile file (for example, when AScenesKit are placed in the same level directory as the Podfile)**

```
  pod 'AScenesKit', :path => './AScenesKit'
```

**Drag KeyCenter.swift into the project**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/add_keycenter_to_voiceroom.jpg)

**Configure microphone and camera permissions**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/WeChatWorkScreenshot_c9c309c0-731c-4964-8ef3-1e60ab6b9241.png)


### 2. Initialize VoiceRoomUIKit
```swift
//Set basic information to VoiceRoomUIKit
let commonConfig = AUICommonConfig()
commonConfig.host = KeyCenter.HostUrl
commonConfig.userId = userInfo.userId
commonConfig.userName = userInfo.userName
commonConfig.userAvatar = userInfo.userAvatar
VoiceRoomUIKit.shared.setup(roomConfig: commonConfig,
                          rtcEngine: nil,   //If there is an externally initialized rtc engine
                          rtmClient: nil)   //If there is an externally initialized rtm client
```

### 3. Get room list
```swift
VoiceRoomUIKit.shared.getRoomInfoList(lastCreateTime: nil,
                                    pageSize: kListCountPerPage,
                                    callback: { error, list in
    //Update UI
})
```

### 4. Create room
```swift
  let room = AUICreateRoomInfo()
  room.roomName = "room name"
  room.thumbnail = self.userInfo.userAvatar
  room.micSeatCount = UInt(AUIRoomContext.shared.seatCount)
  room.micSeatStyle = UInt(AUIRoomContext.shared.seatType.rawValue)
  VoiceChatUIKit.shared.createRoom(roomInfo: room) { roomInfo in
      let vc = RoomViewController()
      vc.roomInfo = roomInfo
      self.navigationController?.pushViewController(vc, animated: true)
  } failure: { error in
      //error handler
  }
```

### 5. Launch room
```swift
//Creating Room Containers
let voiceRoomView = AUIVoiceChatRoomView(frame: self.view.bounds,roomInfo: info)
voiceRoomView.onClickOffButton = { [weak self] in
  //exit room callback
}

VoiceChatUIKit.shared.launchRoom(roomInfo: self.roomInfo!,
                                 roomView: voiceRoomView) {[weak self] error in
    guard let self = self else {return}
    if let _ = error { return }
    //subscription room destroyed callback
    VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
}
```

### 6. Exit the room
#### 6.1 Proactively exiting
```swift
//AUIVoiceChatRoomView provides a closure for onClickOffButton
voiceRoomView.onClickOffButton = { [weak self] in
    self.navigationController?.popViewController(animated: true)
    VoiceRoomUIKit.shared.destoryRoom(roomId: self.roomInfo?.roomId ?? "")
}
```

#### 6.2 Room destruction passive exit
Please refer to [Room Destruction] (#%207.1-Room-Destruction)


### 7. Exception handling

#### 7.1 Room destruction
```swift
//Subscribe to the callback for AUIRoomManagerRespDelegate after VoiceRoomUIKit. shared. launchRoom
VoiceRoomUIKit.shared.bindRespDelegate(delegate: self)

//Unsubscribe when exiting the room
VoiceRoomUIKit.shared.unbindRespDelegate(delegate: self)

//Process room destruction through onRoomDestroy in the AUIRoomManagerRespDelegate callback method
func onRoomDestroy(roomId: String) {
    //Processing room was destroyed
}

//exit room callback
func onRoomUserBeKicked(roomId: String, userId: String) {
        AUIToast.show(text: "You were kicked out!")
    self.navigationController?.popViewController(animated: true)
}
```

### 8. Skin changing
- AUIKit supports one click skin changing, and you can set the skin using the following methods
```swift
//Reset to default theme
AUIRoomContext.shared.resetTheme()
```
```swift
//Switch to the next theme
AUIRoomContext.shared.switchThemeToNext()
```

```swift
//Specify a theme
AUIRoomContext.shared.switchTheme(themeName: "Light")
```
- You can also change the skin of the component by modifying the [theme](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/theme) or replacing the [resource file](../AUIKit/AUIKit/Resource/auiTheme.bundle/UIKit/resource)
- For more skin changing issues, please refer to [Skin Settings](./VoiceRoomTheme.md)

# API reference
## setup
Initialization
```swift
func setup(roomConfig: AUICommonConfig,
           rtcEngine: AgoraRtcEngineKit? = nil,
           rtmClient: AgoraRtmClientKit? = nil) 
```
The parameters are shown in the table below:
| parameter   | type            | meaning     |
| ----------- | --------------- | ------------------------------------------------------------ |
| config      | AUICommonConfig | General configuration, including user information and domain name, etc.                             |
| rtcEngine | AgoraRtcEngineKit     | (Optional) Agora RTC engine. When Agora RTC has been integrated in the project, it can be passed in, otherwise it will be automatically created internally. |
| rtmClient   | AgoraRtmClientKit       | (Optional) Agora RTM engine. When Agora RTM has been integrated in the project, it can be passed in, otherwise it will be automatically created internally.|

## createRoom
Create a Room

```swift
func createRoom(roomInfo: AUICreateRoomInfo,
                success: ((AUIRoomInfo?)->())?,
                failure: ((Error)->())?)
```

The parameters are shown in the table below:
| parameter   | type            | meaning     |
| -------------- | ----------------- | -------------------------------- |
| roomInfo       | AUICreateRoomInfo | Information needed to create a room          |
| success        | Closure          | Success callback, success will return a room information |
| failure        | Closure          | Failure callback                         |



### getRoomInfoList
Get room list

```swift
func getRoomInfoList(lastCreateTime: Int64?, 
                     pageSize: Int, 
                     callback: @escaping AUIRoomListCallback)
```

The parameters are shown in the table below:
| parameter   | type            | meaning     |
| --------- | -------- | ------------------------------------ |
| lastCreateTime | Int64     | The page start time,difference from 1970-01-01:00:00:00, in milliseconds, For example: 1681879844085   |
| pageSize  | Int      | The page size                                 |
| callback   | Closure | Completion callback|

### launchRoom
Launch Room
```swift
func launchRoom(roomInfo: AUIRoomInfo,
                voiceChatView: AUIVoiceChatRoomView) 
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ----------- | --------------- | ------------------------------------- |
| roomInfo    | AUIRoomInfo     | Room information                     |
| voiceChatView | AUIVoiceChatRoomView | Room UI View                    |
| completion | Closure | Join the room to complete the callback                          |

### destroyRoom
Destroy Room

```swift
func destoryRoom(roomId: String)
```

The parameters are shown in the table below:

| parameter   | type            | meaning     |
| ------ | ------ | -------------- |
| roomId | String | The ID of the room to destroy |


## Data Model

### AUICommonConfig

| parameter   | type            | meaning     |
| ---------- | ------- | -------------------- |
| host       | String  | Backend service domain name     |
| userId     | String  | User ID              |
| userName   | String  | User name            |
| userAvatar | String  | User avatar url      |

### AUIRoomInfo
| parameter   | type            | meaning     |
| ----------- | -------------------- | ------------ |
| roomId      | String               | Room id       |
| roomOwner   | AUIUserThumbnailInfo | Room information   |
| memberCount | Int                  | Online user count  |
| createTime  | Int64                | Room create time,difference from 1970-01-01:00:00:00, in milliseconds, For example: 1681879844085 |

### AUIUserThumbnailInfo

| parameter   | type            | meaning     |
| ---------- | ------ | -------- |
| userId     | String | Room id   |
| userName   | String | User name   |
| userAvatar | String | User avatar url |


### AUIRoomManagerRespDelegate
```AUIRoomManagerRespDelegate``` protocol is used to handle various response events related to room operations. It provides the following methods that can be implemented by classes following this protocol to respond to specific events.

#### Method
  - ```func onRoomDestroy(roomId: String)```
    The callback method called when the room is destroyed.
    - Parameter:
      - ```roomId```: Room ID.
    >
  - ```func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo)```
    The callback method called when room information changes.
    - Parameter:
      - ```roomId```: Room ID.
      - ```roomInfo```: Room information.
    >
  - ```func onRoomAnnouncementChange(roomId: String, announcement: String)```
    The method called when a room announcement changes.
    - Parameter:
      - ```roomId```: Room ID.
      - ```announcement```: Announcement of changes.
    >
- ```func onRoomUserBeKicked(roomId: String, userId: String)```
    The method called when a room user is kicked out of the room.
    - Parameter:
      - ```roomId```: Room ID.
      - ```userId```: User ID.

## License
Copyright © Agora Corporation. All rights reserved.
Licensed under the [MIT license](../LICENSE).

