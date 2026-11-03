package dev.bangumi.demo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    // 可点击与焦点描边由 ClickableCard 统一处理。
    ClickableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
            // 部分真实条目没有简介，此时不画这一行，避免卡片里留一块空白。
            if (subject.summary.isNotBlank()) {
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
}
