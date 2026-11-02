# 当前进度

更新时间：2026-11-02。

## 当前停点

最近完成的功能目标是 `BG-003 示例条目详情`：点击卡片进入详情页，查看名称、评分和完整简介，返回后保留关键词与筛选结果；鼠标与键盘操作均已实际验证。本轮由用户要求助手直接实现。

BG-003 已作为一个带目标编号的提交保存在 Git 中。下一步建议开始 `BG-004 真实 Bangumi 搜索`。编号和命名规则见 [项目约定](CONVENTIONS.md)。

## 目标索引

编号永久保留，状态和优先级可调整。“候选”项来自既有学习路线，具体范围尚待细化，不表示已经实施或已经排定执行顺序。

| 编号 | 目标 | 状态 | 完成内容或计划验收范围 |
| --- | --- | --- | --- |
| BG-000 | 项目准备与协作规范 | 已完成 | 建立学习路线、工作规则、进度记录和编号约定 |
| BG-001 | 首个离线窗口 | 已完成 | Windows 窗口显示 3 条示例番剧，构建、启动和默认窗口显示已验证 |
| BG-002 | 关键词筛选与空状态 | 已完成 | 按名称筛选、清空恢复、无匹配提示；9 个测试及桌面操作验证通过 |
| BG-003 | 示例条目详情 | 已完成 | 点击卡片查看名称、评分和完整简介，返回列表保留筛选；10 个测试及鼠标、键盘操作验证通过 |
| BG-004 | 真实 Bangumi 搜索 | 候选 | 确认公开接口后接入搜索，处理加载、空结果、错误与重试 |
| BG-005 | 真实 Bangumi 详情 | 候选 | 根据真实条目获取详情，处理字段缺失和请求失败 |
| BG-006 | 本地保存 | 候选 | 按需求选择本地标记或设置保存，重启后仍能读取，明确仅保存在本机 |
| BG-007 | Windows 安装包 | 候选 | 确定打包方式，验证安装与启动 |
| BG-008 | Git 版本管理 | 进行中 | 仓库已初始化并关联 GitHub 远程仓库；首个提交（2026-09-20）包含 BG-002 为止的内容，BG-003 单独提交，提交说明以目标编号开头。分支、合并与推送方式待约定 |
| BG-009 | 代码格式检查 | 候选 | 按需要选定工具并固定版本，提供可执行的格式检查命令 |
| BG-010 | 账户登录 | 候选 | 确认认证方式与具体范围后实现登录流程 |
| BG-011 | 账户同步 | 候选 | 在登录基础上细化同步范围，明确本地数据与账户数据的关系 |
| BG-012 | 移动端适配 | 候选 | 桌面流程稳定后确定首个平台，并拆成可验证的小目标 |

自动测试随需要验证逻辑的目标一起加入；当前共有 10 个测试，其中 9 个覆盖 BG-002 的筛选与状态逻辑，1 个覆盖 BG-003 的选择与返回状态。

## BG-000 项目准备与协作规范

- 已完成：确定 Kotlin + KMP + Compose、桌面优先的路线，建立项目文档、EditorConfig 和进度交接方式；本轮加入稳定编号。
- 回顾入口：[AGENTS.md](../AGENTS.md)、[README.md](../README.md)、[项目约定](CONVENTIONS.md)。
- 本轮验证：核对目标编号唯一性、引用一致性、文档内容、内部链接与基础格式。本轮为文档调整，未重新执行构建、测试或启动应用。

## BG-001 首个离线窗口

### 完成内容

- 建立 `composeApp` 单模块 KMP 工程，只配置桌面 JVM 目标，使用 JDK 17。
- 固定 Kotlin/Compose 编译器 2.2.21、Compose Multiplatform 1.9.3、Gradle Wrapper 8.14.3；补充 `.gitignore`。
- 在 `commonMain` 中分开实现 `Subject` 模型、示例数据、页面、条目卡片和主题；`desktopMain` 中提供桌面入口与窗口。
- 使用 Material 3 默认浅色主题，集中管理常用间距；显示 3 条虚构番剧的名称、评分和简介，缺少评分时显示“暂无评分”。
- 回顾入口：[入门说明与 Java 对照](FIRST_STEPS.md)、[桌面入口](../composeApp/src/desktopMain/kotlin/dev/bangumi/demo/Main.kt)、[列表页面](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/SubjectListScreen.kt)、[运行截图](images/first-window.jpg)。

