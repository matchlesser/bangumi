# 当前进度

更新时间：2026-11-03。

## 当前停点

最近完成的功能目标是 `BG-017 搜索筛选与类型标签` 与 `BG-018 首页索引`：首页分为“搜索 / 索引”两个标签页；搜索可按类型筛选、选择排序，结果卡片标出“动画 · TV”“书籍 · 漫画”等类型；索引页按类型、分类、年份、月份浏览条目。此前完成安装包（BG-007）以及搜索、详情、角色、图片、分页和 Esc 返回。

2026-11-03 起用户决定由助手全程负责开发（见 [项目约定](CONVENTIONS.md#协作方式)）。BG-017 与 BG-018 已作为一个带目标编号的提交保存在 Git 中并推送到 GitHub；1.0.0 安装包不包含这两个功能，需要时提高版本号重新打包。下一步候选见“下次接续”；编号和命名规则见 [项目约定](CONVENTIONS.md)。

## 目标索引

编号永久保留，状态和优先级可调整。“候选”项来自既有学习路线，具体范围尚待细化，不表示已经实施或已经排定执行顺序。

| 编号 | 目标 | 状态 | 完成内容或计划验收范围 |
| --- | --- | --- | --- |
| BG-000 | 项目准备与协作规范 | 已完成 | 建立学习路线、工作规则、进度记录和编号约定 |
| BG-001 | 首个离线窗口 | 已完成 | Windows 窗口显示 3 条示例番剧，构建、启动和默认窗口显示已验证 |
| BG-002 | 关键词筛选与空状态 | 已完成 | 按名称筛选、清空恢复、无匹配提示；9 个测试及桌面操作验证通过。BG-004 起离线筛选被真实搜索取代 |
| BG-003 | 示例条目详情 | 已完成 | 点击卡片查看名称、评分和完整简介，返回列表保留筛选；10 个测试及鼠标、键盘操作验证通过 |
| BG-004 | 真实 Bangumi 搜索 | 已完成 | 按回车或点击搜索获取真实动画条目，处理加载、空结果、错误与重试；14 个测试及真实搜索、错误显示验证通过 |
| BG-005 | 真实 Bangumi 详情 | 已完成 | 条目详情显示评分分布、排名与收藏统计、主要人物、最新吐槽；各区块独立加载与重试；真实条目验证通过 |
| BG-006 | 本地保存 | 候选 | 按需求选择本地标记或设置保存，重启后仍能读取，明确仅保存在本机 |
| BG-007 | Windows 安装包 | 已完成 | MSI 1.0.0（59 MB，内置运行环境），按用户安装、开始菜单与桌面快捷方式、应用图标；安装、运行、联网搜索与卸载验证通过 |
| BG-008 | Git 版本管理 | 进行中 | 仓库已初始化并关联 GitHub 远程仓库；首个提交（2026-09-20）包含 BG-002 为止的内容，之后每个目标在独立分支单独提交，提交说明以目标编号开头，经用户同意后快进合并到 main 并推送 |
| BG-009 | 代码格式检查 | 候选 | 按需要选定工具并固定版本，提供可执行的格式检查命令 |
| BG-010 | 账户登录 | 候选 | 确认认证方式与具体范围后实现登录流程 |
| BG-011 | 账户同步 | 候选 | 在登录基础上细化同步范围，明确本地数据与账户数据的关系 |
| BG-012 | 移动端适配 | 候选 | 桌面流程稳定后确定首个平台，并拆成可验证的小目标 |
| BG-013 | 角色详情 | 已完成 | 从条目页点击角色查看资料、简介、声优和出演作品，出演作品可跳转条目；返回栈保留页面数据和滚动位置 |
| BG-014 | 封面与头像 | 已完成 | 列表与详情封面、角色头像与立绘、出演作品小封面、吐槽用户头像；Coil 3.3.0 加载并缓存；33 个测试及真实数据渲染验证通过 |
| BG-015 | 分页加载 | 已完成 | 搜索结果与吐槽按每页 20 条“加载更多”，去重、失败重试、过期结果丢弃；真实窗口验证通过 |
| BG-016 | Esc 返回 | 已完成 | 详情页按 Esc 返回上一页，搜索页不处理；窗口级处理，与焦点无关；真实窗口验证通过 |
| BG-017 | 搜索筛选与类型标签 | 已完成 | 搜索不再限于动画，可按类型筛选（全部/动画/书籍/音乐/游戏/三次元）并按匹配度、收藏人数、排名、评分排序；卡片与详情显示“类型 · 形式”和日期；真实窗口验证通过 |
| BG-018 | 首页索引 | 已完成 | 首页“索引”标签页按类型、分类、年份、月份浏览条目，按排名或日期排序，可加载更多；结果与 API 直接请求一致；真实窗口验证通过 |

自动测试随需要验证逻辑的目标一起加入；当前共有 62 个测试，都不联网：JSON 转换与请求参数 16 个，图片地址工具 2 个，分页拼接 4 个，类型与分类 3 个，搜索状态 16 个（含筛选、最新请求优先、加载更多），索引状态 6 个，详情状态 6 个，返回栈 2 个，Esc 返回 3 个，文字与年份 4 个。

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
- 回顾入口：[入门说明与 Java 对照](FIRST_STEPS.md)、[桌面入口](../composeApp/src/desktopMain/kotlin/dev/bangumi/demo/Main.kt)、[列表页面](https://github.com/matchlesser/bangumi/blob/cc08f80/composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/SubjectListScreen.kt)、[运行截图](images/first-window.jpg)。

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
- 回顾入口：[BG-002 学习说明](BG-002.md)。筛选代码与测试已在 BG-004 中替换，原版本见 Git 提交 `5c8743b`。

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

## BG-004 真实 Bangumi 搜索

### 完成内容

- 接口（2026-11-03 核对）：`POST https://api.bgm.tv/v0/search/subjects`，请求体 `{"keyword": ..., "filter": {"type": [2]}}` 只搜动画，每次最多 20 条；官方标注为实验性 API。按 Bangumi 要求设置 User-Agent `matchlesser/bangumi (https://github.com/matchlesser/bangumi)`。
- 依赖：Ktor 3.3.3（`ktor-client-core` 在 `commonMain`，CIO 引擎在 `desktopMain`）、kotlinx-serialization-json 1.9.0 与对应插件、kotlinx-coroutines 1.10.2；测试使用 kotlinx-coroutines-test 1.10.2。选择理由见 [项目约定](CONVENTIONS.md)。
- 数据层：[BangumiApi.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/data/BangumiApi.kt) 发送请求并取回文字，`parseSearchResponse` 转成 `Subject`：优先中文名、评分为 0 或缺失时为 null、简介缺失时为空字符串，忽略用不到的字段。（BG-005 起解析函数移到 `SearchJson.kt`，对应测试改名为 `SearchJsonTest`。）
- 状态层：`SubjectListState` 接收一个 `suspend (String) -> List<Subject>` 搜索函数；`search()` 管理 `isLoading`、`results`（null 表示未搜索）、`errorMessage`，取消不算失败；失败后再次搜索即重试。
- 界面：`App` 用 `rememberCoroutineScope().launch` 启动搜索；列表页改为搜索栏（回车或“搜索”按钮），结果区用统一的提示样式显示未搜索、加载中、失败与重试、无结果；卡片和详情页处理无简介，并把示例说明改为数据来源说明。
- 清理：移除离线筛选函数及其 9 个测试；示例数据移到 `commonTest`，作为状态测试中的假搜索结果；更新 FIRST_STEPS、BG-002 与学习笔记中的历史说明和链接。

### 2026-11-03 验证结果

- 构建：执行 `.\gradlew.bat :composeApp:assemble :composeApp:check --console=plain`，`BUILD SUCCESSFUL`，无编译警告。
- 自动测试：`desktopTest` 运行 14 个测试，0 失败、0 错误、0 跳过。`BangumiApiTest` 4 个（用按真实响应精简的 JSON 验证转换规则，不联网）；`SubjectListStateTest` 10 个（空关键词、成功、加载中、无结果、失败与重试、无消息异常、取消、清空、进入详情再返回）。
- 启动：执行 `.\gradlew.bat :composeApp:run --console=plain`，窗口标题为“Bangumi 动画搜索”，初始显示搜索提示，空关键词时“搜索”按钮禁用。
- 真实搜索：输入“孤独摇滚”按回车，先显示“正在搜索…”，随后显示 19 部作品，中文名、`8.4 / 10`、`7.1 / 10` 与“暂无评分”正常；可滚动，日文与英文简介正常显示（[截图](images/bg-004-search-results.jpg)）。点击第一条进入详情，显示条目编号 328609 与完整简介，返回后关键词和 19 条结果仍在。输入“进击的巨人”后点击“搜索”按钮，显示 20 部作品（达到上限）。
- 无结果：输入不存在的关键词显示“没有找到匹配的番剧”，点击“清空关键词”回到初始提示。
- 错误与重试：临时把接口地址改为不存在的域名运行，约 10 秒后显示“搜索失败”和原因 `UnresolvedAddressException`，点击“重试”重新进入加载（[截图](images/bg-004-search-error.jpg)）；检查后已恢复正式地址并重新构建、测试。
- 验证边界：没有真实断网测试；结果最多 20 条，没有分页；接口为实验性 API，字段可能变化。未验证不同显示缩放、极小窗口或其他平台。

## BG-005 真实 Bangumi 详情 与 BG-013 角色详情

两个目标在同一轮实现（用户要求：详情中加入评分分布、评论、主要人物，点击人物可看人物详情）。

### 完成内容

- 接口（2026-11-03 核对并实际请求）：
  - 条目：`GET /v0/subjects/{id}`（评分分布 `rating.count`、排名、放送信息、收藏人数）、`GET /v0/subjects/{id}/characters`（角色、定位、声优）。
  - 角色：`GET /v0/characters/{id}`（资料表 infobox、简介、收藏数）、`/subjects`（出演作品）、`/persons`（声优）。
  - 吐槽：官方 v0 没有评论接口，使用 Bangumi 新前端的非公开接口 `GET https://next.bgm.tv/p1/subjects/{id}/comments?limit=20`（其文档标题为 "bangumi private api"，可能随时变化），失败时只影响吐槽区块。
- 数据层：`BangumiApi.kt` 只负责发请求；JSON 转换按主题拆到 `SearchJson.kt`、`SubjectJson.kt`、`CharacterJson.kt`，共用工具在 `BangumiJson.kt`。角色按“主角 → 配角 → 其他”排序；infobox 的字符串和列表两种值统一转成文字；评分、排名为 0 视为没有。
- 模型：新增 `SubjectDetail`、`SubjectCollection`、`SubjectComment(s)`、`CharacterSummary`、`CharacterDetail`、`ProfileEntry`、`CharacterAppearance`。
- 状态层：`LoadState`（加载中 / 成功 / 失败）；`SubjectDetailState` 与 `CharacterDetailState` 各有三块数据并行加载，重试只请求未成功的块，取消不算失败。`AppNavigator` 用返回栈保存页面对象（含滚动位置），替代原来 `SubjectListState` 中的选中条目。
- 界面：
  - 条目详情：标题与原名、评分 / 排名 / 评分人数、平台 · 日期 · 话数、收藏统计；10 档评分分布横条图（每行写出人数和百分比）；简介；主要人物两列卡片（默认主角和配角，可展开全部）；吐槽列表（用户、收藏状态、打分、相对时间）。
  - 角色详情：中文名与原名、收藏与吐槽数、基本资料表、简介、声优、出演作品（可点击打开对应条目）。
  - 共用部件：`ClickableCard`（焦点描边）、`TagLabel`、`DetailPage`（顶部固定返回栏）、`DetailSection`、`LoadStateContent`、`RatingDistribution`；格式化函数 `formatCount`、`formatPercent`、`formatRelativeTime`。
  - 搜索结果列表的滚动位置保存在 `App` 中，从详情返回后不再回到顶部；新的搜索结果自动回到顶部。窗口标题改为“Bangumi”。

### 2026-11-03 验证结果

- 构建：执行 `.\gradlew.bat :composeApp:assemble :composeApp:check --console=plain`，`BUILD SUCCESSFUL`，无编译警告。
- 自动测试：`desktopTest` 运行 31 个测试，0 失败、0 错误。新增 `SubjectJsonTest` 5 个、`CharacterJsonTest` 4 个（用按真实响应精简的 JSON）、`DetailStateTest` 4 个（全部成功、单块失败与只重试失败块、取消后保持加载中并在下次补上、角色页独立加载）、`AppNavigatorTest` 2 个、`FormattingTest` 3 个。
- 真实界面操作（`.\gradlew.bat :composeApp:run`）：搜索“孤独摇滚”→ 打开第一条，显示 `8.4 / 10`、排名 #73、41,530 人评分、`TV · 2022-10-08 · 12 话`、收藏统计与评分分布（8 分 14,390 人，34.6%）（[截图](images/bg-005-rating.jpg)）；主要人物 16 位两列显示并有“显示全部 33 位角色”；吐槽显示“共 9,186 条，显示最新 20 条”及用户、状态、打分、相对时间（[截图](images/bg-005-characters-comments.jpg)）。点击“後藤ひとり”进入角色详情，显示“后藤一里”、资料表（别名、性别、生日、血型、身高、体重）、简介、声优“青山吉能、阎么么、邱涵菲、郭雅瑂”和 13 部出演作品（[截图](images/bg-013-character.jpg)）。点击出演作品“女孩的钓鱼慢活”打开该条目详情（6.8 分，#3710）；返回后角色页保持原滚动位置且不重新加载。
- 返回栏固定：上述操作后发现长页面中“← 返回”会随内容滚走，改为顶部固定返回栏。修改后真实窗口因 Windows 不再把键盘焦点交给程序而无法输入搜索词，改用临时测试把详情页渲染成图片检查（滚动 1200 像素后返回栏仍在顶部），检查后删除临时测试。三张截图摄于返回栏固定之前。
- 验证边界：修改返回栏后没有在真实窗口中重新走完整流程；没有测试真实的吐槽接口失败（区块失败逻辑由状态测试覆盖）；没有 Esc 等返回快捷键；吐槽只取最新 20 条，没有分页。未验证不同显示缩放、极小窗口或其他平台。（图片由 BG-014 补上。）

## BG-014 封面与头像

### 完成内容

- 图片库：Coil 3.3.0（`coil-compose` 与 `coil-network-ktor3`）。2026-11-03 核对，3.3.0 以 Kotlin 2.2.0 构建，依赖的 Compose 1.8.2、Ktor 3.1.0 都低于本项目版本，解析后仍为 Compose 1.9.3、Ktor 3.3.3、Kotlin 2.2.21；3.4.0 起需要 Kotlin 2.3。
- 加载配置：[ImageLoading.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/data/ImageLoading.kt) 创建全局图片加载器，用 Ktor 下载并带上项目 User-Agent（与接口共用常量 `BANGUMI_USER_AGENT`），启用淡入；`App` 启动时用 `setSingletonImageLoaderFactory` 注册。
- 图片地址（实际请求核对尺寸）：搜索结果用 `images.small`（200px 宽），条目详情用 `images.common`（400px），角色头像用 `images.grid`（75×75 脸部裁切），角色立绘用 `images.medium`（400px），吐槽头像用 `avatar.small`（100px）；出演作品接口只给原图（例如 996×1358、约 300 KB），用 `resizedImageUrl` 转成 `/r/100/` 缩略图（约 6 KB）。空字符串视为没有图片。
- 界面：共用组件 [RemoteImage.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/RemoteImage.kt)（浅色占位底、等比裁切、可选形状和裁切对齐）；图片尺寸集中在 `AppDimensions`。搜索卡片左侧封面、简介改为最多三行；条目详情头部左封面右信息；角色卡片与吐槽前加圆形头像；角色详情头部左立绘（保留上方脸部）右资料表；出演作品前加小封面。旁边已有文字的图片作为装饰，不重复给读屏软件朗读。

### 2026-11-03 验证结果

- 构建：执行 `.\gradlew.bat :composeApp:assemble :composeApp:check --console=plain`，`BUILD SUCCESSFUL`，无编译警告；依赖解析确认 Compose 1.9.3、Ktor 3.3.3、Kotlin 2.2.21 未被升级。
- 自动测试：33 个，0 失败、0 错误。各解析测试补充图片地址断言（含空字符串与缺失），新增 `BangumiJsonTest` 2 个（缩略图地址转换）。
- 界面（真实数据渲染）：启动程序后 Windows 未把键盘焦点交给窗口（前台为用户的其他程序），无法输入搜索词。改用临时测试：调用真实接口取得《孤独摇滚！》搜索结果、条目详情和角色“後藤ひとり”，用正式的图片加载配置渲染页面图片，等待图片加载后检查。搜索封面、详情封面、16 位主要人物头像、吐槽头像（含 Bangumi 默认头像）、角色立绘和出演作品小封面全部正常显示（[列表](images/bg-014-list.png)、[条目](images/bg-014-subject.png)、[角色头像](images/bg-014-characters.png)、[角色详情](images/bg-014-character.png)）。检查后删除临时测试。
- 验证边界：本轮没有在真实窗口中点击操作；没有验证断网时的图片占位（逻辑上显示浅色底）；磁盘缓存使用 Coil 默认位置，未单独检查。

## BG-015 分页加载 与 BG-016 Esc 返回

两个目标在同一轮实现（用户要求：加分页和 Esc 返回）。

### 完成内容

- 接口（2026-11-03 实际请求核对）：搜索 `POST /v0/search/subjects?limit=20&offset=N` 返回 `total`（“进击的巨人”共 32 条：20 + 12，offset=40 为空）；吐槽 `?limit=20&offset=N` 同样返回 `total`（《孤独摇滚！》9186 条）。
- 模型：新增 [Page.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/model/Page.kt)（已取得的条目 + 总数），`appended` 拼接下一页时按 id 去重（列表以 id 作为 key，重复会出错），下一页为空时以实际数量为总数。原 `SubjectComments` 改为 `Page<SubjectComment>`。
- 数据层：`searchSubjects(keyword, offset)` 与 `getSubjectComments(subjectId, offset)` 返回 `Page`；每页条数统一为 `PAGE_SIZE = 20`。
- 状态层：`SubjectListState.loadMore()` 用本次搜索的关键词（即使输入框已改）和已有条数请求下一页；失败保留已显示结果、记录 `loadMoreError` 可重试；等待期间重新搜索或清空时丢弃过期的一页。`resultsVersion` 只在新搜索时改变，列表滚动位置据此保留或回到顶部。`SubjectDetailState.loadMoreComments()` 同理。异常转文字统一为 `errorMessageOf`。
- 界面：共用 [LoadMoreFooter.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/LoadMoreFooter.kt)，显示“加载更多（已显示 20 / 共 32 部）”、加载中、失败与重试或“已显示全部”。搜索页标题改为“共找到 N 部作品”，列表末尾为加载更多；吐槽列表末尾同样加入。搜索页改为读取 `SubjectListState` 并上报事件，减少参数数量。
- Esc 返回：[BackShortcut.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/BackShortcut.kt) 判断“按下 Esc 且不在搜索页”时返回上一层；桌面入口在窗口级 `onKeyEvent` 调用它，因此与哪个元素有焦点无关。为此 `AppNavigator` 改为在 `Main.kt` 创建并传给 `App`。返回按钮文字改为“← 返回（Esc）”。
- 测试方法说明：构造 Compose 按键事件的函数在 Compose 1.9 桌面版中是内部 API，因此把判断拆成只接收 `Key` 与 `KeyEventType`（公开常量）的函数来测试。

### 2026-11-03 验证结果

- 构建：执行 `.\gradlew.bat :composeApp:assemble :composeApp:check --console=plain`，`BUILD SUCCESSFUL`，无编译警告。
- 自动测试：46 个，0 失败、0 错误。新增 `PageTest` 4 个、`BackShortcutTest` 3 个；`SubjectListStateTest` 增至 13 个（加载更多与取完、版本号、失败重试、清空后丢弃过期页）；`DetailStateTest` 增至 6 个（吐槽加载更多、失败重试）。
- 真实窗口（本轮窗口能正常获得焦点）：
  - 搜索“进击的巨人”显示“共找到 32 部作品”和封面；列表底部“加载更多（已显示 20 / 共 32 部）”，点击后在原位置追加 12 部，底部变为“已显示全部 32 部”（[截图](images/bg-015-search-all-loaded.jpg)）。
  - 打开《进击的巨人》，吐槽底部“加载更多（已显示 20 / 共 4,641 条）”，点击后变为“已显示 40 / 共 4,641 条”，新增的是更早的吐槽（[截图](images/bg-015-comments-load-more.jpg)）。
  - 详情页按 Esc 回到搜索列表；在搜索页再按 Esc 不变。条目 → 角色“黎各·布雷琴斯卡”后连按两次 Esc，依次回到条目页（停在原来的角色区域）和搜索页。
- 本轮在真实窗口中也看到了 BG-014 的封面、角色头像、吐槽头像和角色立绘正常显示，BG-005、BG-013 的点击流程正常。
- 验证边界：没有真实触发“加载更多失败”（由状态测试覆盖）；角色页出演作品小封面在打开约 4 秒时尚未全部出现，未继续等待确认；没有鼠标侧键返回；未验证不同显示缩放、极小窗口或其他平台。

## BG-007 Windows 安装包

### 完成内容

- 打包工具：Compose 桌面插件调用 JDK 17 自带的 `jpackage` 生成 MSI，WiX 3.11 由插件首次打包时自动下载。命令 `.\gradlew.bat :composeApp:packageMsi`，产物 `composeApp/build/compose/binaries/main/msi/Bangumi-1.0.0.msi`。
- 安装包配置（`composeApp/build.gradle.kts` 的 `nativeDistributions`）：名称 Bangumi、版本 1.0.0、厂商 matchlesser；按当前用户安装（`perUserInstall`，无需管理员权限）、可选目录、开始菜单“Bangumi”文件夹、桌面快捷方式；固定的 `upgradeUuid` 让新版本替换旧版本。名称和说明用英文，因为 WiX 默认代码页不支持中文。
- 内置运行环境：`suggestRuntimeModules` 建议 `java.instrument`、`java.management`、`jdk.unsupported`，另外手动加入 `jdk.crypto.ec`（HTTPS 握手需要，静态分析发现不了）。打出的运行环境模块为 `java.base java.datatransfer java.xml java.prefs java.desktop java.instrument java.logging java.management jdk.crypto.ec jdk.unsupported`，约 62 MB。
- 版本号：只在构建脚本的 `appVersion` 维护，`generateBuildInfo` 任务生成 `BuildInfo.VERSION` 供代码使用。按 Bangumi 对“需要分发的应用”的要求，User-Agent 改为 `matchlesser/bangumi/1.0.0 (项目主页)`；搜索页显示“版本 1.0.0”。
- 图标：[tools/MakeIcon.java](../tools/MakeIcon.java) 绘制主题紫色背景上的白色电视（不使用 Bangumi 官方标志），生成 `composeApp/icons/app.ico`（16–256 像素六种尺寸，用于安装包、exe 和快捷方式）和 `desktopMain/resources/app-icon.png`（窗口标题栏和任务栏，在 `Main.kt` 中加载）。

### 2026-11-03 验证结果

- 构建与测试：`assemble`、`check` 成功，46 个测试通过；`packageMsi` 成功，MSI 约 59 MB。
- 安装：`msiexec /i ... /qn` 静默安装，退出码 0；安装到 `%LOCALAPPDATA%\Bangumi`（约 113 MB），开始菜单 `Bangumi\Bangumi.lnk` 与桌面快捷方式存在，Windows Installer 中登记为 “Bangumi 1.0.0”。
- 运行：启动安装后的 `Bangumi.exe`，标题栏显示新图标，搜索页显示“版本 1.0.0”；搜索“孤独摇滚”返回 19 部并显示封面，打开详情显示评分分布，吐槽（next.bgm.tv）正常加载——说明内置运行环境的 HTTPS 正常（[截图](images/bg-007-installed.jpg)）。
- 卸载：`msiexec /x ... /qn` 退出码 0，快捷方式与登记信息已删除。因为验证时测试启动的程序仍在运行，安装目录中被占用的文件未能删除；关闭这些进程后手动删除了残留目录，最终没有安装目录、快捷方式、注册表项或进程残留。
- 验证边界：没有通过双击 MSI 的图形界面安装（只验证了静默安装）；没有在未安装 Java 的另一台电脑上测试；没有代码签名，下载后运行时 Windows SmartScreen 可能提示“未知发布者”。

## BG-017 搜索筛选与类型标签 与 BG-018 首页索引

两个目标在同一轮实现（用户要求：搜索加筛选、结果标出动漫/书籍等类型、首页加索引功能）。

### 完成内容

- 接口（2026-11-03 核对 OpenAPI 并实际请求）：
  - 搜索 `POST /v0/search/subjects` 支持 `filter.type`（不传即全部类型）与 `sort`（match/heat/rank/score）。不限类型时“孤独摇滚”共 299 条，结果含 `type` 与 `platform`（如 TV、漫画、画集）。
  - 浏览 `GET /v0/subjects?type=&cat=&year=&month=&sort=rank|date`。例如 2024 年 10 月 TV 动画按排名共 59 条，2023 年漫画 246 条。分类编号见 `SubjectCategory`（动画 TV=1、WEB=5、OVA=2、剧场版=3；书籍 漫画=1001、小说=1002、画集=1003 等）。
- 模型：[SubjectFilters.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/model/SubjectFilters.kt) 定义 `SubjectType`、`SubjectCategory`、`SearchSort`、`BrowseSort`、`SearchQuery`、`BrowseQuery`；`Subject` 增加类型、形式（platform）和日期，`typeLabel` 组合成“书籍 · 漫画”；`SubjectDetail` 增加类型。
- 数据层：搜索与浏览共用 `parseSubjectPage`；`searchRequestJson`、`browseParameters` 生成请求，单独测试；`BangumiApi.browseSubjects` 新增。
- 状态层：新增共用的 [PagedResults](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/state/PagedResults.kt)（第一页、加载更多、失败重试、丢弃过期结果），搜索和索引都使用它；第一页改为“最新请求优先”，快速切换筛选时只显示最后一次的结果。`SubjectListState` 增加类型筛选和排序，切换后有关键词即重新搜索；新增 [BrowseState](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/state/BrowseState.kt)，换类型清除分类、清除年份同时清除月份、无年份不能选月份。
- 界面：首页改为 [HomeScreen](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/HomeScreen.kt)（标题 + “搜索 / 索引”标签栏），内容分别为 `SearchTab`（搜索栏、类型筛选标签、排序下拉）和 `BrowseTab`（类型、分类两排筛选标签，年份、月份、排序下拉）；结果区抽为共用的 `SubjectResults`，筛选控件为 `OptionChips`、`OptionDropdown`。卡片新增“类型 · 形式”标签和日期，简介改为两行；详情信息行加入类型。原 `SubjectListScreen` 删除。
- 年份列表：从明年倒数到 1920 年；当前年份由 `yearOfEpochSeconds` 按公历规则计算（共享代码不能用 `java.time`）。

### 2026-11-03 验证结果

- 构建与测试：`assemble`、`check` 成功，无编译警告；62 个测试，0 失败。新增或改写：搜索状态 16 个（含筛选重新搜索、无关键词只记住筛选、最新请求优先）、`BrowseStateTest` 6 个、`SubjectFiltersTest` 3 个、请求参数与类型解析 3 个、年份换算 1 个。
- 真实窗口：
  - 搜索“孤独摇滚”（全部类型）共 299 个条目，卡片分别显示“书籍 · 漫画 2019-02-27”和“动画 · TV 2022-10-08”（[截图](images/bg-017-search-all-types.jpg)）；点“书籍”立即重新搜索为 165 个，全是书籍；排序选“评分”后按评分排列，原画集显示“书籍 · 画集”（[截图](images/bg-017-search-book-score.jpg)）。
  - 索引默认全部动画按排名共 9,114 个，“月份”不可选；选 TV → 2024 年得 225 个 → 10 月得 59 个（与直接请求接口一致，[截图](images/bg-018-browse-2024-10-tv.jpg)）；切到“书籍”后分类重置为“全部”并换成漫画/小说/画集/其他，年月保留，共 27 个。
  - 从索引打开条目，详情信息行显示“书籍 · 小说 · 2024-10-19”；按 Esc 返回后索引条件和结果不变；切回搜索标签，之前的关键词、筛选和结果仍在。
- 验证边界：没有逐一检查所有分类组合（音乐无分类、游戏与三次元分类只在测试中覆盖）；按日期排序会出现 Bangumi 上登记为未来日期的条目，属于数据本身；1.0.0 安装包未重新打包。

## 下次接续

候选方向（尚未排定，由助手按用户意见选择）：

- 本地保存（BG-006）：收藏或“想看”标记保存在本机，重启后仍在，明确不等于同步到 Bangumi 账户。
- 发布：提高版本号（例如 1.1.0）重新打包，在 GitHub 上创建 Release 并附上 MSI（需要用户同意后再发布）。
- 小改进：鼠标侧键返回；角色页出演作品“只看动画”筛选；索引页按标签（如“原创”“漫改”）浏览。

## 交接注意

- 先读项目文档并检查现有文件，避免将上述待实施项当成已完成。
- 继续工作时先说明目标编号，以本文件的停点为起点，以用户当前要求决定本轮范围。
- 若目标、进度或停点发生变化，结束前更新对应编号的记录；保留已完成事项、实际验证结果和明确的下一步。
- 本文件只维护当前有效状态；环境问题解决后更新记录，不将旧错误视为永久限制。
