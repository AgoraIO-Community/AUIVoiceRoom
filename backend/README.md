# UIKit Karaoke Scene Backend Service

English | [中文](README_zh.md)

## Project Introduction

-  This project is developed based on the Spring Boot framework and relies on Redis/MongoDB/RTM/NCS/Easemob IM components.
-  Redis is mainly used for refreshing the number of online users and ensuring the consistency of data updates through distributed locks.
-  MongoDB is mainly used to maintain the list of rooms.
-  RTM is used to store scene data and provide message transmission channels.
-  NCS is used for RTC channel event callback notifications and handles logic related to personnel entry/exit and room destruction.
-  Easemob IM is used for chat services in Voice Chat Rooms.

## Service Deployment

### Quick Experience

- The service installation environment needs to have the latest Docker environment installed, and the [docker-compose](https://docs.docker.com/compose/) deployment tool installed.
- You can download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/), which has already installed docker-compose.
- For local deployment, before starting the service, you need to create a `.env` file in the root directory of the project and fill in the following fields:
  - TOKEN_APPID= <Your TOKEN_APPID>
  - TOKEN_APPCERTIFICATE=< Your TOKEN_APPCERTIFICATE >
  - TOKEN_BASICAUTH_USERNAME=< Your TOKEN_BASICAUTH_USERNAME >
  - TOKEN_BASICAUTH_PASSWORD=< Your TOKEN_BASICAUTH_PASSWORD >
  - NCS_SECRET=< Your NCS_SECRET >
  - EM_AUTH_APPKEY=< Your EM_AUTH_APPKEY >
  - EM_AUTH_CLIENTID=< Your EM_AUTH_CLIENTID >
  - EM_AUTH_CLIENTSECRET=< YourEM_AUTH_CLIENTSECRET >

 The field descriptions are as follows:

| Field name | Field description                                     | Relevant documentation link | 
|--|------------------------------------------|-|
| TOKEN_APPID | Agora AppID, used for logging into RTC and RTM                   |https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id|
| TOKEN_APPCERTIFICATE | 	Agora App Certificate, used for logging into RTC and RTM                     |https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id |
| TOKEN_BASICAUTH_USERNAME | Agora RESTful API Basic Auth Username, used for kicking users |https://docportal.shengwang.cn/cn/Agora%20Platform/agora_console_restapi?platform=All%20Platforms |
| TOKEN_BASICAUTH_PASSWORD | Agora RESTful API Basic Auth Password, used for kicking users |https://docportal.shengwang.cn/cn/Agora%20Platform/agora_console_restapi?platform=All%20Platforms |
| NCS_SECRET | Signature key for message notification service, used to verify NCS event callback content                |https://docportal.shengwang.cn/cn/Agora%20Platform/ncs?platform=All%20Platforms |
| EM_AUTH_APPKEY | Easemob IM AppKey, used to create Easemob IM chat rooms               |https://docs-im-beta.easemob.com/document/server-side/enable_and_configure_IM.html |
| EM_AUTH_CLIENTID | Easemob IM ClientID, used to create Easemob IM chat rooms             |https://docs-im-beta.easemob.com/document/server-side/enable_and_configure_IM.html |
| EM_AUTH_CLIENTSECRET | Easemob IM Client Secret, used to create Easemob chat rooms             |https://docs-im-beta.easemob.com/document/server-side/enable_and_configure_IM.html |


- Execute `docker compose up -d --build` in the current project root directory, which will pull related images and start Redis/MongoDB/Web service.
- After the service is started, you can use `curl http://localhost:8080/health/check` to test.
- To debug local services using the app, you need to replace the corresponding backend service domain with http://service_machine_IP:8080 on the app. After replacing the domain, you can experience the related services on the app.
- To stop the service, execute `docker compose down`.
- Note! NCS message notification is not turned on, and personnel entering and leaving and room destruction logic cannot be automatically processed. If you need to enable this feature, NCS service needs to be enabled.
- RTM and Easemob IM are not activated, so the functionality may be limited. If you need the complete experience, please refer to the deployment permissions activation instructions.

