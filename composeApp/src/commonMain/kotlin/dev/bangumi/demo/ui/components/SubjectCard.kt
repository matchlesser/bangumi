package dev.bangumi.demo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 番剧信息卡片组件
 *
 * 显示单个番剧的核心信息：名称、评分和简介预览；完整简介留给详情页。
 * 卡片只上报“被点击”，不知道点击后显示什么页面，因此同一张卡片可以放进不同列表复用。
 *
 * @param subject 要显示的番剧数据对象
 * @param onClick 点击卡片时的回调函数，() -> Unit 类似 Java 的 Runnable 监听器
 */
@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
) {
    // interactionSource 记录卡片的交互（按下、悬停、获得焦点等）；remember 让它在重组之间保持同一个对象。
    // collectIsFocusedAsState 把“是否有键盘焦点”转换成可观察状态，焦点变化时卡片会重组。
    // 类比 Java Swing：给控件注册 FocusListener，在 focusGained/focusLost 里切换边框。
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 带 onClick 的 Card 可被鼠标点击，也能用 Tab 获得焦点后按 Enter 触发。
    // 默认焦点反馈只是底色略深，不够明显；获得焦点时再加一圈主题色描边，没有焦点时不画边框（null）。
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = if (isFocused) {
            BorderStroke(AppDimensions.focusBorderWidth, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        interactionSource = interactionSource,
    ) {
        // 垂直布局：标题行 + 简介文本
        Column(
            modifier = Modifier.padding(AppDimensions.mediumSpacing),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
        ) {
            // 标题行：番剧名称 + 评分标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 番剧名称，占据可用空间
                Text(
                    text = subject.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                // 评分显示规则集中在 ScoreLabel，详情页使用同一个组件。
                ScoreLabel(score = subject.score)
            }
            // 简介最多显示两行，超出部分用省略号表示；点击卡片可在详情页查看全文。
            Text(
                text = subject.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
