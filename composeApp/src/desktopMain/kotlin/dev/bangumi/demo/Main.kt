package dev.bangumi.demo

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.bangumi.demo.data.BangumiApi
import dev.bangumi.demo.ui.App
import dev.bangumi.demo.ui.handleBackShortcut
import dev.bangumi.demo.ui.state.AppNavigator
import javax.imageio.ImageIO

// Kotlin 顶层 main 是程序入口，对应 Java 的 public static void main。
fun main() {
    // 整个程序共用一个 BangumiApi（内部持有一个 HTTP 客户端），在入口创建一次，避免每次搜索都新建连接。
    val api = BangumiApi()
    // 页面返回栈也在入口创建：窗口的按键处理（Esc 返回）和共享界面需要使用同一个导航器。
    val navigator = AppNavigator(api)
    // 窗口标题栏和任务栏的图标，与安装包图标相同。
    val icon = loadWindowIcon()

    // application 管理桌面事件循环；尾随 Lambda 中声明窗口，JVM 专用入口只放 desktopMain。
    application {
        // 关闭窗口时退出应用；rememberWindowState 保存窗口尺寸，默认 960 × 720 dp。
        Window(
            onCloseRequest = ::exitApplication,
            title = "Bangumi",
            icon = icon,
            state = rememberWindowState(width = 960.dp, height = 720.dp),
            // 窗口级按键处理：页面中的组件没有处理的按键会到这里，Esc 用来返回上一页。
            onKeyEvent = { event -> handleBackShortcut(event, navigator) },
        ) {
            // 把数据层和导航器交给共享界面；界面再把数据层的方法交给各页面的状态对象，
            // 页面本身只读取数据和上报事件，不直接接触网络代码。
            App(api = api, navigator = navigator)
        }
    }
}

/**
 * 从程序资源中读取窗口图标（desktopMain/resources/app-icon.png，由 tools/MakeIcon.java 生成）。
 * 用 Java 的 ImageIO 读取 PNG，再转成 Compose 可以绘制的图片；这些都是 JVM 专用 API，所以只放在 desktopMain。
 * 读取失败时返回 null，窗口使用默认图标，不影响程序运行。
 */
private fun loadWindowIcon(): Painter? {
    val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("app-icon.png") ?: return null
    // use { } 读取完后自动关闭输入流，类似 Java 的 try-with-resources。
    return stream.use { input -> ImageIO.read(input)?.let { BitmapPainter(it.toComposeImageBitmap()) } }
}