### 实现时的环境与验证记录

- 2026-09-14：本机 JDK 17.0.10、Windows 11 x86-64；全局 Gradle 不是运行前提，项目已带 Wrapper。
- Wrapper：从 Gradle 官方仓库下载，JAR 的 SHA-256 与官方校验值一致；配置中加入发行包校验值，首次下载已成功完成。
- 构建与检查：执行 `.\gradlew.bat :composeApp:assemble :composeApp:check --console=plain --no-daemon`，结果为 `BUILD SUCCESSFUL`。
- 自动测试：尚无测试用例，`desktopTest` 与 `allTests` 为 `NO-SOURCE`；不能将上述检查结果称为测试用例通过。
- 启动运行：执行 `.\gradlew.bat :composeApp:run --console=plain --no-daemon`，真实桌面窗口已成功打开。
- 界面人工检查：实际查看默认窗口，确认示例数据标识、3 条作品、中文名称与简介、两条数值评分和“暂无评分”正常显示。
- 文件检查：核对项目内部链接、UTF-8 与换行格式、Kotlin 文件行长，并检查共享源码没有导入 JVM 或 Android 专用 API。

## BG-002 关键词筛选与空状态

### 完成内容

- 保留并核对上次中断留下的搜索框、状态对象、筛选函数和测试；补充详细代码注释及 Java 对照说明。
- 按名称片段即时筛选，匹配时忽略首尾空白及英文大小写，输入框保留原文；每次从完整数据筛选。
- 同步显示结果数量，无匹配时显示空状态；输入框与空状态均提供清空入口，恢复全部 3 条示例。
- `App` 通过 `remember` 保持状态，页面接收数据和事件；共享代码不调用 JVM 或 Android 专用 API。
- 回顾入口：[BG-002 学习说明](BG-002.md)、[状态与筛选](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/state/SubjectListState.kt)、[逻辑测试](../composeApp/src/commonTest/kotlin/dev/bangumi/demo/ui/state/SubjectListStateTest.kt)。

### 2026-09-18 验证结果

- 构建：执行 `.\gradlew.bat :composeApp:assemble :composeApp:check --rerun-tasks --console=plain --no-daemon`，`BUILD SUCCESSFUL`，12 个任务实际执行。沙箱内首次下载被拒绝后，改用获准的沙箱外执行。
- 自动测试：`desktopTest` 实际运行 9 个测试，0 失败、0 错误、0 跳过；覆盖空白、片段与顺序、大小写、仅名称、无匹配、空源、连续输入及清空恢复。报告位于 `composeApp/build/reports/tests/desktopTest/index.html`。
- 启动：执行 `.\gradlew.bat :composeApp:run --console=plain --no-daemon`，桌面窗口成功打开。
- 界面操作：默认窗口显示 3 条；输入“书店”显示 1 条，点击输入框“清空”恢复 3 条；输入“不存在的番剧”显示 0 条与空状态，Tab 依次聚焦两个清空按钮，Enter 激活“清空关键词”后恢复 3 条。中文、数量、评分和焦点反馈正常。
- 验证边界：英文大小写、空白和空数据源由逻辑测试覆盖；未验证不同显示缩放、极小窗口或其他平台。后续补充的模型、主题和入口注释不改变运行行为。

## BG-003 示例条目详情

### 完成内容

