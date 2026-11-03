package dev.bangumi.demo.data

import dev.bangumi.demo.model.BrowseQuery
import dev.bangumi.demo.model.BrowseSort
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.SearchQuery
import dev.bangumi.demo.model.SearchSort
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectCategory
import dev.bangumi.demo.model.SubjectType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 测试搜索与索引浏览：结果 JSON 到 Subject 的转换规则，以及请求参数的生成，不联网。
 * 样例按 2026-11-03 实际请求 Bangumi 搜索“孤独摇滚”的返回结构精简而来，保留了 tags、images 等用不到的字段，
 * 用来确认这些多余字段会被忽略而不是导致解析失败。
 */
class SearchJsonTest {

    // 列表封面取 images.small（200px 宽）。
    @Test
    fun usesChineseNameScoreSummaryAndSmallCoverAndIgnoresExtraFields() {
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
                  "images": {
                    "small": "https://lain.bgm.tv/r/200/pic/cover/l/e2/e7/328609_2EHLJ.jpg",
                    "common": "https://lain.bgm.tv/r/400/pic/cover/l/e2/e7/328609_2EHLJ.jpg",
                    "large": "https://lain.bgm.tv/pic/cover/l/e2/e7/328609_2EHLJ.jpg"
                  },
                  "tags": [ { "name": "音乐", "count": 9453 } ],
                  "rating": { "rank": 73, "total": 41530, "score": 8.4 }
                }
              ],
              "total": 19,
              "limit": 20,
              "offset": 0
            }
        """.trimIndent()

        // 本页 1 条，总数 19 条来自响应中的 total；type 2 为动画。
        assertEquals(
            Page(
                items = listOf(
                    Subject(
                        id = 328609,
                        name = "孤独摇滚！",
                        score = 8.4,
                        summary = "后藤一里组建乐队的故事。",
                        imageUrl = "https://lain.bgm.tv/r/200/pic/cover/l/e2/e7/328609_2EHLJ.jpg",
                        type = SubjectType.ANIME,
                        date = "2022-10-08",
                    ),
                ),
                total = 19,
            ),
            parseSubjectPage(text),
        )
    }

    // 混合类型的结果：类型和平台组成卡片上的标签，例如“书籍 · 漫画”“动画 · TV”。
    @Test
    fun parsesTypeAndPlatformForTypeLabel() {
        val text = """
            {
              "data": [
                { "id": 241088, "type": 1, "platform": "漫画", "name": "ぼっち・ざ・ろっく！", "name_cn": "孤独摇滚！" },
                { "id": 328609, "type": 2, "platform": "TV", "name": "ぼっち・ざ・ろっく！" },
                { "id": 9, "type": 3, "platform": "", "name": "Music" },
                { "id": 10, "name": "Unknown" }
              ],
              "total": 4
            }
        """.trimIndent()

        val subjects = parseSubjectPage(text).items

        assertEquals(listOf(SubjectType.BOOK, SubjectType.ANIME, SubjectType.MUSIC, null), subjects.map { it.type })
        assertEquals(listOf("书籍 · 漫画", "动画 · TV", "音乐", null), subjects.map { it.typeLabel })
    }

    // 搜索请求体：不限类型时 filter 为空对象；限定类型时为 {"type":[编号]}；排序用接口的取值。
    @Test
    fun buildsSearchRequestJson() {
        assertEquals(
            """{"keyword":"孤独摇滚","sort":"match","filter":{}}""",
            searchRequestJson(SearchQuery("孤独摇滚")),
        )
        assertEquals(
            """{"keyword":"孤独摇滚","sort":"heat","filter":{"type":[1]}}""",
            searchRequestJson(SearchQuery("孤独摇滚", type = SubjectType.BOOK, sort = SearchSort.HEAT)),
        )
    }

    // 索引浏览的查询参数：未设置的条件不出现；没有年份时月份也不出现。
    @Test
    fun buildsBrowseParameters() {
        assertEquals(
            listOf("type" to "2", "sort" to "rank", "limit" to "20", "offset" to "0"),
            browseParameters(BrowseQuery(), offset = 0, limit = 20),
        )
        assertEquals(
            listOf(
                "type" to "2", "cat" to "1", "year" to "2024", "month" to "10",
                "sort" to "date", "limit" to "20", "offset" to "40",
            ),
            browseParameters(
                BrowseQuery(
                    type = SubjectType.ANIME,
                    category = SubjectCategory.ANIME_TV,
                    year = 2024,
                    month = 10,
                    sort = BrowseSort.DATE,
                ),
                offset = 40,
                limit = 20,
            ),
        )
        assertEquals(
            listOf("type" to "1", "cat" to "1001", "sort" to "rank", "limit" to "20", "offset" to "0"),
            browseParameters(
                BrowseQuery(type = SubjectType.BOOK, category = SubjectCategory.BOOK_COMIC, month = 5),
                offset = 0,
                limit = 20,
            ),
        )
    }

    // 中文名为空字符串或缺失时都显示原名；封面为空字符串或缺失时为 null。
    @Test
    fun fallsBackToOriginalNameAndMissingCoverWhenFieldsAreEmpty() {
        val text = """
            {
              "data": [
                { "id": 1, "name": "Original A", "name_cn": "", "rating": { "score": 7.0 }, "images": { "small": "" } },
                { "id": 2, "name": "Original B", "rating": { "score": 6.5 } }
              ]
            }
        """.trimIndent()

        val page = parseSubjectPage(text)

        assertEquals(listOf("Original A", "Original B"), page.items.map { it.name })
        assertEquals(listOf(null, null), page.items.map { it.imageUrl })
        // 响应中没有 total 时以本页条数为总数，视为没有下一页。
        assertEquals(2, page.total)
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

        val subjects = parseSubjectPage(text).items

        assertEquals(listOf(null, null, null), subjects.map { it.score })
        assertEquals(listOf("", "", ""), subjects.map { it.summary })
    }

    @Test
    fun emptyDataReturnsEmptyPage() {
        val text = """{ "data": [], "total": 0, "limit": 20, "offset": 0 }"""

        val page = parseSubjectPage(text)

        assertTrue(page.items.isEmpty())
        assertEquals(0, page.total)
    }
}
