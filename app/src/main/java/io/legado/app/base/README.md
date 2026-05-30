# 基类

所有 Activity、Fragment、Dialog、ViewModel、Service 和 RecyclerView Adapter 的公共基类。提供生命周期管理、主题应用、协程辅助等通用行为。

## 子目录

| 子目录 | 说明 |
|--------|------|
| `adapter/` | RecyclerView 适配器基类。包含 `RecyclerAdapter`（主适配器，支持 Header/Footer/Diff 更新）、`DiffRecyclerAdapter`（基于 AsyncListDiffer 的简化适配器）、`ItemViewHolder`（共享 ViewHolder）及 `animations/`（列表项动画） |

## 文件说明

### `AppContextWrapper.kt`
- **功能**: Context 包装器，处理运行时语言切换和主题配置，确保 Context 持有正确的 Locale 和主题信息
- **使用页面**: 全局生效，通过 `App.kt` 中的 `attachBaseContext()` 注入

### `BaseActivity.kt`
- **功能**: Activity 基类，处理全屏模式、主题应用、系统栏颜色、背景图片、菜单着色。子类通过 `onActivityCreated()` 替代 `onCreate()`。构造参数：`fullScreen`、`theme`（Theme 枚举）、`toolBarTheme`、`transparent`、`imageBg`
- **使用页面**: 所有 Activity 的间接基类（通过 `VMBaseActivity`）

### `BaseFragment.kt`
- **功能**: Fragment 基类，通过布局 ID 构造。子类实现 `onFragmentCreated()` 替代 `onViewCreated()`，提供 `setSupportToolbar()` 设置 Fragment 级别的工具栏
- **使用页面**: 所有 Fragment 的间接基类（通过 `VMBaseFragment`）

### `BaseDialogFragment.kt`
- **功能**: Dialog Fragment 基类，自动设置圆角背景（`filletBackground`）、处理 E-Ink 模式适配、提供 `execute()` 协程辅助方法
- **使用页面**: 所有对话框（如 `TextDialog`、`CodeDialog`、`ReadStyleDialog`、`BgTextConfigDialog` 等）

### `BasePrefDialogFragment.kt`
- **功能**: 偏好设置风格的 Dialog 基类，比 `BaseDialogFragment` 更轻量，用于简单的偏好设置弹窗
- **使用页面**: 偏好设置相关的弹窗组件

### `BaseService.kt`
- **功能**: 后台服务基类，继承 `LifecycleService`，处理前台通知显示和权限检查
- **使用页面**: 所有后台服务（`AudioPlayService`、`TTSReadAloudService`、`CacheBookService`、`DownloadService`、`WebService` 等）

### `BaseViewModel.kt`
- **功能**: ViewModel 基类，继承 `AndroidViewModel`，提供 `execute()`、`executeLazy()`、`submit()` 等协程辅助方法，内部封装了自定义的 `Coroutine` 包装器
- **使用页面**: 所有 ViewModel 的间接基类

### `VMBaseActivity.kt`
- **功能**: 带 ViewModel 的 Activity 基类，继承 `BaseActivity`，要求子类提供 `viewModel` 属性。通过泛型同时绑定 ViewBinding 和 ViewModel
- **使用页面**: 所有需要 ViewModel 的 Activity（如 `ReadBookActivity`、`SearchActivity`、`BookInfoActivity`、`ConfigActivity` 等）

### `VMBaseFragment.kt`
- **功能**: 带 ViewModel 的 Fragment 基类，继承 `BaseFragment`，要求子类提供 `viewModel` 属性
- **使用页面**: 所有需要 ViewModel 的 Fragment（如 `MainFragment`、`BookshelfFragment`、`ExploreFragment` 等）