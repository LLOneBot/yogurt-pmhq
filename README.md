<div align="center">

![acidify](https://socialify.git.ci/LagrangeDev/acidify/image?custom_description=&description=1&font=Inter&forks=1&issues=1&language=1&logo=https%3A%2F%2Fstatic.live.moe%2Flagrange.jpg&name=1&owner=1&pulls=1&stargazers=1&theme=Light)

[![QQ 群](https://img.shields.io/badge/QQ_Group-570335215-green?logo=qq)](https://qm.qq.com/q/C04kPQzayk)
[![Telegram](https://img.shields.io/badge/Telegram-WeavingStar-orange?logo=telegram)](https://t.me/WeavingStar)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LagrangeDev/acidify)

</div>

## 使用

### 通过 Milky 协议对接其他框架

使用 **[Yogurt](yogurt/)**。参考 Yogurt 的文档下载并启动 Yogurt。

### 基于 `acidify-core` 使用 Kotlin 进行开发

[![Maven Central](https://img.shields.io/maven-central/v/org.ntqqrev/acidify-core?label=Maven%20Central&logo=maven&color=blue)](https://central.sonatype.com/artifact/org.ntqqrev/acidify-core)

对于 JVM 项目，在 `build.gradle.kts` 中添加以下依赖：

```kotlin
dependencies {
    implementation("org.ntqqrev:acidify-core:$version")
}
```

对于 Kotlin Multiplatform 项目，在 `build.gradle.kts` 中添加以下依赖：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.ntqqrev:acidify-core:$version")
        }
    }
}
```

> [!tip]
> 使用时，你需要在项目中添加一个**支持 SSL 的** Ktor Client 引擎依赖。特别需要注意的是，`CIO` 引擎在 Native 平台下不支持 SSL，因此在 Native 下使用时，需要指定 CIO 之外的引擎，参见 [yogurt 的 build.gradle.kts](yogurt/build.gradle.kts)。

## 模块一览

- `acidify-core` - PC NTQQ 协议的核心实现
- `yogurt` - 基于 Acidify 的 Milky 实现
- `yogurt-jvm` - Yogurt 的 JVM 平台实现 (Workaround for Ktor plugin's incompatibility issue)
- `yogurt-media-codec` - Yogurt 的多媒体编解码支持模块
- `yogurt-qrcode` - Yogurt 二维码矩阵生成
- `docs` - 项目文档

## 支持平台

- Kotlin/JVM
- Kotlin/Native
    - Windows via `mingwX64`
    - macOS via `macosX64` and `macosArm64`
    - Linux via `linuxX64` and `linuxArm64`

## Special Thanks

- [Lagrange.Core](https://github.com/LagrangeDev/Lagrange.Core) 提供项目的基础架构和绝大多数协议包定义
- [Konata.Core](https://github.com/KonataDev/Konata.Core) 最初的 PC NTQQ 协议实现
- [lagrange-kotlin](https://github.com/LagrangeDev/lagrange-kotlin) 提供 TEA & 登录认证的实现
- [qrcode-kotlin](https://github.com/g0dkar/qrcode-kotlin/) 提供二维码矩阵生成的实现
- [LagrangeCodec](https://github.com/LagrangeDev/LagrangeCodec) 提供多媒体编解码的实现
- [@Linwenxuan04](https://github.com/Linwenxuan04) 编写 crypto 和 multiprecision 部分
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
