# 文件结构介绍

应用主模块 `io.legado.app` 的源码目录，采用 MVVM 架构（AndroidViewModel + ViewBinding），按功能分包组织。

## 子目录

| 子目录 | 说明 |
|--------|------|
| `api/` | 外部 API 接口。通过 `ReaderProvider`（Content Provider）对外暴露书籍/章节数据，`controller/` 下的控制器供内置 Web 服务器调用 |
| `base/` | 所有 UI 组件和服务的基类。包含 Activity、Fragment、Dialog、ViewModel、RecyclerView Adapter、Service 的公共基类，定义了生命周期管理、主题应用、协程辅助等通用行为 |
| `constant/` | 全局常量定义。包括事件总线键名（`EventBus`）、意图动作（`IntentAction`）、偏好设置键名（`PreferKey`）、书源类型（`BookSourceType`）、页面动画类型（`PageAnim`）、通知ID、主题枚举等 |
| `data/` | Room 数据库层（v76，22 张表）。包含数据库定义（`AppDatabase`，全局实例 `appDb`）、DAO 接口、实体类、数据库迁移脚本 |
| `exception/` | 自定义异常类型。包括文件为空、目录缺失、内容为空、正则超时、并发等业务异常 |
| `help/` | 业务辅助工具集。涵盖配置管理、协程封装、HTTP 客户端、加解密、图片加载、存储备份、书源处理、Rhino JS 引擎、应用更新等 |
| `lib/` | 第三方库封装。包括主题引擎（`theme/`）、WebDAV 网络存储（`webdav/`）、运行时权限（`permission/`）、偏好设置 UI（`prefs/`）、Cronet 网络库（`cronet/`）、对话框（`dialogs/`）、编码识别（`icu4j/`）、MOBI 解析（`mobi/`） |
| `model/` | 业务逻辑核心层。书源规则解析引擎（`analyzeRule/`）、本地书籍解析（`localBook/`）、网络书源获取（`webBook/`）、RSS 解析（`rss/`）、远程书籍管理（`remote/`），以及音频播放、阅读状态、书籍封面等全局业务状态管理 |
| `receiver/` | 广播接收器。监听媒体按钮、耳机插拔、电量变化、时间变化、网络变化、启动完成等系统广播 |
| `service/` | 后台服务。包括音频播放、TTS 朗读、在线朗读、书籍缓存、文件下载、书源检测、书籍导出、Web 服务、快速设置磁贴等 |
| `ui/` | 界面层，按功能模块组织为 15 个子包。包含所有 Activity、Fragment、ViewModel、Adapter 及自定义 UI 组件（`widget/`） |
| `utils/` | 80+ 个 Kotlin 扩展函数文件。覆盖 Context、Activity、Fragment、View、String、File、JSON、网络、颜色、动画等各类扩展工具 |
| `web/` | 内置 Web 服务器。基于 NanoHTTPD 的 HTTP 服务器 + WebSocket 实时通信服务器，提供 REST API 和书源调试功能 |

## 顶层文件

| 文件 | 说明 |
|------|------|
| `App.kt` | Application 入口类。初始化 Cronet 网络引擎、Rhino JS 引擎、通知渠道、主题、LiveEventBus，注册全局 Activity 生命周期回调 |