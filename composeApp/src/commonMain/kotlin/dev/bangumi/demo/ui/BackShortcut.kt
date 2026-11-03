package dev.bangumi.demo.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import dev.bangumi.demo.ui.state.AppNavigator

/**
 * “返回上一页”的快捷键：按下 Esc 时，如果当前不在搜索页，就返回上一页。
 *
 * 由桌面入口在窗口级别调用（见 Main.kt 中 Window 的 onKeyEvent），
 * 所以无论页面上哪个元素有键盘焦点、甚至没有元素有焦点，都能生效。
 * 判断逻辑放在共享代码中，以后其他平台（例如手机的系统返回键）也可以复用同一个导航器。
 *
 * @return true 表示这个按键已经处理；false 表示不相关，交给其他地方处理。
 */
fun handleBackShortcut(event: KeyEvent, navigator: AppNavigator): Boolean =
    handleBackShortcut(key = event.key, type = event.type, navigator = navigator)

/**
 * 上面函数的实际判断，只看“哪个键”和“按下还是松开”。
 * 单独拆出来是为了测试：Key.Escape、KeyEventType.KeyDown 都是公开常量，测试不需要构造完整的按键事件。
 * internal 表示只在本模块（包括测试）内可见。
 */
internal fun handleBackShortcut(key: Key, type: KeyEventType, navigator: AppNavigator): Boolean {
    // 只处理“按下”，不处理“松开”，否则按一次 Esc 会触发两次、连退两层。
    if (type != KeyEventType.KeyDown || key != Key.Escape) return false
    // 已经在搜索页时没有上一页，不处理，让输入框等组件照常收到 Esc。
    if (navigator.depth <= 1) return false
    navigator.back()
    return true
}
