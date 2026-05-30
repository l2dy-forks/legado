# 数据层

Room 数据库层（v76，22 张表），提供本地持久化存储。全局数据库实例通过 `AppDatabase.kt` 中的 `appDb` 访问。

## 子目录

| 子目录 | 说明 |
|--------|------|
| `dao/` | 数据访问对象（22 个 DAO），定义了对各实体表的增删改查操作，支持 Room 的 `@Query`、`@Insert`、`@Delete`、`@Update` 注解 |
| `entities/` | 数据实体类（30+），定义了数据库表结构和数据模型。包含 `rule/` 子目录存放书源规则相关实体 |

## 文件说明

### `AppDatabase.kt`
- **功能**: Room 数据库定义类，声明所有实体和 DAO，当前版本为 v76。提供全局单例 `appDb`，是整个应用访问数据库的唯一入口
- **使用页面**: 全局使用，所有需要读写数据的页面和逻辑均通过 `appDb` 访问

### `DatabaseMigrations.kt`
- **功能**: 数据库迁移脚本，定义了从 v1 到 v76 的所有 Schema 迁移逻辑，确保用户升级时数据不丢失
- **使用页面**: 应用启动时由 Room 自动执行

---

## DAO 列表

| DAO | 说明 | 主要使用页面 |
|-----|------|-------------|
| `BookDao` | 书籍信息 CRUD | 书架、阅读、搜索、导入 |
| `BookChapterDao` | 书籍章节 CRUD | 目录、阅读、缓存 |
| `BookGroupDao` | 书籍分组 CRUD | 书架分组管理 |
| `BookmarkDao` | 书签 CRUD | 阅读书签、书签管理 |
| `BookSourceDao` | 书源 CRUD | 书源管理、搜索、发现 |
| `CacheDao` | 章节缓存 CRUD | 离线缓存、阅读 |
| `CookieDao` | Cookie 存储 | WebView 登录、网络请求 |
| `DailyReadRecordDao` | 每日阅读记录 | 阅读统计、关于页 |
| `DictRuleDao` | 词典规则 CRUD | 词典查询设置 |
| `HttpTTSDao` | 在线 TTS 引擎 CRUD | 朗读设置、朗读引擎 |
| `KeyboardAssistsDao` | 键盘辅助配置 | 阅读键盘工具栏 |
| `ReadRecordDao` | 阅读记录 | 阅读统计、关于页 |
| `ReplaceRuleDao` | 替换规则 CRUD | 替换净化管理 |
| `RssArticleDao` | RSS 文章 CRUD | RSS 阅读、收藏 |
| `RssReadRecordDao` | RSS 阅读记录 | RSS 已读状态 |
| `RssSourceDao` | RSS 源 CRUD | RSS 源管理 |
| `RssStarDao` | RSS 收藏 CRUD | RSS 收藏列表 |
| `RuleSubDao` | 规则订阅 CRUD | 书源/RSS 源订阅更新 |
| `SearchBookDao` | 搜索结果存储 | 搜索结果页 |
| `SearchKeywordDao` | 搜索关键字历史 | 搜索历史、搜索页 |
| `ServerDao` | 服务器配置存储 | Web 服务设置 |
| `TxtTocRuleDao` | TXT 目录规则 CRUD | TXT 导入目录规则设置 |

---

## 实体列表

### 书籍相关

| 实体 | 说明 | 使用页面 |
|------|------|---------|
| `BaseBook` | 书籍基类，定义通用字段（书名、作者、封面等） | 被 `Book`、`SearchBook` 继承 |
| `Book` | 书籍实体，包含书源 URL、最新章节、阅读进度等 | 书架、阅读、书籍详情 |
| `BookChapter` | 章节实体，包含章节标题、URL、内容长度等 | 目录、阅读、缓存 |
| `BookGroup` | 书籍分组（本地、网络、音频、漫画等） | 书架分组筛选 |
| `Bookmark` | 书签，记录章节位置和内容片段 | 阅读书签 |
| `BookProgress` | 阅读进度（用于同步） | 云同步、备份恢复 |
| `SearchBook` | 搜索结果实体，继承 `BaseBook` | 搜索结果列表 |

### 书源相关

| 实体 | 说明 | 使用页面 |
|------|------|---------|
| `BaseSource` | 书源/RSS 源基类 | 被 `BookSource`、`RssSource` 继承 |
| `BookSource` | 书源实体，包含搜索、发现、详情、目录、内容等规则 | 书源管理、搜索、阅读 |
| `BookSourcePart` | 书源部分字段（用于增量更新） | 书源编辑 |
| `rule/BookInfoRule` | 书籍信息解析规则 | 书籍详情页 |
| `rule/BookListRule` | 书籍列表解析规则 | 搜索、发现页 |
| `rule/ContentRule` | 正文内容解析规则 | 阅读页 |
| `rule/ExploreKind` | 发现分类 | 发现页 |
| `rule/ExploreRule` | 发现规则 | 发现页 |
| `rule/FlexChildStyle` | 发现页弹性布局样式 | 发现页 |
| `rule/ReviewRule` | 书评规则 | 书籍评论 |
| `rule/RowUi` | 发现行 UI 配置 | 发现页 |
| `rule/SearchRule` | 搜索规则 | 搜索页 |
| `rule/TocRule` | 目录解析规则 | 目录页 |

### RSS 相关

| 实体 | 说明 | 使用页面 |
|------|------|---------|
| `BaseRssArticle` | RSS 文章基类 | 被 `RssArticle` 继承 |
| `RssArticle` | RSS 文章实体 | RSS 文章列表、阅读 |
| `RssReadRecord` | RSS 阅读记录 | RSS 已读标记 |
| `RssSource` | RSS 源实体 | RSS 源管理、订阅 |
| `RssStar` | RSS 收藏 | RSS 收藏列表 |
| `RuleSub` | 规则订阅（书源/RSS 源自动更新） | 订阅管理 |

### 其他

| 实体 | 说明 | 使用页面 |
|------|------|---------|
| `Cache` | 章节内容缓存 | 离线阅读 |
| `Cookie` | HTTP Cookie 存储 | 登录状态保持 |
| `DailyReadRecord` | 每日阅读记录（时长/字数） | 阅读统计 |
| `DictRule` | 词典查询规则 | 划词翻译/词典 |
| `HttpTTS` | 在线 TTS 朗读引擎 | 朗读设置 |
| `KeyboardAssist` | 键盘辅助快捷操作配置 | 阅读键盘工具栏 |
| `ReadRecord` | 阅读记录（章节阅读次数） | 阅读统计 |
| `ReadRecordShow` | 阅读记录展示模型 | 关于页阅读统计 |
| `SearchKeyword` | 搜索关键字 | 搜索历史 |
| `Server` | Web 服务器配置 | Web 服务设置 |
| `TxtTocRule` | TXT 文件目录识别规则 | TXT 导入设置 |
