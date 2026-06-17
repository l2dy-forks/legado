# 第三方库封装

对第三方库的封装和定制，使其适配应用的需求。部分为修改后的源码内嵌（如 icu4j），部分为接口封装。

## 子目录

### `aliyun/`
- **说明**: 阿里云 SDK 相关封装
- **使用页面**: 涉及阿里云服务的功能

### `cronet/`
- **说明**: Google Cronet 网络库封装，提供 HTTP/3 和 QUIC 协议支持。包含协程拦截器（`CronetCoroutineInterceptor`）、OkHttp 兼容拦截器（`CronetInterceptor`）、原生库加载器（`CronetLoader`）、回调封装（`AbsCallBack`、`NewCallBack`）等
- **使用页面**: 全局网络请求（`HttpHelper` 中集成），当 Cronet 可用时自动使用其替代 OkHttp 默认连接

### `dialogs/`
- **说明**: 对话框构建器封装，提供跨版本一致的 AlertDialog/Selector API。包括 `AlertBuilder`（对话框构建接口）、`AndroidAlertBuilder`（平台实现）、`AndroidSelectors`（选择器）、`SelectItem`（选择项模型）
- **使用页面**: 全局使用，所有需要弹出对话框的页面

### `icu4j/`
- **说明**: ICU4J 编码识别库的精简内嵌版本（Java），用于自动检测文本文件编码。包含 `CharsetDetector`、`CharsetMatch` 及多种编码识别器（UTF-8、Unicode、MBCS、SBCS、ISO-2022 等）
- **使用页面**: 本地 TXT 文件导入（`TextFile`）、文本编码检测（`EncodingDetect`）

### `mobi/`
- **说明**: MOBI 电子书格式解析库。支持 MOBI/KF8 格式，包括 `MobiBook`（MOBI 书籍模型）、`KF8Book`（KF8 格式）、`MobiReader`（解析器）、`PDBFile`（PDB 容器解析），以及 `decompress/`（解压缩）、`entities/`（实体）、`utils/`（工具）
- **使用页面**: 本地 MOBI 文件导入（通过 `LocalBook` 调用）

### `permission/`
- **说明**: 运行时权限请求封装，提供简洁的链式 API。包括 `PermissionsCompat`（权限请求入口）、`Request`（请求构建器）、`PermissionActivity`（透明权限请求 Activity）、各种回调接口
- **使用页面**: 全局使用，所有需要运行时权限的页面（存储权限、通知权限、蓝牙权限等）

### `prefs/`
- **说明**: 偏好设置 UI 组件库。包括自定义 Preference 组件（`ColorPreference`、`SwitchPreference`、`IconListPreference`、`EditTextPreference`、`NameListPreference`、`PreferenceCategory`）和对应的 Dialog（`EditTextPreferenceDialog`、`ListPreferenceDialog`、`MultiSelectListPreferenceDialog`），以及 `fragment/`（PreferenceFragment 基类）
- **使用页面**: 所有设置页面（`ThemeConfigFragment`、`OtherConfigFragment`、`BackupConfigFragment`、`CoverConfigFragment`、`WelcomeConfigFragment` 等）

### `theme/`
- **说明**: 主题引擎系统。`ThemeColors`（不可变颜色快照，内存缓存层，一次 SP 读取全部 20+ 种颜色）、`ThemeManager`（StateFlow 响应式颜色源，Compose 通过 collectAsState 消费）、`ThemeStore`（主题颜色持久化，基于 SharedPreferences，现通过 ThemeColors 缓存读取）、`ThemeStoreInterface`（主题存储接口）、`ThemeStorePrefKeys`（持久化键名常量）、`TintHelper`（控件着色辅助，覆盖所有标准 Material 组件）、`Selector`（StateListDrawable/ColorStateList 构建器）、`MaterialValueHelper`（Context/Fragment 扩展属性：`primaryColor`、`accentColor`、`backgroundColor` 等）、`ThemeUtils`（主题工具）、`M3ColorHelper`（Material 3 颜色辅助）、`view/`（自动着色控件：`ThemeCheckBox`、`ThemeSwitch`、`ThemeEditText`、`ThemeSeekBar`、`ThemeRadioButton`、`ThemeProgressBar`、`ThemeBottomNavigationVIew`）
- **使用页面**: 全局生效，所有界面的主题颜色和控件着色

### `webdav/`
- **说明**: WebDAV 网络存储客户端。`WebDav`（WebDAV 操作核心：上传、下载、列举、删除）、`Authorization`（认证信息）、`WebDavFile`（文件模型）、`WebDavException`（异常类型）
- **使用页面**: 备份恢复到 WebDAV（`Backup`、`Restore`）、远程书籍管理（`RemoteBookWebDav`）、阅读进度云同步（`AppWebDav`）