package dev.bangumi.demo.data

import dev.bangumi.demo.model.CharacterAppearance
import dev.bangumi.demo.model.ProfileEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 测试角色资料、出演作品和声优的 JSON 转换，不联网。
 * 样例按 2026-11-03 实际请求角色“後藤ひとり”（87968）的返回结构精简而来。
 */
class CharacterJsonTest {

    // infobox 的值既有字符串也有列表；“简体中文名”单独取出，“引用来源”不显示。
    @Test
    fun parsesCharacterProfileFromInfobox() {
        val text = """
            {
              "id": 87968, "name": "後藤ひとり", "type": 1, "gender": "female",
              "summary": "本作の主人公。",
              "infobox": [
                { "key": "简体中文名", "value": "后藤一里" },
                { "key": "别名", "value": [ { "v": "后藤独" }, { "k": "外号", "v": "波奇酱" } ] },
                { "key": "性别", "value": "女" },
                { "key": "生日", "value": "2月21日" },
                { "key": "血型", "value": "O型" },
                { "key": "引用来源", "value": "wiki" }
              ],
              "stat": { "comments": 393, "collects": 1969 },
              "images": {
                "grid": "https://lain.bgm.tv/pic/crt/g/b1/9c/87968_crt_z9LaF.jpg",
                "medium": "https://lain.bgm.tv/r/400/pic/crt/l/b1/9c/87968_crt_z9LaF.jpg"
              }
            }
        """.trimIndent()

        val detail = parseCharacterDetail(text)

        // 角色页立绘取 images.medium（400px 宽）。
        assertEquals("https://lain.bgm.tv/r/400/pic/crt/l/b1/9c/87968_crt_z9LaF.jpg", detail.imageUrl)
        assertEquals("後藤ひとり", detail.name)
        assertEquals("后藤一里", detail.chineseName)
        assertEquals("本作の主人公。", detail.summary)
        assertEquals(
            listOf(
                ProfileEntry("别名", "后藤独、外号：波奇酱"),
                ProfileEntry("性别", "女"),
                ProfileEntry("生日", "2月21日"),
                ProfileEntry("血型", "O型"),
            ),
            detail.profile,
        )
        assertEquals(1969, detail.collects)
        assertEquals(393, detail.comments)
    }

    // 没有中文名、资料和统计时使用空值，空白的资料项不显示。
    @Test
    fun missingFieldsGiveEmptyProfile() {
        val text = """
            { "id": 1, "name": "Someone", "infobox": [ { "key": "身高", "value": "" } ] }
        """.trimIndent()

        val detail = parseCharacterDetail(text)

        assertNull(detail.chineseName)
        assertEquals(emptyList(), detail.profile)
        assertEquals("", detail.summary)
        assertEquals(0, detail.collects)
        assertNull(detail.imageUrl)
    }

    // 出演作品的 image 是原图地址，转换为 100px 宽的缩略图；空字符串为 null。
    @Test
    fun parsesAppearancesWithTypeLabelChineseNameAndThumbnail() {
        val text = """
            [
              { "id": 328609, "type": 2, "name": "ぼっち・ざ・ろっく！", "name_cn": "孤独摇滚！", "staff": "主角", "image": "" },
              { "id": 241088, "type": 1, "name": "ぼっち・ざ・ろっく！", "name_cn": "", "staff": "主角",
                "image": "https://lain.bgm.tv/pic/cover/l/45/dc/241088_hOp6G.jpg" }
            ]
        """.trimIndent()

        assertEquals(
            listOf(
                CharacterAppearance(subjectId = 328609, name = "孤独摇滚！", type = "动画", relation = "主角", imageUrl = null),
                CharacterAppearance(
                    subjectId = 241088,
                    name = "ぼっち・ざ・ろっく！",
                    type = "书籍",
                    relation = "主角",
                    imageUrl = "https://lain.bgm.tv/r/100/pic/cover/l/45/dc/241088_hOp6G.jpg",
                ),
            ),
            parseCharacterAppearances(text),
        )
    }

    // 同一位声优在多部作品中出现，只保留一次，顺序按首次出现。
    @Test
    fun voiceActorsAreDistinct() {
        val text = """
            [
              { "id": 13684, "name": "青山吉能", "subject_id": 217844 },
              { "id": 13684, "name": "青山吉能", "subject_id": 328609 },
              { "id": 79225, "name": "邱涵菲", "subject_id": 328609 }
            ]
        """.trimIndent()

        assertEquals(listOf("青山吉能", "邱涵菲"), parseCharacterVoiceActors(text))
    }
}
