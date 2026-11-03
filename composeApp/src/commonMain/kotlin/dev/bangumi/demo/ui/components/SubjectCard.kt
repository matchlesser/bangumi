package dev.bangumi.demo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * 显示单个条目的核心信息：封面、名称、评分、类型与日期、简介预览；完整简介留给详情页。
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
        // 左侧封面，右侧文字（标题行 + 简介）。
        Row(
            modifier = Modifier.padding(AppDimensions.mediumSpacing),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
        ) {
            // 标题就在旁边，封面只是装饰，所以 contentDescription 传 null，读屏软件不重复朗读。
            RemoteImage(
                url = subject.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(AppDimensions.listCoverWidth, AppDimensions.listCoverHeight),
            )
            Column(
                modifier = Modifier.weight(1f),
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
                // 类型与日期行：例如“动画 · TV”标签和“2022-10-08”，混合类型的搜索结果靠它区分动画、漫画等。
                val typeLabel = subject.typeLabel
                if (typeLabel != null || subject.date != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (typeLabel != null) TagLabel(text = typeLabel)
                        subject.date?.let { SecondaryText(it) }
                    }
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
}
