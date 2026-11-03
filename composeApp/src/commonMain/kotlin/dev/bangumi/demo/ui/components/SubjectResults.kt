package dev.bangumi.demo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.state.PagedResults
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 条目结果区，搜索页和索引页共用。同一时间只显示下面一种情况：
 * 加载中 → 失败与重试 → 尚未加载的提示 → 没有结果 → 结果列表（末尾是“加载更多”）。
 *
 * @param results 结果及加载状态。PagedResults<*, Subject> 中的 * 表示“请求条件是什么类型都可以”，
 *   类似 Java 的 PagedResults<?, Subject>，因为这里只读取结果，不关心请求条件。
 * @param listState 列表滚动位置，由调用方保存。
 * @param idleTitle、idleDetail 还没有加载过时的提示文字。
 * @param emptyTitle、emptyDetail 加载完成但没有结果时的提示文字。
 * @param emptyActionLabel 没有结果时显示的按钮文字（例如“清空关键词”）；null 表示不显示按钮。
 * @param onRetry 第一页失败后点“重试”时调用。
 */
@Composable
fun SubjectResults(
    results: PagedResults<*, Subject>,
    listState: LazyListState,
    idleTitle: String,
    idleDetail: String,
    emptyTitle: String,
    emptyDetail: String,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onSubjectClick: (Subject) -> Unit,
    modifier: Modifier = Modifier,
    emptyActionLabel: String? = null,
    onEmptyAction: () -> Unit = {},
) {
    // 局部 val 只读一次，下面判空后编译器能自动当作非空使用（智能转换）。
    val page = results.page
    val error = results.error

    when {
        results.isLoading -> StatusMessage(
            title = "正在加载…",
            detail = "正在等待 Bangumi 返回结果。",
            modifier = modifier,
            showProgress = true,
        )

        error != null -> StatusMessage(
            title = "加载失败",
            detail = "请检查网络连接后重试。\n原因：$error",
            modifier = modifier,
            actionLabel = "重试",
            onAction = onRetry,
        )

        page == null -> StatusMessage(title = idleTitle, detail = idleDetail, modifier = modifier)

        page.items.isEmpty() -> StatusMessage(
            title = emptyTitle,
            detail = emptyDetail,
            modifier = modifier,
            actionLabel = emptyActionLabel,
            onAction = onEmptyAction,
        )

        else -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
        ) {
            // 总数来自服务器；每次请求 20 条，更多结果通过列表底部的“加载更多”取得。
            Text(
                text = "共找到 ${formatCount(page.total)} 个条目",
                style = MaterialTheme.typography.titleMedium,
            )
            // LazyColumn 按需绘制可见条目，结果较多时可以滚动。
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
                contentPadding = PaddingValues(bottom = AppDimensions.smallSpacing),
            ) {
                // 稳定 id 用于识别条目，避免结果变化后把其他条目当成原来的条目。
                items(items = page.items, key = { subject -> subject.id }) { subject ->
                    // 卡片的 onClick 不带参数，这里用 Lambda 把当前 subject 补上再上报。
                    SubjectCard(subject = subject, onClick = { onSubjectClick(subject) })
                }
                // item { } 在列表末尾追加一行非条目内容：加载更多 / 加载中 / 重试 / 已全部显示。
                item(key = "load-more") {
                    LoadMoreFooter(
                        shown = page.items.size,
                        total = page.total,
                        unit = "个",
                        isLoading = results.isLoadingMore,
                        error = results.loadMoreError,
                        onLoadMore = onLoadMore,
                    )
                }
            }
        }
    }
}

/**
 * 结果区中的统一提示：加载中、出错、尚未加载、没有结果都使用同一种居中排版，保证表现一致。
 *
 * @param showProgress 为 true 时在标题上方显示转圈的加载指示器。
 * @param actionLabel 按钮文字；null 表示不显示按钮。
 * @param onAction 点击按钮时执行的操作。
 */
@Composable
fun StatusMessage(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            space = AppDimensions.mediumSpacing,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showProgress) {
            CircularProgressIndicator()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null) {
            OutlinedButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * 说明 LazyColumn 的工作原理。
 *
【你在页面写的代码】：
items(items = subjects) { subject ->
    SubjectCard(subject = subject)   <── 你的卡片绘制函数
}
      │
      │ 传给了外层 items 方法的 itemContent 形参
      ▼
【外层包装 items 方法】：
底层的 items(count = subjects.size) { index ->
    val subject = subjects[index]
    itemContent(subject)             <── 把它转手交给了底层引擎
}
      │
      │ 注册给了 Compose 底层滚动系统
      ▼
【底层 LazyColumn 滚动引擎】：
当用户滚动屏幕，需要画第 1 个位置时：
引擎执行：itemContent(subjects[1])
结果：屏幕上成功画出了第 1 部番剧的 SubjectCard！
 */
