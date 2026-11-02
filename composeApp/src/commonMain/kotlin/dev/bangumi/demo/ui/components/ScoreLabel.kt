package dev.bangumi.demo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 评分标签：列表卡片和详情页共用，保证“暂无评分”的文案与底色在各页面一致。
 *
 * 单独成组件的原因：评分显示规则只维护在这一处；以后改成星级或换颜色时，
 * 不需要逐个页面查找相同的 if 判断。类比 Java 中把重复的界面片段抽成一个自定义控件类。
 *
 * @param score 十分制评分；null 表示示例数据没有评分，此时显示“暂无评分”。
 * @param modifier 由调用方决定外部位置或间距；默认值 Modifier 表示不额外修饰，
 *   这是 Compose 组件的通用约定，类似 Java 方法重载中“不传就用默认配置”的版本。
 */
@Composable
fun ScoreLabel(
    score: Double?,
    modifier: Modifier = Modifier,
) {
    // Surface 提供带圆角的背景色块，颜色与形状都取自主题，不在组件里写死。
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        // score 是函数参数（只读），判空后编译器能自动把它当作非空 Double 使用。
        Text(
            text = if (score == null) "暂无评分" else "$score / 10",
            modifier = Modifier.padding(AppDimensions.smallSpacing),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
