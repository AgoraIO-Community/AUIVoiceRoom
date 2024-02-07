//
//  KeyCenter.swift
//  AUIVoiceChatApp
//
//  Created by wushengtao on 2023/5/6.
//


struct KeyCenter {
    
    /*
     声网APP ID
     Agora 给应用程序开发人员分配 App ID，以识别项目和组织。如果组织中有多个完全分开的应用程序，例如由不同的团队构建，
     则应使用不同的 App ID。如果应用程序需要相互通信，则应使用同一个App ID。
     进入声网控制台(https://console.shengwang.cn/)，创建一个项目，进入项目配置页，即可看到APP ID。
     */
    static var AppId: String = <#Your AppId#>
//    static var AppId: String = "925dd81d763a42919862fee9f3f204a7"  //该AppId仅限体验使用，请勿用户正式商用环境
    
    /*
     声网APP证书
     Agora 提供 App certificate 用以生成 Token。您可以在您的服务器部署并生成，或者使用控制台生成临时的 Token。
     进入声网控制台(https://console.shengwang.cn/)，创建一个带证书鉴权的项目，进入项目配置页，即可看到APP证书。
     注意：如果项目没有开启证书鉴权或者使用的是体验的AppId，这个字段留空。
     */
    static var AppCertificate: String = ""
    
    
    /*
     声网BasicAuth
     Agora 提供 App certificate 用以使用踢人服务。您可以在您的服务器部署并生成，或者使用控制台生成临时的 Token。
     进入声网控制台(https://console.shengwang.cn/)，创建一个带证书鉴权的项目，进入项目配置页/RESTful Api里，添加密钥，根据密钥创建BasicAuth。
     注意：如果项目使用的是体验的AppId。
     */
    static var AppBasicAuth: String = ""
    
    /*
     环信APPKEY
     在环信即时通讯云控制台创建应用时填入的应用名称。
     如需使用语聊房场景，需要设置该参数。
     详见获取环信即时通讯IM的信息(http://docs-im-beta.easemob.com/product/enable_and_configure_IM.html#%E8%8E%B7%E5%8F%96%E7%8E%AF%E4%BF%A1%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF-im-%E7%9A%84%E4%BF%A1%E6%81%AF)
     */
    static var IMAppKey: String = ""
    
    /**
     环信的 client id，用于生成 app token 调用 REST API。
     如需使用语聊房场景，需要设置该参数。
     详见 环信即时通讯云控制台(https://console.easemob.com/user/login/) 的应用详情页面。
     */
    static var IMClientId: String = ""
    
    /*
     环信Client Secret
     App 的 client_secret，用于生成 app token 调用 REST API。
     如需使用语聊房场景，需要设置该参数。
     详见 环信即时通讯云控制台( https://console.easemob.com/user/login/) 的应用详情页面。
     */
    static var IMClientSecret: String = ""
    
    /*
     后端微服务域名
     */
    static var HostUrl: String = <#Your Host Url#>
//    static var HostUrl: String = "https://service.shengwang.cn/uikit-v2" //该域名仅限体验使用，请勿用户正式商用环境
}