- 状态：`SubjectListState` 用可空的 `selectedSubject` 表示当前详情，`selectSubject` 与 `clearSelectedSubject` 分别处理进入和返回；`query` 不受影响，返回后筛选保持不变。（2026-09-20 起以结对方式完成状态、测试、卡片点击和页面切换的初版。）
- 事件链：`SubjectCard.onClick` → `SubjectListScreen.onSubjectClick` → `App` 中的 `state::selectSubject`；`App` 读取 `selectedSubject`，为空时显示列表，否则显示详情。
- 详情页：[SubjectDetailScreen.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/SubjectDetailScreen.kt) 显示返回按钮、名称、评分、示例数据说明和完整简介；边距与最大宽度和列表页一致，内容过长时可滚动。
- 共用评分组件：[ScoreLabel.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/ScoreLabel.kt) 供卡片和详情页使用，“暂无评分”规则只维护一处；卡片上的简介改为两行预览，全文在详情页显示。
- 焦点反馈：卡片获得键盘焦点时显示主题色描边（`AppDimensions.focusBorderWidth`），不只依赖底色深浅。
- 整理注释与格式：修正 `App.kt` 中过时的注释和关于智能转换的解释（详细说明移至 [学习笔记 4.19](kotlin-learning-notes.md#419-委托属性不能智能转换为什么要先存进局部-val)），统一缩进、import 顺序和文件末尾换行。

### 2026-11-02 验证结果

- 构建：执行 `.\gradlew.bat :composeApp:assemble :composeApp:check --console=plain`，`BUILD SUCCESSFUL`。
- 自动测试：`desktopTest` 运行 10 个测试，0 失败、0 错误、0 跳过；其中 `selectingAndClearingSubjectPreservesListState` 覆盖进入详情再返回时关键词和筛选结果不变。界面切换没有自动化 UI 测试。
- 启动：执行 `.\gradlew.bat :composeApp:run --console=plain`，桌面窗口成功打开；关闭窗口后命令正常结束。
- 鼠标操作：输入“书店”剩 1 条，点击卡片进入详情，显示名称、`7.9 / 10`、示例说明和简介；点击“← 返回列表”后关键词“书店”与 1 条结果仍在。截图见 [详情页](images/bg-003-detail.jpg)。
- 键盘操作：清空关键词后，在输入框按 Tab 依次聚焦卡片，聚焦的卡片显示紫色描边（[截图](images/bg-003-card-focus.jpg)）；按 Enter 打开“云端练习曲”，详情显示“暂无评分”；按 Tab 聚焦返回按钮（有高亮底色），按 Enter 回到列表。鼠标悬停卡片只加深底色，不出现描边。
- 验证边界：返回列表后键盘焦点不会回到原卡片，需要重新按 Tab；没有 Esc 返回快捷键，也没有可见的滚动条（可用滚轮滚动）。未验证不同显示缩放、极小窗口或其他平台。

## 下次接续

目标：`BG-004 真实 Bangumi 搜索`。建议对话标题：`BG-004 真实 Bangumi 搜索`。

1. 先用 `git status` 确认工作树干净、BG-003 提交位于当前分支，便于比较接入网络前后的差异。
2. 查阅 Bangumi 官方 API 文档，确认搜索接口、请求头要求（例如是否必须提供自定义 User-Agent）和返回字段；以下均为待核对的建议，不是已确定的方案。
3. 选定网络请求与 JSON 解析依赖（候选：Ktor Client + kotlinx.serialization），兼容 Kotlin 2.2.21 后固定版本，并记录到项目约定。
4. 在 `data` 层增加数据来源，把接口返回的数据转换为界面使用的模型；示例数据保留，用于离线演示和测试，界面上明确区分示例与真实数据。
5. 在状态层处理加载、空结果、错误和重试，UI 只读取状态；完成后分别执行构建、测试、启动和界面检查。

继续使用现有条目卡片、评分组件和主题，所有代码添加详细注释。

## 交接注意

- 先读项目文档并检查现有文件，避免将上述待实施项当成已完成。
- 继续工作时先说明目标编号，以本文件的停点为起点，以用户当前要求决定本轮范围。
- 若目标、进度或停点发生变化，结束前更新对应编号的记录；保留已完成事项、实际验证结果和明确的下一步。
- 本文件只维护当前有效状态；环境问题解决后更新记录，不将旧错误视为永久限制。
