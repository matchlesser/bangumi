package dev.bangumi.demo.data

import dev.bangumi.demo.model.CharacterAppearance
import dev.bangumi.demo.model.CharacterDetail
import dev.bangumi.demo.model.CharacterSummary
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectComment
import dev.bangumi.demo.model.SubjectDetail
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * 访问 Bangumi 接口的数据层：只负责“发请求、拿回文字、交给 *Json.kt 中的函数转换”，不知道界面长什么样。
 *
 * 每个公开函数都是 suspend 函数：等待网络时不占用界面线程，只能在协程中调用。
 * 请求失败（断网、超时、服务器返回错误码、JSON 格式不符）时抛出异常，由状态层决定怎样提示。
 *
 * 接口说明（2026-11-03 核对）：
 * - api.bgm.tv 的 /v0 是官方公开接口；其中搜索接口标注为实验性。
 * - next.bgm.tv 的 /p1 是 Bangumi 新前端使用的非公开接口，目前只用于吐槽箱，可能随时变化。
 *
 * @param client Ktor 的 HTTP 客户端。不指定引擎时，会在运行时找到 desktopMain 引入的 CIO 引擎。
 */
class BangumiApi(private val client: HttpClient = HttpClient()) {

    /**
     * 按关键词搜索动画，返回从第 offset 条开始的一页（最多 [PAGE_SIZE] 条）以及匹配总数。
     * offset 默认为 0，即第一页；“加载更多”时传入已取得的条数。
     */
    suspend fun searchSubjects(keyword: String, offset: Int = 0): Page<Subject> {
        val response = client.post("$API_HOST/v0/search/subjects") {
            parameter("limit", PAGE_SIZE)
            parameter("offset", offset)
            header(HttpHeaders.UserAgent, BANGUMI_USER_AGENT)
            contentType(ContentType.Application.Json)
            setBody(searchRequestJson(keyword))
        }
        return parseSearchResponse(response.textOrThrow())
    }

    /** 条目详情：评分分布、排名、放送信息、收藏人数等。 */
    suspend fun getSubject(subjectId: Int): SubjectDetail =
        parseSubjectDetail(getText("$API_HOST/v0/subjects/$subjectId"))

    /** 条目中的角色，已按“主角 → 配角 → 其他”排序。 */
    suspend fun getSubjectCharacters(subjectId: Int): List<CharacterSummary> =
        parseSubjectCharacters(getText("$API_HOST/v0/subjects/$subjectId/characters"))

    /** 条目吐槽箱，按时间从新到旧，从第 offset 条开始取一页（非公开接口，见类说明）。 */
    suspend fun getSubjectComments(subjectId: Int, offset: Int = 0): Page<SubjectComment> =
        parseSubjectComments(getText("$NEXT_HOST/p1/subjects/$subjectId/comments?limit=$PAGE_SIZE&offset=$offset"))

    /** 角色资料。 */
    suspend fun getCharacter(characterId: Int): CharacterDetail =
        parseCharacterDetail(getText("$API_HOST/v0/characters/$characterId"))

    /** 角色出现过的作品。 */
    suspend fun getCharacterAppearances(characterId: Int): List<CharacterAppearance> =
        parseCharacterAppearances(getText("$API_HOST/v0/characters/$characterId/subjects"))

    /** 为角色配音的声优（已去重）。 */
    suspend fun getCharacterVoiceActors(characterId: Int): List<String> =
        parseCharacterVoiceActors(getText("$API_HOST/v0/characters/$characterId/persons"))

    /** 发送带 User-Agent 的 GET 请求并返回响应文字。 */
    private suspend fun getText(url: String): String {
        val response = client.get(url) {
            header(HttpHeaders.UserAgent, BANGUMI_USER_AGENT)
        }
        return response.textOrThrow()
    }

    /** Ktor 默认不会因 404、500 等错误码抛异常，这里主动检查，让调用方统一按“失败”处理。 */
    private suspend fun HttpResponse.textOrThrow(): String {
        if (!status.isSuccess()) {
            throw IllegalStateException("Bangumi 返回 HTTP ${status.value}")
        }
        return bodyAsText()
    }

    private companion object {
        // companion object 中的 const val 相当于 Java 类里的 private static final 常量。
        const val API_HOST = "https://api.bgm.tv"
        const val NEXT_HOST = "https://next.bgm.tv"
        // 每页条数：搜索结果和吐槽都按每页 20 条请求。
        const val PAGE_SIZE = 20
    }
}

/**
 * Bangumi 要求非浏览器客户端带上“开发者 ID/应用名 (项目主页)”形式的 User-Agent，否则可能被拒绝。
 * 接口请求和图片下载（见 ImageLoading.kt）都使用它；internal 表示只在本模块内可见。
 */
internal const val BANGUMI_USER_AGENT = "matchlesser/bangumi (https://github.com/matchlesser/bangumi)"
