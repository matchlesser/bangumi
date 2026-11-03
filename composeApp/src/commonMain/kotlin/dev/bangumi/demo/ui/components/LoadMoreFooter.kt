package dev.bangumi.demo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 分页列表底部的“加载更多”区域，搜索结果和吐槽共用，同一时间只显示下面一种情况：
 * - 正在加载下一页：小转圈和“正在加载更多…”；
 * - 上一次加载失败：原因和“重试”按钮（已显示的内容保留）；
 * - 还有更多：“加载更多”按钮，并写出已显示数量和总数；
 * - 已经全部显示：一行说明文字。
 *
 * 用按钮而不是滚动到底自动加载：每次请求都由用户明确触发，也可以用 Tab 和 Enter 操作。
 *
 * @param shown 已显示的条数。
 * @param total 总条数。
 * @param unit 计数单位，例如“部”“条”。
 */
@Composable
fun LoadMoreFooter(
    shown: Int,
    total: Int,
    unit: String,
    isLoading: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppDimensions.smallSpacing),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                SecondaryText("正在加载更多…")
            }

            error != null -> Row(
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "加载更多失败：$error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                // 重试就是再加载一次下一页，所以直接复用 onLoadMore。
                TextButton(onClick = onLoadMore) {
                    Text("重试")
                }
            }

            shown < total -> OutlinedButton(onClick = onLoadMore) {
                Text("加载更多（已显示 ${formatCount(shown)} / 共 ${formatCount(total)} $unit）")
            }

            else -> SecondaryText("已显示全部 ${formatCount(total)} $unit")
        }
    }
}
