# 业务辅助工具集

提供应用各模块共用的业务辅助工具，涵盖配置管理、协程封装、网络请求、加解密、图片加载、数据备份、书源处理等。

## 子目录

| 子目录 | 说明 |
|--------|------|
| `book/` | 书籍内容处理。包括章节内容解析（`ContentProcessor`）、内容替换净化（`ContentHelp`）、书籍元数据操作（`BookHelp`、`BookExtensions`） |
| `config/` | 应用配置管理。`AppConfig`（全局应用配置）、`ReadBookConfig`（阅读界面配置）、`ThemeConfig`（主题颜色配置）、`ReadTipConfig`（阅读提示配置）、`SourceConfig`（书源配置）、`LocalConfig`（本地配置） |
| `coroutine/` | 自定义协程封装。`Coroutine`（协程任务包装，支持取消/生命周期）、`CompositeCoroutine`（协程组管理）、`CoroutineContainer`（协程容器接口） |
| `crypto/` | 加解密工具。对称加密（AES/DES）、非对称加密（RSA）、数字签名，主要供书源规则中的 JS 脚本调用 |
| `exoplayer/` | ExoPlayer 音频播放辅助。`ExoPlayerHelper`（播放器配置）、`InputStreamDataSource`（自定义数据源） |
| `glide/` | Glide 图片加载定制。自定义 Model Loader（`OkHttpModelLoader`、`LegadoDataUrlLoader`）、模糊变换（`BlurTransformation`）、图片加载器（`ImageLoader`）、进度监听（`progress/`） |
| `http/` | HTTP 客户端封装。`HttpHelper`（OkHttp 单例）、`Cronet`（Cronet 引擎集成，支持 HTTP/3）、`CookieStore`（Cookie 管理）、`SSLHelper`（SSL 证书处理）、`BackstageWebView`（后台 WebView 请求）、`OkHttpUtils`（请求工具） |
| `rhino/` | Rhino JS 引擎封装。`NativeBaseSource` 提供书源 JS 脚本可调用的原生方法（网络请求、加密、存储等） |
| `source/` | 书源/RSS 源扩展函数。`BookSourceExtensions`（书源操作扩展）、`RssSourceExtensions`（RSS 源操作扩展）、`SourceHelp`（源管理辅助）、`SourceVerificationHelp`（源验证） |
| `storage/` | 数据备份恢复。`Backup`（备份到本地/WebDAV）、`Restore`（从本地恢复）、`BackupAES`（AES 加密备份）、`BackupConfig`（备份配置）、`ImportOldData`（导入旧版数据） |
| `update/` | 应用更新检查。`AppUpdate`（更新检查入口）、`AppUpdateGitHub`（GitHub Release 更新源）、`AppReleaseInfo`（版本信息解析） |

## 顶层文件说明

### `AppFreezeMonitor.kt`
- **功能**: 应用冻结监控，检测应用是否被系统冻结或无响应
- **使用页面**: 全局生效

### `AppWebDav.kt`
- **功能**: WebDAV 操作封装，提供自动备份到 WebDAV、同步阅读进度等功能
- **使用页面**: 备份恢复设置、阅读进度云同步

### `CacheManager.kt`
- **功能**: 缓存管理器，管理章节内容缓存和图片缓存的读写、清理
- **使用页面**: 阅读页、缓存管理页、设置页

### `ConcurrentRateLimiter.kt`
- **功能**: 并发速率限制器，控制书源检测等并发操作的速率
- **使用页面**: 书源检测（`CheckSource`）

### `CrashHandler.kt`
- **功能**: 全局未捕获异常处理器，将崩溃日志写入本地文件
- **使用页面**: 全局生效，崩溃日志在"关于页 > 崩溃日志"查看

### `DefaultData.kt`
- **功能**: 默认数据初始化，包括默认书源分组、默认 TXT 目录规则、默认替换规则等
- **使用页面**: 应用首次启动时初始化

### `DirectLinkUpload.kt`
- **功能**: 直链上传，将图片等内容上传到图床获取直链
- **使用页面**: 书源编辑、分享功能

### `DispatchersMonitor.kt`
- **功能**: 协程调度器监控，检测协程调度器的健康状态
- **使用页面**: 全局生效

### `EventMessage.kt`
- **功能**: LiveEventBus 事件消息定义，定义跨组件通信的消息类型
- **使用页面**: 全局使用，通过 `LiveEventBus` 进行事件分发

### `ExecutorService.kt`
- **功能**: 全局线程池配置，提供应用级别的线程池管理
- **使用页面**: 全局生效

### `IntentData.kt`
- **功能**: Intent 数据辅助，处理 Activity 间传递的复杂数据
- **使用页面**: 各 Activity/Fragment 间数据传递

### `IntentHelp.kt`
- **功能**: Intent 构建辅助，封装常用的 Intent 创建方法
- **使用页面**: 各页面的跳转逻辑

### `JsEncodeUtils.kt`
- **功能**: JS 编码工具，提供 Base64、URL 编码等 JS 脚本中常用的编码方法
- **使用页面**: 书源规则 JS 脚本执行

### `JsExtensions.kt`
- **功能**: JS 扩展函数，为 Rhino JS 引擎提供额外的原生方法（如 `java.ajax`、`java.cookies` 等）
- **使用页面**: 书源规则 JS 脚本执行

### `LauncherIconHelp.kt`
- **功能**: 应用图标管理，支持动态切换启动图标（自适应图标）
- **使用页面**: 设置页 > 其他设置

### `LayoutManager.kt`
- **功能**: RecyclerView LayoutManager 辅助，管理书架网格/列表布局切换
- **使用页面**: 书架页面

### `LifecycleHelp.kt`
- **功能**: 生命周期辅助工具，简化生命周期观察者的注册
- **使用页面**: 全局使用

### `MediaHelp.kt`
- **功能**: 媒体播放辅助，处理音频焦点、媒体会话等
- **使用页面**: 音频播放、TTS 朗读

### `PaintPool.kt`
- **功能**: Paint 对象池，复用 Paint 对象减少 GC 压力
- **使用页面**: 阅读页自定义绘制

### `ReplaceAnalyzer.kt`
- **功能**: 替换规则分析器，解析和执行文本替换净化规则
- **使用页面**: 阅读页内容净化、替换规则管理

### `RuleBigDataHelp.kt`
- **功能**: 书源大数据辅助，处理书源规则中的大数据量场景
- **使用页面**: 书源规则执行

### `RuleComplete.kt`
- **功能**: 书源规则补全，自动补全不完整的书源规则
- **使用页面**: 书源编辑、书源导入

### `TTS.kt`
- **功能**: TTS（Text-to-Speech）封装，统一管理本地 TTS 和在线 TTS 引擎的切换
- **使用页面**: 朗读功能（`ReadAloudDialog`、`SpeakEngineDialog`）