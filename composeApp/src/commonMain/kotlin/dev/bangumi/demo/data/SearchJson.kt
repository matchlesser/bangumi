package dev.bangumi.demo.data

import dev.bangumi.demo.model.Subject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 把搜索接口返回的 JSON 文字转成界面使用的 Subject 列表。
 *
 * 单独写成不联网的普通函数，是为了能用一段固定的 JSON 直接测试转换规则：
 * - 优先显示中文名 name_cn；它为空时退回原名 name。
 * - 没有评分或评分为 0 时返回 null，界面显示“暂无评分”。
 * - 简介缺失时用空字符串，界面自行决定是否显示“暂无简介”。
 *
 * JSON 格式不符合预期时抛出 SerializationException，与网络错误一样由状态层显示为失败。
 */
fun parseSearchResponse(text: String): List<Subject> {
    val response = bangumiJson.decodeFromString(SearchResponse.serializer(), text)
    return response.data.map { item ->
        val score = item.rating?.score
        Subject(
            id = item.id,
            name = preferChineseName(item.name, item.nameCn),
            score = if (score == null || score == 0.0) null else score,
            summary = item.summary ?: "",
            imageUrl = imageUrl(item.images?.small),
        )
    }
}

/** 生成搜索请求体的 JSON 文字：只搜动画（type 2）。 */
internal fun searchRequestJson(keyword: String): String =
    bangumiJson.encodeToString(
        SearchRequest.serializer(),
        SearchRequest(keyword = keyword, filter = SearchFilter(type = listOf(2))),
    )

// ---- 以下数据类只描述接口的 JSON 结构，仅在本文件内使用；界面只接触转换后的 Subject。----
// @Serializable 让插件在编译时生成“JSON ↔ 对象”的转换代码，类似 Java 中 Jackson 注解的作用。

/** 请求体：{"keyword": "...", "filter": {"type": [2]}}。属性不写默认值，确保每个字段都会被发送。 */
@Serializable
private data class SearchRequest(
    val keyword: String,
    val filter: SearchFilter,
)

/** 筛选条件；type 中的 2 表示动画。 */
@Serializable
private data class SearchFilter(
    val type: List<Int>,
)

/** 响应体中我们需要的部分：data 是本页的条目列表。 */
@Serializable
private data class SearchResponse(
    val data: List<SearchItem>,
)

/**
 * 单个条目。接口可能省略字段或返回 null，所以可选字段都声明为可空并给出默认值 null。
 * @SerialName 把 JSON 中的 name_cn 对应到 Kotlin 习惯的驼峰名 nameCn。
 */
@Serializable
private data class SearchItem(
    val id: Int,
    val name: String,
    @SerialName("name_cn") val nameCn: String? = null,
    val summary: String? = null,
    val rating: SearchRating? = null,
    val images: ImageSetDto? = null,
)

/** 评分信息中我们只取十分制的 score。 */
@Serializable
private data class SearchRating(
    val score: Double? = null,
)
