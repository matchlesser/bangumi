package dev.bangumi.demo.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import dev.bangumi.demo.data.BangumiApi
import dev.bangumi.demo.ui.state.AppNavigator
import dev.bangumi.demo.ui.state.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** 测试 Esc 返回快捷键的判断：只在按下 Esc 且不在搜索页时返回一层。 */
class BackShortcutTest {

    @Test
    fun escapeGoesBackOnePage() {
        val navigator = AppNavigator(BangumiApi())
        navigator.openSubject(subjectId = 328609, title = "孤独摇滚！")
        navigator.openCharacter(characterId = 87968, title = "後藤ひとり")

        assertTrue(handleBackShortcut(Key.Escape, KeyEventType.KeyDown, navigator))

        assertIs<Screen.SubjectDetail>(navigator.current)
    }

    // 在搜索页按 Esc 不处理（返回 false），交给输入框等组件。
    @Test
    fun escapeOnSearchPageIsNotHandled() {
        val navigator = AppNavigator(BangumiApi())

        assertFalse(handleBackShortcut(Key.Escape, KeyEventType.KeyDown, navigator))
        assertEquals(Screen.Search, navigator.current)
    }

    // 松开 Esc 和其他按键都不触发返回，避免按一次退两层或误触。
    @Test
    fun keyReleaseAndOtherKeysAreIgnored() {
        val navigator = AppNavigator(BangumiApi())
        navigator.openSubject(subjectId = 328609, title = "孤独摇滚！")

        assertFalse(handleBackShortcut(Key.Escape, KeyEventType.KeyUp, navigator))
        assertFalse(handleBackShortcut(Key.Backspace, KeyEventType.KeyDown, navigator))
        assertIs<Screen.SubjectDetail>(navigator.current)
    }
}
