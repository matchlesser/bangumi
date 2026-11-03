package dev.bangumi.demo.ui.state

import dev.bangumi.demo.data.BangumiApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * 测试返回栈：打开页面、返回上一页、返回时保留原来的页面对象（因此保留已加载的数据和滚动位置）。
 * 这里只创建 BangumiApi 而不调用它，所以不联网。
 */
class AppNavigatorTest {

    @Test
    fun startsAtSearchAndBackDoesNothingThere() {
        val navigator = AppNavigator(BangumiApi())

        navigator.back()

        assertEquals(Screen.Search, navigator.current)
        assertEquals(1, navigator.depth)
    }

    // 搜索 → 条目 → 角色 → 另一部条目，再逐层返回；返回到的条目页是原来那个对象。
    @Test
    fun opensPagesAndReturnsToTheSamePageObjects() {
        val navigator = AppNavigator(BangumiApi())

        navigator.openSubject(subjectId = 328609, title = "孤独摇滚！")
        val subjectPage = assertIs<Screen.SubjectDetail>(navigator.current)
        assertEquals(328609, subjectPage.state.subjectId)
        assertEquals("孤独摇滚！", subjectPage.state.title)

        navigator.openCharacter(characterId = 87968, title = "後藤ひとり")
        val characterPage = assertIs<Screen.CharacterDetail>(navigator.current)
        assertEquals(87968, characterPage.state.characterId)

        navigator.openSubject(subjectId = 241088, title = "孤独摇滚（漫画）")
        assertEquals(4, navigator.depth)

        navigator.back()
        assertSame(characterPage, navigator.current)

        navigator.back()
        assertSame(subjectPage, navigator.current)

        navigator.back()
        assertEquals(Screen.Search, navigator.current)
    }
}
