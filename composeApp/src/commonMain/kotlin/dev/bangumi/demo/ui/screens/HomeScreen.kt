package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bangumi.demo.BuildInfo
import dev.bangumi.demo.ui.theme.AppDimensions

/** 首页的两个标签页。 */
enum class HomeTab(val label: String) {
    SEARCH("搜索"),
    BROWSE("索引"),
}

/**
 * 首页外框：标题、数据来源和版本、“搜索 / 索引”标签栏，下面是当前标签页的内容。
 *
 * 两个标签页的内容由调用方（App）通过 searchContent、browseContent 传入：
 * 首页只负责排版和切换，不需要知道搜索和索引各自的状态。
 * 这种把一段界面作为参数传入的写法叫“插槽（slot）”，类似 Java 中把一个回调对象交给容器去调用。
 *
 * @param selectedTab 当前选中的标签页。
 * @param onTabSelect 点击标签时调用。
 */
@Composable
fun HomeScreen(
    selectedTab: HomeTab,
    onTabSelect: (HomeTab) -> Unit,
    searchContent: @Composable () -> Unit,
    browseContent: @Composable () -> Unit,
) {
    // Box 负责水平居中；内层 Column 限制内容宽度，从上到下排列标题、标签栏和标签页内容。
    Box(
        modifier = Modifier.fillMaxSize().padding(AppDimensions.largeSpacing),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = AppDimensions.contentMaxWidth).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
                Text(
                    text = "Bangumi",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                // 说明数据来源和版本，与以前的“离线示例数据”区分开。
                Text(
                    text = "数据来自 Bangumi 番组计划（bgm.tv），需要联网 · 版本 ${BuildInfo.VERSION}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 标签栏：ordinal 是枚举值的序号（0、1），与 Java 枚举的 ordinal() 相同。
            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                HomeTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { onTabSelect(tab) },
                        text = { Text(tab.label) },
                    )
                }
            }

            // 当前标签页的内容占满剩余空间。
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (selectedTab) {
                    HomeTab.SEARCH -> searchContent()
                    HomeTab.BROWSE -> browseContent()
                }
            }
        }
    }
}
