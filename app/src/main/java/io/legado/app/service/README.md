# 后台服务

应用的后台服务层，处理需要长时间运行或在后台执行的任务。所有服务继承自 `BaseService`（LifecycleService），支持前台通知和权限检查。

## 文件说明

### `AudioPlayService.kt`
- **功能**: 音频播放服务，管理有声书的后台播放，处理媒体通知栏控制（播放/暂停/上下章）、音频焦点、耳机拔出暂停、锁屏控制
- **触发页面**: `AudioPlayActivity`（音频播放界面）启动此服务

### `BaseReadAloudService.kt`
- **功能**: 朗读服务基类，定义了朗读服务的通用接口（播放/暂停/上一段/下一段/停止），被 `TTSReadAloudService` 和 `HttpReadAloudService` 继承
- **触发页面**: 被具体朗读服务继承使用

### `CacheBookService.kt`
- **功能**: 书籍缓存服务，在后台批量下载书籍章节内容到本地数据库，支持前台通知显示缓存进度
- **触发页面**: `CacheActivity`（缓存管理界面）、书架批量缓存操作

### `CheckSourceService.kt`
- **功能**: 书源检测服务，在后台批量测试书源的可用性，支持前台通知显示检测进度和结果统计
- **触发页面**: 书源管理页（`BookSourceActivity`）的批量检测功能

### `DownloadService.kt`
- **功能**: 文件下载服务，在后台下载文件（如书籍附件、字体文件等），支持前台通知和断点续传
- **触发页面**: 字体下载、文件下载相关页面

### `ExportBookService.kt`
- **功能**: 书籍导出服务，在后台将书籍导出为 TXT/EPUB 等格式文件，支持前台通知显示导出进度
- **触发页面**: 书籍详情页、书架的导出功能

### `HttpReadAloudService.kt`
- **功能**: 在线朗读服务，通过 HTTP API 调用在线 TTS 引擎（如微软 TTS、百度 TTS 等）进行朗读，继承自 `BaseReadAloudService`
- **触发页面**: 阅读页朗读功能（选择在线引擎时）、`ReadAloudDialog`（朗读设置）

### `TTSReadAloudService.kt`
- **功能**: 本地 TTS 朗读服务，使用 Android 系统内置 TTS 引擎进行朗读，继承自 `BaseReadAloudService`
- **触发页面**: 阅读页朗读功能（选择本地引擎时）、`ReadAloudDialog`（朗读设置）

### `WebService.kt`
- **功能**: 内置 Web 服务，在后台运行 NanoHTTPD HTTP 服务器和 WebSocket 服务器，提供书籍管理、书源编辑等 Web 界面
- **触发页面**: 设置页中 Web 服务开关、`MainActivity` 底部导航

### `WebTileService.kt`
- **功能**: Android 快速设置磁贴服务（Quick Settings Tile），提供下拉通知栏快捷开关 Web 服务
- **触发页面**: 系统下拉通知栏的快速设置面板