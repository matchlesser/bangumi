package dev.bangumi.demo.data

import dev.bangumi.demo.model.Subject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 测试搜索结果 JSON 到 Subject 的转换规则，不联网。
 * 样例按 2026-11-03 实际请求 Bangumi 搜索“孤独摇滚”的返回结构精简而来，保留了 tags、images 等用不到的字段，
 * 用来确认这些多余字段会被忽略而不是导致解析失败。
 */
class SearchJsonTest {

    @Test
    fun usesChineseNameScoreAndSummaryAndIgnoresExtraFields() {
        val text = """
            {
              "data": [
                {
                  "id": 328609,
                  "type": 2,
                  "name": "ぼっち・ざ・ろっく！",
                  "name_cn": "孤独摇滚！",
                  "summary": "后藤一里组建乐队的故事。",
                  "date": "2022-10-08",
                  "images": { "large": "https://lain.bgm.tv/pic/cover/l/e2/e7/328609_2EHLJ.jpg" },
                  "tags": [ { "name": "音乐", "count": 9453 } ],
                  "rating": { "rank": 73, "total": 41530, "score": 8.4 }
                }
              ],
              "total": 19,
              "limit": 20,
              "offset": 0
            }
        """.trimIndent()

        assertEquals(
            listOf(Subject(id = 328609, name = "孤独摇滚！", score = 8.4, summary = "后藤一里组建乐队的故事。")),
            parseSearchResponse(text),
        )
    }

    // 中文名为空字符串或缺失时都显示原名。
    @Test
    fun fallsBackToOriginalNameWhenChineseNameIsMissing() {
        val text = """
            {
              "data": [
                { "id": 1, "name": "Original A", "name_cn": "", "rating": { "score": 7.0 } },
                { "id": 2, "name": "Original B", "rating": { "score": 6.5 } }
              ]
            }
        """.trimIndent()

        assertEquals(listOf("Original A", "Original B"), parseSearchResponse(text).map { it.name })
    }

    // 评分为 0、为 null 或整个 rating 缺失，都表示没有可显示的评分；简介缺失时为空字符串。
    @Test
    fun missingOrZeroScoreBecomesNullAndMissingSummaryBecomesEmpty() {
        val text = """
            {
              "data": [
                { "id": 1, "name": "A", "rating": { "score": 0 } },
                { "id": 2, "name": "B", "rating": { "score": null } },
                { "id": 3, "name": "C" }
              ]
            }
        """.trimIndent()

        val subjects = parseSearchResponse(text)

        assertEquals(listOf(null, null, null), subjects.map { it.score })
        assertEquals(listOf("", "", ""), subjects.map { it.summary })
    }

    @Test
    fun emptyDataReturnsEmptyList() {
        val text = """{ "data": [], "total": 0, "limit": 20, "offset": 0 }"""

        assertTrue(parseSearchResponse(text).isEmpty())
    }
}
