# Plan: 从 legado-with-MD3 迁移改进功能到 legado

**状态**: Design 阶段
**目标**: 从 legado-with-MD3 参考实现，改进 legado 项目的功能：
1. MD 文档查看弹窗 → 改为底部弹出式
2. 阅读记录页功能 → Compose 重建，UI 样式照搬 MD3
3. 页面切换动画 → Activity 跳转 + MainActivity Tab 切换
4. 书架→书籍详情封面共享元素动画（新增）

**约束**: 保持 legado 现有颜色设置不变（ThemeStore 体系），新功能适配现有主题系统。

> **变更记录**: 已移除原计划中的 Phase 2b（书籍详情页阅读记录/新增数据表），新增 Phase 4（封面动画）。

---

## 调研总结

### 现有代码对比

| 功能 | legado (原版) | legado-with-MD3 (参考) |
|------|--------------|----------------------|
| MD 弹窗 | `TextDialog` (BaseDialogFragment) + Markwon 库, 全屏对话框 | `MarkdownSheet` (Compose AppModalBottomSheet) + com.mikepenz.markdown.m3 |
| 阅读记录页 | `ReadRecordActivity` (View-based), 热力图+柱状图+统计+列表 | `ReadRecordScreen` (Compose), GlassCard + 热力图 + 统计 + 封面列表 |
| 封面动画 | 无 | `SharedTransitionLayout` via `bookCoverSharedElementKey` |
| 页面动画 | 无自定义动画 | NavDisplay transitionSpec: slide+fade (480ms), pop: scale+fade |

### 主题系统

- **legado**: ThemeStore (SharedPreferences 持久化) → MaterialValueHelper 扩展属性
- **legado Compose 桥接**: `rememberLegadoColorScheme()` 从 ThemeStore 读取 → M3 ColorScheme — 新 Compose 页面自动适配现有颜色

### MD3 阅读记录页架构（参考来源）

```
ui/book/readRecord/
├── ReadRecordScreen.kt         ← AppScaffold + CollapsibleHeader + LazyColumn
├── ReadRecordViewModel.kt      ← MVI ViewModel
├── ReadRecordOverviewScreen.kt ← 概览/统计页
├── ReadRecordFormatter.kt      ← 格式化工具
└── component/
    ├── ReadRecordSummary.kt    ← GlassCard 阅读概览卡片
    ├── ReadRecordStats.kt      ← 统计数字
    ├── ReadRecordListItems.kt  ← 书籍条目（CoilBookCover 48x64dp + 书名 + 作者 + 时长）
    ├── ReadRecordHeatmap.kt    ← 热力图
    └── ReadRecordCharts.kt     ← 图表
```

---

## 实施计划

### Phase 1: MD 文档查看弹窗 — 改为底部弹出式

**目标**: 将 MD 文档查看从全屏对话框改为底部半屏面板（Bottom Sheet），保留 Markwon 引擎，适配 ThemeStore 主题色。

**步骤**:

1. **创建 `MarkdownBottomSheetDialog`** (新建 `ui/widget/dialog/MarkdownBottomSheetDialog.kt`)
   - 继承 `BottomSheetDialogFragment`（MaterialComponents）
   - 复用 `TextDialog` 的 Markwon 构建逻辑（CoilImagesPlugin + HtmlPlugin + TablePlugin）
   - Toolbar 背景: `context.primaryColor`；面板背景: `context.popupBackgroundColor`
   - 文字颜色: `context.primaryTextColor` / `context.secondaryTextColor`
   - 构造参数: `(title: String, content: String)`

2. **更新调用处**
   - `AboutFragment.showMdFile()` → 改用 `MarkdownBottomSheetDialog`
   - `FileAssociationActivity.kt` 中 `storageHelp.md` → 改用底部面板
   - 其他（UpdateDialog、AppLogDialog、CrashLogsDialog）保持使用 TextDialog

**参考文件**: `legado-with-MD3/ui/about/AboutSheets.kt:37-70`、`legado/ui/widget/dialog/TextDialog.kt`

---

### Phase 2: 阅读记录页 — Compose 重建，UI 照搬 MD3

**目标**: 将 `ReadRecordActivity` 从 View-based 改为 Compose 实现，UI 样式参考 MD3 的 `ReadRecordScreen`，数据层**复用现有 DAO（无需新增表）**，颜色通过 `LegadoTheme.colorScheme` 自动适配 ThemeStore。

