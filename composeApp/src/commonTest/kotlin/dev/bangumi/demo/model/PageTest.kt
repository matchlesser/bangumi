package dev.bangumi.demo.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 测试分页拼接：按顺序追加、按 id 去重、下一页为空时停止。 */
class PageTest {

    @Test
    fun appendsNextPageAndKeepsOrder() {
        val first = Page(items = listOf(1, 2), total = 5)

        val merged = first.appended(Page(items = listOf(3, 4), total = 5)) { it }

        assertEquals(Page(items = listOf(1, 2, 3, 4), total = 5), merged)
        assertTrue(merged.hasMore)
    }

    // 两次请求之间数据后移，同一条目出现在两页中时只保留第一次出现的那条。
    @Test
    fun removesDuplicatesAcrossPages() {
        val first = Page(items = listOf(1, 2, 3), total = 6)

        val merged = first.appended(Page(items = listOf(3, 4, 5), total = 6)) { it }

        assertEquals(listOf(1, 2, 3, 4, 5), merged.items)
    }

    // 服务器说还有更多，但下一页是空的：以实际数量为总数，不再显示“加载更多”。
    @Test
    fun emptyNextPageStopsLoadingMore() {
        val first = Page(items = listOf(1, 2), total = 10)

        val merged = first.appended(Page(items = emptyList(), total = 10)) { it }

        assertEquals(2, merged.total)
        assertFalse(merged.hasMore)
    }

    @Test
    fun hasMoreComparesShownWithTotal() {
        assertTrue(Page(items = listOf(1), total = 2).hasMore)
        assertFalse(Page(items = listOf(1, 2), total = 2).hasMore)
        assertFalse(Page(items = emptyList<Int>(), total = 0).hasMore)
    }
}
