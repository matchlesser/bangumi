package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.components.LoadMoreFooter
import dev.bangumi.demo.ui.components.SubjectCard
import dev.bangumi.demo.ui.components.formatCount
import dev.bangumi.demo.ui.state.SubjectListState
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 搜索页：只读取 state 中的数据绘制页面，把用户操作通过回调上报；页面本身不发网络请求。
 * (String) -> Unit 类似 Java Consumer<String>，() -> Unit 类似 Runnable；Unit 表示不返回有用值。
 *
 * @param state 搜索状态：输入框文字、结果、加载与错误情况。
 * @param listState 结果列表的滚动位置；由调用方保存，从详情页返回时仍停在原来的位置。
 * @param onSearch 用户按回车、点击“搜索”或“重试”时调用。
 * @param onLoadMore 点击列表底部的“加载更多”或其“重试”时调用。
 * @param onSubjectClick 点击条目时调用，传入被点击的条目；类似 Java 的 Consumer<Subject>，页面切换由调用方决定。
 */
@Composable
fun SubjectListScreen(
    state: SubjectListState,
    listState: LazyListState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onClearQuery: () -> Unit,
    onSubjectClick: (Subject) -> Unit,
) {
    // 先取出本次绘制要用的值。局部 val 只读一次，下面判空后编译器能自动当作非空使用（智能转换）。
    val results = state.results
    val subjects = results?.items
    val query = state.query
    val isLoading = state.isLoading
    val errorMessage = state.errorMessage

    // Box 负责水平居中；内层 Column 限制内容宽度，并从上到下排列标题、搜索栏和结果区。
    Box(
        modifier = Modifier.fillMaxSize().padding(AppDimensions.largeSpacing),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = AppDimensions.contentMaxWidth).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.largeSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
                Text(
                    text = "Bangumi",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "动画搜索",
                    style = MaterialTheme.typography.titleLarge,
                )
                // 说明数据来源，与以前的“离线示例数据”区分开。
                Text(
                    text = "数据来自 Bangumi 番组计划（bgm.tv），搜索需要联网。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 搜索栏：输入框占满剩余宽度，右侧是“搜索”按钮。
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 受控输入：value 来自状态，onValueChange 只上报新文本；输入时不发请求。
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("搜索 Bangumi 动画") },
                    placeholder = { Text("例如：孤独摇滚") },
                    // imeAction 声明回车键的含义是“搜索”；按下回车时执行 KeyboardActions 中的 onSearch。
                    // 花括号里的 onSearch() 调用的是本函数的参数，即上报给状态层的搜索事件。
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            TextButton(onClick = onClearQuery) {
                                Text("清空")
                            }
                        }
                    },
                )
                // 正在加载或关键词为空时禁用按钮，避免重复请求或发出空搜索。
                Button(
                    onClick = onSearch,
                    enabled = !isLoading && query.isNotBlank(),
                ) {
                    Text("搜索")
                }
            }

            // 结果区占据标题和搜索栏之外的空间；同一时间只显示下面一种情况。
            // when 不带参数时按顺序检查条件，类似 Java 的 if / else if 链。
            val contentModifier = Modifier.fillMaxWidth().weight(1f)
            when {
                isLoading -> StatusMessage(
                    title = "正在搜索…",
                    detail = "正在等待 Bangumi 返回结果。",
                    modifier = contentModifier,
                    showProgress = true,
                )

                // “重试”就是用当前关键词再搜索一次，所以直接复用 onSearch。
                errorMessage != null -> StatusMessage(
                    title = "搜索失败",
                    detail = "请检查网络连接后重试。\n原因：$errorMessage",
                    modifier = contentModifier,
                    actionLabel = "重试",
                    onAction = onSearch,
                )

                results == null || subjects == null -> StatusMessage(
                    title = "输入作品名称开始搜索",
                    detail = "按回车或点击“搜索”，结果来自 Bangumi。",
                    modifier = contentModifier,
                )

                // 前一个分支已排除 null，编译器在这里自动把 results 和 subjects 当作非空（智能转换）。
                subjects.isEmpty() -> StatusMessage(
                    title = "没有找到匹配的番剧",
                    detail = "换个关键词试试，或清空后重新输入。",
                    modifier = contentModifier,
                    actionLabel = "清空关键词",
                    onAction = onClearQuery,
                )

                else -> Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
                ) {
                    // 总数来自服务器；每次请求 20 条，更多结果通过列表底部的“加载更多”取得。
                    Text(
                        text = "共找到 ${formatCount(results.total)} 部作品",
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
                        items(items = subjects, key = { subject -> subject.id }) { subject ->
                            // 卡片的 onClick 不带参数，这里用 Lambda 把当前 subject 补上再上报，
                            // 卡片因此不需要知道列表页的回调类型。
                            SubjectCard(
                                subject = subject,
                                onClick = { onSubjectClick(subject) },
                            )
                        }
                        // item { } 在列表末尾追加一行非条目内容：加载更多 / 加载中 / 重试 / 已全部显示。
                        item(key = "load-more") {
                            LoadMoreFooter(
                                shown = subjects.size,
                                total = results.total,
                                unit = "部",
                                isLoading = state.isLoadingMore,
                                error = state.loadMoreError,
                                onLoadMore = onLoadMore,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 结果区中的统一提示：加载中、出错、尚未搜索、没有结果都使用同一种居中排版，保证表现一致。
 * private 表示只在本文件中使用。
 *
 * @param showProgress 为 true 时在标题上方显示转圈的加载指示器。
 * @param actionLabel 按钮文字；null 表示不显示按钮。
 * @param onAction 点击按钮时执行的操作。
 */
@Composable
private fun StatusMessage(
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
