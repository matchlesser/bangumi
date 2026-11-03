package dev.bangumi.demo.ui.state

import dev.bangumi.demo.data.sampleSubjects
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
 * runTest { ... } 提供一个测试用协程环境，让测试里可以调用 suspend 函数 search()。
 */
class SubjectListStateTest {

    @Test
    fun initiallyNothingIsSearched() {
        val state = SubjectListState { sampleSubjects }

        assertNull(state.results)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    // 空关键词或只有空白时，不调用搜索函数，结果保持“未搜索”。
    @Test
    fun blankQueryDoesNotSearch() = runTest {
        var calls = 0
        val state = SubjectListState { calls++; sampleSubjects }

        for (query in listOf("", "   ", "\t\n")) {
            state.updateQuery(query)
            state.search()
        }

        assertEquals(0, calls)
        assertNull(state.results)
    }

    // 关键词去掉首尾空白后交给搜索函数；成功后保存结果，输入框原文不变。
    @Test
    fun successfulSearchStoresResultsWithTrimmedKeyword() = runTest {
        var receivedKeyword = ""
        val state = SubjectListState { keyword -> receivedKeyword = keyword; sampleSubjects }
        state.updateQuery("  孤独摇滚 ")

        state.search()

        assertEquals("孤独摇滚", receivedKeyword)
        assertEquals(sampleSubjects, state.results)
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
        state = SubjectListState { loadingDuringSearch = state.isLoading; emptyList() }
        state.updateQuery("任意")

        state.search()

        assertTrue(loadingDuringSearch)
        assertFalse(state.isLoading)
    }

    // 搜过但没有匹配时是空列表，与“还没搜索”的 null 区分开。
    @Test
    fun noMatchGivesEmptyListInsteadOfNull() = runTest {
        val state = SubjectListState { emptyList() }
        state.updateQuery("不存在的番剧")

        state.search()

        assertEquals(emptyList(), state.results)
    }

    // 搜索失败时记录原因并结束加载；再次搜索成功后错误被清除，这就是“重试”。
    @Test
    fun failureShowsErrorAndRetryClearsIt() = runTest {
        var shouldFail = true
        val state = SubjectListState {
            if (shouldFail) throw IllegalStateException("Bangumi 返回 HTTP 503")
            sampleSubjects
        }
        state.updateQuery("孤独摇滚")

        state.search()

        assertEquals("Bangumi 返回 HTTP 503", state.errorMessage)
        assertNull(state.results)
        assertFalse(state.isLoading)

        shouldFail = false
        state.search()

        assertNull(state.errorMessage)
        assertEquals(sampleSubjects, state.results)
    }

    // 异常没有文字说明时，显示异常类型名，而不是笼统的“未知错误”。
    @Test
    fun failureWithoutMessageShowsExceptionType() = runTest {
        val state = SubjectListState { throw UnsupportedOperationException() }
        state.updateQuery("孤独摇滚")

        state.search()

        assertEquals("UnsupportedOperationException", state.errorMessage)
    }

    // 协程被取消（例如关闭窗口）不算搜索失败：不显示错误，异常继续抛出，加载状态结束。
    @Test
    fun cancellationIsNotReportedAsError() = runTest {
        val state = SubjectListState { throw CancellationException("窗口已关闭") }
        state.updateQuery("孤独摇滚")

        assertFailsWith<CancellationException> { state.search() }

        assertNull(state.errorMessage)
        assertFalse(state.isLoading)
    }

    // 清空关键词回到初始提示：结果和错误都被清除。
    @Test
    fun clearQueryResetsResultsAndError() = runTest {
        val state = SubjectListState { sampleSubjects }
        state.updateQuery("孤独摇滚")
        state.search()

        state.clearQuery()

        assertEquals("", state.query)
        assertNull(state.results)
        assertNull(state.errorMessage)
    }

    // 进入详情再返回时，只改变选中条目，不应丢失关键词和搜索结果。
    @Test
    fun selectingAndClearingSubjectPreservesSearchResults() = runTest {
        val state = SubjectListState { sampleSubjects }
        state.updateQuery("孤独摇滚")
        state.search()

        state.selectSubject(sampleSubjects[1])

        assertEquals(sampleSubjects[1], state.selectedSubject)

        state.clearSelectedSubject()

        assertNull(state.selectedSubject)
        assertEquals("孤独摇滚", state.query)
        assertEquals(sampleSubjects, state.results)
    }
}
