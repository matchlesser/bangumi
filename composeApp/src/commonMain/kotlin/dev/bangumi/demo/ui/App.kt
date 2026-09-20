package dev.bangumi.demo.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.screens.SubjectListScreen
import dev.bangumi.demo.ui.state.SubjectListState
import dev.bangumi.demo.ui.theme.BangumiTheme

/**
 * 共享界面入口：接收桌面入口提供的数据，连接状态与页面，并应用统一主题。
 * 状态存放在页面调用者中，页面只读取值和上报事件，便于分别阅读与测试。
 */
@Composable
fun App(subjects: List<Subject>) {
    // remember 在界面刷新（重组）时保留同一个状态对象；subjects 按 equals 比较变化时重新创建。
    // 类比 Java 界面持有一个字段，而不是每次刷新都 new；它不负责关闭程序后的持久化。
    val state = remember(subjects) { SubjectListState(subjects) }

    BangumiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // 读取可观察状态后，Compose 会在它变化时重新执行依赖它的界面代码。
            // state::updateQuery 是函数引用，类似 Java 的 state::updateQuery 方法引用。
            SubjectListScreen(
                subjects = state.visibleSubjects,
                query = state.query,
                onQueryChange = state::updateQuery,
                onClearQuery = state::clearQuery,
            )
        }
    }
}
