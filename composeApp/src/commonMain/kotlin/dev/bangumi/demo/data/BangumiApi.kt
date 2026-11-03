package dev.bangumi.demo.data

import dev.bangumi.demo.model.Subject
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 访问 Bangumi 公开接口的数据层：只负责“发请求、拿回文字、转成 Subject”，不知道界面长什么样。
 *
 * 接口说明（2026-11-03 核对）：`POST /v0/search/subjects` 是官方标注的实验性 API，
 * 字段或行为以后可能变化；出问题时先对照 https://github.com/bangumi/api 。
 *
 * @param client Ktor 的 HTTP 客户端。HttpClient() 不指定引擎时，会在运行时找到 desktopMain 引入的 CIO 引擎，
 *   因此这里的共享代码不需要知道当前是哪个平台。
 */
class BangumiApi(private val client: HttpClient = HttpClient()) {

    /**
     * 按关键词搜索动画，返回最多 [SEARCH_LIMIT] 条结果。
     *
     * suspend 表示这是“可挂起”的函数：等待网络响应时不占用界面线程，
     * 只能在协程或另一个 suspend 函数中调用。类比 Java 中返回 CompletableFuture、
     * 但写法仍像普通的顺序代码。
     *
     * 请求失败（断网、超时、服务器返回错误码）时抛出异常，由调用方（状态层）决定怎样提示用户。
     */
    suspend fun searchSubjects(keyword: String): List<Subject> {
        val request = SearchRequest(keyword = keyword, filter = SearchFilter(type = listOf(ANIME_TYPE)))

        val response = client.post("https://api.bgm.tv/v0/search/subjects") {
            parameter("limit", SEARCH_LIMIT)
            // Bangumi 要求非浏览器客户端带上“开发者 ID/应用名 (项目主页)”形式的 User-Agent，否则可能被拒绝。
            header(HttpHeaders.UserAgent, USER_AGENT)
            contentType(ContentType.Application.Json)
            // 先自己把请求对象转成 JSON 文字再发送，每一步都看得见；serializer() 由 serialization 插件生成。
            setBody(json.encodeToString(SearchRequest.serializer(), request))
        }

        // Ktor 默认不会因 404、500 等错误码抛异常，这里主动检查，让调用方统一按“失败”处理。
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Bangumi 返回 HTTP ${response.status.value}")
        }
        return parseSearchResponse(response.bodyAsText())
    }

    private companion object {
        // companion object 中的 const val 相当于 Java 类里的 private static final 常量。
        const val ANIME_TYPE = 2
        const val SEARCH_LIMIT = 20
        const val USER_AGENT = "matchlesser/bangumi (https://github.com/matchlesser/bangumi)"
    }
}

/**
 * 把接口返回的 JSON 文字转成界面使用的 Subject 列表。
 *
 * 单独写成不联网的普通函数，是为了能用一段固定的 JSON 直接测试转换规则：
 * - 优先显示中文名 name_cn；它为空时退回原名 name。
 * - 没有评分或评分为 0 时返回 null，界面显示“暂无评分”。
 * - 简介缺失时用空字符串，界面自行决定是否显示“暂无简介”。
 *
 * JSON 格式不符合预期时抛出 SerializationException，与网络错误一样由状态层显示为搜索失败。
 */
fun parseSearchResponse(text: String): List<Subject> {
    val response = json.decodeFromString(SearchResponse.serializer(), text)
    return response.data.map { item ->
        val score = item.rating?.score
        Subject(
            id = item.id,
            // isNullOrBlank 为 false 时，编译器知道 item.nameCn 不为空，可以直接当作 String 使用。
            name = if (item.nameCn.isNullOrBlank()) item.name else item.nameCn,
            score = if (score == null || score == 0.0) null else score,
            summary = item.summary ?: "",
        )
    }
}

// ignoreUnknownKeys：接口返回的标签、图片等几十个字段我们暂时不用，忽略它们而不是报错。
private val json = Json { ignoreUnknownKeys = true }

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
)

/** 评分信息中我们只取十分制的 score。 */
@Serializable
private data class SearchRating(
    val score: Double? = null,
)
