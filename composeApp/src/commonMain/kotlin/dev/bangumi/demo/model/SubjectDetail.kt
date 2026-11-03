package dev.bangumi.demo.model

/**
 * 条目详情页需要的完整信息，由 data 层从 /v0/subjects/{id} 转换而来。
 *
 * @param name 显示用名称：优先中文名，没有时为原名。
 * @param originalName 原名（通常是日文）；与 name 相同时界面不重复显示。
 * @param score 十分制评分；没有评分时为 null。
 * @param rank Bangumi 排名；未上榜时为 null。
 * @param ratingTotal 参与评分的人数。
 * @param ratingCounts 各分数的人数，固定 10 个元素：下标 0 对应 1 分，下标 9 对应 10 分。
 * @param date 放送或发售日期，例如 "2022-10-08"；未知时为 null。
 * @param platform 平台，例如 "TV"、"剧场版"；未知时为 null。
 * @param episodes 话数；未知时为 null。
 * @param imageUrl 详情页用的封面地址（约 400px 宽）；没有封面时为 null。
 */
data class SubjectDetail(
    val id: Int,
    val name: String,
    val originalName: String,
    val summary: String,
    val score: Double?,
    val rank: Int?,
    val ratingTotal: Int,
    val ratingCounts: List<Int>,
    val date: String?,
    val platform: String?,
    val episodes: Int?,
    val collection: SubjectCollection,
    val imageUrl: String? = null,
)

/** 收藏人数统计：想看、看过、在看、搁置、抛弃。 */
data class SubjectCollection(
    val wish: Int,
    val collect: Int,
    val doing: Int,
    val onHold: Int,
    val dropped: Int,
)

/**
 * 条目页中的一条吐槽（用户收藏条目时写的短评）。
 *
 * @param userName 用户昵称。
 * @param status 收藏状态文字，例如“看过”“在看”。
 * @param rate 用户打的分（1–10）；没有打分时为 null。
 * @param updatedAt 最后更新时间，Unix 秒。
 * @param avatarUrl 用户头像地址（约 100px）；没有时为 null。
 */
data class SubjectComment(
    val id: Long,
    val userName: String,
    val status: String,
    val rate: Int?,
    val text: String,
    val updatedAt: Long,
    val avatarUrl: String? = null,
)

/** 一页吐槽：total 是全部吐槽数量，items 是本次取回的最新若干条。 */
data class SubjectComments(
    val total: Int,
    val items: List<SubjectComment>,
)
