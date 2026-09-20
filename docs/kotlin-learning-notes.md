# Kotlin 学习笔记

> 本文档来源于实际项目代码阅读过程中遇到的问题，用于记录 Kotlin 中容易忘记、容易误解以及值得长期积累的知识。
>
> 当前示例来自 Windows 桌面入口 [Main.kt](../composeApp/src/desktopMain/kotlin/dev/bangumi/demo/Main.kt)、数据模型 [Subject.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/model/Subject.kt)、示例数据 [SampleSubjects.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/data/SampleSubjects.kt)、界面中枢 [App.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/App.kt)、状态管理 [SubjectListState.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/state/SubjectListState.kt) 与页面 [SubjectListScreen.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/screens/SubjectListScreen.kt)。项目使用 Kotlin 2.2.21 与 Compose Multiplatform 1.9.3。

## 目录

- [1. 本次学习背景](#1-本次学习背景)
- [2. Kotlin 核心知识点](#2-kotlin-核心知识点)
  - [2.1 顶层函数与 main](#21-顶层函数与-main)
  - [2.2 表达式函数体 `=`](#22-表达式函数体-)
  - [2.3 Lambda 与函数类型](#23-lambda-与函数类型)
  - [2.4 高阶函数与函数参数](#24-高阶函数与函数参数)
  - [2.5 函数引用 `::`](#25-函数引用-)
  - [2.6 命名参数与默认参数](#26-命名参数与默认参数)
  - [2.7 尾随 Lambda](#27-尾随-lambda)
  - [2.8 带接收者的 Lambda](#28-带接收者的-lambda)
  - [2.9 Compose 中的 `@Composable` 与 `content`](#29-compose-中的-composable-与-content)
  - [2.10 数据类 `data class` 与常用生成方法](#210-数据类-data-class-与常用生成方法)
  - [2.11 只读属性 `val` 与可空类型 `Type?`](#211-只读属性-val-与可空类型-type)
  - [2.12 变量声明结构与只读列表 `listOf(...)`](#212-变量声明结构与只读列表-listof)
  - [2.13 响应式状态 `mutableStateOf` 与可观察包装](#213-响应式状态-mutablestateof-与可观察包装)
  - [2.14 状态记忆 `remember` 与重组生命周期](#214-状态记忆-remember-与重组生命周期)
  - [2.15 绑定到对象的方法引用 `instance::method`](#215-绑定到对象的方法引用-instancemethod)
- [3. Kotlin 语法糖与等价写法](#3-kotlin-语法糖与等价写法)
  - [3.1 入口代码的逐层展开](#31-入口代码的逐层展开)
  - [3.2 参数归属的判断](#32-参数归属的判断)
  - [3.3 回调的立即执行与延迟执行](#33-回调的立即执行与延迟执行)
  - [3.4 `data class` 的 Java 对照与等价展开](#34-data-class-的-java-对照与等价展开)
  - [3.5 变量声明与 Java 对照表](#35-变量声明与-java-对照表)
  - [3.6 尾随 Lambda 构筑 DSL 容器（以 BangumiTheme 为例）](#36-尾随-lambda-构筑-dsl-容器以-bangumitheme-为例)
- [4. 易错点与理解难点](#4-易错点与理解难点)
  - [4.1 `application` 没有圆括号，不代表没有参数](#41-application-没有圆括号不代表没有参数)
  - [4.2 `content` 没有参数，不代表它的函数体不能传参数](#42-content-没有参数不代表它的函数体不能传参数)
  - [4.3 `Window` 接收参数但函数体可能只负责转发](#43-window-接收参数但函数体可能只负责转发)
  - [4.4 `::exitApplication` 不是函数执行](#44-exitapplication-不是函数执行)
  - [4.5 `Window` 和 `App` 看起来像类，但这里是可调用的函数](#45-window-和-app-看起来像类但这里是可调用的函数)
  - [4.6 “嵌套”不等于物理窗口嵌套](#46-嵌套不等于物理窗口嵌套)
  - [4.7 `ApplicationScope.() -> Unit` 不等于普通无参数函数](#47-applicationscope-unit-不等于普通无参数函数)
  - [4.8 普通 `class` 与 `data class` 在 `==` 比较上的区别](#48-普通-class-与-data-class-在--比较上的区别)
  - [4.9 `data class` 配合 `val` 的不可变性原则](#49-data-class-配合-val-的不可变性原则)
  - [4.10 误把 val 当作类型关键字，认为与冒号后的类型冲突](#410-误把-val-当作类型关键字认为与冒号后的类型冲突)
  - [4.11 过程式顺序思维 vs GUI 事件驱动心智模型（为什么底层控件能反向调用外层方法）](#411-过程式顺序思维-vs-gui-事件驱动心智模型为什么底层控件能反向调用外层方法)
  - [4.12 Compose 受控组件（Controlled Component）与传统 Swing 控件的区别](#412-compose-受控组件controlled-component与传统-swing-控件的区别)
  - [4.13 用户输入到界面刷新的完整生命周期三阶段](#413-用户输入到界面刷新的完整生命周期三阶段)
  - [4.14 混淆函数类型 `(A) -> B` 与 Lambda 实现 `{ a -> b }`（同用箭头 `->` 的认知碰撞）](#414-混淆函数类型-a---b-与-lambda-实现-a---b-同用箭头---的认知碰撞)
- [5. Kotlin 代码阅读技巧](#5-kotlin-代码阅读技巧)
- [6. 重点速查](#6-重点速查)
- [7. 待继续学习](#7-待继续学习)
- [8. 更新记录](#8-更新记录)

## 1. 本次学习背景

本次从桌面入口开始阅读：

```kotlin
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bangumi · 离线示例",
        state = rememberWindowState(width = 960.dp, height = 720.dp),
    ) {
        App(subjects = sampleSubjects)
    }
}
```

这段代码集中出现了许多 Kotlin 初学者容易误解的写法：函数可以放进变量或参数、参数名可以写在调用处、最后一个 Lambda 可以放到括号外、函数体可以用 `=` 简写，以及多个 Lambda 看起来像“函数嵌套”。本笔记将它们按知识主题重新整理。

这里的嵌套主要是**函数调用和 Lambda 代码范围的嵌套**，不是一个桌面窗口物理地嵌套在另一个窗口中：

```text
application：应用运行环境
└── content Lambda
    └── Window：桌面窗口
        └── content Lambda
            └── App：窗口中的 Compose 界面
```

在入口代码阅读完成后，进一步深入阅读了数据模型 [Subject.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/model/Subject.kt)、示例数据 [SampleSubjects.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/data/SampleSubjects.kt)、状态管理 [SubjectListState.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/state/SubjectListState.kt) 与界面总控 [App.kt](../composeApp/src/commonMain/kotlin/dev/bangumi/demo/ui/App.kt)。

本轮学习完成了一次重要的认知升级 —— 从单纯的 **Kotlin 语法层面**，跨越到了 **现代响应式 GUI 架构核心机制**：
- 为什么需要 `remember` 及其充当“函数式 UI 成员变量”的意义；
- `mutableStateOf` 响应式状态与 Compose 重组（Recomposition）的观察者驱动原理；
- 破除“面向过程单向顺序执行”的思维盲区，建立“事件驱动（Event-Driven）+ 函数引用交接 + 状态触发全链路重绘”的完整闭环心智模型。

## 2. Kotlin 核心知识点

### 2.1 顶层函数与 `main`

**核心概念**

Kotlin 允许函数直接定义在文件顶层，不必先放进类中。`main` 是 JVM 程序的入口函数。

**语法**

```kotlin
fun main() {
    println("程序开始")
}
```

也可以写成表达式函数体：

```kotlin
fun main() = println("程序开始")
```

**本次代码中的实际用法**

```kotlin
fun main() = application {
    // 启动 Compose Desktop 应用并创建窗口
}
```

`main` 是 Kotlin 顶层函数；桌面入口放在 `desktopMain`，因为它依赖 JVM/桌面窗口 API。

**为什么这样写**

Kotlin 不要求入口函数必须是某个类的静态方法。编译到 JVM 后，顶层函数会由编译器生成相应的静态入口形式，因此 Java 开发者可以先把它理解为 `public static void main` 的 Kotlin 写法。

**常见误区**

`main` 后面使用 `=` 不代表它不是普通函数；这只是函数体的另一种写法。

**记忆方式**

顶层 `fun main` 就是程序入口；是否放进类里不是 Kotlin 的要求。

### 2.2 表达式函数体 `=`

**核心概念**

当函数体只有一个表达式时，可以用 `=` 直接返回这个表达式的结果。

**语法**

```kotlin
fun add(left: Int, right: Int): Int = left + right
```

如果返回类型可以推断，还可以省略返回类型：

```kotlin
fun add(left: Int, right: Int) = left + right
```

**本次代码中的实际用法**

```kotlin
fun main() = application {
    Window(/* ... */)
}
```

它在结构上接近：

```kotlin
fun main() {
    application {
        Window(/* ... */)
    }
}
```

这里 `application { ... }` 是 `main` 的唯一表达式。

**为什么这样写**

`=` 表示函数直接以右侧表达式作为函数体。它不是赋值给 `main`，也不是把函数本身传给 `application`。

**常见误区**

不要把 `fun main() = application` 看成“只引用 application”。本次实际代码还有后面的 `{ ... }`，完整表达式是 `application { ... }`，即调用 `application` 并传入 Lambda。

**记忆方式**

`fun f() = expression` 可以先展开为 `fun f() { return expression }`；如果返回值是 `Unit`，则可以把它理解为执行该表达式。

### 2.3 Lambda 与函数类型

**核心概念**

Lambda 是一段没有名字的函数代码，可以保存到变量、作为参数传递，或在以后被调用。函数类型的基本形式是：

```kotlin
(参数类型1, 参数类型2) -> 返回值类型
```

**语法**

无参数、无有意义返回值的 Lambda：

```kotlin
val action: () -> Unit = {
    println("执行动作")
}

action()
```

接收两个整数并返回整数的 Lambda：

```kotlin
val add: (Int, Int) -> Int = { left, right ->
    left + right
}
```

`Unit` 类似 Java/C++ 中的 `void`，表示没有有意义的返回值。

**本次代码中的实际用法**

窗口关闭回调可以写成 Lambda：

```kotlin
onCloseRequest = {
    exitApplication()
}
```

窗口内容也是 Lambda：

```kotlin
content = {
    App(subjects = sampleSubjects)
}
```

**为什么这样写**

GUI 框架经常需要记录“某个事件发生后要执行什么”。调用者把这段行为交给框架，框架在关闭窗口、重组界面等时机执行它。Lambda 不是立即执行的结果，而是一段可传递的代码值；只有写出 `action()` 或框架内部调用它时，代码才执行。

**等价写法**

```kotlin
val action = {
    println("执行动作")
}

// action 是函数值，下面才是调用
action()
```

**常见误区**

- `exitApplication()` 是现在立即调用函数。
- `::exitApplication` 是取得函数引用，交给别人以后调用。
- `{ exitApplication() }` 是创建一个新的 Lambda；创建时不执行其中代码。
- `content` 不是 Kotlin 关键字，而是 Compose API 定义的一个参数名。

**记忆方式**

Lambda 就是“可以当作值传递的一段代码”。

### 2.4 高阶函数与函数参数

**核心概念**

接收函数作为参数，或返回函数的函数，称为高阶函数。

**语法**

```kotlin
fun runAction(action: () -> Unit) {
    action()
}

runAction {
    println("运行传入的动作")
}
```

`action: () -> Unit` 的含义是：`action` 是一个不接收参数、返回 `Unit` 的函数。

**本次代码中的实际用法**

`Window` 接收两个重要的 Lambda 参数：

```kotlin
onCloseRequest: () -> Unit
content: @Composable FrameWindowScope.() -> Unit
```

第一个表示关闭回调，第二个表示窗口内容。`application` 也接收内容 Lambda：

```kotlin
content: @Composable ApplicationScope.() -> Unit
```

**为什么这样写**

`application` 不需要知道内容的具体实现，只需要在应用环境建立后执行 `content`。`Window` 也不需要把关闭逻辑写死为退出，它接收调用者提供的 `onCloseRequest`，再把它交给更底层窗口实现。这样框架负责时机，调用者负责行为。

可以把它看成：

```kotlin
fun application(content: () -> Unit) {
    // 建立应用环境
    content()
}
```

实际 Compose 定义比这个示例多了 `@Composable` 和接收者类型，作用仍然是“接收一段内容代码”。

**常见误区**

`application` 的 `content` 没有参数，并不表示它里面不能调用带参数的函数：

```kotlin
application(
    content = {
        // content 自己没有参数
        Window(title = "Bangumi") // 这里是 content 函数体调用 Window，并给 Window 传参数
    },
)
```

函数参数属于当前调用的函数。`Window` 的参数不会变成 `application.content` 的参数。

**记忆方式**

“传给外层的 Lambda”与“Lambda 函数体里调用其他函数时传的参数”是两层不同的事情。

### 2.5 函数引用 `::`

**核心概念**

`::函数名` 表示函数引用：取得函数本身，作为值传给其他代码，不会在这一行立即调用函数。

**语法**

```kotlin
fun sayHello() {
    println("Hello")
}

val action = ::sayHello
action()
```

**本次代码中的实际用法**

```kotlin
Window(
    onCloseRequest = ::exitApplication,
)
```

这表示把退出函数交给窗口，关闭事件发生时由窗口调用。

**为什么这样写**

函数引用和回调参数的类型需要匹配。`exitApplication` 的无参数、`Unit` 返回特征与 `onCloseRequest: () -> Unit` 匹配，所以可以直接传入。

**等价写法**

在本例中，函数引用可以近似改写为：

```kotlin
Window(
    onCloseRequest = {
        exitApplication()
    },
)
```

C++ 中可类比函数指针 `&exitApplication`，但 Kotlin 的函数类型和引用语法更统一。

**常见误区**

- `exitApplication()`：立即执行。
- `::exitApplication`：传递函数引用，不立即执行。
- `::` 不是“复制函数体”；它表示引用这个函数。

**记忆方式**

看到括号 `()` 想“调用”；看到 `::` 想“把函数交出去”。

### 2.6 命名参数与默认参数

**核心概念**

Kotlin 调用函数时可以写 `参数名 = 值`，明确这个值对应哪个参数。函数定义也可以为参数提供默认值。

**语法**

```kotlin
fun greet(name: String, punctuation: String = "!") {
    println("Hello, $name$punctuation")
}

greet(name = "Kotlin")
greet(name = "Kotlin", punctuation = ".")
```

**本次代码中的实际用法**

```kotlin
Window(
    onCloseRequest = ::exitApplication,
    title = "Bangumi · 离线示例",
    state = rememberWindowState(width = 960.dp, height = 720.dp),
)
```

`onCloseRequest`、`title` 和 `state` 是 `Window` 的参数名，不是这里新声明的临时变量。

`Window` 还有许多默认参数，例如 `visible: Boolean = true`、`resizable: Boolean = true` 等，因此调用处可以只写需要修改的参数。

**为什么这样写**

命名参数提高可读性，尤其是多个参数类型相近时不容易传错。默认参数减少了调用处的重复配置。

**等价写法**

概念上，省略的默认参数类似于使用定义中的默认值：

```kotlin
Window(
    onCloseRequest = ::exitApplication,
    state = rememberWindowState(),
    visible = true,
    title = "Bangumi · 离线示例",
    resizable = true,
    // 其他默认参数也使用定义中的值
    content = { App(subjects = sampleSubjects) },
)
```

这不是要求开发者真的把所有默认参数写出来，而是帮助阅读 API。

**常见误区**

看到 `title = "..."` 不要理解成在函数调用内部声明局部变量；这是给 `Window` 的 `title` 参数传值。

**记忆方式**

调用处的 `名字 = 值` 是“把值交给这个参数”，不是变量声明。

### 2.7 尾随 Lambda

**核心概念**

如果函数的最后一个参数是 Lambda，Kotlin 允许把它从圆括号中移到括号外。

**语法**

完整写法：

```kotlin
runAction(
    action = {
        println("执行")
    },
)
```

尾随 Lambda 写法：

```kotlin
runAction {
    println("执行")
}
```

**本次代码中的实际用法**

```kotlin
application {
    Window(/* ... */) {
        App(subjects = sampleSubjects)
    }
}
```

展开后可读作：

```kotlin
application(
    content = {
        Window(
            /* 其他参数 */
            content = {
                App(subjects = sampleSubjects)
            },
        )
    },
)
```

**为什么这样写**

UI 代码的最后一个参数通常是“内容”，把它放到括号外可以让代码呈现出容器包裹内容的结构。它不是省略参数，也不是因为 `application` 没有参数。

**为什么同样是 `{}` 却有不同含义**

大括号本身不能决定它是函数体还是 Lambda，要看前面的语法结构：

```kotlin
fun run(action: () -> Unit) {
    action()
}
```

这里的 `{ action() }` 跟在函数声明 `fun run(...)` 后面，所以是 `run` 的函数体。

```kotlin
run {
    println("执行")
}
```

这里的 `{ println("执行") }` 跟在函数调用 `run` 后面，所以是传给 `run` 最后一个 Lambda 参数的尾随 Lambda。它等价于：

```kotlin
run(
    action = {
        println("执行")
    },
)
```

在窗口示例中也一样：

```kotlin
fun Window(content: () -> Unit) {
    realWindow.show {
        content()
    }
}
```

逐层看：

```text
fun Window(...) { ... }       Window 的函数体
realWindow.show { ... }       传给 show 的尾随 Lambda
content()                     调用 content 函数值
```

`fun Window(...) {}` 是定义函数；`Window(...) {}` 是调用函数并传入 Lambda。两种写法的 `{}` 外观相同，但前面的 `fun` 声明和函数调用上下文不同。

**常见误区**

`application { ... }` 仍然是一次函数调用，并且传了 `content` 参数。`Window(...) { ... }` 也仍然给 `Window` 传了最后一个 `content` 参数。

不要仅凭大括号判断含义；先看前面是 `fun name(...)` 还是 `name(...)`。

**记忆方式**

`fun name(...) {}` 是定义函数；`name(...) {}` 是调用函数并传入尾随 Lambda。

### 2.8 带接收者的 Lambda

**核心概念**

函数类型可以写成 `接收者类型.() -> 返回值类型`。这表示 Lambda 执行时拥有一个隐式的接收者对象，可以直接访问该对象的成员。

**语法**

```kotlin
class BuilderScope {
    fun addText() {
        println("添加文本")
    }
}

fun build(content: BuilderScope.() -> Unit) {
    val scope = BuilderScope()
    scope.content()
}

build {
    addText()
}
```

**本次代码中的实际用法**

Compose Desktop 的相关参数是：

```kotlin
content: @Composable ApplicationScope.() -> Unit
content: @Composable FrameWindowScope.() -> Unit
```

`ApplicationScope.()` 和 `FrameWindowScope.()` 表示 Lambda 有对应的 Compose 作用域作为接收者。`ApplicationScope` 中定义了 `exitApplication()`，因此应用内容中可以使用这个操作。

**为什么这样写**

接收者 Lambda 让 DSL 风格的代码更自然：在某个作用域里，成员可以直接写，不必每次显式写对象名。它比普通 `() -> Unit` 多了一层“当前环境”。

**常见误区**

`ApplicationScope.()` 不是说 Lambda 接收一个普通位置参数。它是隐式接收者；与 `(ApplicationScope) -> Unit` 的参数写法不同。

**记忆方式**

普通 Lambda 是“传入参数”；带接收者 Lambda 是“在某个对象环境中执行”。

### 2.9 Compose 中的 `@Composable` 与 `content`

**核心概念**

`@Composable` 是 Compose 用来标记可参与界面声明和重组的函数/代码。`content` 是 API 自己定义的参数名，通常代表容器内部的 Compose 内容，不是 Kotlin 关键字。

**本次使用的实际 API 定义**

项目依赖中的 `application` 定义为：

```kotlin
fun application(
    exitProcessOnExit: Boolean = true,
    content: @Composable ApplicationScope.() -> Unit,
)
```

项目使用的 `Window` 主要重载可简化为：

```kotlin
@Composable
fun Window(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable FrameWindowScope.() -> Unit,
)
```

源码中还存在使用 `WindowDecoration` 的相关重载；阅读当前调用时，先抓住 `onCloseRequest`、普通窗口参数和最后的 `content` 即可。

**本次代码中的实际用法**

```kotlin
application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bangumi · 离线示例",
    ) {
        App(subjects = sampleSubjects)
    }
}
```

这里有两层内容 Lambda：

```text
application 的 content
└── 调用 Window，并传入 Window 的参数
    └── Window 的 content
        └── 调用 App
```

`application` 的 `content` 没有显式位置参数，但它的函数体当然可以调用带参数的 `Window`。这些参数属于 `Window`，不会变成 `application.content` 的参数。

**为什么这样写**

Compose API 采用声明式写法：外层函数建立运行环境，内层 Lambda 描述该环境中的内容。`Window` 负责接收窗口配置并把它们交给更底层的窗口实现；`onCloseRequest` 通常会被保存并在关闭事件发生时调用，`content` 则由窗口的 Compose 内容系统执行。

因此 `Window` 函数体不一定直接调用 `onCloseRequest`。它可以转交参数：

```kotlin
fun Window(
    onCloseRequest: () -> Unit,
    title: String,
    content: () -> Unit,
) {
    SwingWindow(
        onCloseRequest = onCloseRequest,
        title = title,
        content = content,
    )
}
```

这是一种常见的包装/转发层。继续追踪参数，才能找到真正响应关闭事件的底层实现。

**记忆方式**

`content` 是普通参数名，但在 Compose API 中通常表示“这个容器里面声明什么界面”。看起来像嵌套窗口，实际是嵌套的 Lambda 和调用范围。

### 2.10 数据类 `data class` 与常用生成方法

**核心概念**

`data class` 是 Kotlin 专门用于保存纯数据的类。只要在 `class` 前加上 `data` 关键字，编译器就会基于主构造函数中声明的所有属性，自动生成 `equals()`、`hashCode()`、`toString()`、`copy()` 和 `componentN()` 方法。

**语法**

```kotlin
data class User(
    val id: Int,
    val name: String,
)
```

**本次代码中的实际用法**

```kotlin
data class Subject(
    val id: Int,
    val name: String,
    val score: Double?,
    val summary: String,
)
```

在 `model/` 目录中，`Subject` 纯粹用作番剧的数据载体，不包含 UI 绘制或网络请求逻辑。

**为什么这样写**

在传统 Java 中，只存数据的 POJO 需要手写或生成私有字段、全参构造器、Getter、`equals()`、`hashCode()`、`toString()` 等几十行样板代码；或者借助 Lombok 注解（如 `@Data`）。Kotlin 直接在语言层面内置 `data class`，由编译器在编译期自动生成。

编译器自动生成的几个关键方法：
1. **`toString()`**：输出清晰可读的属性内容，如 `Subject(id=1, name=星海邮递员, ...)`，而非普通对象无意义的内存哈希地址（如 `Subject@7a81197d`）。
2. **`equals()` / `hashCode()`**：按“字段内容”进行比较，而非比对对象引用地址。两个对象的各个字段完全一致时，`s1 == s2` 即为 `true`。这在单元测试中比对数据极其实用。
3. **`copy()`**：允许在保持对象不可变（`val`）的前提下，快速复制一个新对象并修改指定字段，例如 `subject.copy(score = 9.0)`。
4. **`componentN()`**：支持解构声明（如 `val (id, name) = subject` 直接提取前两个属性）。

**语法规则与约束**
- 主构造函数必须至少声明一个参数。
- 主构造函数的所有参数必须显式标记为 `val`（只读）或 `var`（可变），通常强烈推荐 `val`。
- `data class` 不能是 `abstract`、`open`、`sealed` 或 `inner` 类。

**记忆方式**

`data class` 就是自带 `toString`、`equals`、`hashCode`、`copy` 的纯数据包裹（类比 Java 14+ 的 `record` 或 Lombok `@Data`）。

### 2.11 只读属性 `val` 与可空类型 `Type?`

**核心概念**

- **`val`（只读属性）**：一旦赋值就不能再次指向其他对象，类似 Java 的 `final` 字段。Kotlin 会自动为其生成 Getter 方法。
- **`Type?`（可空类型）**：类型后面带 `?` 显式表示该属性允许为 `null`；不带 `?` 则在编译期强制保证绝不为 `null`。

**语法**

```kotlin
val id: Int        // 不可为 null，且不可重新赋值
val score: Double? // 可以是 Double 数值，也可以是 null
```

**本次代码中的实际用法**

在 `Subject` 中：
- `id`、`name`、`summary` 是不可空的；
- `score: Double?` 明确允许部分条目暂无评分。界面通过 `if (subject.score == null) ...` 显式分支处理。

**为什么这样写**

Java 的引用类型（包括包装类 `Double`、`String`）默认都可以为 `null`，编译器不会强制检查，容易在运行时发生 `NullPointerException`（NPE）。Kotlin 把可空性纳入了类型系统，如果类型声明为 `Double?`，后续使用时编译器会强制要求判空，将空指针隐患消灭在编译阶段。

**记忆方式**

没有 `?` 绝对不为空；带 `?` 必须先判空才能安全使用。

### 2.12 变量声明结构与只读列表 `listOf(...)`

**核心概念**

Kotlin 声明变量的完整结构为：
```text
[声明关键字] [变量名] : [数据类型] = [初始值]
```
- **`val` / `var`**：负责声明变量的**可变性**（是否可重新赋值），它们不是类型关键字。
  - `val`（value）：只读变量，初始化后引用不可修改，对应 Java 的 `final`。
  - `var`（variable）：可变变量，后续可以重新赋值，对应 Java 普通变量。
- **`: Type`**：显式声明变量的**数据类型**。由于 Kotlin 具备类型推导能力，当初始值类型明确时，`: Type` 可以省略。
- **`listOf(...)`**：Kotlin 标准库提供的顶层工厂函数，用于创建包含指定元素的**只读列表**（`List<T>`），在内存中不可对其增删改（没有 `.add()` 或 `.remove()` 方法）。

**语法**

```kotlin
val list: List<String> = listOf("A", "B") // 显式类型
val list = listOf("A", "B")               // 省略类型（由编译器推导为 List<String>）
```

**本次代码中的实际用法**

```kotlin
val sampleSubjects: List<Subject> = listOf(
    Subject(id = 1, name = "星海邮递员", ...),
    Subject(id = 2, name = "雨后的小小书店", ...),
    Subject(id = 3, name = "云端练习曲", ...),
)
```

**为什么这样写**

1. 顶层数据作为公共只读数据源，用 `val` 保证引用本身不可替换。
2. 使用 `listOf(...)` 返回只读接口 `List<Subject>`，保证列表内部元素不会被调用方随意增删，确保数据源在离线运行期间的稳定性。
3. 显式写出 `: List<Subject>` 可以提高公共顶层属性的可读性，阅读代码时无需自行推断其类型。

**记忆方式**

`val` 管“能不能重新赋值”，冒号后的 `: Type` 管“装的是什么类型”，两者各司其职。

### 2.13 响应式状态 `mutableStateOf` 与可观察包装

**核心概念**

`mutableStateOf(value)` 是 Compose 提供的核心响应式状态容器。它接收一个**初始值**（例如 `""` 代表空字符串），返回一个 `MutableState<T>` 对象。该对象在底层实现了观察者模式：
- **读取（Get）**：当任何 `@Composable` 函数在执行期间读取其值时，Compose 运行时会自动记录该函数对此状态的依赖关系。
- **写入（Set）**：当该状态的值被赋予新值时，Compose 框架会自动触发所有依赖它的 `@Composable` 函数重新执行（重组 Recomposition）。

**语法**

```kotlin
// 基础写法：直接返回 MutableState 对象
val queryState = mutableStateOf("")
queryState.value = "书店" // 修改值，触发重组

// 属性委托写法（通过 by 关键字，推荐）
var query by mutableStateOf("")
query = "书店" // 语法上如同普通变量赋值，底层自动调用 set 并通知框架
```

**本次代码中的实际用法**

```kotlin
class SubjectListState(private val subjects: List<Subject>) {
    var query by mutableStateOf("")
        private set
    // ...
}
```

- 初始值 `""` 表示刚启动时搜索框为空；
- `private set` 保证外部只能读取 `query`，修改只能通过 `updateQuery` 方法，维护封装性。

**为什么这样写**

传统 Java 中普通字段（如 `String query`）被修改后，没有通知机制，屏幕组件无法得知数据已变。要在 Java 中实现类似效果，需要手写 Observer/Listener 列表或使用 JavaFX 的 `SimpleStringProperty`。`mutableStateOf` 将这套“通知-重绘”管道与 Compose 编译器深度绑定，使响应式 UI 极为简洁。

**记忆方式**

`mutableStateOf(初始值)` 是一个“只要被修改就会大声通知 Compose 重跑相关界面的包装盒”。

### 2.14 状态记忆 `remember` 与重组生命周期

**核心概念**

`remember(keys) { calculation }` 是 Compose 用于跨重组保留对象的函数。
在 Compose 中，界面是不断重新执行的函数。如果没有 `remember`，每次重组都会重新执行对象创建语句，导致之前的用户交互状态全部丢失。`remember` 会在内存缓存中保留该对象，只有当指定的 key（如 `subjects`）发生改变时，才会重新计算。

**语法**

```kotlin
// 无 key：只要 Composable 在界面树中存活，永远复用同一个对象
val state = remember { SubjectListState(subjects) }

// 带 key：当 subjects 发生变化（按 equals 比较）时，丢弃旧缓存并重新执行创建
val state = remember(subjects) { SubjectListState(subjects) }
```

**本次代码中的实际用法**

在 `App.kt` 中：
```kotlin
val state = remember(subjects) { SubjectListState(subjects) }
```

**为什么这样写**

类比 Java GUI：在 Java Swing 中，我们把状态保存为**窗体类的成员变量（Field）**，以保证窗体重绘（`repaint()`）时状态不被覆盖。Compose 中没有长期存活的“类实例”，全都是函数，`remember` 就是函数式 UI 中充当“成员变量”的机制。

**记忆方式**

`remember` 就是给函数式 UI 配备的“长期记忆”，防止每次刷新把内存重置为初始状态。

### 2.15 绑定到对象的方法引用 `instance::method`

**核心概念**

方法引用不仅可以引用顶层函数（如 `::exitApplication`），还可以绑定到具体的对象实例上（如 `state::updateQuery`）。这生成了一个保留了目标对象 `this` 上下文的函数值。

**语法与对比**

| 写法 | 类型 | 含义 | Java 8 等价写法 |
| :--- | :--- | :--- | :--- |
| `::exitApplication` | `() -> Unit` | 引用顶层函数（无需实例） | `ClassName::staticMethod` |
| `state::updateQuery` | `(String) -> Unit` | 绑定到 `state` 实例的方法引用 | `state::updateQuery` |
| `SubjectListState::updateQuery` | `(SubjectListState, String) -> Unit` | 未绑定实例的方法引用 | `SubjectListState::updateQuery` |

**本次代码中的实际用法**

在 `App.kt` 中：
```kotlin
SubjectListScreen(
    onQueryChange = state::updateQuery,
    onClearQuery = state::clearQuery,
)
```

**为什么这样写**

比写完整 Lambda `{ text -> state.updateQuery(text) }` 更简洁直接，表达“将此对象的更新行为直接交接给子组件”。

## 3. Kotlin 语法糖与等价写法

### 3.1 入口代码的逐层展开

简洁写法：

```kotlin
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bangumi · 离线示例",
    ) {
        App(subjects = sampleSubjects)
    }
}
```

把表达式函数体、尾随 Lambda 和函数引用逐步展开后，可以写成接近下面的形式：

```kotlin
fun main() {
    application(
        content = {
            Window(
                onCloseRequest = {
                    exitApplication()
                },
                title = "Bangumi · 离线示例",
                state = rememberWindowState(width = 960.dp, height = 720.dp),
                content = {
                    App(subjects = sampleSubjects)
                },
            )
        },
    )
}
```

这不是逐字符的编译器输出，而是帮助阅读的语义展开。实际 API 还包含 `@Composable`、接收者类型和默认参数。

### 3.2 参数归属的判断

阅读下面代码时：

```kotlin
application {
    Window(title = "Bangumi") {
        App(subjects = sampleSubjects)
    }
}
```

应按调用层级判断：

- `application { ... }` 的大括号是传给 `application` 的 `content`。
- `title = "Bangumi"` 是传给 `Window` 的命名参数。
- `Window(...) { ... }` 的大括号是传给 `Window` 的 `content`。
- `subjects = sampleSubjects` 是传给 `App` 的命名参数。

不要把所有大括号里的代码都当成同一个函数的参数。

### 3.3 回调的立即执行与延迟执行

```kotlin
fun runNow(action: () -> Unit) {
    action()
}
```

这里会在 `runNow` 内立即执行传入的 Lambda。

事件 API 通常是另一种模式：先保存，再在事件发生时执行：

```kotlin
class SimpleButton {
    private var clickAction: (() -> Unit)? = null

    fun setOnClick(action: () -> Unit) {
        clickAction = action
    }

    fun click() {
        clickAction?.invoke()
    }
}
```

Compose Desktop 的 `onCloseRequest` 属于回调概念：传入时不应理解为立即退出，真正关闭窗口时才会触发。

### 3.4 `data class` 的 Java 对照与等价展开

Kotlin 代码：

```kotlin
data class Subject(
    val id: Int,
    val name: String,
    val score: Double?,
    val summary: String,
)
```

在语义上完全等价于 Java 14+ 引入的 `record`：

```java
public record Subject(int id, String name, Double score, String summary) {}
```

如果是在传统 Java 中，则相当于编译器替你写完了如下几十行样板代码：

```java
public final class Subject {
    private final int id;
    private final String name;
    private final Double score; // 包装类型允许为 null
    private final String summary;

    public Subject(int id, String name, Double score, String summary) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.summary = summary;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Double getScore() { return score; }
    public String getSummary() { return summary; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id == subject.id &&
               Objects.equals(name, subject.name) &&
               Objects.equals(score, subject.score) &&
               Objects.equals(summary, subject.summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, score, summary);
    }

    @Override
    public String toString() {
        return "Subject(id=" + id + ", name=" + name + ", score=" + score + ", summary=" + summary + ")";
    }
}
```

### 3.5 变量声明与 Java 对照表

| 场景 | Kotlin 写法 | Java 等价写法 | 说明 |
| :--- | :--- | :--- | :--- |
| **显式类型的只读列表** | `val list: List<Subject> = listOf(...)` | `final List<Subject> list = List.of(...);` | `val` 对照 `final`；`: List<Subject>` 对照 Java 类型声明 |
| **类型推导的只读列表** | `val list = listOf(...)` | `var list = List.of(...);` (Java 10+) | Kotlin 省略类型，但 `val` 仍保证只读；Java 10 的 `var` 是可变变量 |
| **显式类型的普通可变变量**| `var count: Int = 0` | `int count = 0;` | `var` 允许后续重新赋值 `count = 1` |
| **不可变集合创建** | `listOf("A", "B")` | `List.of("A", "B")` (Java 9+) 或 `Arrays.asList(...)` | 返回不可增删元素的只读集合 |

### 3.6 尾随 Lambda 构筑 DSL 容器（以 `BangumiTheme` 为例）

在 `App.kt` 中：
```kotlin
BangumiTheme {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        SubjectListScreen(...)
    }
}
```

初看像是一种特殊的语法结构或 HTML 标签，但它本质上**只是一次普通的函数调用**。

还原为不带任何语法糖的传统函数调用写法：
```kotlin
BangumiTheme(
    content = {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = {
                SubjectListScreen(...)
            },
        )
    }
)
```

- `BangumiTheme` 在 `BangumiTheme.kt` 中定义为接收单个参数 `content: @Composable () -> Unit` 的普通函数。
- 因为 `content` 是唯一且最后一个参数，Kotlin 允许省略圆括号 `()`，直接将 Lambda 大括号写在函数名后面。
- 这使得组件的调用形式从视觉上呈现为“样式/环境容器包裹着内部内容”，形成了极具表现力的声明式 DSL。

## 4. 易错点与理解难点

### 4.1 `application` 没有圆括号，不代表没有参数

```kotlin
application {
    Window(/* ... */)
}
```

圆括号外的 Lambda 仍然是参数。完整形式是 `application(content = { ... })`。

### 4.2 `content` 没有参数，不代表它的函数体不能传参数

```kotlin
application(
    content = {
        Window(title = "Bangumi")
    },
)
```

`content` 本身不接收数据；`Window` 是它函数体内调用的另一个函数，`title` 属于 `Window`。

### 4.3 `Window` 接收参数但函数体可能只负责转发

一个 API 函数可以把参数继续传给底层实现，而不是在当前函数体直接消费。阅读框架代码时，要沿着 `onCloseRequest = onCloseRequest` 这类赋值继续追踪。

### 4.4 `::exitApplication` 不是函数执行

```kotlin
::exitApplication  // 引用
exitApplication()  // 调用
```

事件回调通常需要前者；否则在创建窗口时就会执行退出逻辑，并且传入的也不再是符合类型的函数值。

### 4.5 `Window` 和 `App` 看起来像类，但这里是可调用的函数

在 Compose 中，界面函数常用大写名称，例如 `Window`、`App`。名称大写不能单独证明它是类；结合调用形式和导入声明，当前代码中 `Window` 是函数，`App` 是项目定义的界面函数。

### 4.6 “嵌套”不等于物理窗口嵌套

`application -> Window -> App` 同时描述了调用/内容层级和界面职责层级。只有 `Window` 表示桌面窗口；`application` 是运行环境，`App` 是窗口中的 Compose 内容。

### 4.7 `ApplicationScope.() -> Unit` 不等于普通无参数函数

它没有显式位置参数，但有一个隐式接收者。初读时可以先把它简化成 `() -> Unit` 理解，再在需要调用作用域成员时回看 `ApplicationScope.`。

### 4.8 普通 `class` 与 `data class` 在 `==` 比较上的区别

在 Kotlin 中，`==` 操作符会翻译为调用对象的 `.equals()` 方法（引用地址比较则是 `===`）。

- **普通 class**（未手动重写 `equals`）：`s1 == s2` 默认按对象内存地址比较，只要不是同一个实例就返回 `false`。
- **data class**：编译器自动重写了 `equals()`，只要主构造方法中声明的所有属性值对应相等，`s1 == s2` 就会返回 `true`。

### 4.9 `data class` 配合 `val` 的不可变性原则

虽然 Kotlin 允许在 `data class` 的主构造方法中使用 `var`，但强烈推荐统一使用 `val`：
- **线程安全与防窜改**：只读对象（Immutable）在传递给 UI、跨线程或作为函数入参时极其安全，不用担心对象在不知情的情况下被其他地方修改。
- **状态修改的标准姿势**：如果需要“修改”某个属性，不要把属性改成 `var` 去直接赋值，而是使用编译器生成的 `copy()` 方法（如 `subject.copy(score = 9.0)`）生成一份修改后的全新副本。这也是 Compose 响应式状态管理的核心最佳实践。

### 4.10 误把 val 当作类型关键字，认为与冒号后的类型冲突

这是习惯了 Java 10 `var` 的开发者最容易产生的认知碰撞：

```kotlin
val sampleSubjects: List<Subject> = listOf(...)
```

**产生误解的原因**：
在 Java 10+ 中，`var` 的作用是**“替代/省略类型”**（如 `var list = List.of(...)`），写了 `var` 就绝不能再写具体的类型。因此，当看到 Kotlin 代码中最前面的 `val` 时，直觉上会把它当成“类型声明占位符”，进而觉得后面冒号跟的 `: List<Subject>` 产生了“两个类型互相冲突”的错觉。

**正确理解**：
1. Kotlin 中的 `val` / `var` 根本**不是类型**，而是**声明修饰符**，专门用来管“能不能二次赋值”（`val` 对应 Java 的 `final`，`var` 对应普通可变变量）。
2. 变量名后面冒号跟的 `: List<Subject>` 才是**唯一的真实类型**。
3. 两者分工明确：`val` 管可变性，冒号后的内容管数据类型，绝不冲突。
4. 虽然类型推导允许简写为 `val sampleSubjects = listOf(...)`，但对于对外暴露的公共顶层属性，显式写出 `: List<Subject>` 是推荐的工程实践。

### 4.11 过程式顺序思维 vs GUI 事件驱动心智模型（为什么底层控件能反向调用外层方法）

**常见困惑**：
“明明程序是从 `App` 开始自顶向下执行到 `SubjectListScreen` 再到 `OutlinedTextField`，当这套代码跑完后，`App` 已经不跑了，凭什么敲键盘时底层的输入框还能反过来调用外层的方法？”

**认知破局**：
这正是传统面向过程批处理脚本与现代 GUI 事件驱动（Event-Driven）程序的分水岭。
1. **构建期（交接电话号码）**：
   在首次自顶向下渲染时，`App` 并没有立刻执行更新。它只是把 `state::updateQuery` 作为一个**函数引用（如同留给子组件的一张紧急联系电话）**，通过参数一路交给最底层的 `OutlinedTextField`。输入框把这个引用存进了自己的内存组件节点中。至此，首遍执行全部结束，主线程进入休眠，等待系统事件循环。
2. **交互期（拨打电话）**：
   用户在物理键盘敲下 `"书"`。此时并不是 `App` 在主动跑，而是**操作系统捕获到了按键硬件中断**，发送给当前窗口并唤醒获得光标焦点的 `OutlinedTextField`。
3. **调用发生**：
   输入框被操作系统唤醒后，从内存里取出之前存好的联系电话，直接执行它：`onValueChange.invoke("书")`。这一瞬间，外层的 `state.updateQuery("书")` 就在这一刻被调通了。

### 4.12 Compose 受控组件（Controlled Component）与传统 Swing 控件的区别

- **传统 Java Swing 控件（自主控制）**：
  在 Swing 中，`JTextField` 自身维护文本缓冲区。用户敲键盘，它自己把字写进屏幕并显示出来，然后再被动向外发送通知。它的屏幕内容是由控件自身掌控的。
- **Compose 受控组件（提线木偶）**：
  `OutlinedTextField` 自身**没有任何自主决定屏幕文字的权力**。它在屏幕上显示什么，100% 由外层传入的 `value = query` 参数决定。用户敲键盘时，它无法自己把字写在屏幕上，只能通过 `onValueChange("书")` 向上告状。只有当外层的状态修改为 `"书"`，并且 Compose 重新调用它传入 `value = "书"` 时，新字符才会被允许绘制在屏幕上。

### 4.13 用户输入到界面刷新的完整生命周期三阶段

整个交互闭环严格遵循以下时序流动：

```text
【阶段一：初始化构建（自顶向下）】
Main() -> App() -> remember 创建 state(query="") -> Screen(query="") -> OutlinedTextField(value="")
-> 界面首次呈现，程序休眠进入事件等待

【阶段二：用户敲键盘交互（事件驱动与回调）】
用户敲击 "书" -> 操作系统硬件事件唤醒 OutlinedTextField -> 执行内部保存的 onValueChange("书")
-> 调用 onQueryChange("书") -> 触发 state.updateQuery("书") -> 修改了 state.query 为 "书"

【阶段三：响应式状态触发重组（再次自顶向下）】
mutableStateOf 检测到 query 被写入新值 -> 触发 Compose 运行时对依赖它的 App() 进行重组（重新执行）
-> App() 重新运行，remember 复用现有 state 实例（不重新 new）
-> 读取最新 query ("书") 与重新计算的 visibleSubjects (只剩 1 部作品)
-> 带着新数据调用 Screen(subjects = 1部, query = "书")
-> 调用 OutlinedTextField(value = "书")
-> 界面完成重绘，用户看到文本框文字上屏，且列表瞬间被筛选！
```

### 4.14 混淆函数类型 `(A) -> B` 与 Lambda 实现 `{ a -> b }`（同用箭头 `->` 的认知碰撞）

**产生困惑的原因**：
初学者看到 `items` 中的 `key = { subject -> subject.id }` 时，常误以为这是类似 `() -> Unit` 的“定义函数类型”语法，产生“这里难道在声明类型吗”的错觉。

**本质辨析**：
两者都包含箭头 `->`，但所处的**语法位置**与**职责**截然不同：

| 维度 | 函数类型（Function Type，定义规格） | Lambda 表达式（Function Literal，执行代码） |
| :--- | :--- | :--- |
| **扮演角色** | 类似 `String`、`Int`，是**类型的声明** | 具体的可执行**代码块（值）** |
| **外层括号** | **小括号** `( )` | **大括号** `{ }` |
| **括号内内容**| 放**类型名称**（大写开头的 `Subject`、`Any`、`Unit`） | 放**变量名称**与**执行逻辑**（小写开头的 `subject`、`subject.id`） |
| **箭头左侧** | 入参的**数据类型** | 入参的**形参变量名**（由写代码的人自由命名） |
| **箭头右侧** | 返回值的**数据类型** | 实际要计算并**返回的代码表达式** |
| **出现位置** | 变量声明的冒号 `:` 后面，或函数形参列表中 | 等号 `=` 后面作为值，或作为函数实参传递 |

**在同一行代码中对比两者的对应关系**：
```kotlin
//    [变量名] :     [函数类型 (规格)]    =       [Lambda 实现 (代码)]
      val getKey : (Subject) -> Any  =  { subject -> subject.id }
```
- 左侧 `(Subject) -> Any`（小括号 + 类型名）：告诉编译器“这是一个接收 `Subject`、返回 `Any` 的函数规格”。
- 右侧 `{ subject -> subject.id }`（大括号 + 变量名）：具体的代码实现，接收一个命名为 `subject` 的入参，返回它的 `id` 属性。

## 5. Kotlin 代码阅读技巧

### 5.1 先找当前调用的函数签名

看到：

```kotlin
Window(
    title = "Bangumi",
) {
    App()
}
```

先确认 `Window` 的最后一个参数是否是 Lambda，再判断括号外大括号的归属。不要先凭视觉猜它是不是代码块。

### 5.2 为每一层标记参数归属

可以临时改写成显式参数名：

```kotlin
application(
    content = {
        Window(
            title = "Bangumi",
            content = {
                App()
            },
        )
    },
)
```

这样能快速分辨 `application.content` 和 `Window.content`。

### 5.3 识别 Lambda 的参数和返回值

看到：

```kotlin
(KeyEvent) -> Boolean
```

读作“接收 `KeyEvent`，返回 `Boolean`”。看到：

```kotlin
() -> Unit
```

读作“不接收参数，返回 `Unit`”。看到：

```kotlin
FrameWindowScope.() -> Unit
```

再额外加上“有 `FrameWindowScope` 隐式接收者”。

### 5.4 区分四种相似写法

```kotlin
exitApplication()       // 立即调用
::exitApplication       // 函数引用
{ exitApplication() }   // 新建 Lambda，执行时才调用
exitApplication         // 在需要函数值的上下文中可能被推断为函数引用，但初学时不要依赖这种省略
```

优先把第四种写成明确的 `::exitApplication`，便于阅读和确认意图。

### 5.5 看到参数赋值时，先判断它是命名参数还是变量声明

```kotlin
Window(title = "Bangumi") // 命名参数
val title = "Bangumi"     // 变量声明
```

判断依据是所在语法位置：函数调用括号内的 `名字 = 值` 是命名参数；`val/var` 后面才是变量声明。

### 5.6 追踪回调的生命周期

对 `onCloseRequest` 这类参数，按以下顺序阅读：

```text
调用处传入什么
→ API 签名要求什么类型
→ 当前函数把参数传给谁或保存在哪里
→ 哪个事件最终调用这个函数
```

不要因为当前函数体没有出现 `onCloseRequest()` 就认为参数没有被使用。

### 5.7 区分代码层级和界面层级

当前入口可以同时画两张图：

```text
代码调用：main → application → Window → App
```

```text
界面职责：应用运行环境 → 桌面窗口 → 窗口内容
```

这能避免把 Lambda 的代码嵌套误认为窗口对象的物理嵌套。

## 6. 重点速查

| 语法 | 一句话记忆 |
| --- | --- |
| `fun name(...) {}` | 定义函数 |
| `fun name(...) = expression` | 用一个表达式作为函数体/返回结果 |
| `() -> Unit` | 无参数、无有意义返回值的函数类型 |
| `(Int) -> String` | 接收 `Int`、返回 `String` 的函数类型 |
| `Type.() -> Unit` | 带 `Type` 隐式接收者的 Lambda |
| `{ code }` | 没有名字的一段可传递代码 |
| `action()` | 调用函数值 |
| `::action` | 引用函数，不立即调用 |
| `name = value`（调用括号内） | 命名参数传值 |
| `param: Type = default` | 带默认值的参数 |
| `f { ... }` | 最后一个 Lambda 的尾随写法 |
| `content` | 常见参数名，不是 Kotlin 关键字 |
| `@Composable` | 标记 Compose 可组合内容/函数 |
| `application { ... }` | 调用 `application` 并传入 `content` Lambda |
| `Window(...) { ... }` | 调用 `Window`，最后一个 Lambda 是窗口内容 |
| `data class Name(...)` | 纯数据类，自动生成 `equals`、`hashCode`、`toString`、`copy` 等 |
| `val name: Type = val` | 完整变量声明（`val` 只读引用，`: Type` 类型标注） |
| `var name: Type = val` | 可变变量声明（允许后续重新赋值） |
| `val name: Type` | 只读属性（不可二次赋值，自带 getter） |
| `Type?` | 可空类型，允许为 `null`，编译期强制检查判空 |
| `listOf(...)` | 创建只读列表（类似 Java `List.of(...)`，无法增删改） |
| `obj.copy(prop = val)` | 复制数据类对象并修改指定属性，保留其余属性不变 |
| `mutableStateOf(init)` | 响应式状态容器，值变化时自动通知 Compose 触发依赖函数重组 |
| `remember(key) { ... }` | 跨重组缓存对象，充当函数式 UI 的“成员变量”，防重复 new |
| `instance::method` | 绑定到具体对象实例的方法引用，保留该对象的 this 上下文 |
| `(Type) -> ReturnType` | 函数类型：声明函数规格（小括号包裹类型名称） |
| `{ param -> body }` | Lambda 表达式：函数可执行实现（大括号包裹变量名与逻辑） |

## 7. 待继续学习

以下内容在后续深入开发时值得继续探索：

- `ApplicationScope`、`FrameWindowScope` 接收者的完整作用域规则。
- `Window` 到 `SwingWindow` 的底层窗口事件准确触发路径。
- `rememberWindowState` 的状态保存机制。
- `dp`、`WindowState`、默认参数和 Compose 状态之间的关系。
- Compose 进阶性能优化（如 `derivedStateOf`、重组跳过规则等）。

## 8. 更新记录

| 日期 | 新增/修改内容 | 来源或学习场景 |
| --- | --- | --- |
| 2026-09-18 | 新建文档；整理顶层 `main`、表达式函数体、Lambda、函数类型、高阶函数、函数引用、命名/默认参数、尾随 Lambda、带接收者 Lambda，以及 Compose `application`/`Window`/`content` 的阅读方式。 | 阅读 `composeApp/src/desktopMain/kotlin/dev/bangumi/demo/Main.kt`，围绕 `fun main() = application { ... }`、`onCloseRequest = ::exitApplication` 和 `content` 展开讨论。 |
| 2026-09-19 | 新增 `data class`（数据类）、`val` 只读属性与 `Type?` 可空类型；补充编译器生成方法、Java 对照（`record`/Lombok/POJO）、`==` 比较机制与不可变性设计。 | 阅读 `composeApp/src/commonMain/kotlin/dev/bangumi/demo/model/Subject.kt`，梳理 `model` 目录职责与数据类原理。 |
| 2026-09-19 | 新增 `val/var` 变量声明结构、类型推导、Java 10 `var` 对比误区澄清，以及只读列表 `listOf(...)` 的说明。 | 阅读 `composeApp/src/commonMain/kotlin/dev/bangumi/demo/data/SampleSubjects.kt`，梳理 `val sampleSubjects: List<Subject>` 的语法构成与 Java 对照。 |
| 2026-09-19 | 补充响应式状态（`mutableStateOf`）、状态记忆（`remember`）、绑定实例的方法引用（`state::updateQuery`）、受控组件（Controlled Component）机制，以及从物理按键到界面重组刷新的三阶段完整闭环生命周期。 | 阅读 `App.kt`、`SubjectListState.kt` 与 `SubjectListScreen.kt`，系统梳理 GUI 事件驱动与响应式状态流转心智模型。 |
| 2026-09-20 | 辨析函数类型声明 `(Type) -> ReturnType` 与 Lambda 实现 `{ param -> body }` 的本质区别（小括号与类型名 vs 大括号与变量名/实现代码）。 | 阅读 `SubjectListScreen.kt`，围绕 Lambda 箭头语法与函数类型的认知混淆展开讨论。 |