### Local Development

- Java version is recommended to be >=11.
- The editor may use [Visual Studio Code](https://code.visualstudio.com/) and install the following plug-ins:
    - [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) (provides Dockerfile_dev for local container development)
    - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- RTM operation relies on the Linux environment.
- Open the project directory in vscode and enter container development.
- Modify the configuration file [application.yml](src/main/resources/application.yml):
    - spring.data.mongodb.uri
    - spring.redis.host
    - spring.redis.password
    - ncs.secret
    - token.appId
    - token.appCertificate
    - token.basicAuth.username
    - token.basicAuth.password
    - em.auth.appKey
    - em.auth.clientId
    - em.auth.clientSecret

### Online Deployment

- Before going online, Redis/MongoDB and other configurations need to be adjusted, and the service needs to be deployed behind the gateway. The gateway can provide authentication/traffic limit and other capabilities, and this service does not have gateway capabilities.
- At the same time, the following services need to be enabled:
    - RTM, [Contact customer service to enable](https://www.agora.io)
    - NCS, RTC channel event callback notification and processing of personnel entering and leaving/room destruction logic
        - [Enable Message Notification Service](https://docs-beta.agora.io/en/video-calling/develop/receive-notifications?platform=android#enable-notifications)
            - Choose the following event types
                - channel create, 101
                - channel destroy, 102
                - broadcaster join channel, 103
                - broadcaster leave channel, 104
            - Callback URL
                - https://yourdomain.com/v1/ncs/callback
            - Modify Secret
                - Based on the Secret value provided on the configuration page, modify the ncs.secret field in the project configuration file application.yml.
        - [Channel Event Callback](https://docs-beta.agora.io/en/video-calling/develop/receive-notifications?platform=android#channel-events)
    - Easemob IM: Configure in the Agora Console Enable and Configure the IM Service [Enable and Configure the IM Service](https://docs-im-beta.easemob.com/document/server-side/enable_and_configure_IM.html)
- Metric collection, https://yourdomain:9090/metrics/prometheus, can collect corresponding indicators to monitor the service as needed.
- The service can be deployed on cloud platforms, such as [Alibaba Cloud Container Service ACK](https://www.alibabacloud.com/en/product/kubernetes).

## Directory Structure

```
├── Dockerfile
├── Dockerfile_Dev
├── HELP.md
├── README.md
├── README_zh.md
├── docker-compose.yaml
├── init-mongo.js
├── mvn_settings.xml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │         ├── java
    │         │         └── io
    │         │             └── agora
    │         │                 └── uikit
    │         │                     ├── Application.java
    │         │                     ├── bean
    │         │                     │         ├── domain                                        // Domain Objects
    │         │                     │         ├── dto                                           // Data Transfer Objects
    │         │                     │         ├── entity                                        // Data Transfer Objects
    │         │                     │         ├── enums                                         // Enumerations 
    │         │                     │         ├── exception                                     // Exceptions
    │         │                     │         ├── process                                       // Business Logic        
    │         │                     │         ├── req                                           // Request Objects
    │         │                     │         └── valid                                         // Parameter Validation 
    │         │                     ├── config
    │         │                     │         ├── EmServiceConfig.java                          // Easemob Configuration
    │         │                     │         ├── GlobalExceptionHandler.java                   // Global Exception Handler
    │         │                     │         ├── RedisConfig.java                              // Redis Configuration
    │         │                     │         ├── RtcChannelAPIClient.java                      // RTC Channel API Client
    │         │                     │         └── WebMvcConfig.java                             // Web MVC Configuration
    │         │                     ├── controller
    │         │                     │         ├── ApplicationController.java                    // Application Controller
    │         │                     │         ├── ChatRoomController.java                       // Chat Room Controller
    │         │                     │         ├── ChorusController.java                         // Chorus Controller
    │         │                     │         ├── GiftController.java                           // Gift Controller
    │         │                     │         ├── HealthController.java                         // Health Check Controller
    │         │                     │         ├── InvitationController.java                     // Invitation Controller
    │         │                     │         ├── MicSeatController.java                        // Mic Seat Controller
    │         │                     │         ├── NcsController.java                            // NCS Controller
    │         │                     │         ├── RoomController.java                           // Room Controller
    │         │                     │         ├── SongController.java                           // Song Controller
    │         │                     │         ├── TokenController.java                          // Token Controller
    │         │                     │         └── UserController.java                           // User Controller 
    │         │                     ├── interceptor                                             // Interceptors        
    │         │                     │         ├── PrometheusMetricInterceptor.java              // Metrics Interceptor
    │         │                     │         └── TraceIdInterceptor.java                       // Trace Id Interceptor
    │         │                     ├── metric                                                  // Metrics
    │         │                     │         └── PrometheusMetric.java
    │         │                     ├── repository                                              // Database Operations
    │         │                     │         └── RoomListRepository.java
    │         │                     ├── service
    │         │                     │         ├── IApplicationService.java                      // Application Service
    │         │                     │         ├── IChatRoomService.java                         // Chat Room Service
    │         │                     │         ├── IChorusService.java                           // Chorus Service
    │         │                     │         ├── IGiftService.java                             // Gift Service
    │         │                     │         ├── IIMService.java                               // IM Service
    │         │                     │         ├── IInvitationService.java                       // Invitation Service
    │         │                     │         ├── IMicSeatService.java                          // Mic Seat Service
    │         │                     │         ├── INcsService.java                              // NCS Service
    │         │                     │         ├── IProcessService.java                          // Application and Invitation Business Process Service
    │         │                     │         ├── IRoomService.java                             // Room Service
    │         │                     │         ├── IRtcChannelAPIService.java                    // RTC Channel Management Service
    │         │                     │         ├── IRtcChannelService.java                       // RTC Channel Service  
    │         │                     │         ├── IService.java
    │         │                     │         ├── ISongService.java                             // Song Service
    │         │                     │         ├── ITokenService.java                            // Token Service
    │         │                     │         ├── IUserService.java                             // User Service
    │         │                     │         └── impl
    │         │                     │             ├── ApplicationServiceImpl.java
    │         │                     │             ├── ChatRoomServiceImpl.java
    │         │                     │             ├── ChorusServiceImpl.java
    │         │                     │             ├── GiftServiceImpl.java
    │         │                     │             ├── IMServiceImpl.java
    │         │                     │             ├── InvitationServiceImpl.java
    │         │                     │             ├── MicSeatServiceImpl.java
    │         │                     │             ├── NcsServiceImpl.java
    │         │                     │             ├── ProcessServiceImpl.java
    │         │                     │             ├── RoomServiceImpl.java
    │         │                     │             ├── RtcChannelServiceImpl.java
    │         │                     │             ├── SongServiceImpl.java
    │         │                     │             ├── TokenServiceImpl.java
    │         │                     │             └── UserServiceImpl.java
    │         │                     ├── task                                                    // Scheduled Tasks 
    │         │                     │         └── RoomListTask.java                             // Room List Scheduled Task
    │         │                     └── utils
    │         │                         ├── HmacShaUtil.java                                    // HmacSha1 Encryption Utility
    │         │                         ├── RedisUtil.java                                      // Redis Utility
    │         │                         ├── RtmUtil.java                                        // RTM Utility
    │         │                         └── TokenUtil.java                                      // Token Utility 
    │         └── resources
    │             ├── application.yml                                                           // Default Configuration File
    │             ├── lib                                                                       // Agora RTM SDK
    │             │         ├── agora-rtm-sdk.jar
    │             │         ├── libagora-fdkaac.so
    │             │         ├── libagora-ffmpeg.so
    │             │         ├── libagora-soundtouch.so
    │             │         ├── libagora_rtc_sdk.so
    │             │         └── libagora_rtm_sdk.so
    │             └── logback-spring.xml                                                        // Logging configuration
    └── test                                                                                    // Unit tests
```
