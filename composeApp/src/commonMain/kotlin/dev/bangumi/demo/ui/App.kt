package dev.bangumi.demo.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import coil3.compose.setSingletonImageLoaderFactory
import dev.bangumi.demo.data.BangumiApi
import dev.bangumi.demo.data.createImageLoader
import dev.bangumi.demo.ui.screens.CharacterDetailScreen
import dev.bangumi.demo.ui.screens.SubjectDetailScreen
import dev.bangumi.demo.ui.screens.SubjectListScreen
import dev.bangumi.demo.ui.state.AppNavigator
import dev.bangumi.demo.ui.state.Screen
import dev.bangumi.demo.ui.state.SubjectListState
import dev.bangumi.demo.ui.theme.BangumiTheme
import kotlinx.coroutines.launch

/**
 * 共享界面入口：创建各页面的状态，根据返回栈顶部的页面决定显示什么，并把页面事件交给状态对象。
 * 页面本身只读取数据和上报事件，不知道其他页面存在，也不直接发网络请求。
 *
 * @param api 访问 Bangumi 的数据层，由桌面入口创建并传入。
 */
@Composable
fun App(api: BangumiApi) {
    // 注册全局图片加载器（下载时带 User-Agent，并启用缓存），之后各页面的图片都由它加载。
    setSingletonImageLoaderFactory(::createImageLoader)

    // remember 在重组时保留同一个对象；App 一直显示，所以搜索状态和返回栈在切换页面时都不会丢失。
    val searchState = remember(api) { SubjectListState(api::searchSubjects) }
    val navigator = remember(api) { AppNavigator(api) }

    // 协程作用域：用来启动搜索、重试这样的后台任务；窗口关闭时自动取消未完成的请求。
    val scope = rememberCoroutineScope()

    // 搜索结果列表的滚动位置保存在这里而不是列表页里，从详情返回时仍停在原来的位置。
    // 以 results 为键：每次搜索得到新结果时换一个新的 LazyListState，列表自动回到顶部。
    val listState = remember(searchState.results) { LazyListState() }

    BangumiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // navigator.current 读取可观察的返回栈；打开或关闭页面时这里会重组并显示新的页面。
            // when (val screen = ...) 先取出当前页面，再按类型分别处理；is 判断后 screen 自动转换为对应类型。
            when (val screen = navigator.current) {
                Screen.Search -> SubjectListScreen(
                    subjects = searchState.results,
                    query = searchState.query,
                    isLoading = searchState.isLoading,
                    errorMessage = searchState.errorMessage,
                    listState = listState,
                    onQueryChange = searchState::updateQuery,
                    // search 是 suspend 函数，不能在点击回调里直接调用；launch 启动一个协程去执行它。
                    onSearch = { scope.launch { searchState.search() } },
                    onClearQuery = searchState::clearQuery,
                    onSubjectClick = { subject -> navigator.openSubject(subject.id, subject.name) },
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
