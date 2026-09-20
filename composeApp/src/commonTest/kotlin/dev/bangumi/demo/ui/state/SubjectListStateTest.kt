package dev.bangumi.demo.ui.state

import dev.bangumi.demo.model.Subject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 直接测试筛选和状态事件，无需窗口；@Test 类似 Java JUnit 的测试方法标记。 */
class SubjectListStateTest {
    // 英文名验证大小写；简介中的书店用于排除误搜简介；重复名称片段用于验证顺序。
    private val subjects = listOf(
        Subject(id = 1, name = "星海邮递员", score = 8.4, summary = "邮递员把信件送到书店。"),
        Subject(id = 2, name = "雨后的小小书店", score = 7.9, summary = "街角书店的故事。"),
        Subject(id = 3, name = "Sky Melody", score = null, summary = "少年们组建乐队。"),
        Subject(id = 4, name = "星海日记", score = 8.1, summary = "旅行中的日常。"),
    )

    // 空字符串、普通空格、制表符与全角空格都应恢复全部作品。
    @Test
    fun blankQueryShowsAllSubjects() {
        for (query in listOf("", "   ", "\t\n", "　")) {
            assertEquals(subjects, filterSubjectsByName(subjects, query))
        }
    }

    // 多条命中时仍保持原始顺序。
    @Test
    fun partialNameMatchesAndKeepsOriginalOrder() {
        assertEquals(
            listOf(subjects[0], subjects[3]),
            filterSubjectsByName(subjects, "星海"),
        )
    }

    // 首尾的普通及全角空格都不参与匹配。
    @Test
    fun surroundingWhitespaceIsIgnored() {
        assertEquals(listOf(subjects[1]), filterSubjectsByName(subjects, "  书店　"))
    }

    // 混合大小写的英文片段也应命中。
    @Test
    fun englishNamesMatchIgnoringCase() {
        assertEquals(listOf(subjects[2]), filterSubjectsByName(subjects, "sKY mEL"))
    }

    // 简介与评分不能参与名称筛选。
    @Test
    fun onlyTheNameIsSearched() {
        assertEquals(listOf(subjects[1]), filterSubjectsByName(subjects, "书店"))
        assertTrue(filterSubjectsByName(subjects, "8.4").isEmpty())
    }

    // 无结果用空列表表达，由页面决定提示文字。
    @Test
    fun unmatchedQueryReturnsEmptyList() {
        assertTrue(filterSubjectsByName(subjects, "不存在的番剧").isEmpty())
    }

    // 即使没有任何源数据，筛选也应安全返回空列表。
    @Test
    fun emptySourceReturnsEmptyList() {
        assertTrue(filterSubjectsByName(emptyList(), "").isEmpty())
        assertTrue(filterSubjectsByName(emptyList(), "星海").isEmpty())
    }

    // 模拟连续输入，验证从完整数据筛选并保留输入原文，而不是继续过滤上一次结果。
    @Test
    fun changingQueryAlwaysUsesTheFullSourceAndPreservesInput() {
        val state = SubjectListState(subjects)
        assertEquals("", state.query)
        assertEquals(subjects, state.visibleSubjects)

        state.updateQuery("星海")
        assertEquals(listOf(subjects[0], subjects[3]), state.visibleSubjects)

        state.updateQuery("  书店 ")
        assertEquals("  书店 ", state.query)
        assertEquals(listOf(subjects[1]), state.visibleSubjects)

        state.updateQuery("  ")
        assertEquals("  ", state.query)
        assertEquals(subjects, state.visibleSubjects)
    }

    // 有结果与无结果两条路径都必须能清空，恢复关键词和完整列表。
    @Test
    fun clearingQueryRestoresAllSubjectsAfterMatchOrNoMatch() {
        val state = SubjectListState(subjects)
        for (query in listOf("书店", "不存在的番剧")) {
            state.updateQuery(query)
            state.clearQuery()

            assertEquals("", state.query)
            assertEquals(subjects, state.visibleSubjects)
        }
    }
}