**MD3 样式要点**:
- `AppScaffold` + `GlassMediumFlexibleTopAppBar` 顶栏
- `CollapsibleHeader` 可折叠头部区域（热力图 + 统计卡片）
- 统计卡片：今日阅读时长、连续天数、总书籍数
- 书籍列表：`CoilBookCover` (48×64dp) + 书名 + 作者 + 阅读时长 + 最后阅读时间
- 搜索栏 + 排序切换（按名称/阅读时长/最近阅读）
- 下拉刷新 + 分页加载

**步骤**:

1. **创建 Compose 组件** (`ui/book/readRecord/` — 新建目录)
   - `ReadRecordScreen.kt` — 主 Composable（替代 `activity_read_record.xml`）
   - `ReadRecordViewModel.kt` — ViewModel（迁移 `ReadRecordActivity` 中的逻辑）
   - `ReadRecordContract.kt` — UiState + Intent + Effect
   - `component/ReadRecordSummary.kt` — 统计概览卡片
   - `component/ReadRecordListItems.kt` — 书籍列表项（封面 + 书名 + 时长）
   - `component/ReadRecordHeatmap.kt` — 热力图

2. **改造 `ReadRecordActivity`**
   - 继承 `BaseComposeActivity`（替代 `BaseActivity`）
   - `setContent { ReadRecordScreen(...) }`
   - 原数据加载逻辑迁移到 ViewModel

3. **数据层复用**
   - `ReadRecordDao`（`allTime`, `allShow`, `searchPaged`, `getReadTime` 等）
   - `DailyReadRecordDao`（`sumDailyByDateRange`）
   - **无需新增表或数据库迁移**

4. **主题适配**: Compose 组件通过 `LegadoTheme.colorScheme` 自动适配 ThemeStore 颜色

**参考文件**: `legado-with-MD3/ui/book/readRecord/`（全部） + `legado/ui/about/ReadRecordActivity.kt`（数据逻辑）

---

### Phase 3a: Activity 跳转动画 (XML 动画)

**目标**: 为所有 Activity 跳转添加统一的 slide+fade 转场动画。

**步骤**:

1. **创建动画资源** (`res/anim/`)
   - `slide_in_right.xml` — 右侧滑入 + fade in (360ms, `fast_out_slow_in`)
   - `slide_out_left.xml` — 左侧滑出 + fade out (360ms)
   - `slide_in_left.xml` — 左侧滑入 + fade in (360ms，用于返回)
   - `slide_out_right.xml` — 右侧滑出 + fade out (360ms，用于返回)

2. **统一应用**
   - `ContextExtensions.kt` 的 `startActivity<T>()` 中添加 `overridePendingTransition`
   - `BaseActivity.finish()` 中添加返回动画

3. **排除**: `ReadBookActivity`、`WelcomeActivity` 不应用动画

---

### Phase 3b: MainActivity Tab 切换动画 (ViewPager PageTransformer)

**目标**: 为 MainActivity 底部导航 Tab 切换添加过渡动画。

**步骤**:

1. **创建 `FadeSlidePageTransformer`** (`ui/widget/FadeSlidePageTransformer.kt`)
   - 实现 `ViewPager.PageTransformer`，fade+scale 效果
   - E-Ink 模式禁用

2. **应用到 MainActivity**: `binding.viewPagerMain.setPageTransformer(...)`

---

### Phase 4: 书架→书籍详情 封面共享元素动画（新增）

**目标**: 从书架点击书籍时，封面从列表位置"飞入"详情页 Hero Header，实现类似 MD3 的 `SharedTransitionLayout` 效果。

**架构差异（关键）**:

| | MD3 | legado |
|---|---|---|
| 书架 | `NavDisplay` 路由（同 Activity） | `BookshelfComposeFragment` (MainActivity 内) |
| 详情页 | `NavDisplay` 路由（同 Activity） | `BookInfoComposeActivity`（**独立 Activity**） |
| 动画 | `SharedTransitionLayout`（同 Composition 树） | ❌ 无法跨 Activity 使用 |

**推荐方案: Activity 共享元素过渡**（`ActivityOptions.makeSceneTransitionAnimation` + `MaterialContainerTransform`）

