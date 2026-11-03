package dev.bangumi.demo.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 测试条目类型与分类：编号查找、每种类型的分类列表、卡片类型标签。 */
class SubjectFiltersTest {

    @Test
    fun findsTypeById() {
        assertEquals(SubjectType.BOOK, SubjectType.fromId(1))
        assertEquals(SubjectType.REAL, SubjectType.fromId(6))
        // 接口没有编号 5；未知或缺失的编号返回 null。
        assertNull(SubjectType.fromId(5))
        assertNull(SubjectType.fromId(null))
    }

    @Test
    fun categoriesBelongToTheirType() {
        assertEquals(listOf("TV", "WEB", "OVA", "剧场版", "其他"), SubjectCategory.of(SubjectType.ANIME).map { it.label })
        assertEquals(listOf(1001, 1002, 1003, 0), SubjectCategory.of(SubjectType.BOOK).map { it.id })
        // 音乐没有分类，索引页不显示分类行。
        assertTrue(SubjectCategory.of(SubjectType.MUSIC).isEmpty())
        assertTrue(SubjectCategory.entries.all { it in SubjectCategory.of(it.type) })
    }

    @Test
    fun typeLabelCombinesTypeAndPlatform() {
        val base = Subject(id = 1, name = "作品", score = null, summary = "")

        assertEquals("书籍 · 漫画", base.copy(type = SubjectType.BOOK, platform = "漫画").typeLabel)
        assertEquals("动画", base.copy(type = SubjectType.ANIME).typeLabel)
        assertEquals("TV", base.copy(platform = "TV").typeLabel)
        assertNull(base.typeLabel)
    }
}
