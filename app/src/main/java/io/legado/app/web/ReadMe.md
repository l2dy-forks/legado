# 内置 Web 服务

应用内置的 Web 服务器，基于 NanoHTTPD 提供 HTTP REST API 和 WebSocket 实时通信。用户可通过浏览器远程管理书籍、编辑书源、调试规则等。默认端口通过 `AppConfig` 配置。

## 子目录

| 子目录 | 说明 |
|--------|------|
| `socket/` | WebSocket 处理器。`BookSearchWebSocket`（远程书源搜索）、`BookSourceDebugWebSocket`（书源规则调试，逐步执行并返回结果）、`RssSourceDebugWebSocket`（RSS 源调试） |
| `utils/` | Web 工具。`AssetsWeb`（从 APK assets 目录提供前端静态资源服务） |

## 文件说明

### `HttpServer.kt`
- **功能**: NanoHTTPD HTTP 服务器核心，注册 REST API 路由，处理 HTTP 请求。API 涵盖书籍管理（增删改查）、书源管理、替换规则、RSS 源、文件操作、搜索等。前端使用 Vue 3 + Element Plus 构建（`modules/web/`），构建产物放置在 `assets/web/vue/`
- **触发页面**: `WebService`（后台服务）启动此服务器，设置页中 Web 服务开关控制

### `WebSocketServer.kt`
- **功能**: WebSocket 服务器，提供实时双向通信。主要用于书源调试场景，客户端（Web 前端）发送调试指令，服务端逐步执行书源规则并实时返回结果
- **触发页面**: `WebService`（后台服务）启动此服务器，Web 前端的书源调试页面连接