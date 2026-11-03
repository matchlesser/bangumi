# 项目约定

## 已确定的方向

- 目标是学习并制作 Bangumi 客户端，先做 Windows 桌面，再逐步考虑移动端。
- 使用 Kotlin、KMP 和 Compose Multiplatform。
- 学习者已有 Java 基础，采用能够逐步理解和修改的实现。
- 创建工程前先说明概念；每轮具体执行范围以用户当前请求为准。

## 协作方式

2026-11-03 起，用户决定由助手全程负责开发：默认由助手直接实现、运行验证并更新文档，完成后简要说明改动、设计理由和验证结果。提交、合并和推送仍先征得用户同意。用户要求讲解或亲手练习时，采用下面的结对学习方式。

## 结对学习方式

按用户要求采用的“小步讲解、用户输入、检查验证”方式（BG-003 至 BG-004 初期的默认方式）：

1. 每次只处理一个容易理解和验证的改动，例如一个状态属性、一个事件方法、一个回调或一个测试。
2. 修改前先从需求出发，讲清数据或事件怎样流动、各文件为什么承担当前职责，以及不这样设计可能产生的耦合或限制；有多种合理方案时说明当前选择及取舍。
3. 在理解设计原因后，再解释新语法、函数或 Compose API 为什么适合这里，并给出准确位置和短代码片段供用户亲手输入。学习者不需要从空白猜测 API、函数或项目结构，也不以背诵语法为目标。
4. 用户完成后，助手读取实际文件，分别指出行为、类型设计、注释和格式问题；先解释原因，再给出下一小步。
5. 在形成可验证行为后，由助手运行对应的编译或测试，并区分代码检查、自动测试、程序启动和人工界面检查。
6. 不一次性生成完整代码文件或连续堆叠大量未解释的改动。必要的练习融入当前功能，不把独立完成课后练习作为继续项目的条件。

用户可以在任何会话中改变协作方式；以当前会话中的明确要求为准。

## 起步默认方案

以下内容是当前建议采用的默认方案，不代表用户已经逐项选择了视觉设计或依赖版本。后续作出不同决定时直接更新本文件并说明原因。

- 第一版 demo 使用内存中的示例条目，先完成列表、筛选和详情。
- 首期采用一个应用模块，按职责分目录。
- 初始界面采用 Compose Material 3 的现成组件和默认主题。具体品牌色和页面样式在首个界面实现时确定。
- Kotlin、Compose 和 Gradle 使用经过兼容性核对的固定版本。

## 当前构建选择

