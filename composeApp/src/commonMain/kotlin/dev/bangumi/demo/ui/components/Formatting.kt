package dev.bangumi.demo.ui.components

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// 界面用的文字格式化函数。共享代码不能使用 Java 的 String.format，所以这里用整数运算自己拼接。
// 都是不依赖界面的普通函数，可以直接写测试。

/**
 * 当前时间的 Unix 秒数，用于计算吐槽的相对时间。
 * kotlin.time.Clock 是 Kotlin 标准库中跨平台的时钟（Kotlin 2.2 中仍标记为实验性，所以需要 @OptIn）。
 */
@OptIn(ExperimentalTime::class)
fun currentEpochSeconds(): Long = Clock.System.now().epochSeconds

/** 当前年份（按 UTC 计算），用于索引页的年份列表。 */
fun currentYear(): Int = yearOfEpochSeconds(currentEpochSeconds())

/**
 * 把 Unix 秒数换算成公历年份（UTC）。共享代码不能使用 java.time，所以按公历规则自己计算：
 * 先换算成天数，再以 400 年（146097 天）为一个周期计算年份。算法来自 Howard Hinnant 的 days_from_civil 的逆运算。
 */
fun yearOfEpochSeconds(epochSeconds: Long): Int {
    // floorDiv：向下取整的除法，负数（1970 年以前）也能得到正确的天数。
    val days = epochSeconds.floorDiv(86_400L)
    val shifted = days + 719_468          // 把起点移到公元 0 年 3 月 1 日，闰日落在一年的最后
    val era = shifted.floorDiv(146_097L)  // 第几个 400 年周期
    val dayOfEra = shifted - era * 146_097
    val yearOfEra = (dayOfEra - dayOfEra / 1_460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthIndex = (5 * dayOfYear + 2) / 153 // 0 表示 3 月，10、11 表示次年 1、2 月
    val year = yearOfEra + era * 400 + if (monthIndex >= 10) 1 else 0
    return year.toInt()
}

/**
 * 千位分隔：67666 → "67,666"。
 * chunked(3) 把倒序后的数字每三位分一组，再倒回来，类似 Java 中 DecimalFormat("#,###") 的效果。
 */
fun formatCount(value: Int): String {
    if (value < 0) return "-" + formatCount(-value)
    return value.toString().reversed().chunked(3).joinToString(",").reversed()
}

/**
 * 百分比，保留一位小数并四舍五入：formatPercent(7021, 41530) → "16.9%"。total 为 0 时返回 "0.0%"。
 * 先算出“千分之几”的整数，再拆成整数部分和一位小数，避免浮点数格式化。
 */
fun formatPercent(count: Int, total: Int): String {
    if (total <= 0) return "0.0%"
    val tenths = (count * 1000L + total / 2) / total
    return "${tenths / 10}.${tenths % 10}%"
}

/**
 * 相对时间：根据两个 Unix 秒数之差显示“刚刚”“5 分钟前”“3 天前”等。
 * 把“现在”作为参数传入而不是在函数里读取系统时间，测试时就能给出固定的值。
 */
fun formatRelativeTime(epochSeconds: Long, nowEpochSeconds: Long): String {
    val seconds = (nowEpochSeconds - epochSeconds).coerceAtLeast(0)
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "刚刚"
        hours < 1 -> "$minutes 分钟前"
        days < 1 -> "$hours 小时前"
        days < 30 -> "$days 天前"
        days < 365 -> "${days / 30} 个月前"
        else -> "${days / 365} 年前"
    }
}
