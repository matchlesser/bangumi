package dev.bangumi.demo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.theme.AppDimensions


/**
 * 番剧信息卡片组件
 *
 * 显示单个番剧的核心信息：名称、评分和简介
 *
 * @param subject 要显示的番剧数据对象
 */
@Composable
fun SubjectCard(subject: Subject) {
    // 外层卡片容器
    Card(modifier = Modifier.fillMaxWidth()) {
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
                // 评分标签：背景色容器
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    // Kotlin 的 if 可以直接产生一个值，类似 Java 的条件表达式。
                    // 如果评分为空则显示"暂无评分"，否则显示评分值（满分10分）
                    Text(
                        text = if (subject.score == null) "暂无评分" else "${subject.score} / 10",
                        modifier = Modifier.padding(AppDimensions.smallSpacing),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            // 番剧简介文本
            Text(
                text = subject.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
