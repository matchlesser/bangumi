package dev.bangumi.demo.data

import dev.bangumi.demo.model.SubjectCollection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 测试条目详情、角色列表和吐槽的 JSON 转换，不联网。
 * 样例按 2026-11-03 实际请求《孤独摇滚！》（条目 328609）的返回结构精简而来，保留了部分用不到的字段。
 */
class SubjectJsonTest {

    @Test
    fun parsesSubjectDetailWithRatingDistribution() {
        val text = """
            {
              "id": 328609, "type": 2,
              "name": "ぼっち・ざ・ろっく！", "name_cn": "孤独摇滚！",
              "summary": "后藤一里组建乐队的故事。",
              "date": "2022-10-08", "platform": "TV", "eps": 12, "total_episodes": 16,
              "infobox": [ { "key": "中文名", "value": "孤独摇滚！" } ],
              "rating": {
                "rank": 73, "total": 41530, "score": 8.4,
                "count": { "1": 200, "2": 49, "3": 51, "4": 112, "5": 378,
                           "6": 1426, "7": 5250, "8": 14390, "9": 12653, "10": 7021 }
              },
              "collection": { "on_hold": 1213, "dropped": 628, "wish": 4688, "collect": 67666, "doing": 5322 }
            }
        """.trimIndent()

        val detail = parseSubjectDetail(text)

        assertEquals("孤独摇滚！", detail.name)
        assertEquals("ぼっち・ざ・ろっく！", detail.originalName)
        assertEquals(8.4, detail.score)
        assertEquals(73, detail.rank)
        assertEquals(41530, detail.ratingTotal)
        // 下标 0 是 1 分，下标 9 是 10 分。
        assertEquals(listOf(200, 49, 51, 112, 378, 1426, 5250, 14390, 12653, 7021), detail.ratingCounts)
        assertEquals("TV", detail.platform)
        assertEquals("2022-10-08", detail.date)
        assertEquals(12, detail.episodes)
        assertEquals(
            SubjectCollection(wish = 4688, collect = 67666, doing = 5322, onHold = 1213, dropped = 628),
            detail.collection,
        )
    }

    // 新条目可能没有评分、排名、日期等信息：评分和排名为 0 视为没有，分布补齐为 10 个 0。
    @Test
    fun missingRatingAndOptionalFieldsUseEmptyValues() {
        val text = """
            {
              "id": 1, "name": "Original", "name_cn": "",
              "date": "", "eps": 0,
              "rating": { "rank": 0, "total": 0, "score": 0, "count": { "8": 3 } }
            }
        """.trimIndent()

        val detail = parseSubjectDetail(text)

        assertEquals("Original", detail.name)
        assertNull(detail.score)
        assertNull(detail.rank)
        assertNull(detail.date)
        assertNull(detail.platform)
        assertNull(detail.episodes)
        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0, 3, 0, 0), detail.ratingCounts)
        assertEquals("", detail.summary)
        assertEquals(SubjectCollection(0, 0, 0, 0, 0), detail.collection)
    }

    // 角色按“主角 → 配角 → 其他”排序，同类保持原顺序；声优取第一位。
    @Test
    fun charactersAreSortedByRelationAndUseFirstVoiceActor() {
        val text = """
            [
              { "id": 1, "name": "モブ", "relation": "闲角", "type": 1, "actors": [] },
              { "id": 2, "name": "伊地知虹夏", "relation": "配角", "actors": [ { "id": 10, "name": "鈴代紗弓" } ] },
              { "id": 3, "name": "後藤ひとり", "relation": "主角",
                "actors": [ { "id": 11, "name": "青山吉能" }, { "id": 12, "name": "邱涵菲" } ] },
              { "id": 4, "name": "山田リョウ", "relation": "主角", "actors": [ { "id": 13, "name": "水野朔" } ] }
            ]
        """.trimIndent()

        val characters = parseSubjectCharacters(text)

        assertEquals(listOf(3, 4, 2, 1), characters.map { it.id })
        assertEquals("青山吉能", characters[0].voiceActor)
        assertNull(characters.last().voiceActor)
        assertEquals(listOf(true, true, true, false), characters.map { it.isMain })
    }

    // 吐槽：收藏类型转文字，未打分（0）视为没有评分，缺少昵称时显示“匿名用户”。
    @Test
    fun parsesCommentsWithStatusAndOptionalRate() {
        val text = """
            {
              "data": [
                { "id": 54875955, "type": 2, "rate": 9, "comment": "演出太好了", "updatedAt": 1793642889,
                  "user": { "id": 784444, "username": "784444", "nickname": "凤流川行" } },
                { "id": 2, "type": 3, "rate": 0, "comment": "追番中", "updatedAt": 1793600000,
                  "user": { "id": 1, "nickname": "" } }
              ],
              "total": 9186
            }
        """.trimIndent()

        val comments = parseSubjectComments(text)

        assertEquals(9186, comments.total)
        assertEquals(listOf("凤流川行", "匿名用户"), comments.items.map { it.userName })
        assertEquals(listOf("看过", "在看"), comments.items.map { it.status })
        assertEquals(listOf(9, null), comments.items.map { it.rate })
        assertEquals("演出太好了", comments.items[0].text)
        assertEquals(1793642889L, comments.items[0].updatedAt)
    }

    @Test
    fun emptyCommentsGiveEmptyList() {
        val comments = parseSubjectComments("""{ "data": [], "total": 0 }""")

        assertTrue(comments.items.isEmpty())
        assertEquals(0, comments.total)
    }
}
