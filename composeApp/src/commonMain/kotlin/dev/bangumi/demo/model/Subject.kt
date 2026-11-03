package dev.bangumi.demo.model

/**
 * 列表中的一个条目（搜索结果、索引结果、测试示例数据都用它），不包含界面或筛选行为。
 * data class 自动提供 equals、hashCode、toString 等方法，可类比 Java 数据类。
 * val 是只读属性，类似 final 字段加 getter；Double? 明确允许没有评分。
 * 网络返回的 JSON 先在 data 层转换成这个模型，界面只认识 Subject，不接触接口的原始字段。
 *
 * 后四个参数有默认值，让不关心它们的代码（如测试数据）可以省略。
 *
 * @param id Bangumi 条目编号，也是列表区分条目的稳定标识。
 * @param name 显示用的作品名：优先中文名，没有时为原名。
 * @param score 十分制评分，null 时由界面显示暂无评分。
 * @param summary 作品简介，可能为空字符串。
 * @param imageUrl 列表用的小封面地址（约 200px 宽）；没有封面时为 null。
 * @param type 条目类型（动画、书籍等）；未知时为 null。
 * @param platform 更细的形式，例如动画的“TV”“剧场版”、书籍的“漫画”“小说”；未知时为 null。
 * @param date 放送或发售日期，例如 "2022-10-08"；未知时为 null。
 */
data class Subject(
    val id: Int,
    val name: String,
    val score: Double?,
    val summary: String,
    val imageUrl: String? = null,
    val type: SubjectType? = null,
    val platform: String? = null,
    val date: String? = null,
) {
    /**
     * 卡片上的类型标签，例如“动画 · TV”“书籍 · 漫画”；只知道类型时为“动画”，都不知道时为 null。
     * 这是根据其他属性计算出来的只读属性（自定义 getter），不占用存储，也不参与 equals 比较。
     */
    val typeLabel: String?
        get() = when {
            type == null -> platform
            platform.isNullOrBlank() -> type.label
            else -> "${type.label} · $platform"
        }
}
