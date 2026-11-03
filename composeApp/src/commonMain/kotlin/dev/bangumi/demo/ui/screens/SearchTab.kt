package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import dev.bangumi.demo.model.SearchSort
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectType
import dev.bangumi.demo.ui.components.OptionChips
import dev.bangumi.demo.ui.components.OptionDropdown
import dev.bangumi.demo.ui.components.SubjectResults
import dev.bangumi.demo.ui.state.SubjectListState
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 搜索标签页：搜索栏、类型筛选和排序、结果列表。
 * 只读取 state 中的数据绘制，把用户操作通过回调上报；页面本身不发网络请求。
 * (String) -> Unit 类似 Java Consumer<String>，() -> Unit 类似 Runnable；Unit 表示不返回有用值。
 *
 * @param listState 结果列表的滚动位置；由调用方保存，从详情页返回时仍停在原来的位置。
 * @param onSearch 用户按回车、点击“搜索”或“重试”时调用。
 * @param onTypeChange 点击类型筛选标签时调用；null 表示全部类型。
 * @param onSortChange 选择排序方式时调用。
 * @param onLoadMore 点击列表底部的“加载更多”或其“重试”时调用。
 * @param onSubjectClick 点击条目时调用，页面切换由调用方决定。
 */
@Composable
fun SearchTab(
    state: SubjectListState,
    listState: LazyListState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTypeChange: (SubjectType?) -> Unit,
    onSortChange: (SearchSort) -> Unit,
    onLoadMore: () -> Unit,
    onClearQuery: () -> Unit,
    onSubjectClick: (Subject) -> Unit,
) {
    val query = state.query

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
    ) {
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
                label = { Text("搜索 Bangumi 条目") },
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
            // 关键词为空时禁用按钮，避免发出空搜索。
            Button(onClick = onSearch, enabled = query.isNotBlank()) {
                Text("搜索")
            }
        }

        // 筛选栏：左侧是类型标签（全部、动画、书籍……），右侧是排序下拉菜单。
        // listOf(null) + entries：在全部类型前面加一个 null，表示“全部”。
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OptionChips(
                options = listOf(null) + SubjectType.entries,
                selected = state.typeFilter,
                label = { type -> type?.label ?: "全部" },
                onSelect = onTypeChange,
                modifier = Modifier.weight(1f),
            )
            OptionDropdown(
                title = "排序",
                options = SearchSort.entries,
                selected = state.sort,
                label = { it.label },
                onSelect = onSortChange,
            )
        }

        SubjectResults(
            results = state.results,
            listState = listState,
            idleTitle = "输入作品名称开始搜索",
            idleDetail = "按回车或点击“搜索”；可以用上面的标签只看某一类型。",
            emptyTitle = "没有找到匹配的条目",
            emptyDetail = "换个关键词或类型试试，或清空后重新输入。",
            onRetry = onSearch,
            onLoadMore = onLoadMore,
            onSubjectClick = onSubjectClick,
            modifier = Modifier.fillMaxWidth().weight(1f),
            emptyActionLabel = "清空关键词",
            onEmptyAction = onClearQuery,
        )
    }
}
