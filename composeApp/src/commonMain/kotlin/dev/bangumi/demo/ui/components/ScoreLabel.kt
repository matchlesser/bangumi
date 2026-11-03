package dev.bangumi.demo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 评分标签：列表卡片和详情页共用，保证“暂无评分”的文案与底色在各页面一致。
 *
 * @param score 十分制评分；null 表示没有评分，此时显示“暂无评分”。
 */
@Composable
fun ScoreLabel(
    score: Double?,
    modifier: Modifier = Modifier,
) {
    // score 是函数参数（只读），判空后编译器能自动把它当作非空 Double 使用。
    TagLabel(
        text = if (score == null) "暂无评分" else "$score / 10",
        modifier = modifier,
    )
}

/**
 * 小标签：带圆角底色的一小段文字，用于评分、角色定位（主角/配角）、收藏状态、作品类型等。
 * 颜色与形状取自主题，各处标签外观一致。
 */
@Composable
fun TagLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
