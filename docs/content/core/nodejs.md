# 在 Node.js 中使用

## 引入依赖

[![npm](https://img.shields.io/npm/v/%40acidify%2Fcore)](https://www.npmjs.com/package/@acidify/core)

Acidify 支持导出到 Kotlin/JS 平台，除了在 Maven Central 发布了 `acidify-core-js` 库之外，还在 NPM Registry 发布了供纯 JavaScript 开发者使用的 `@acidify/core`，包含了 Acidify 的核心功能的 JavaScript 实现，可以在 Node.js 16+ 环境中使用。可以通过以下命令在项目中引入：

```bash
npm install @acidify/core
```

`@acidify/core` 包含完整的 TypeScript 类型定义，因此可以在 TypeScript 项目中使用。

<details>

<summary>为什么 build.gradle.kts 中写了 useEsModule()，但发布的包还是 CommonJS 模块？</summary>

这是 Ktor 目前[尚未解决的一个问题](https://youtrack.jetbrains.com/issue/KTOR-9082)。Ktor 的代码中使用了 `eval("require")("node:net")` 的方式来动态加载 Node.js 内置模块，而这种写法在 CommonJS 模块中才能工作。我们在构建中使用了 `useEsModule()` 来生成 ES 模块版本的包，但由于这个问题，最终发布的包仍然是通过 ESBuild 转换成的 CommonJS 模块。

</details>
