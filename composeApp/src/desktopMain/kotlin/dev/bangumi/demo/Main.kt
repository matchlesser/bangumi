package dev.bangumi.demo

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.bangumi.demo.data.sampleSubjects
import dev.bangumi.demo.ui.App

// Kotlin 顶层 main 是程序入口，对应 Java 的 public static void main。
// application 管理桌面事件循环；尾随 Lambda 中声明窗口，JVM 专用入口只放 desktopMain。
fun main() = application {
    // 关闭窗口时退出应用；rememberWindowState 保存窗口尺寸，默认 960 × 720 dp。
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bangumi · 离线示例",
        state = rememberWindowState(width = 960.dp, height = 720.dp),
    ) {
        // 在入口选择离线数据，共享页面仅接收列表，未来替换数据来源时无需让 UI 读文件或请求网络。
        App(subjects = sampleSubjects)
    }
}
