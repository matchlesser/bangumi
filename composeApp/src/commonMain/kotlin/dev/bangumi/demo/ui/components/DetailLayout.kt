package dev.bangumi.demo.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bangumi.demo.ui.state.LoadState
import dev.bangumi.demo.ui.theme.AppDimensions

// 条目详情页与角色详情页共用的排版部件，保证两个页面的边距、标题和加载/失败提示一致。

/**
 * 详情页外框：顶部是固定的“返回”栏，下面是可滚动的内容区，边距和最大内容宽度与搜索页相同。
 * 返回栏不随内容滚动，在长页面的任何位置都能直接返回。
 *
 * @param scrollState 滚动位置；由返回栈中的页面对象保存，从下一页返回时回到原来的位置。
 * @param content 页面内容，按从上到下排列。
 */
@Composable
fun DetailPage(
    scrollState: ScrollState,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 固定的返回栏。文字按钮可用鼠标点击，也可用 Tab 聚焦后按 Enter；未引入图标库，因此用文字箭头。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimensions.largeSpacing, vertical = AppDimensions.smallSpacing),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(modifier = Modifier.widthIn(max = AppDimensions.contentMaxWidth).fillMaxWidth()) {
                TextButton(onClick = onBack) {
                    Text("← 返回（Esc）")
                }
            }
        }
        HorizontalDivider()

        // 可滚动的内容区：weight(1f) 占满返回栏以下的空间。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(AppDimensions.largeSpacing),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = AppDimensions.contentMaxWidth).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.largeSpacing),
                content = content,
            )
        }
    }
}

/** 详情页中的一个区块：小标题 + 内容。 */
@Composable
fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        content()
    }
}

/**
 * 按 LoadState 显示一块内容：加载中显示小转圈，失败显示原因和“重试”，成功时调用 content 绘制数据。
 *
 * <T> 是泛型参数，与 Java 泛型方法相同：state 中的数据是什么类型，content 收到的就是什么类型。
 */
@Composable
fun <T> LoadStateContent(
    state: LoadState<T>,
    onRetry: () -> Unit,
    content: @Composable (T) -> Unit,
) {
    // when 处理了密封接口的全部三种情况，所以不需要 else。
    when (state) {
        LoadState.Loading -> Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(
                text = "加载中…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is LoadState.Error -> Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "加载失败：${state.message}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        }

        is LoadState.Success -> content(state.data)
    }
}

/** 次要说明文字（来源、统计等），统一使用较小、较浅的样式。 */
@Composable
fun SecondaryText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
