package dev.bangumi.demo.data

import dev.bangumi.demo.model.CharacterAppearance
import dev.bangumi.demo.model.CharacterDetail
import dev.bangumi.demo.model.ProfileEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement

// 角色详情页用到的三种响应：角色资料、出演作品、声优。

/**
 * 解析 GET /v0/characters/{id}。
 * - infobox 中的“简体中文名”单独取出作为 chineseName，显示在标题处，不再放进资料列表。
 * - “引用来源”只说明资料出处，对读者没有帮助，不显示。
 */
fun parseCharacterDetail(text: String): CharacterDetail {
    val dto = bangumiJson.decodeFromString(CharacterDto.serializer(), text)
    val entries = dto.infobox.map { item -> ProfileEntry(label = item.key, value = infoboxValueText(item.value)) }
    return CharacterDetail(
        id = dto.id,
        name = dto.name,
        chineseName = entries.firstOrNull { it.label == "简体中文名" }?.value?.takeIf { it.isNotBlank() },
        summary = dto.summary ?: "",
        profile = entries.filter { it.label != "简体中文名" && it.label != "引用来源" && it.value.isNotBlank() },
        collects = dto.stat?.collects ?: 0,
        comments = dto.stat?.comments ?: 0,
        imageUrl = imageUrl(dto.images?.medium),
    )
}

/** 解析 GET /v0/characters/{id}/subjects：角色出现过的作品，名称优先中文名。 */
fun parseCharacterAppearances(text: String): List<CharacterAppearance> {
    val items = bangumiJson.decodeFromString(ListSerializer(AppearanceDto.serializer()), text)
    return items.map { item ->
        CharacterAppearance(
            subjectId = item.id,
            name = preferChineseName(item.name, item.nameCn),
            type = subjectTypeLabel(item.type),
            relation = item.staff ?: "",
            // 这个接口只给原图地址（可能几百 KB），列表中换成 100px 宽的缩略图。
            imageUrl = resizedImageUrl(item.image, width = 100),
        )
    }
}

/**
 * 解析 GET /v0/characters/{id}/persons：为该角色配音的人。
 * 同一位声优会因为多部作品重复出现，这里按编号去重，保持首次出现的顺序。
 */
fun parseCharacterVoiceActors(text: String): List<String> {
    val items = bangumiJson.decodeFromString(ListSerializer(PersonDto.serializer()), text)
    // distinctBy 按 id 去重，类似 Java Stream 中按某个键保留第一个元素。
    return items.distinctBy { it.id }.map { it.name }
}

// ---- 以下数据类只描述接口的 JSON 结构，仅在本文件内使用。----

@Serializable
private data class CharacterDto(
    val id: Int,
    val name: String,
    val summary: String? = null,
    val infobox: List<InfoboxItemDto> = emptyList(),
    val stat: CharacterStatDto? = null,
    val images: ImageSetDto? = null,
)

/** value 可能是字符串也可能是列表，用 JsonElement 先原样接收，再由 infoboxValueText 处理。 */
@Serializable
private data class InfoboxItemDto(
    val key: String,
    val value: JsonElement,
)

@Serializable
private data class CharacterStatDto(
    val comments: Int? = null,
    val collects: Int? = null,
)

@Serializable
private data class AppearanceDto(
    val id: Int,
    val name: String,
    @SerialName("name_cn") val nameCn: String? = null,
    val type: Int? = null,
    val staff: String? = null,
    val image: String? = null,
)

@Serializable
private data class PersonDto(
    val id: Int,
    val name: String,
)
