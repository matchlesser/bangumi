package dev.bangumi.demo.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.compose.setSingletonImageLoaderFactory
import dev.bangumi.demo.data.BangumiApi
import dev.bangumi.demo.data.createImageLoader
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.screens.BrowseTab
import dev.bangumi.demo.ui.screens.CharacterDetailScreen
import dev.bangumi.demo.ui.screens.HomeScreen
import dev.bangumi.demo.ui.screens.HomeTab
import dev.bangumi.demo.ui.screens.SearchTab
import dev.bangumi.demo.ui.screens.SubjectDetailScreen
import dev.bangumi.demo.ui.state.AppNavigator
import dev.bangumi.demo.ui.state.BrowseState
import dev.bangumi.demo.ui.state.Screen
import dev.bangumi.demo.ui.state.SubjectListState
import dev.bangumi.demo.ui.theme.BangumiTheme
import kotlinx.coroutines.launch

/**
 * 共享界面入口：创建首页两个标签页的状态，根据返回栈顶部的页面决定显示什么，并把页面事件交给状态对象。
 * 页面本身只读取数据和上报事件，不知道其他页面存在，也不直接发网络请求。
 *
 * @param api 访问 Bangumi 的数据层，由桌面入口创建并传入。
 * @param navigator 页面返回栈，由桌面入口创建：窗口的 Esc 快捷键和这里使用同一个导航器。
 */
@Composable
fun App(api: BangumiApi, navigator: AppNavigator) {
    // 注册全局图片加载器（下载时带 User-Agent，并启用缓存），之后各页面的图片都由它加载。
    setSingletonImageLoaderFactory(::createImageLoader)

    // remember 在重组时保留同一个对象；App 一直显示，所以这些状态在切换页面、切换标签时都不会丢失。
    val searchState = remember(api) { SubjectListState(api::searchSubjects) }
    val browseState = remember(api) { BrowseState(api::browseSubjects) }
    var homeTab by remember { mutableStateOf(HomeTab.SEARCH) }

    // 协程作用域：用来启动搜索、筛选、加载更多、重试这样的后台任务；窗口关闭时自动取消未完成的请求。
    val scope = rememberCoroutineScope()

    // 两个结果列表的滚动位置保存在这里，从详情返回或切换标签后仍停在原来的位置。
    // 以 version 为键：换成新结果时换一个新的 LazyListState（回到顶部）；“加载更多”不换，保持当前位置。
    val searchListState = remember(searchState.results.version) { LazyListState() }
    val browseListState = remember(browseState.results.version) { LazyListState() }

    // 点击任何条目都打开它的详情页。
    val openSubject: (Subject) -> Unit = { subject -> navigator.openSubject(subject.id, subject.name) }

    BangumiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // navigator.current 读取可观察的返回栈；打开或关闭页面时这里会重组并显示新的页面。
            // when (val screen = ...) 先取出当前页面，再按类型分别处理；is 判断后 screen 自动转换为对应类型。
            when (val screen = navigator.current) {
                Screen.Search -> HomeScreen(
                    selectedTab = homeTab,
                    onTabSelect = { homeTab = it },
                    searchContent = {
                        SearchTab(
                            state = searchState,
                            listState = searchListState,
                            onQueryChange = searchState::updateQuery,
                            // search 等是 suspend 函数，不能在点击回调里直接调用；launch 启动一个协程去执行它。
                            onSearch = { scope.launch { searchState.search() } },
                            onTypeChange = { type -> scope.launch { searchState.changeType(type) } },
                            onSortChange = { sort -> scope.launch { searchState.changeSort(sort) } },
                            onLoadMore = { scope.launch { searchState.loadMore() } },
                            onClearQuery = searchState::clearQuery,
                            onSubjectClick = openSubject,
                        )
                    },
                    browseContent = {
                        // 第一次切到索引页时加载；之后再显示时 loadIfNeeded 不会重复请求。
                        LaunchedEffect(Unit) { browseState.loadIfNeeded() }
                        BrowseTab(
                            state = browseState,
                            listState = browseListState,
                            onTypeChange = { type -> scope.launch { browseState.changeType(type) } },
                            onCategoryChange = { category -> scope.launch { browseState.changeCategory(category) } },
                            onYearChange = { year -> scope.launch { browseState.changeYear(year) } },
                            onMonthChange = { month -> scope.launch { browseState.changeMonth(month) } },
                            onSortChange = { sort -> scope.launch { browseState.changeSort(sort) } },
                            onRetry = { scope.launch { browseState.reload() } },
                            onLoadMore = { scope.launch { browseState.loadMore() } },
                            onSubjectClick = openSubject,
                        )
                    },
                )

                is Screen.SubjectDetail -> {
                    // LaunchedEffect 在这个页面显示时启动一次加载；以 screen 为键，换成别的页面对象时重新启动。
                    // 从角色页返回时页面重新显示，load 只会补上未完成的部分，已经成功的数据不会重新请求。
                    LaunchedEffect(screen) { screen.state.load() }
                    SubjectDetailScreen(
                        state = screen.state,
                        scrollState = screen.scrollState,
                        onBack = navigator::back,
                        onRetry = { scope.launch { screen.state.load() } },
                        onLoadMoreComments = { scope.launch { screen.state.loadMoreComments() } },
                        onCharacterClick = { character -> navigator.openCharacter(character.id, character.name) },
                    )
                }

                is Screen.CharacterDetail -> {
                    LaunchedEffect(screen) { screen.state.load() }
                    CharacterDetailScreen(
                        state = screen.state,
                        scrollState = screen.scrollState,
                        onBack = navigator::back,
                        onRetry = { scope.launch { screen.state.load() } },
                        onSubjectClick = { appearance -> navigator.openSubject(appearance.subjectId, appearance.name) },
                    )
                }
            }
        }
    }
}
