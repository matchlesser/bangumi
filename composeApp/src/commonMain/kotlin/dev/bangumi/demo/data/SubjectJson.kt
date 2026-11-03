package dev.bangumi.demo.data

import dev.bangumi.demo.model.CharacterSummary
import dev.bangumi.demo.model.SubjectCollection
import dev.bangumi.demo.model.SubjectComment
import dev.bangumi.demo.model.SubjectComments
import dev.bangumi.demo.model.SubjectDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

// 条目详情页用到的三种响应：条目详情、角色列表、吐槽箱。

/**
 * 解析 GET /v0/subjects/{id}。
 * - 评分分布 count 在 JSON 中是 {"1": 200, ..., "10": 7021}，这里转成固定 10 个元素的列表，缺少的分数记为 0。
 * - 评分为 0 视为没有评分；排名为 0 视为未上榜。
 */
fun parseSubjectDetail(text: String): SubjectDetail {
    val dto = bangumiJson.decodeFromString(SubjectDto.serializer(), text)
    val rating = dto.rating
    val score = rating?.score
    val rank = rating?.rank
    val collection = dto.collection
    return SubjectDetail(
        id = dto.id,
        name = preferChineseName(dto.name, dto.nameCn),
        originalName = dto.name,
        summary = dto.summary ?: "",
        score = if (score == null || score == 0.0) null else score,
        rank = if (rank == null || rank == 0) null else rank,
        ratingTotal = rating?.total ?: 0,
        // (1..10).map 依次取 "1" 到 "10" 的人数，类似 Java 的 IntStream.rangeClosed(1, 10).map(...)。
        ratingCounts = (1..10).map { point -> rating?.count?.get(point.toString()) ?: 0 },
        date = dto.date?.takeIf { it.isNotBlank() },
        platform = dto.platform?.takeIf { it.isNotBlank() },
        episodes = dto.eps?.takeIf { it > 0 },
        collection = SubjectCollection(
            wish = collection?.wish ?: 0,
            collect = collection?.collect ?: 0,
            doing = collection?.doing ?: 0,
            onHold = collection?.onHold ?: 0,
            dropped = collection?.dropped ?: 0,
        ),
        imageUrl = imageUrl(dto.images?.common),
    )
}

/**
 * 解析 GET /v0/subjects/{id}/characters。
 * 接口返回的顺序不按重要程度，这里按“主角 → 配角 → 其他”排序；同一类保持原有顺序。
 */
fun parseSubjectCharacters(text: String): List<CharacterSummary> {
    val items = bangumiJson.decodeFromString(ListSerializer(SubjectCharacterDto.serializer()), text)
    return items
        .map { item ->
            CharacterSummary(
                id = item.id,
                name = item.name,
                relation = item.relation ?: "",
                voiceActor = item.actors.firstOrNull()?.name,
                imageUrl = imageUrl(item.images?.grid),
            )
        }
        // sortedBy 是稳定排序：排序键相同的元素保持原来的先后顺序。
        .sortedBy { character ->
            when (character.relation) {
                "主角" -> 0
                "配角" -> 1
                else -> 2
            }
        }
}

/**
 * 解析 GET https://next.bgm.tv/p1/subjects/{id}/comments（吐槽箱）。
 * 注意：这是 Bangumi 新前端使用的非公开接口（文档标题为 "bangumi private api"），可能随时变化；
 * 解析失败时只影响吐槽区域，详情页其他部分照常显示。
 */
fun parseSubjectComments(text: String): SubjectComments {
    val dto = bangumiJson.decodeFromString(CommentsDto.serializer(), text)
    return SubjectComments(
        total = dto.total,
        items = dto.data.map { item ->
            SubjectComment(
                id = item.id,
                userName = item.user?.nickname?.takeIf { it.isNotBlank() } ?: "匿名用户",
                status = collectionTypeLabel(item.type),
                rate = item.rate?.takeIf { it in 1..10 },
                text = item.comment ?: "",
                updatedAt = item.updatedAt ?: 0,
                avatarUrl = imageUrl(item.user?.avatar?.small),
            )
        },
    )
}

/** 收藏类型编号对应的文字（动画条目）。 */
private fun collectionTypeLabel(type: Int?): String = when (type) {
    1 -> "想看"
    2 -> "看过"
    3 -> "在看"
    4 -> "搁置"
    5 -> "抛弃"
    else -> "收藏"
}

// ---- 以下数据类只描述接口的 JSON 结构，仅在本文件内使用。可选字段都给出默认值，缺失时不报错。----

@Serializable
private data class SubjectDto(
    val id: Int,
    val name: String,
    @SerialName("name_cn") val nameCn: String? = null,
    val summary: String? = null,
    val date: String? = null,
    val platform: String? = null,
    val eps: Int? = null,
    val rating: RatingDto? = null,
    val collection: CollectionDto? = null,
    val images: ImageSetDto? = null,
)

/** count 是“分数 → 人数”的映射，JSON 对象的键是字符串 "1"–"10"，所以用 Map<String, Int> 接收。 */
@Serializable
private data class RatingDto(
    val rank: Int? = null,
    val total: Int? = null,
    val count: Map<String, Int>? = null,
    val score: Double? = null,
)

@Serializable
private data class CollectionDto(
    val wish: Int? = null,
    val collect: Int? = null,
    val doing: Int? = null,
    @SerialName("on_hold") val onHold: Int? = null,
    val dropped: Int? = null,
)

@Serializable
private data class SubjectCharacterDto(
    val id: Int,
    val name: String,
    val relation: String? = null,
    val actors: List<ActorDto> = emptyList(),
    val images: ImageSetDto? = null,
)

@Serializable
private data class ActorDto(
    val name: String,
)

@Serializable
private data class CommentsDto(
    val data: List<CommentDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
private data class CommentDto(
    val id: Long,
    val user: CommentUserDto? = null,
    val type: Int? = null,
    val rate: Int? = null,
    val comment: String? = null,
    val updatedAt: Long? = null,
)

@Serializable
private data class CommentUserDto(
    val nickname: String? = null,
    val avatar: ImageSetDto? = null,
)
