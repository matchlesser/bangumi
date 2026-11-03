package dev.bangumi.demo.model

/**
 * 条目页“主要人物”列表中的一个角色。
 *
 * @param name 角色名（Bangumi 角色列表只提供原名，中文名在角色详情中）。
 * @param relation 在该条目中的定位，例如“主角”“配角”“客串”。
 * @param voiceActor 第一位声优（通常是原版配音）；没有时为 null。
 * @param imageUrl 头像地址（75×75 的脸部裁切）；没有时为 null。
 */
data class CharacterSummary(
    val id: Int,
    val name: String,
    val relation: String,
    val voiceActor: String?,
    val imageUrl: String? = null,
) {
    /** 主角和配角算作“主要人物”，其余（客串、闲角等）默认折叠。 */
    val isMain: Boolean get() = relation == "主角" || relation == "配角"
}

/**
 * 角色详情，由 /v0/characters/{id} 转换而来。
 *
 * @param chineseName 简体中文名；Bangumi 没有登记时为 null。
 * @param profile 基本资料，例如（"性别", "女"）、（"生日", "2月21日"），保持接口中的顺序。
 * @param collects 收藏该角色的人数。
 * @param comments 该角色的吐槽数。
 * @param imageUrl 角色立绘地址（约 400px 宽）；没有时为 null。
 */
data class CharacterDetail(
    val id: Int,
    val name: String,
    val chineseName: String?,
    val summary: String,
    val profile: List<ProfileEntry>,
    val collects: Int,
    val comments: Int,
    val imageUrl: String? = null,
)

/** 资料中的一项，例如 label = "血型"，value = "O型"。 */
data class ProfileEntry(
    val label: String,
    val value: String,
)

/**
 * 角色出现过的作品。
 *
 * @param name 作品名：优先中文名。
 * @param type 作品类型文字，例如“动画”“书籍”。
 * @param relation 角色在该作品中的定位，例如“主角”。
 * @param imageUrl 作品小封面地址（约 100px 宽）；没有时为 null。
 */
data class CharacterAppearance(
    val subjectId: Int,
    val name: String,
    val type: String,
    val relation: String,
    val imageUrl: String? = null,
)
