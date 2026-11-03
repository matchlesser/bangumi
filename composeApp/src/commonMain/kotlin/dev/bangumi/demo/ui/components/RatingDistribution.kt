package dev.bangumi.demo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 评分分布：从 10 分到 1 分各一行，每行是“分数、横条、人数（百分比）”。
 * 横条长度按人数最多的那一档为满格计算，所以最多的一档总是画满，便于比较各档差距。
 * 除了横条长短，每行都写出具体人数和百分比，不只靠图形或颜色传达信息。
 *
 * @param counts 固定 10 个元素：下标 0 对应 1 分，下标 9 对应 10 分（见 SubjectDetail.ratingCounts）。
 */
@Composable
fun RatingDistribution(
    counts: List<Int>,
    modifier: Modifier = Modifier,
) {
    val total = counts.sum()
    // 人数最多的一档作为满格；全部为 0 时用 1 避免除以 0。
    val max = counts.maxOrNull()?.takeIf { it > 0 } ?: 1

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 10 downTo 1：从 10 分倒数到 1 分，类似 Java 的 for (int i = 10; i >= 1; i--)。
        for (point in 10 downTo 1) {
            val count = counts.getOrElse(point - 1) { 0 }
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$point",
                    modifier = Modifier.width(24.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.End,
                )
                // 外层浅色底槽占满剩余宽度，内层深色横条按比例填充。
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(count.toFloat() / max)
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall),
                    )
                }
                Text(
                    text = "${formatCount(count)}（${formatPercent(count, total)}）",
                    modifier = Modifier.width(140.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
