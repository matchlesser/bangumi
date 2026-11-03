package dev.bangumi.demo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 可点击的卡片：鼠标点击或 Tab 聚焦后按 Enter 都会触发 onClick。
 * 获得键盘焦点时加一圈主题色描边，让焦点不只靠底色深浅区分（见项目约定“界面一致性”）。
 * 条目卡片、角色卡片、出演作品等所有可点击卡片都使用它，保证表现一致。
 *
 * @param content 卡片内容；ColumnScope 表示内容按从上到下排列，与 Card 本身的约定相同。
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // interactionSource 记录卡片的交互（按下、悬停、获得焦点等）；collectIsFocusedAsState 把“是否有焦点”变成可观察状态。
    // 类比 Java Swing：给控件注册 FocusListener，在 focusGained/focusLost 里切换边框。
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Card(
        onClick = onClick,
        modifier = modifier,
        border = if (isFocused) {
            BorderStroke(AppDimensions.focusBorderWidth, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        interactionSource = interactionSource,
        content = content,
    )
}
