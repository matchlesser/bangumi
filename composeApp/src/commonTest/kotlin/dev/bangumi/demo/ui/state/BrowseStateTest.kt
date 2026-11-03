package dev.bangumi.demo.ui.state

import dev.bangumi.demo.model.BrowseQuery
import dev.bangumi.demo.model.BrowseSort
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectCategory
import dev.bangumi.demo.model.SubjectType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 测试索引页状态：首次加载、改变条件时重新加载，以及条件之间的约束。不联网。 */
class BrowseStateTest {

    private val onePage = Page(items = listOf(Subject(id = 1, name = "作品", score = null, summary = "")), total = 1)

    // 第一次显示时加载默认条件（全部动画、按排名）；再次显示时不重复请求。
    @Test
    fun loadIfNeededLoadsDefaultQueryOnce() = runTest {
        val received = mutableListOf<BrowseQuery>()
        val state = BrowseState { query, _ -> received += query; onePage }

        state.loadIfNeeded()
        state.loadIfNeeded()

        assertEquals(listOf(BrowseQuery(type = SubjectType.ANIME, sort = BrowseSort.RANK)), received)
        assertEquals(onePage, state.results.page)
    }

    // 换类型时清除分类（“TV”不属于书籍），并按新条件重新加载。
    @Test
    fun changingTypeClearsCategoryAndReloads() = runTest {
        val received = mutableListOf<BrowseQuery>()
        val state = BrowseState { query, _ -> received += query; onePage }
        state.changeCategory(SubjectCategory.ANIME_TV)

        state.changeType(SubjectType.BOOK)

        assertEquals(
            listOf(
                BrowseQuery(type = SubjectType.ANIME, category = SubjectCategory.ANIME_TV),
                BrowseQuery(type = SubjectType.BOOK, category = null),
            ),
            received,
        )
    }

    // 不属于当前类型的分类被忽略，不发请求。
    @Test
    fun categoryOfAnotherTypeIsIgnored() = runTest {
        var calls = 0
        val state = BrowseState { _, _ -> calls++; onePage }

        state.changeCategory(SubjectCategory.BOOK_COMIC)

        assertNull(state.query.category)
        assertEquals(0, calls)
    }

    // 没有年份时不能选月份；清除年份时同时清除月份。
    @Test
    fun monthRequiresYearAndIsClearedWithIt() = runTest {
        val state = BrowseState { _, _ -> onePage }

        state.changeMonth(10)
        assertNull(state.query.month)

        state.changeYear(2024)
        state.changeMonth(10)
        assertEquals(BrowseQuery(year = 2024, month = 10), state.query)

        state.changeYear(null)
        assertEquals(BrowseQuery(year = null, month = null), state.query)
    }

    // 选择相同的条件不重复请求；换排序会重新加载。
    @Test
    fun sameConditionDoesNotReload() = runTest {
        var calls = 0
        val state = BrowseState { _, _ -> calls++; onePage }
        state.loadIfNeeded()

        state.changeType(SubjectType.ANIME)
        state.changeYear(null)
        state.changeSort(BrowseSort.RANK)
        state.changeSort(BrowseSort.DATE)

        assertEquals(2, calls)
        assertEquals(BrowseSort.DATE, state.query.sort)
    }

    // 加载失败后 loadIfNeeded 不会自动重复请求（等用户点重试），reload 才会重新请求。
    @Test
    fun failedLoadWaitsForRetry() = runTest {
        var calls = 0
        val state = BrowseState { _, _ ->
            calls++
            if (calls == 1) throw IllegalStateException("Bangumi 返回 HTTP 500")
            onePage
        }

        state.loadIfNeeded()
        state.loadIfNeeded()
        assertEquals(1, calls)
        assertEquals("Bangumi 返回 HTTP 500", state.results.error)

        state.reload()
        assertEquals(onePage, state.results.page)
    }
}
