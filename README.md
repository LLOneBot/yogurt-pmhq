<div align="center">

# Yogurt-PMHQ

由 [PMHQ](https://github.com/linyuchen/PMHQ) 强力驱动的 QQ 协议端，基于 [Acidify](https://github.com/LagrangeDev/acidify)

</div>

## 使用

运行前请先启动 PMHQ，并确保它的 WebSocket 服务可以访问。默认情况下，Yogurt 会连接 `ws://localhost:13000/ws`。随后启动 Yogurt；如果当前工作目录下还没有 `config.json`，程序会先生成一份默认配置文件，等待你修改后再继续。

当前版本面向 PC 协议使用，内置固定使用 `Linux` 协议信息，不需要再额外配置 PC 协议版本，也不需要签名服务。最常见的配置如下：

```json
{
  "pmhqUrl": "ws://127.0.0.1:13000/ws",
  "quickLoginUin": 123456789,
  "preloadContacts": false,
  "reportSelfMessage": true,
  "transformIncomingMFaceToImage": false,
  "httpConfig": {
    "host": "127.0.0.1",
    "port": 3000,
    "accessToken": "change-me",
    "corsOrigins": []
  },
  "webhookConfig": {
    "url": [],
    "accessToken": ""
  },
  "logging": {
    "ansiLevel": "ANSI256",
    "coreLogLevel": "DEBUG"
  },
  "skipSecurityCheck": false
}
```

其中，`pmhqUrl` 表示 PMHQ 的 WebSocket 地址；如果 PMHQ 运行在其他主机或使用了不同端口，只需要修改这一项即可。`quickLoginUin` 是一个可选项，用于指定优先尝试快速登录的 QQ 号。Yogurt 启动后会先检查当前 PMHQ 是否已经登录；如果已经登录且账号正好是这个 QQ 号，就会直接进入初始化；如果已经登录但账号不是这个 QQ 号，则会直接报错，避免误用错误账号；如果当前尚未登录，则会检查 PMHQ 的快速登录列表，在列表中找到该 QQ 号时优先尝试快速登录，失败后再回落到二维码登录。不填写 `quickLoginUin` 时，Yogurt 会直接按照当前 PMHQ 状态继续，必要时进入二维码登录。

`preloadContacts` 用于控制是否在登录完成后预加载联系人列表。关闭时启动更快；开启后会预先拉取好友、群和部分成员信息，能改善部分接口在刚启动时的表现，但也会增加启动时间和内存占用。`reportSelfMessage` 表示是否上报自己发送的消息，`transformIncomingMFaceToImage` 表示是否将收到的市场表情转换成普通图片段。

`httpConfig` 和 `webhookConfig` 用于配置 Yogurt 对外提供的 Milky 服务。`httpConfig.host` 与 `httpConfig.port` 决定监听地址，默认是 `127.0.0.1:3000`。如果 `httpConfig.accessToken` 为空，任何能够访问该地址的客户端都可以直接调用 API；如果需要在局域网或公网环境下使用，建议务必设置一个访问令牌。`httpConfig.corsOrigins` 留空时表示允许所有来源跨域访问。`webhookConfig.url` 可以填写一个或多个事件推送地址，`webhookConfig.accessToken` 则会在推送时一并携带。

`logging.coreLogLevel` 用于控制 Yogurt 和 `acidify-core` 的日志等级，可选值有 `VERBOSE`、`DEBUG`、`INFO`、`WARN` 和 `ERROR`。`logging.ansiLevel` 用于控制终端颜色输出等级，可选值有 `NONE`、`ANSI16`、`ANSI256` 和 `TRUECOLOR`。`skipSecurityCheck` 默认为 `false`；当你把服务绑定到 `0.0.0.0` 且没有设置访问令牌时，程序会给出一段安全提示，如果你明确知道自己在做什么，可以将其设为 `true` 跳过检查。

登录时，Yogurt 会优先复用 PMHQ 当前已有的登录状态；如果没有可用的在线状态，则会按前述逻辑尝试快速登录，最后再进入二维码登录。二维码登录开始后，终端侧会收到二维码相关事件；如果你的接入层订阅了这些事件，就可以直接展示二维码图片或链接。登录成功后，Yogurt 会继续执行初始化，并开始对外提供 Milky 接口。

Yogurt 启动并登录完成后，HTTP API 的地址是 `http://<host>:<port>/api`，事件接口的地址是 `http://<host>:<port>/event`。如果设置了 `accessToken`，调用方需要使用同一个令牌完成认证；在 HTTP 场景下，通常使用 `Authorization: Bearer <token>` 请求头即可。

## 支持平台

- Kotlin/JVM
- Kotlin/Native
  - Windows via `mingwX64`
  - macOS via `macosX64` and `macosArm64`
  - Linux via `linuxX64` and `linuxArm64`
- Kotlin/JS (for `acidify-core`, Node.js only)

## See Also

- [Milky](https://milky.ntqqrev.org/) - 基于 HTTP / WebSocket 通信的新时代 QQ 机器人应用接口标准
- [Saltify](https://saltify.ntqqrev.org/) - 跨平台、可扩展的 QQ Bot 框架 & Milky SDK
- [acidify-codec](https://github.com/SaltifyDev/acidify-codec) - LagrangeCodec 的 Kotlin 绑定
- [acidify-codec-js](https://github.com/SaltifyDev/acidify-codec-js) - LagrangeCodec 的 JavaScript 绑定
- [Cecilia](https://github.com/Wesley-Young/Cecilia) - 实验性的基于 Compose 的即时聊天软件
- [yogurt-captcha-solver](https://github.com/SaltifyDev/yogurt-captcha-solver) - QQ 图形验证码 Ticket 抓取辅助工具

## Special Thanks

- [Lagrange.Core](https://github.com/LagrangeDev/Lagrange.Core) 提供项目的基础架构和绝大多数协议包定义
- [Konata.Core](https://github.com/KonataDev/Konata.Core) 最初的 PC NTQQ 协议实现
- [lagrange-kotlin](https://github.com/LagrangeDev/lagrange-kotlin) 提供 TEA & 登录认证的实现
- [qrcode-kotlin](https://github.com/g0dkar/qrcode-kotlin/) 提供二维码矩阵生成的实现
- [LagrangeCodec](https://github.com/LagrangeDev/LagrangeCodec) 提供多媒体编解码的实现
- [@Linwenxuan04](https://github.com/Linwenxuan04) 编写 crypto 和 math（原 multiprecision） 部分
- ... and all the contributors along the way!

## Contributors

### Directly to this repository

![Contributors of Acidify](https://contributors-img.web.app/image?repo=LagrangeDev/Acidify)

### Lagrange.Core

![Contributors of Lagrange.Core](https://contributors-img.web.app/image?repo=LagrangeDev/Lagrange.Core)

### LagrangeV2

![Contributors of LagrangeV2](https://contributors-img.web.app/image?repo=LagrangeDev/LagrangeV2)

### Konata.Core

![Contributors of Konata.Core](https://contributors-img.web.app/image?repo=KonataDev/Konata.Core)
