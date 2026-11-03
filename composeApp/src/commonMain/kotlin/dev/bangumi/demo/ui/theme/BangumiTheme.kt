package dev.bangumi.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// 复用 Material 3 默认浅色配色；集中管理，页面不各自指定品牌颜色。
private val lightColors = lightColorScheme()

/** object 声明单例，类似 Java 中集中存放静态常量的工具类；dp 用于适配显示缩放。 */
object AppDimensions {
    val smallSpacing = 8.dp
    val mediumSpacing = 16.dp
    val largeSpacing = 24.dp
    val contentMaxWidth = 840.dp

    // 可点击卡片获得键盘焦点时的描边宽度；用形状变化提示焦点，而不只依赖底色深浅。
    val focusBorderWidth = 2.dp

    // 图片尺寸。封面按 Bangumi 常见的竖版比例（约 5:7），各页面引用这里的值保持一致。
    val listCoverWidth = 64.dp
    val listCoverHeight = 90.dp
    val detailCoverWidth = 140.dp
    val detailCoverHeight = 196.dp
    val thumbnailCoverWidth = 40.dp
    val thumbnailCoverHeight = 56.dp
    val avatarSize = 48.dp
    val smallAvatarSize = 32.dp
}

/** content 是可绘制界面的回调；为所有子组件提供相同颜色、字体和形状。 */
@Composable
fun BangumiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColors,
        content = content,
    )
}
