# 开始使用

Yogurt 是基于 Acidify 的 [Milky](https://milky.ntqqrev.org/) 实现，支持 Kotlin/JVM 和 Kotlin/Native 平台。

> **Acid**ify + **Milk**y = Yogurt!

## 启动

Yogurt 支持的平台有 Kotlin/JVM 和 Kotlin/Native。

### 通过 Releases 下载并启动 (Kotlin/Native)

在 [Releases](https://github.com/LagrangeDev/acidify/releases) 中下载对应平台的可执行文件，解压到工作目录后运行：

```
./yogurt.kexe   (for Linux and macOS)
yogurt.exe      (for Windows)
```

支持的平台如下：

| OS      | Arch       |
|---------|------------|
| Windows | x64        |
| Linux   | x64, arm64 |
| macOS   | x64, arm64 |

### 通过包管理器安装并启动 (Kotlin/Native)

[![npm](https://img.shields.io/npm/v/%40acidify%2Fyogurt)](https://www.npmjs.com/package/@acidify/yogurt)

Yogurt 的预编译二进制包也发布在 npm 的 `@acidify/yogurt` 包中。如果你的系统上安装有 `npm` 等 Node.js 包管理器，也可以通过以下命令安装并运行 Yogurt：

```
npm install -g @acidify/yogurt
yogurt
```

同样，通过 `npm` 安装的 Yogurt 也是基于 Kotlin/Native 构建的，支持的平台如上所示。

### 通过 Java 运行时启动 (Kotlin/JVM)

配置 Java 21+ 运行时，然后在 [Releases](https://github.com/LagrangeDev/acidify/releases) 中下载 JAR 文件，运行：

```
java -jar yogurt-jvm-all.jar
```

注意：Yogurt 的 JVM 版本理论上可以在任何支持 Java 21+ 的平台上运行，但由于 Yogurt 依赖 [LagrangeCodec](https://github.com/LagrangeDev/LagrangeCodec) 的预编译构建，因此只支持在以下平台**发送语音和视频消息**：

| OS      | Arch       |
|---------|------------|
| Windows | x86, x64   |
| Linux   | x64, arm64 |
| macOS   | x64, arm64 |
