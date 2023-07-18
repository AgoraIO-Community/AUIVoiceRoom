# VoiceRoom Theme

*English | [中文](VoiceRoomTheme_zh.md)*

AUIKit uses [SwiftTheme](https://github.com/wxxsw/SwiftTheme) as the basic ability for theme changing

Here is how to add a skin to a component, using AScenesKit as an example

### 1.Create theme directory
![](https://fullapp.oss-cn-beijing.aliyuncs.com/pic/1686831479688.jpg)
Resource is the image resource directory, and theme is the skin configuration file directory

### 2.Add the required custom theme configuration file
**_Note: Please confirm that the corresponding Key ("Test") does not conflict with other theme configurations, otherwise it will be overwritten_**
```json
{
  "Test": {
      "backgroundImage": "aui_room_bg",           //Image resources will be searched in resource, currently only supported in the root directory of the resource directory
      "backgroundColor": "#ff66cc77",             //color(RGBA)
      "iconWidth": 24,                            //width
      "iconHeight": 24,                           //height
      "cornerRadius": 16,                         //corner radius
      "normalFont": "PingFangSC-Semibold,17",     //font and size  
      "normalGradient": ["#099dfd", "#6c7192"]    //Gradient color (can have multiple color values, gradient location needs to be modified in the code)
  }
}
```

### 3.Set the added bundle path into AUIKit
```swift
if let folderPath = Bundle.main.path(forResource: "auiVoiceRoomTheme", ofType: "bundle") {
    AUIRoomContext.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
}
```

### 4. Set theme attributes for the specified View
```swift
let imageView = UIImageView()
//Set the image and search for the corresponding image in the bundle's resource
imageView.theme_image = auiThemeImage("Test.backgroundImage")
//Set width
imageView.theme_width = "Test.iconWidth"
//Set height
imageView.theme_height = "Test.iconHeight"
let label = UILabel()
//Set background color
label.theme_backgroundColor = AUIColor("Test.backgroundColor")
//Set font
label.theme_font = "Test.normalFont"

let gradientLayer = CAGradientLayer()
//Set gradient color
gradientLayer.theme_colors = AUIGradientColor("Test.normalGradient")
```

### 5.Set theme
#### 5.1 Based on default index settings
Set the index to 0
```swift
AUIRoomContext.shared.resetTheme()
```
Switch to the next
```swift
AUIRoomContext.shared.switchThemeToNext()
```

#### 5.2 Specify a theme
```swift
AUIRoomContext.shared.switchTheme(themeName: "UIKit")
```

Follow the above 5 steps to quickly switch the theme of a new component