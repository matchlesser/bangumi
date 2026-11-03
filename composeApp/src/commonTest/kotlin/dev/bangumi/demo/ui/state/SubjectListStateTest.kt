package dev.bangumi.demo.ui.state

import dev.bangumi.demo.data.sampleSubjects
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.Subject
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 测试搜索状态的变化，无需窗口也不联网；@Test 类似 Java JUnit 的测试方法标记。
 *
 * SubjectListState 接收一个“怎样搜索”的函数，这里传入返回固定结果或抛出异常的 Lambda 代替真实网络。
 * Lambda 的两个参数是关键词和起始位置 offset；不需要的参数用 _ 表示忽略。
 * runTest { ... } 提供一个测试用协程环境，让测试里可以调用 suspend 函数 search()、loadMore()。
 */
class SubjectListStateTest {

    /** 所有示例作为一页、总数等于条数（没有下一页）的搜索结果。 */
    private val onePage = Page(items = sampleSubjects, total = sampleSubjects.size)

    /** 生成编号从 from 开始的 count 个假条目，用于测试分页。 */
    private fun subjects(from: Int, count: Int) =
        (from until from + count).map { Subject(id = it, name = "作品$it", score = null, summary = "") }

    @Test
    fun initiallyNothingIsSearched() {
        val state = SubjectListState { _, _ -> onePage }

        assertNull(state.results)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    // 空关键词或只有空白时，不调用搜索函数，结果保持“未搜索”。
    @Test
    fun blankQueryDoesNotSearch() = runTest {
        var calls = 0
        val state = SubjectListState { _, _ -> calls++; onePage }

        for (query in listOf("", "   ", "\t\n")) {
            state.updateQuery(query)
            state.search()
        }

        assertEquals(0, calls)
        assertNull(state.results)
    }

    // 关键词去掉首尾空白后交给搜索函数，从第 0 条开始；成功后保存结果，输入框原文不变。
    @Test
    fun successfulSearchStoresFirstPageWithTrimmedKeyword() = runTest {
        var received = ""
        val state = SubjectListState { keyword, offset -> received = "$keyword@$offset"; onePage }
        state.updateQuery("  孤独摇滚 ")

        state.search()

        assertEquals("孤独摇滚@0", received)
        assertEquals(onePage, state.results)
        assertEquals("  孤独摇滚 ", state.query)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    // 等待结果期间 isLoading 为 true；结束后恢复为 false。
    @Test
    fun isLoadingWhileWaitingForResults() = runTest {
        var loadingDuringSearch = false
        // lateinit：先声明、稍后赋值的变量，因为 Lambda 里要读取的 state 正是用这个 Lambda 创建的。
        lateinit var state: SubjectListState
        state = SubjectListState { _, _ -> loadingDuringSearch = state.isLoading; Page(emptyList(), 0) }
        state.updateQuery("任意")

        state.search()

        assertTrue(loadingDuringSearch)
        assertFalse(state.isLoading)
    }

    // 搜过但没有匹配时是空的一页，与“还没搜索”的 null 区分开。
    @Test
    fun noMatchGivesEmptyPageInsteadOfNull() = runTest {
        val state = SubjectListState { _, _ -> Page(emptyList(), 0) }
        state.updateQuery("不存在的番剧")

        state.search()

        assertEquals(Page(emptyList<Subject>(), 0), state.results)
    }

    // 搜索失败时记录原因并结束加载；再次搜索成功后错误被清除，这就是“重试”。
    @Test
    fun failureShowsErrorAndRetryClearsIt() = runTest {
        var shouldFail = true
        val state = SubjectListState { _, _ ->
            if (shouldFail) throw IllegalStateException("Bangumi 返回 HTTP 503")
            onePage
        }
        state.updateQuery("孤独摇滚")

        state.search()

        assertEquals("Bangumi 返回 HTTP 503", state.errorMessage)
        assertNull(state.results)
        assertFalse(state.isLoading)

        shouldFail = false
        state.search()

        assertNull(state.errorMessage)
        assertEquals(onePage, state.results)
    }

    // 异常没有文字说明时，显示异常类型名，而不是笼统的“未知错误”。
    @Test
    fun failureWithoutMessageShowsExceptionType() = runTest {
        val state = SubjectListState { _, _ -> throw UnsupportedOperationException() }
        state.updateQuery("孤独摇滚")

        state.search()

        assertEquals("UnsupportedOperationException", state.errorMessage)
    }

    // 协程被取消（例如关闭窗口）不算搜索失败：不显示错误，异常继续抛出，加载状态结束。
    @Test
    fun cancellationIsNotReportedAsError() = runTest {
        val state = SubjectListState { _, _ -> throw CancellationException("窗口已关闭") }
        state.updateQuery("孤独摇滚")

        assertFailsWith<CancellationException> { state.search() }

        assertNull(state.errorMessage)
        assertFalse(state.isLoading)
    }

    // 清空关键词回到初始提示：结果和错误都被清除。
    @Test
    fun clearQueryResetsResultsAndError() = runTest {
        val state = SubjectListState { _, _ -> onePage }
        state.updateQuery("孤独摇滚")
        state.search()

        state.clearQuery()

        assertEquals("", state.query)
        assertNull(state.results)
        assertNull(state.errorMessage)
    }

    // 加载更多：用这次搜索的关键词（即使输入框已改动）和已有条数作为 offset，结果拼接在后面；取完后不再请求。
    @Test
    fun loadMoreAppendsNextPageUntilAllLoaded() = runTest {
        val requests = mutableListOf<String>()
        val state = SubjectListState { keyword, offset ->
            requests += "$keyword@$offset"
            Page(items = subjects(from = offset, count = if (offset == 0) 20 else 12), total = 32)
        }
        state.updateQuery("进击的巨人")
        state.search()
        state.updateQuery("输入框里改了别的字")

        state.loadMore()

        assertEquals(listOf("进击的巨人@0", "进击的巨人@20"), requests)
        assertEquals((0 until 32).toList(), state.results?.items?.map { it.id })
        assertFalse(state.results!!.hasMore)

        state.loadMore()

        assertEquals(2, requests.size)
    }

    // 加载更多不改变 resultsVersion（列表保持滚动位置）；新的搜索会让它加一（列表回到顶部）。
    @Test
    fun onlyNewSearchChangesResultsVersion() = runTest {
        val state = SubjectListState { _, offset -> Page(items = subjects(from = offset, count = 20), total = 40) }
        state.updateQuery("孤独摇滚")
        state.search()
        val afterFirstSearch = state.resultsVersion

        state.loadMore()
        assertEquals(afterFirstSearch, state.resultsVersion)

        state.search()
        assertEquals(afterFirstSearch + 1, state.resultsVersion)
    }

    // 加载下一页失败：已显示的结果保留，只记录 loadMoreError；重试成功后错误清除。
    @Test
    fun loadMoreFailureKeepsResultsAndCanRetry() = runTest {
        var failNextPage = true
        val state = SubjectListState { _, offset ->
            if (offset > 0 && failNextPage) throw IllegalStateException("Bangumi 返回 HTTP 502")
            Page(items = subjects(from = offset, count = 20), total = 40)
        }
        state.updateQuery("孤独摇滚")
        state.search()

        state.loadMore()

        assertEquals("Bangumi 返回 HTTP 502", state.loadMoreError)
        assertEquals(20, state.results?.items?.size)
        assertFalse(state.isLoadingMore)

        failNextPage = false
        state.loadMore()

        assertNull(state.loadMoreError)
        assertEquals(40, state.results?.items?.size)
    }

    // 下一页在等待期间用户清空了关键词：这一页作废，不会把结果又拼回来。
    @Test
    fun staleNextPageIsDroppedAfterClear() = runTest {
        lateinit var state: SubjectListState
        state = SubjectListState { _, offset ->
            if (offset > 0) state.clearQuery()
            Page(items = subjects(from = offset, count = 20), total = 40)
        }
        state.updateQuery("孤独摇滚")
        state.search()

        state.loadMore()

        assertNull(state.results)
        assertNull(state.loadMoreError)
    }
}
