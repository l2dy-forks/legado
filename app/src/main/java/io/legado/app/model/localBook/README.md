# 书籍文件导入解析

本地书籍文件的导入和解析模块，支持多种电子书格式的读取和章节拆分。通过 `LocalBook` 作为统一入口，根据文件扩展名自动选择对应的解析器。

## 使用场景

- **导入页面**: `ImportBookActivity`（本地书籍导入界面，用户选择文件后调用）
- **书架页面**: `BookshelfFragment`（打开本地书籍时触发解析）
- **阅读页面**: `ReadBookActivity`（加载本地书籍章节内容）
- **关联打开**: `FileAssociationActivity`（通过文件管理器直接打开书籍文件）

## 文件说明

### `BaseLocalBookParse.kt`
- **功能**: 本地书籍解析接口，定义了所有解析器必须实现的通用方法（获取目录、获取章节内容等）
- **使用页面**: 被所有具体解析器实现

### `LocalBook.kt`
- **功能**: 本地书籍导入总入口，根据文件类型（扩展名/MIME）分发到对应的解析器。负责文件复制到应用私有目录、封面提取、书籍元数据初始化
- **使用页面**: `ImportBookActivity`、`FileAssociationActivity`、`OnLineImportActivity`

### `TextFile.kt`
- **功能**: TXT 文件解析器，支持自动编码检测（通过 `icu4j`）、正则目录识别（可配置 `TxtTocRule`）、大文件分段读取
- **使用页面**: 导入 TXT 格式本地书籍

### `EpubFile.kt`
- **功能**: EPUB 文件解析器，解析 EPUB 容器结构（OPF、NCX），提取章节内容、封面图片、书籍元数据
- **使用页面**: 导入 EPUB 格式本地书籍

### `PdfFile.kt`
- **功能**: PDF 文件解析器，以纯图片形式渲染 PDF 页面（每页作为一张图片展示）
- **使用页面**: 导入 PDF 格式本地书籍

### `UmdFile.kt`
- **功能**: UMD 文件解析器，解析 UMD（掌阅）格式电子书
- **使用页面**: 导入 UMD 格式本地书籍