- 首个工程使用 `composeApp` 单模块，只声明 `desktop` JVM 目标，编译与运行工具链为 JDK 17。
- Kotlin 与 Compose 编译器使用 2.2.21，Compose Multiplatform 使用 1.9.3，Gradle Wrapper 使用 8.14.3。插件版本在根构建脚本中固定，Gradle 版本与发行包 SHA-256 在 Wrapper 配置中固定。
- 选择理由：Kotlin 2.2.21 的官方 Gradle 兼容范围覆盖 8.14 系列，Compose 使用 Kotlin 2.x 的编译器插件，现有 JDK 17 满足桌面运行要求；这组配置已在本机完成构建与启动，便于保持学习环境一致。
- 兼容性参考：[Kotlin Gradle 配置](https://kotlinlang.org/docs/gradle-configure-project.html)、[Compose 平台与版本兼容性](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compatibility-and-versioning.html)。
- 当前引入界面所需的 Compose 库和 `kotlin("test")` 测试库（版本随 Kotlin 插件固定）；尚未添加持久化或依赖注入库。
- BG-004 引入网络与 JSON：Ktor 3.3.3（`ktor-client-core` 放在 `commonMain`，`ktor-client-cio` 引擎放在 `desktopMain`）、kotlinx-serialization-json 1.9.0 及版本与 Kotlin 相同的 serialization 插件、kotlinx-coroutines-core 1.10.2；测试使用 kotlinx-coroutines-test 1.10.2。依赖版本直接写在 `composeApp/build.gradle.kts`。
- 版本上限：2026-11-03 核对，Ktor 3.3.3 以 Kotlin 2.2.21 构建，serialization 1.9.0 以 Kotlin 2.2.0 构建；Ktor 3.4 起和 serialization 1.10 起需要 Kotlin 2.3 以上。升级这些库时要先升级 Kotlin 与 Compose，并重新核对兼容性。
- BG-014 引入图片库 Coil 3.3.0（`coil-compose`、`coil-network-ktor3`）：以 Kotlin 2.2.0 构建，依赖的 Compose、Ktor 低于本项目版本；3.4.0 起需要 Kotlin 2.3。选它而不是自己下载解码，是因为它已处理异步加载、取消、内存与磁盘缓存，且同时支持将来的移动端。
- JSON 解析采用“取回文字 → `Json.decodeFromString` 手动解析”，未使用 Ktor 的 ContentNegotiation 插件，以便看清并单独测试转换步骤。
- 打包（BG-007）：用 Compose 桌面插件的 `nativeDistributions` 生成 MSI（`packageMsi`），依赖 JDK 17 的 `jpackage`，WiX 由插件自动下载。安装包内置的运行环境只包含 `modules(...)` 列出的模块；新增依赖后先运行 `suggestRuntimeModules` 核对，并保留 `jdk.crypto.ec`（HTTPS 需要），改动后必须用安装后的程序实际联网验证。
- 版本号只在 `composeApp/build.gradle.kts` 的 `appVersion` 维护（MSI 要求三段数字），代码通过生成的 `BuildInfo.VERSION` 读取；User-Agent 包含版本号。`upgradeUuid` 固定不变，保证新版本替换旧版本。安装包名称、说明等元数据使用英文（WiX 默认代码页不支持中文）。

## 代码组织

当前模块为 `composeApp`，包名为 `dev.bangumi.demo`，源码放在 `composeApp/src` 下。职责划分如下，目录随实际功能逐步创建：

| 位置 | 职责 |
| --- | --- |
| `commonMain/.../model/` | 界面使用的数据模型：`Subject`、`SubjectDetail`、角色相关模型、分页 `Page`、筛选条件（`SubjectFilters.kt`） |
| `commonMain/.../data/` | `BangumiApi.kt` 只负责发请求；`SearchJson.kt`（搜索与浏览）、`SubjectJson.kt`、`CharacterJson.kt` 把各接口的 JSON 转成模型并生成请求参数；`BangumiJson.kt` 放共用解析工具 |
| `commonMain/.../ui/screens/` | 页面布局和事件连接：首页（`HomeScreen` 及 `SearchTab`、`BrowseTab` 两个标签页）、条目详情页、角色详情页 |
| `commonMain/.../ui/components/` | 被多个页面使用的界面组件和文字格式化函数 |
| `commonMain/.../ui/state/` | 页面状态、加载状态 `LoadState`、分页状态 `PagedResults` 和返回栈 `AppNavigator` |
| `commonMain/.../ui/theme/` | 统一主题和设计常量 |
| `desktopMain/` | 桌面程序入口、窗口及平台能力 |

这里的 `...` 表示 `kotlin/dev/bangumi/demo`。`commonTest` 保存共享逻辑测试和测试用的示例数据，由当前桌面目标执行。

- 状态对象通过构造参数接收“怎样加载”的函数（例如 `suspend (String) -> List<Subject>`），而不是直接创建网络客户端：运行时由 `App` / `AppNavigator` 传入 `BangumiApi` 的方法，测试时传入返回固定结果的 Lambda。只有一种实现时不额外定义接口。
- 接口原始结构（DTO）只在各 `*Json.kt` 文件内部使用（`private`），界面只接触 `model` 中的类型。每个解析函数都是不联网的普通函数，用按真实响应精简的 JSON 测试。
- 页面导航使用 `AppNavigator` 中的返回栈：栈中保存页面对象（含状态和滚动位置），返回时恢复原页面而不重新请求。导航器在桌面入口 `Main.kt` 创建，供窗口级快捷键（Esc 返回）和 `App` 共用。暂未引入导航库；页面类型或参数明显增多时再评估。
- 详情页的数据按区块各自加载（`LoadState`：加载中 / 成功 / 失败），一块失败不影响其他区块；“重试”只重新请求未成功的区块。
- 分页数据统一用 `Page<T>`（已取得的条目 + 总数），每页 20 条；拼接下一页用 `Page.appended` 并按 id 去重。分页列表的加载状态统一用 `PagedResults<R, T>`：第一页“最新请求优先”（快速切换条件时只显示最后一次的结果），“加载更多”沿用当前结果的请求条件，失败时保留已显示内容，等待期间换了新结果或清空时丢弃过期的一页。
- 筛选条件用 `model` 中的枚举和 `SearchQuery` / `BrowseQuery` 表示，编号与接口一致；条件之间的约束（换类型清除分类、无年份不选月份）写在状态层并测试，界面只上报用户的选择。
- 快捷键的判断逻辑放在 `commonMain`（例如 `handleBackShortcut`），只在 `desktopMain` 中接到窗口事件上；测试时传入 `Key`、`KeyEventType` 常量，不使用 Compose 构造按键事件的内部 API。

第一阶段不必预先建立所有目录或接口。需要从示例数据切换到网络数据、替换实现或隔离平台差异时，再引入相应抽象。

## Kotlin 与状态

- 优先使用 `val` 和明确的数据模型，通过显式事件修改界面状态。
- 类型允许空值时显式处理，避免用 `!!` 隐藏数据缺失问题。
- 类和界面函数使用清晰的名称，例如 `Subject`、`SubjectList`；普通函数和变量使用小驼峰命名。
- 所有代码都需要详细注释，说明职责、参数含义、主要处理过程与设计原因；新的 Kotlin 概念用 Java 作必要对照。避免只逐句复述语法。
- 网络请求写成 `suspend` 函数，由界面通过 `rememberCoroutineScope().launch` 启动，避免阻塞 UI；捕获异常时先单独重新抛出 `CancellationException`，取消操作不误报为请求失败。
- 访问 Bangumi 接口时必须带项目的 User-Agent（见 `BangumiApi`）；接口返回的原始数据类只在数据层内部使用，界面只接触 `Subject` 等模型。
- 优先使用 `api.bgm.tv/v0` 官方公开接口。吐槽箱只有 `next.bgm.tv/p1` 非公开接口提供（文档标题为 "bangumi private api"），使用时必须保证它失败或变化只影响对应区块，并在代码注释中标明。
- 共享代码不能使用 `String.format`、`java.time` 等 JVM 专用 API：数字和百分比格式化用整数运算（见 `Formatting.kt`），当前时间用 Kotlin 标准库的 `kotlin.time.Clock`（Kotlin 2.2 中需 `@OptIn(ExperimentalTime::class)`）。
- 遵循 `.editorconfig`。引入格式检查工具时，把版本及命令加入构建配置和 README。

## 界面一致性

- 同一类内容使用同一组件；条目卡片、评分、空状态等优先复用已有实现。
- 常用颜色、字体、间距、圆角集中维护；页面引用主题或公共常量。
- 第一个经过确认的演示页面作为后续页面的样式参考，形成基准后在这里记录对应文件和必要截图。
- 支持键盘操作及清晰的焦点反馈，避免只有颜色才能区分状态。
- 首页用 `PrimaryTabRow` 分为“搜索 / 索引”两个标签页，两页的条目结果统一使用 [SubjectResults.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/SubjectResults.kt)：加载、空结果、错误和重试用其中的 `StatusMessage` 居中排版，结果列表末尾为加载更多。
- 单选条件统一使用 [FilterControls.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/FilterControls.kt)：选项少（类型、分类）用一排 `OptionChips` 筛选标签，选项多（年份、月份、排序）用 `OptionDropdown` 下拉菜单，按钮上写明“名称：当前值”。
- 条目卡片在标题下显示“类型 · 形式”标签（如“动画 · TV”“书籍 · 漫画”）和日期，混合类型的结果靠它区分。
- 分页列表的末尾统一使用 [LoadMoreFooter.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/LoadMoreFooter.kt)：“加载更多（已显示 n / 共 m）”按钮、加载中、失败与重试、已显示全部。用按钮而不是滚动到底自动加载，请求由用户明确触发，也便于键盘操作。
- 详情页的返回按钮写明快捷键“← 返回（Esc）”；按 Esc 返回上一页，在搜索页不处理。
- 详情页的区块使用 [DetailLayout.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/DetailLayout.kt) 中的部件：`DetailPage`（顶部固定的“← 返回”栏 + 可滚动内容区，边距和最大宽度与搜索页一致）、`DetailSection`（区块标题）、`LoadStateContent`（区块内的加载中 / 失败与重试 / 内容）、`SecondaryText`（来源、统计等次要文字）。新的详情类页面沿用这些部件。
- 当前首页为 [HomeScreen.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/HomeScreen.kt)（搜索见 [SearchTab.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/SearchTab.kt)，索引见 [BrowseTab.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/BrowseTab.kt)），使用 Material 3 默认浅色主题和统一条目卡片。主题与间距集中在 [BangumiTheme.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/theme/BangumiTheme.kt)。
- 评分统一使用 [ScoreLabel.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/ScoreLabel.kt)，不在页面中重复“暂无评分”判断；角色定位、收藏状态、作品类型等小标签统一使用同一文件中的 `TagLabel`。
- 网络图片统一使用 [RemoteImage.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/RemoteImage.kt)：浅色占位底、等比裁切，没有图片或加载失败时只显示占位，布局不跳动。图片尺寸集中在 `AppDimensions`（列表封面 64×90、详情封面 140×196、小封面 40×56、头像 48 与 32）。旁边已有文字的图片 `contentDescription` 传 null。按显示尺寸选择接口提供的图片规格（如 small、common、grid），不在列表中加载原图；只给原图时用 `resizedImageUrl` 转成 `/r/宽度/` 缩略图。
- 可点击的卡片统一使用 [ClickableCard.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/components/ClickableCard.kt)：获得键盘焦点时显示主题色描边（宽度为 `AppDimensions.focusBorderWidth`），让焦点不只靠底色深浅区分。
- 图表除图形外同时写出具体数值（例如评分分布每行都有人数和百分比），不只靠长度或颜色传达信息。
- 已保存 [首个窗口截图](images/first-window.jpg)、[详情页截图](images/bg-003-detail.jpg)、[卡片焦点截图](images/bg-003-card-focus.jpg)、[搜索结果截图](images/bg-004-search-results.jpg)、[搜索失败截图](images/bg-004-search-error.jpg)、[评分分布截图](images/bg-005-rating.jpg)、[角色与吐槽截图](images/bg-005-characters-comments.jpg)、[角色详情截图](images/bg-013-character.jpg)，以及带图片的 [搜索列表](images/bg-014-list.png)、[条目详情](images/bg-014-subject.png)、[角色头像](images/bg-014-characters.png) 和 [角色详情](images/bg-014-character.png) 供查看；长期品牌色和视觉基准仍待后续反馈确定。

## 验证与版本管理

- 每个阶段先验证其主要用户流程；UI 调整需要实际查看显示效果。
- 对可能出错的筛选、数据转换和状态逻辑按需要添加有意义的测试。
- 不为文案、格式等低影响修改增加只重复实现的测试。
- 文档不能代替构建、格式检查或测试；检查通过也不能代替视觉确认。
- Git 仓库已初始化（首个提交为 2026-09-20）。建议按完成的小功能保存版本，便于比较和回退；提交节奏的约定见 `PROGRESS.md` 中的 BG-008。

## 进度编号与对话命名

- 使用 `BG-000` 形式的稳定目标编号：`BG` 表示 Bangumi 项目，后三位为序号。`BG-000` 记录项目准备与协作规范，`BG-001` 对应首个离线窗口。
- 一个编号对应一个可验收的小目标，其构建配置、代码、说明和验证归在同一目标下。编号的唯一索引维护在 [PROGRESS.md](PROGRESS.md)，其他文档只引用。
- 新目标使用下一个未使用的编号。完成、调整优先级或移动表格位置时保留原编号；编号不代表执行优先级。拆分目标时新增编号并记录关联，取消目标时保留原条目并标记“已取消”，不复用编号。
- 状态单独记录为“候选”“待开始”“进行中”“待验证”“已完成”或“已取消”。“候选”表示方向已有记录、具体范围尚待细化；代码已写好但必要验证未完成时使用“待验证”。
- 已完成目标保留简短的完成内容、代码或说明入口、实际验证结果。文档维护沿用相关目标编号；补充讲解或回顾不另建功能编号。
- 对话标题建议使用 `BG-002 关键词筛选与空状态`。同一目标分多次对话时，使用 `BG-002 关键词筛选与空状态 · 01`、`BG-002 关键词筛选与空状态 · 02`；末尾数字是对话序号，不是新目标。
- 回顾对话可命名为 `BG-001 回顾：首个离线窗口`，并在提示中说明是讲解、review 还是继续实现。一次对话涉及多个目标时，标题选主要编号，交接记录说明其余关联编号。
- Git 用于保存代码版本；提交说明可带上同一目标编号，便于把对话、进度与代码变更关联起来。

## 长期决定记录

2026-09-14：记录 KMP + Compose、桌面优先及面向 Java 学习者的方向。此时仅有文档，尚无应用代码或已验证的构建配置。

2026-09-14（首个窗口）：建立单模块桌面工程并固定上述工具链；保留 `commonMain` 与 `desktopMain` 边界，使用默认 Material 3 主题，从示例数据列表开始学习。

2026-09-14（进度编号）：引入稳定目标编号，作为进度索引、对话命名和后续代码回顾的共同标识；状态与执行顺序独立维护。

2026-09-20（结对学习方式）：根据 BG-003 的实际学习反馈，确定默认使用“小步讲解、用户输入、检查验证”的方式；助手负责选择并解释函数与修改位置，避免直接交付难以消化的完整代码文件。

2026-09-21（解释设计原因）：结对步骤先解释需求、数据流、职责边界和方案取舍，再进入语法及修改位置，避免把学习过程退化为机械照抄操作。

2026-11-02（BG-003 界面复用）：评分抽成共用的 `ScoreLabel`，可点击卡片获得焦点时加主题色描边，详情页沿用列表页布局；目的是让列表和详情的显示规则只维护一处，并满足“焦点不只靠颜色区分”的要求。

2026-11-03（BG-004 网络与数据来源）：列表页改为按回车搜索真实 Bangumi 动画（用户决定），示例数据只用于测试。网络与 JSON 选用 Ktor + kotlinx.serialization，版本受 Kotlin 2.2.21 限制；平台引擎放在 `desktopMain`，为将来移动端换引擎保留边界。

2026-11-03（协作方式）：用户决定由助手全程负责开发，默认改为助手直接实现、验证并更新文档；结对学习方式保留，在用户要求时使用。

2026-11-03（BG-005、BG-013 详情与导航）：页面增加到三层（搜索 → 条目 → 角色，角色还能跳回其他条目），采用自写的返回栈 `AppNavigator` 保存页面对象，而不是引入导航库；详情数据按区块用 `LoadState` 独立加载。吐槽只能使用非公开的 p1 接口，按“可失败的独立区块”处理。

2026-11-03（BG-015、BG-016 分页与快捷键）：分页用“加载更多”按钮而不是无限滚动；Esc 返回在窗口级处理，使其不依赖页面焦点，导航器因此上移到桌面入口创建。

2026-11-03（BG-007 安装包）：采用 MSI 并按当前用户安装，避免安装时需要管理员权限；应用图标自行绘制（`tools/MakeIcon.java`），不使用 Bangumi 官方标志；暂不做代码签名。

2026-11-03（BG-017、BG-018 筛选与索引）：搜索不再限于动画，默认搜索全部类型并在卡片上标出类型；首页拆为“搜索 / 索引”标签页，索引使用官方浏览接口。搜索和索引的分页逻辑抽成共用的 `PagedResults`，第一页由“加载中忽略新请求”改为“最新请求优先”，以适应快速切换筛选条件。

后续仅记录会影响未来开发的决定、原因和适用范围，不把每次改动写成长期规范。
