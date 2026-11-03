package dev.bangumi.demo.data

import dev.bangumi.demo.model.BrowseQuery
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.SearchQuery
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 搜索（POST /v0/search/subjects）和索引浏览（GET /v0/subjects）：请求参数的生成，以及两者共用的结果解析。

/**
 * 把条目列表接口返回的 JSON 文字转成一页 Subject（本页条目 + 总数）。
 * 搜索和索引浏览的响应结构相同（data、total、limit、offset），共用这个函数。
 *
 * 单独写成不联网的普通函数，是为了能用一段固定的 JSON 直接测试转换规则：
 * - 优先显示中文名 name_cn；它为空时退回原名 name。
 * - 没有评分或评分为 0 时返回 null，界面显示“暂无评分”。
 * - 简介、平台、日期缺失时分别为空字符串、null、null。
 * - 响应中没有 total 时，以本页条数为总数（视为没有下一页）。
 *
 * JSON 格式不符合预期时抛出 SerializationException，与网络错误一样由状态层显示为失败。
 */
fun parseSubjectPage(text: String): Page<Subject> {
    val response = bangumiJson.decodeFromString(SubjectPageDto.serializer(), text)
    val subjects = response.data.map { item ->
        val score = item.rating?.score
        Subject(
            id = item.id,
            name = preferChineseName(item.name, item.nameCn),
            score = if (score == null || score == 0.0) null else score,
            summary = item.summary ?: "",
            imageUrl = imageUrl(item.images?.small),
            type = SubjectType.fromId(item.type),
            platform = item.platform?.takeIf { it.isNotBlank() },
            date = item.date?.takeIf { it.isNotBlank() },
        )
    }
    return Page(items = subjects, total = response.total ?: subjects.size)
}

/**
 * 生成搜索请求体的 JSON 文字，例如 {"keyword":"孤独摇滚","sort":"heat","filter":{"type":[1]}}。
 * 不限类型时 filter 为空对象 {}，搜索全部类型。
 */
internal fun searchRequestJson(query: SearchQuery): String =
    bangumiJson.encodeToString(
        SearchRequest.serializer(),
        SearchRequest(
            keyword = query.keyword,
            sort = query.sort.apiValue,
            filter = SearchFilter(type = query.type?.let { listOf(it.id) }),
        ),
    )

/**
 * 生成索引浏览的查询参数（GET /v0/subjects?type=2&cat=1&year=2024&...），没有设置的条件不出现。
 * 返回“参数名 → 值”的列表，由 BangumiApi 逐个加到请求上；单独成函数便于测试。
 */
internal fun browseParameters(query: BrowseQuery, offset: Int, limit: Int): List<Pair<String, String>> =
    // buildList 一边判断一边往列表里添加元素，类似 Java 中 new ArrayList 后多次 add。
    buildList {
        add("type" to query.type.id.toString())
        query.category?.let { add("cat" to it.id.toString()) }
        query.year?.let { add("year" to it.toString()) }
        // 月份只在指定年份时有意义。
        if (query.year != null) query.month?.let { add("month" to it.toString()) }
        add("sort" to query.sort.apiValue)
        add("limit" to limit.toString())
        add("offset" to offset.toString())
    }

// ---- 以下数据类只描述接口的 JSON 结构，仅在本文件内使用；界面只接触转换后的 Subject。----
// @Serializable 让插件在编译时生成“JSON ↔ 对象”的转换代码，类似 Java 中 Jackson 注解的作用。

/** 搜索请求体。keyword、sort、filter 不写默认值，确保总会被发送。 */
@Serializable
private data class SearchRequest(
    val keyword: String,
    val sort: String,
    val filter: SearchFilter,
)

/** 筛选条件。type 为 null 时等于默认值，不会被写进 JSON（即不限类型）。 */
@Serializable
private data class SearchFilter(
    val type: List<Int>? = null,
)

/** 条目列表响应：data 是本页条目，total 是符合条件的总数。 */
@Serializable
private data class SubjectPageDto(
    val data: List<SubjectItemDto>,
    val total: Int? = null,
)

/**
 * 单个条目。接口可能省略字段或返回 null，所以可选字段都声明为可空并给出默认值 null。
 * @SerialName 把 JSON 中的 name_cn 对应到 Kotlin 习惯的驼峰名 nameCn。
 */
@Serializable
private data class SubjectItemDto(
    val id: Int,
    val name: String,
    @SerialName("name_cn") val nameCn: String? = null,
    val summary: String? = null,
    val rating: ItemRatingDto? = null,
    val images: ImageSetDto? = null,
    val type: Int? = null,
    val platform: String? = null,
    val date: String? = null,
)

/** 评分信息中我们只取十分制的 score。 */
@Serializable
private data class ItemRatingDto(
    val score: Double? = null,
)