```kotlin
// 书架点击时
val options = ActivityOptions.makeSceneTransitionAnimation(
    activity, coverView to "cover_${book.bookUrl}"
)
startActivity(intent, options.toBundle())
```

**步骤**:

1. **创建 Key 工具** (`ui/main/BookCoverSharedElement.kt`)
   ```kotlin
   fun bookCoverSharedElementKey(bookUrl: String, sourceId: String? = null): String {
       val src = sourceId?.takeIf { it.isNotBlank() } ?: return "book-cover:$bookUrl"
       return "book-cover:$src:$bookUrl"
   }
   ```

2. **书架侧** (`BookshelfScreen.kt`)
   - 封面 Composable 上设置 `transitionName = bookCoverSharedElementKey(bookUrl)`
   - 点击时构造 `ActivityOptions.makeSceneTransitionAnimation()` 启动详情页

3. **详情页侧** (`BookInfoComposeActivity` + `HeroHeader.kt`)
   - 封面 Composable 设置相同的 `transitionName`
   - `onCreate` 中设置 `MaterialContainerTransformSharedElementCallback`
   - 入场动画使用 fade-in（避免与共享元素冲突）

**参考文件**: 
- `legado-with-MD3/ui/main/BookCoverSharedElement.kt`
- `legado-with-MD3/ui/book/audio/AudioPlayActivity.kt:132-139`

---

## 实施顺序

```
Phase 1 (MD Bottom Sheet) ──┐
                             ├── 可并行
Phase 3a (Activity Anim) ───┘

Phase 2 (ReadRecord Compose) ── 独立，先行

Phase 3b (Tab Anim) ── 独立小改动

Phase 4 (Cover Shared Element) ── 最后，需书架和详情页配合
```

**推荐顺序**: Phase 1 → Phase 3a → Phase 2 → Phase 3b → Phase 4

---

## 关键文件修改清单

| 文件 | 操作 | Phase |
|------|------|-------|
| `ui/widget/dialog/MarkdownBottomSheetDialog.kt` | **新建** | 1 |
| `ui/about/AboutFragment.kt` | 修改 showMdFile() | 1 |
| `ui/association/FileAssociationActivity.kt` | 修改 storageHelp 展示 | 1 |
| `ui/book/readRecord/ReadRecordScreen.kt` | **新建** (Compose) | 2 |
| `ui/book/readRecord/ReadRecordViewModel.kt` | **新建** | 2 |
| `ui/book/readRecord/ReadRecordContract.kt` | **新建** | 2 |
| `ui/book/readRecord/component/ReadRecordSummary.kt` | **新建** | 2 |
| `ui/book/readRecord/component/ReadRecordListItems.kt` | **新建** | 2 |
| `ui/book/readRecord/component/ReadRecordHeatmap.kt` | **新建** | 2 |
| `ui/about/ReadRecordActivity.kt` | 改为 BaseComposeActivity | 2 |
| `res/anim/slide_in_right.xml` | **新建** | 3a |
| `res/anim/slide_out_left.xml` | **新建** | 3a |
| `res/anim/slide_in_left.xml` | **新建** | 3a |
| `res/anim/slide_out_right.xml` | **新建** | 3a |
| `utils/ContextExtensions.kt` | startActivity 添加动画 | 3a |
| `ui/widget/FadeSlidePageTransformer.kt` | **新建** | 3b |
| `ui/main/MainActivity.kt` | 设置 PageTransformer | 3b |
| `ui/main/BookCoverSharedElement.kt` | **新建** (key 生成) | 4 |
| `ui/main/bookshelf/compose/BookshelfScreen.kt` | 封面 transitionName + ActivityOptions | 4 |
| `ui/book/info/compose/HeroHeader.kt` | 封面 transitionName | 4 |
| `ui/book/info/compose/BookInfoComposeActivity.kt` | 启用共享元素过渡 | 4 |

---

## 决策记录

1. **MD 弹窗**: View-based `BottomSheetDialogFragment`（复用 Markwon），不用 Compose
2. **阅读记录页**: 升级为 Compose（UI 照搬 MD3），数据层复用现有 DAO，**不新增数据表**
3. **封面动画**: 采用 Activity `SharedElement` Transition，不改变现有 Activity 架构
4. **动画降级**: E-Ink 模式自动禁用所有过渡动画
5. **范围排除**: ReadBookActivity 动画、UpdateDialog 全屏样式、DampedDragAnimation 均不迁移
