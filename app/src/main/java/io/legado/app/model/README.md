# 业务逻辑核心层

应用的核心业务逻辑，包括书源规则解析引擎、本地书籍解析、网络书源获取、RSS 解析、远程书籍管理，以及阅读、音频、漫画等全局状态管理。

## 子目录

| 子目录 | 说明 |
|--------|------|
| `analyzeRule/` | 书源规则解析引擎。支持 Jsoup（HTML）、XPath、JsonPath、Regex 四种解析方式，`AnalyzeRule` 为统一入口，`AnalyzeUrl` 处理 URL 拼接和参数替换，`RuleAnalyzer` 为规则语法分析器，`QueryTTF`（Java）处理字体反爬 |
| `localBook/` | 本地书籍解析。支持 TXT、EPUB、PDF、UMD、MOBI 格式的导入和解析 |
| `remote/` | 远程书籍管理。`RemoteBook` 为远程文件管理入口，`RemoteBookWebDav` 通过 WebDAV 协议访问远程书籍文件 |
| `rss/` | RSS 订阅解析。`Rss` 为 RSS 操作入口，`RssParserDefault` 解析标准 RSS/Atom 格式，`RssParserByRule` 按自定义规则解析 |
| `webBook/` | 网络书源获取。`WebBook` 为网络请求入口，`SearchModel`（搜索）、`BookList`（列表）、`BookInfo`（详情）、`BookChapterList`（目录）、`BookContent`（正文）分别处理各环节的网络请求和规则解析 |

## 顶层文件说明

### `AudioPlay.kt`
- **功能**: 音频播放全局状态管理，维护当前播放的有声书、播放状态、播放进度等
- **使用页面**: `AudioPlayActivity`（音频播放界面）、`AudioPlayService`（播放服务）

### `BookCover.kt`
- **功能**: 书籍封面获取与缓存管理，支持从书源规则获取封面 URL、默认封面生成（绘制书名/作者）、Glide 缓存策略
- **使用页面**: 书架、书籍详情、搜索结果、阅读页等所有展示封面的页面

### `CacheBook.kt`
- **功能**: 书籍缓存管理，负责批量缓存书籍章节内容到本地数据库，支持并发控制和进度回调
- **使用页面**: `CacheActivity`（缓存管理界面）、书架批量缓存

### `CheckSource.kt`
- **功能**: 书源批量检测，自动测试书源的可用性（搜索、发现、详情等功能是否正常），支持并发和超时控制
- **使用页面**: `CheckSourceService`（检测服务）、书源管理页（`BookSourceActivity`）

### `Debug.kt`
- **功能**: 书源调试器，逐步执行书源规则（搜索→详情→目录→正文），输出每步的执行结果和耗时
- **使用页面**: 书源编辑页（`BookSourceEditActivity`）的调试功能、Web 端书源调试

### `Download.kt`
- **功能**: 文件下载管理器，管理下载任务的创建、暂停、恢复、取消
- **使用页面**: `DownloadService`（下载服务）、缓存页面

### `ImageProvider.kt`
- **功能**: 图片内容提供器，为阅读页提供漫画/图片章节的图片数据，支持缓存和预加载
- **使用页面**: 阅读页（`ReadBookActivity`）、漫画阅读页（`ReadMangaActivity`）

### `ReadAloud.kt`
- **功能**: 朗读全局状态管理，维护朗读状态（播放/暂停）、当前朗读位置、朗读引擎类型（TTS/在线）
- **使用页面**: 阅读页朗读功能、`ReadAloudDialog`（朗读设置）、`SpeakEngineDialog`（引擎选择）

### `ReadBook.kt`
- **功能**: 阅读全局状态管理（核心），维护当前阅读的书籍、章节列表、阅读进度、章节切换逻辑，是阅读功能的中枢
- **使用页面**: `ReadBookActivity`（阅读界面）、书架（跳转阅读）、搜索结果（跳转阅读）、书籍详情页

### `ReadManga.kt`
- **功能**: 漫画阅读全局状态管理，类似 `ReadBook` 但针对漫画场景，管理漫画章节和图片加载
- **使用页面**: `ReadMangaActivity`（漫画阅读界面）

### `SharedJsScope.kt`
- **功能**: 共享 JS 作用域管理，为书源规则中的 JS 脚本提供共享的执行上下文，支持跨书源共享变量
- **使用页面**: 书源规则执行（`analyzeRule/` 调用）

