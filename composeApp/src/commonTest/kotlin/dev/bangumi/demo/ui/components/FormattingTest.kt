package dev.bangumi.demo.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

/** 测试界面文字格式化：千位分隔、百分比四舍五入、相对时间的各个区间。 */
class FormattingTest {

    @Test
    fun formatsCountsWithThousandsSeparators() {
        assertEquals(
            listOf("0", "999", "1,000", "67,666", "1,234,567", "-1,000"),
            listOf(0, 999, 1000, 67666, 1234567, -1000).map { formatCount(it) },
        )
    }

    // 7021 / 41530 = 16.906…%，保留一位小数为 16.9%；2 / 3 = 66.66…% 四舍五入为 66.7%。
    @Test
    fun formatsPercentWithOneDecimalRounded() {
        assertEquals("16.9%", formatPercent(7021, 41530))
        assertEquals("66.7%", formatPercent(2, 3))
        assertEquals("33.3%", formatPercent(1, 3))
        assertEquals("100.0%", formatPercent(5, 5))
        assertEquals("0.0%", formatPercent(0, 0))
    }

    @Test
    fun formatsRelativeTimeByRange() {
        val now = 1_800_000_000L
        val minute = 60L
        val hour = 60 * minute
        val day = 24 * hour

        assertEquals("刚刚", formatRelativeTime(now - 30, now))
        assertEquals("5 分钟前", formatRelativeTime(now - 5 * minute, now))
        assertEquals("3 小时前", formatRelativeTime(now - 3 * hour, now))
        assertEquals("3 天前", formatRelativeTime(now - 3 * day, now))
        assertEquals("1 个月前", formatRelativeTime(now - 45 * day, now))
        assertEquals("1 年前", formatRelativeTime(now - 400 * day, now))
        // 本机时间比服务器慢时差值为负，按“刚刚”处理。
        assertEquals("刚刚", formatRelativeTime(now + 100, now))
    }

    // 年份换算：边界处（新年前后一秒）、闰年 2 月 29 日、1970 年以前都要正确。
    @Test
    fun convertsEpochSecondsToYear() {
        assertEquals(1970, yearOfEpochSeconds(0))
        assertEquals(1969, yearOfEpochSeconds(-1))
        assertEquals(2023, yearOfEpochSeconds(1_704_067_199)) // 2023-12-31 23:59:59 UTC
        assertEquals(2024, yearOfEpochSeconds(1_704_067_200)) // 2024-01-01 00:00:00 UTC
        assertEquals(2024, yearOfEpochSeconds(1_709_164_800)) // 2024-02-29 00:00:00 UTC
        assertEquals(2026, yearOfEpochSeconds(1_793_642_889)) // 2026-11 的一条吐槽时间
        assertEquals(1900, yearOfEpochSeconds(-2_208_988_800)) // 1900-01-01 00:00:00 UTC
    }
}
