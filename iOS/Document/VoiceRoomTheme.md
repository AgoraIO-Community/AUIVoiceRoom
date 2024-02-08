# VoiceRoom Theme


AUIKit通过使用[SwiftTheme](https://github.com/wxxsw/SwiftTheme) 来作为主题更换的基础能力

下面介绍如何为一个组件新增主题，以AScenesKit为例

### 1.创建主题目录
![](https://fullapp.oss-cn-beijing.aliyuncs.com/pic/1686831479688.jpg)
其中resource为图片资源目录，theme为主题配置文件目录

### 2.增加需要的自定义主题配置文件
**_注意：请确认对应的Key("Test")没有和其他主题配置里的Key冲突，否则会被覆盖_**
```json
{
  "Test": {
      "backgroundImage": "aui_room_bg",           //图片资源，会在resource查找，目前只支持在resource目录根目录查找
      "backgroundColor": "#ff66cc77",             //颜色(RGBA)
      "iconWidth": 24,                            //宽度
      "iconHeight": 24,                           //高度
      "cornerRadius": 16,                         //圆角大小
      "normalFont": "PingFangSC-Semibold,17",     //字体及大小  
      "normalGradient": ["#099dfd", "#6c7192"]    //渐变色(可以多个颜色值，渐变location需要自行在代码中修改)
  }
}
```

### 3.把新增的bundle路径设置进AUIKit里
```swift
if let folderPath = Bundle.main.path(forResource: "auiVoiceRoomTheme", ofType: "bundle") {
    AUIRoomContext.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
}
```

### 4. 对指定的View使用设置好的主题属性
```swift
let imageView = UIImageView()
//设置图片，会在bundle的resource里查找对应图片
imageView.theme_image = auiThemeImage("Test.backgroundImage")
//设置宽度
imageView.theme_width = "Test.iconWidth"
//设置高度
imageView.theme_height = "Test.iconHeight"
let label = UILabel()
//设置背景色
label.theme_backgroundColor = AUIColor("Test.backgroundColor")
//设置字体
label.theme_font = "Test.normalFont"

let gradientLayer = CAGradientLayer()
//设置渐变色
gradientLayer.theme_colors = AUIGradientColor("Test.normalGradient")
```

### 5.设置主题
#### 5.1 根据默认索引设置
设置索引index为0
```swift
AUIRoomContext.shared.resetTheme()
```
切换到下一个
```swift
AUIRoomContext.shared.switchThemeToNext()
```

#### 5.2 指定某个主题
```swift
AUIRoomContext.shared.switchTheme(themeName: "UIKit")
```

根据上述5步即可快速对一个新的组件进行主题切换