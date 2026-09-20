package dev.bangumi.demo.model

/**
 * 一部番剧的数据模型，不包含界面或筛选行为。
 * data class 自动提供 equals、hashCode、toString 等方法，可类比 Java 数据类。
 * val 是只读属性，类似 final 字段加 getter；Double? 明确允许没有评分。
 * @param id 稳定标识，列表通过它区分条目。
 * @param name 用于显示和名称筛选的作品名。
 * @param score 十分制评分，null 时由界面显示暂无评分。
 * @param summary 作品简介，不参与本阶段的名称筛选。
 */
data class Subject(
    val id: Int,
    val name: String,
    val score: Double?,
    val summary: String,
)
