# Bangumi 桌面学习项目

用 Kotlin、Kotlin Multiplatform（KMP）和 Compose Multiplatform 制作 Bangumi 客户端，先从 Windows 桌面 demo 开始。

学习者已有 Java 基础，没有完整项目开发经验，尚未系统学习 Kotlin。项目采用小步实现的方式：理解一个概念，完成一个能运行的小功能，再亲手修改和验证。

## 当前状态

当前在 `BG-001 首个离线窗口` 基础上加入 `BG-002 关键词筛选与空状态`：按名称片段即时筛选 3 条虚构示例，支持清空恢复，无匹配时显示提示。具体验证记录见进度文档。

第一次接触项目开发，可以从 [docs/FIRST_STEPS.md](docs/FIRST_STEPS.md) 开始，按说明运行并阅读代码。

目标编号、最新进度、验证记录和下一步统一见 [docs/PROGRESS.md](docs/PROGRESS.md)。

## 文档分工

| 文件 | 保存什么 | 什么时候更新 |
| --- | --- | --- |
| [AGENTS.md](AGENTS.md) | 给编程助手的工作规则和阅读入口 | 协作方式或长期规则变化时 |
| README.md | 项目目标、学习路线、环境和启动方式 | 项目范围、环境或运行步骤变化时 |
| [docs/CONVENTIONS.md](docs/CONVENTIONS.md) | 技术边界、代码组织、界面与验证约定 | 形成或修改长期设计决定时 |
| [docs/PROGRESS.md](docs/PROGRESS.md) | 稳定目标编号、当前进度、验证与接续点 | 完成一个目标或状态发生变化时 |
| [docs/FIRST_STEPS.md](docs/FIRST_STEPS.md) | 首个窗口的运行、代码阅读顺序和 Java 对照 | 起步代码或操作步骤变化时 |
| [.editorconfig](.editorconfig) | 缩进、编码、换行等基础格式 | 格式规则变化时 |

每类信息只维护一个主要位置，其他文档通过链接引用，避免互相矛盾。

## 建议的学习路线

下列路线按学习主题分组；用于对话命名的目标编号及状态以 [进度索引](docs/PROGRESS.md) 为准，当前任务仍以用户在会话中的要求为准。

### 用 Java 知识理解 Kotlin

先学习 `val` / `var`、函数、数据类、可空类型、集合处理和 Lambda。用一个 `Subject` 数据类保存条目，尝试根据名称筛选列表。

练习目标：能自己增加一个字段，并解释筛选函数的输入和输出。

### 离线桌面 demo

建立只配置桌面目标的 KMP 工程，用少量明确标注为示例的数据实现：

- 窗口显示番剧列表。
- 输入关键词，在示例列表中筛选。
- 点击条目显示名称、评分和简介。
- 没有匹配结果时显示空状态，清空关键词后恢复列表。

对应知识：`@Composable`、布局、列表、状态、事件和界面刷新。

验收目标：能启动、能筛选、能查看详情；学习者能独立修改一个显示字段或筛选条件。

### 接入真实 Bangumi 数据

确认公开接口后，逐步加入搜索和详情请求，以及加载、空结果、错误和重试状态。

对应知识：协程、`suspend`、HTTP、JSON 和数据到界面状态的转换。示例数据和真实接口数据需要明确区分。

### 完成一个可以持续使用的小版本

根据实际需求选择本地标记或设置保存，学习持久化。若增加本地收藏，应明确它只保存在本机，不表示已同步到 Bangumi 账户。

桌面流程稳定后，再安排登录、账户同步或移动端适配。

## 每轮学习的方式

1. 选一个可验证的小目标，例如“输入文字后筛选列表”。
2. 先说明这个目标涉及的概念和文件职责，用 Java 做必要对照。
3. 实现后实际运行或检查对应行为。
4. 留一个小改动供学习者练习，并说明如何验证结果。

当用户明确要求实现时，应完成该目标；练习不应成为继续工作的强制前提。

## 开发环境

- 首个目标平台：Windows 桌面，使用 JVM。
- 当前机器在 2026-09-14 已确认安装 JDK 17.0.10。
- Kotlin 与 Compose 编译器固定为 2.2.21，Compose Multiplatform 固定为 1.9.3，Gradle Wrapper 固定为 8.14.3。
- 版本选择理由和官方兼容性参考见 [项目约定](docs/CONVENTIONS.md)。
- 使用项目自带的 Gradle Wrapper，无需安装全局 Gradle。首次运行需要联网下载 Gradle 和依赖。

## 启动与构建

在 PowerShell 中进入项目根目录后启动：

```powershell
cd D:\Projects\workspace\bangumi
.\gradlew.bat :composeApp:run
```

窗口打开时，这条命令会保持运行；关闭窗口后结束。修改代码后，保存文件并重新启动。

编译工程：

```powershell
.\gradlew.bat :composeApp:assemble
```

执行当前检查任务：

```powershell
.\gradlew.bat :composeApp:check
```

已有 9 个筛选与状态逻辑测试，`check` 会执行桌面目标的测试任务；报告位于 `composeApp/build/reports/tests/desktopTest/index.html`。构建、测试、启动和界面查看的结果分别记录在 [docs/PROGRESS.md](docs/PROGRESS.md)。关键词筛选的阅读顺序和 Java 对照见 [docs/BG-002.md](docs/BG-002.md)。

## 开始新会话

在同一项目目录中继续上次工作，可以使用这段提示：

> 请先读取 AGENTS.md、README.md、docs/CONVENTIONS.md 和 docs/PROGRESS.md，检查当前文件，按 docs/PROGRESS.md 的“下次接续”继续，先说明目标编号和本轮范围。我只有 Java 基础，请沿用现有规范，并用 Java 对照解释新的 Kotlin 概念。

也可以在提示末尾指定“本轮实现 BG-002”或“本轮 review BG-001，请解释已有实现和验证记录”。如果本轮另有目标，以最新要求为准。对话标题与编号的使用方式见 [项目约定](docs/CONVENTIONS.md)。

根据 [Codex 官方 AGENTS.md 文档](https://developers.openai.com/codex/guides/agents-md/)，Codex 在每次运行开始时发现并组合适用的指令文件。项目根目录的 `AGENTS.md` 是本项目的规则入口；`README.md` 默认不是指令发现文件，因此这里通过 `AGENTS.md` 明确要求读取它及其他项目文档。

这些文档用于保存可核对的项目事实和约定，不代替全部聊天记录。新增或修改规则后，应让已经运行中的会话显式重新读取；不同会话都需要使用包含这些文件的项目目录。若使用独立工作树，也需要让对应工作树包含最新文档。

其他编程工具的规则加载方式可能不同，可以继续使用上面的明确阅读提示。文档提供约定，格式检查、构建和测试负责验证可自动检查的要求。
