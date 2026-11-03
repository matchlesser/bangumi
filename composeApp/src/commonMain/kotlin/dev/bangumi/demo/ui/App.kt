package dev.bangumi.demo.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.screens.SubjectDetailScreen
import dev.bangumi.demo.ui.screens.SubjectListScreen
import dev.bangumi.demo.ui.state.SubjectListState
import dev.bangumi.demo.ui.theme.BangumiTheme
import kotlinx.coroutines.launch

/**
 * 共享界面入口：接收“怎样搜索”的函数，连接状态与页面，并应用统一主题。
 * 状态存放在页面调用者中，页面只读取值和上报事件，便于分别阅读与测试。
 * 根据 selectedSubject 决定显示列表页或详情页；两个页面都不知道对方存在。
 *
 * @param searchSubjects 执行搜索的 suspend 函数，由桌面入口传入 BangumiApi 的方法。
 */
@Composable
fun App(searchSubjects: suspend (String) -> List<Subject>) {
    // remember 在界面刷新（重组）时保留同一个状态对象；类比 Java 界面持有一个字段，而不是每次刷新都 new。
    val state = remember(searchSubjects) { SubjectListState(searchSubjects) }

    // 协程作用域：用来启动搜索这样的后台任务。它跟随 App 的生命周期，窗口关闭时会自动取消未完成的请求。
    val scope = rememberCoroutineScope()

    BangumiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // 先读到局部 val 再判空：selectedSubject 是 by 委托属性，每次读取都会重新调用 getter，
            // 编译器不能保证两次读到同一个值，所以不会把 state.selectedSubject 自动当作非空。
            // 局部 val 只读一次，判空后在 else 分支中就是非空的 Subject（智能转换）。
            val selectedSubject = state.selectedSubject
            if (selectedSubject == null) {
                // 读取可观察状态后，Compose 会在它变化时重新执行依赖它的界面代码。
                // state::updateQuery 是函数引用，类似 Java 的 state::updateQuery 方法引用。
                SubjectListScreen(
                    subjects = state.results,
                    query = state.query,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage,
                    onQueryChange = state::updateQuery,
                    // search 是 suspend 函数，不能在点击回调里直接调用；
                    // launch 启动一个协程去执行它，点击回调本身立即返回，界面不会卡住。
                    onSearch = { scope.launch { state.search() } },
                    onClearQuery = state::clearQuery,
                    // 列表上报被点击的条目，状态对象保存后 selectedSubject 变化，本函数重组并进入 else 分支。
                    onSubjectClick = state::selectSubject,
                )
            } else {
                // 返回只清空选中条目；搜索结果未被修改，回到列表时仍然显示。
                SubjectDetailScreen(
                    subject = selectedSubject,
                    onBack = state::clearSelectedSubject,
                )
            }
        }
    }
}
