package dev.bangumi.demo.data

import dev.bangumi.demo.model.Subject

// 名称、评分和简介均为虚构示例，不来自 Bangumi 接口。
// 顶层 val 可由桌面入口直接导入；listOf 提供只读 List，UI 与筛选均不修改该列表。
// 构造调用无需 Java 的 new；命名参数便于对应模型字段，null 用来演示评分缺失。
val sampleSubjects: List<Subject> = listOf(
    Subject(
        id = 1,
        name = "星海邮递员",
        score = 8.4,
        summary = "一位新手邮递员驾驶小飞船，穿梭在星球之间，把信件送到等待它们的人手中。",
    ),
    Subject(
        id = 2,
        name = "雨后的小小书店",
        score = 7.9,
        summary = "街角书店迎来了一位新店员。整理旧书的日子里，她渐渐认识了小镇上的读者。",
    ),
    Subject(
        id = 3,
        name = "云端练习曲",
        score = null,
        summary = "几位热爱音乐的少年，在山顶的旧车站组建乐队，为第一次演出认真排练。",
    ),
)
