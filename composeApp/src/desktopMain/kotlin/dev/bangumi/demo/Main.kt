package dev.bangumi.demo

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.bangumi.demo.data.BangumiApi
import dev.bangumi.demo.ui.App

// Kotlin 顶层 main 是程序入口，对应 Java 的 public static void main。
fun main() {
    // 整个程序共用一个 BangumiApi（内部持有一个 HTTP 客户端），在入口创建一次，避免每次搜索都新建连接。
    val api = BangumiApi()

    // application 管理桌面事件循环；尾随 Lambda 中声明窗口，JVM 专用入口只放 desktopMain。
    application {
        // 关闭窗口时退出应用；rememberWindowState 保存窗口尺寸，默认 960 × 720 dp。
        Window(
            onCloseRequest = ::exitApplication,
            title = "Bangumi",
            state = rememberWindowState(width = 960.dp, height = 720.dp),
        ) {
            // 把数据层交给共享界面；界面再把其中的方法交给各页面的状态对象，
            // 页面本身只读取数据和上报事件，不直接接触网络代码。
            App(api = api)
        }
    }
}
