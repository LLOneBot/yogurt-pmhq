<div align="center">

# Yogurt-PMHQ

由 [PMHQ](https://github.com/linyuchen/PMHQ) 强力驱动的 QQ 协议端，基于 [Acidify](https://github.com/LagrangeDev/acidify)

</div>

## 使用

### Docker 部署（推荐）

> [!important]
>
> 在 macOS 上请使用 [OrbStack](https://orbstack.dev/) 而非 Docker Desktop。

由于 Yogurt-PMHQ 需要一个启动的 PMHQ 实例，因此推荐使用 Docker Compose 来将 PMHQ 与本项目一同部署。下面是一个示例 `docker-compose.yml`，展示了如何将 PMHQ 和 Yogurt 放在同一个网络里，并通过环境变量配置它们的行为：

```yaml
services:
  pmhq:
    image: linyuchen/pmhq:latest
    container_name: pmhq
    privileged: true
    environment:
      - ENABLE_HEADLESS=true
      - RUST_LOG=error
    volumes:
      - qq_volume:/root/.config/QQ
    networks:
      - app_network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:13000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  yogurt:
    image: ghcr.io/llonebot/yogurt-pmhq:latest
    container_name: yogurt-pmhq
    environment:
      - PMHQ_HOST=pmhq
      - PMHQ_PORT=13000
      - YOGURT_HOST=0.0.0.0
      - YOGURT_PORT=3000
      - YOGURT_ACCESS_TOKEN=change-me # 请在此修改 Access Token，确保安全
      - HTTP_CORS_ORIGINS=
      - WEBHOOK_URLS=
      - WEBHOOK_ACCESS_TOKEN=
      - QUICK_LOGIN_UIN=
      - PRELOAD_CONTACTS=false
      - REPORT_SELF_MESSAGE=true
      - TRANSFORM_INCOMING_MFACE_TO_IMAGE=false
      - SKIP_SECURITY_CHECK=true
      - ANSI_LEVEL=ANSI256
      - CORE_LOG_LEVEL=DEBUG
    extra_hosts:
      - "host.docker.internal:host-gateway"
    volumes:
      - ./data:/app/data
    ports:
      - "3000:3000"
    depends_on:
      pmhq:
        condition: service_healthy
    networks:
      - app_network
    restart: unless-stopped

volumes:
  qq_volume:

networks:
  app_network:
    driver: bridge
```

Yogurt 支持三种登录方式：当前登录状态继续、快速登录和二维码登录，主要通过 `QUICK_LOGIN_UIN` 这一项来控制。Yogurt 启动后会先检查当前 PMHQ 是否已经登录。登录成功后，Yogurt 会继续执行初始化，并开始对外提供 Milky 接口。


- 填写了 `QUICK_LOGIN_UIN` 时：
  - 如果已经登录且账号与填写的 QQ 号相符，就会直接进入初始化；
  - 如果已经登录但账号与填写的 QQ 号不符，则会直接报错，避免误用账号；
  - 如果当前尚未登录，则会检查 PMHQ 的快速登录列表：
    - 在列表中找到该 QQ 号时，优先尝试快速登录；
    - 在列表中未找到该 QQ 号或快速登录失败后，回落到二维码登录。
- 不填写 `QUICK_LOGIN_UIN` 时：
  - 如果已经登录，则直接进入初始化；
  - 如果尚未登录，则进入二维码登录。

PMHQ 的 QQ 配置目录会挂载到独立卷 `qq_volume`。Yogurt 的运行目录会挂载到 `./data`，启动时自动生成 `config.json` 和脚本目录，适合长期保留登录状态与本地数据。当前发布的 Docker 镜像同时包含 `amd64` 和 `arm64` 两种架构，Docker 会按宿主机自动拉取对应的原生二进制镜像。

Yogurt 容器额外写入了 `host.docker.internal:host-gateway`，因此 webhook 或其他回调服务如果运行在宿主机上，也可以直接从容器内访问。如果你确实需要从宿主机直接访问 PMHQ，再自行给 `pmhq` 服务补端口映射即可。

其他配置选项的含义请参见下文的“配置”部分。

### 手动部署

运行前请先启动 PMHQ，并确保它的 WebSocket 服务可以访问。默认情况下，Yogurt 会连接 `ws://localhost:13000/ws`。随后启动 Yogurt；如果当前工作目录下还没有 `config.json`，程序会先生成一份默认配置文件，等待你修改后再继续。Yogurt 启动后的工作流程如上所示。

## 配置

> [!important]
> 
> 通过 Docker 部署的用户不要手动修改 `config.json`，请直接通过环境变量来配置。手动修改的结果在 Docker 容器重启后会被覆盖。

当前版本面向 PC 协议使用，内置固定使用 `Linux` 协议信息，不需要再额外配置 PC 协议版本，也不需要签名服务。最常见的配置如下：

```json5
{
  "signApiUrl": "", // 留空即可
  "pmhqUrl": "ws://localhost:13000/ws",
  "quickLoginUin": null,
  "protocol": {
    "os": "Linux",
    "version": "fetched" // 保持默认值即可，Yogurt 会在登录时自动从 PMHQ 获取协议版本信息
  },
  "androidCredentials": { // 留空即可，Yogurt-PMHQ 不使用 Android 协议
    "uin": 0,
    "password": ""
  },
  "androidUseLegacySign": false, // 留空即可，Yogurt-PMHQ 不使用 Android 协议
  "reportSelfMessage": true,
  "preloadContacts": false,
  "transformIncomingMFaceToImage": false,
  "httpConfig": {
    "host": "127.0.0.1",
    "port": 30001,
    "accessToken": "",
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

### `pmhqUrl`

表示 PMHQ 的 WebSocket 地址；如果 PMHQ 运行在其他主机或使用了不同端口，只需要修改这一项即可。

### `quickLoginUin`

可选项，用于指定优先尝试快速登录的 QQ 号。

### `preloadContacts`

用于控制是否在登录完成后预加载联系人列表。关闭时启动更快；开启后会预先拉取好友、群和部分成员信息，能改善部分接口在刚启动时的表现，但也会增加启动时间和内存占用。

### `reportSelfMessage`

表示是否上报自己发送的消息。

### `transformIncomingMFaceToImage`

表示是否将收到的市场表情转换成普通图片段。

### `httpConfig` 和 `webhookConfig`

用于配置 Yogurt 对外提供的 Milky 服务，详见 [Milky 文档的“通信”部分](https://milky.ntqqrev.org/guide/communication)。

- `httpConfig.host` 与 `httpConfig.port` 决定监听地址，默认是 `127.0.0.1:3000`。
- 如果 `httpConfig.accessToken` 为空，任何能够访问该地址的客户端都可以直接调用 API；如果需要在局域网或公网环境下使用，建议务必设置一个访问令牌。
- `httpConfig.corsOrigins` 留空时表示允许所有来源跨域访问。
- `webhookConfig.url` 可以填写一个或多个事件推送地址。
- `webhookConfig.accessToken` 会在推送时携带在 `Authorization` 请求头中。

### 日志配置

见 [Yogurt 文档的“日志配置”部分](https://acidify.ntqqrev.org/yogurt/configuration#%E6%97%A5%E5%BF%97%E9%85%8D%E7%BD%AE)。

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
- [yogurt-pmhq](https://github.com/LLOneBot/yogurt-pmhq) - Yogurt 的 Fork，使用 PMHQ 实现登录，无需签名 API
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
