package dev.bangumi.demo.ui.state

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateListOf
import dev.bangumi.demo.data.BangumiApi

/**
 * 应用中的一个页面。返回栈里保存的是这些对象，所以每个页面的状态（已加载的数据、滚动位置）
 * 在打开下一页再返回时仍然保留，不需要重新请求。
 */
sealed interface Screen {
    /** 搜索页；它的状态由 App 单独保存，这里不需要携带数据。 */
    data object Search : Screen

    /** 条目详情页。scrollState 保存滚动位置，返回本页时回到原来看到的地方。 */
    class SubjectDetail(val state: SubjectDetailState) : Screen {
        val scrollState = ScrollState(initial = 0)
    }

    /** 角色详情页。 */
    class CharacterDetail(val state: CharacterDetailState) : Screen {
        val scrollState = ScrollState(initial = 0)
    }
}

/**
 * 简单的页面导航：用一个列表当作返回栈，最后一个元素就是当前页面。
 * 打开新页面就在末尾添加，返回就移除末尾；搜索页始终在最底部，不会被移除。
 *
 * mutableStateListOf 是 Compose 可观察的列表：增删元素时，读取 current 的界面会自动重组。
 * 类比 Java 中用 Deque 保存浏览历史。
 *
 * @param api 用于为新页面创建状态对象时提供加载函数。
 */
class AppNavigator(private val api: BangumiApi) {
    private val backStack = mutableStateListOf<Screen>(Screen.Search)

    /** 当前显示的页面。 */
    val current: Screen get() = backStack.last()

    /** 返回栈中的页面数量；为 1 时表示只剩搜索页。 */
    val depth: Int get() = backStack.size

    /** 打开条目详情。title 用于数据加载完成前的标题。 */
    fun openSubject(subjectId: Int, title: String) {
        val state = SubjectDetailState(
            subjectId = subjectId,
            title = title,
            loadSubject = api::getSubject,
            loadCharacters = api::getSubjectCharacters,
            loadComments = api::getSubjectComments,
        )
        backStack.add(Screen.SubjectDetail(state))
    }

    /** 打开角色详情。title 用于资料加载完成前的标题。 */
    fun openCharacter(characterId: Int, title: String) {
        val state = CharacterDetailState(
            characterId = characterId,
            title = title,
            loadCharacter = api::getCharacter,
            loadAppearances = api::getCharacterAppearances,
            loadVoiceActors = api::getCharacterVoiceActors,
        )
        backStack.add(Screen.CharacterDetail(state))
    }

    /** 返回上一页；已经在搜索页时不做任何事。 */
    fun back() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}
