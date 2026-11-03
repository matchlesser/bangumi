package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bangumi.demo.model.BrowseSort
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectCategory
import dev.bangumi.demo.model.SubjectType
import dev.bangumi.demo.ui.components.OptionChips
import dev.bangumi.demo.ui.components.OptionDropdown
import dev.bangumi.demo.ui.components.SubjectResults
import dev.bangumi.demo.ui.components.currentYear
import dev.bangumi.demo.ui.state.BrowseState
import dev.bangumi.demo.ui.theme.AppDimensions

/** 年份列表的最早一年。Bangumi 也收录更早的作品，但很少；需要时可以调小。 */
private const val EARLIEST_YEAR = 1920

/**
 * 索引标签页：按类型、分类、年份、月份浏览条目，可以按排名或日期排序。
 * 改变任何条件都会立即重新加载（由状态层处理），结果列表与搜索页相同，可以加载更多。
 *
 * @param onTypeChange 点击类型标签时调用。
 * @param onCategoryChange 点击分类标签时调用；null 表示全部分类。
 * @param onYearChange 选择年份时调用；null 表示不限。
 * @param onMonthChange 选择月份时调用；null 表示不限。
 * @param onRetry 加载失败后点“重试”时调用。
 */
@Composable
fun BrowseTab(
    state: BrowseState,
    listState: LazyListState,
    onTypeChange: (SubjectType) -> Unit,
    onCategoryChange: (SubjectCategory?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onMonthChange: (Int?) -> Unit,
    onSortChange: (BrowseSort) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onSubjectClick: (Subject) -> Unit,
) {
    val query = state.query
    val categories = SubjectCategory.of(query.type)
    // 年份从明年（已公布的新作）倒数到最早一年；remember 让列表只计算一次。
    val years = remember { listOf(null) + (currentYear() + 1 downTo EARLIEST_YEAR).toList() }
    val months = remember { listOf(null) + (1..12).toList() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
    ) {
        // 第一行：条目类型。
        OptionChips(
            options = SubjectType.entries,
            selected = query.type,
            label = { it.label },
            onSelect = onTypeChange,
        )
        // 第二行：该类型下的分类（例如动画的 TV、剧场版，书籍的漫画、小说）；音乐没有分类，不显示这一行。
        if (categories.isNotEmpty()) {
            OptionChips(
                options = listOf(null) + categories,
                selected = query.category,
                label = { category -> category?.label ?: "全部" },
                onSelect = onCategoryChange,
            )
        }
        // 第三行：年份、月份、排序。没有选年份时，月份不可选。
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OptionDropdown(
                title = "年份",
                options = years,
                selected = query.year,
                label = { year -> if (year == null) "全部" else "$year 年" },
                onSelect = onYearChange,
            )
            OptionDropdown(
                title = "月份",
                options = months,
                selected = query.month,
                label = { month -> if (month == null) "全部" else "$month 月" },
                onSelect = onMonthChange,
                enabled = query.year != null,
            )
            OptionDropdown(
                title = "排序",
                options = BrowseSort.entries,
                selected = query.sort,
                label = { it.label },
                onSelect = onSortChange,
            )
        }

        SubjectResults(
            results = state.results,
            listState = listState,
            idleTitle = "正在准备索引",
            idleDetail = "选择类型、分类和年份，浏览 Bangumi 上的条目。",
            emptyTitle = "没有符合条件的条目",
            emptyDetail = "换个分类、年份或月份试试。",
            onRetry = onRetry,
            onLoadMore = onLoadMore,
            onSubjectClick = onSubjectClick,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}
