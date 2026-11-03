package dev.bangumi.demo.model

/**
 * 一部番剧的数据模型，不包含界面或筛选行为。
 * data class 自动提供 equals、hashCode、toString 等方法，可类比 Java 数据类。
 * val 是只读属性，类似 final 字段加 getter；Double? 明确允许没有评分。
 * 网络返回的 JSON 先在 data 层转换成这个模型，界面只认识 Subject，不接触接口的原始字段。
 * @param id Bangumi 条目编号，也是列表区分条目的稳定标识。
 * @param name 显示用的作品名：优先中文名，没有时为原名。
 * @param score 十分制评分，null 时由界面显示暂无评分。
 * @param summary 作品简介，可能为空字符串。
 * @param imageUrl 列表用的小封面地址（约 200px 宽）；没有封面时为 null。默认值 null 让不关心图片的代码（如测试数据）可以省略它。
 */
data class Subject(
    val id: Int,
    val name: String,
    val score: Double?,
    val summary: String,
    val imageUrl: String? = null,
)
