package dev.bangumi.demo.data

import dev.bangumi.demo.model.SubjectType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// 各 *Json.kt 文件共用的解析工具。internal 表示只在本模块内可见，界面代码不会用到这些细节。

/**
 * 共用的 JSON 解析配置。
 * ignoreUnknownKeys：接口返回的几十个字段我们只用一部分，其余忽略而不是报错。
 */
internal val bangumiJson = Json { ignoreUnknownKeys = true }

/**
 * 接口中的一组图片地址。条目封面有 small(200px 宽)、grid(100px)、common(400px)、medium(800px)、large(原图)；
 * 角色图片有 grid(75×75 的脸部裁切)、small(100px)、medium(400px)、large(原图)；用户头像有 small、medium、large。
 * 字段可能缺失或为空字符串，读取时用 imageUrl 统一转成 null。
 */
@Serializable
internal data class ImageSetDto(
    val small: String? = null,
    val grid: String? = null,
    val common: String? = null,
    val medium: String? = null,
    val large: String? = null,
)

/** 空字符串和 null 都表示没有图片。 */
internal fun imageUrl(url: String?): String? = url?.takeIf { it.isNotBlank() }

/**
 * 把 lain.bgm.tv 的原图地址换成指定宽度的缩略图地址，例如
 * https://lain.bgm.tv/pic/cover/l/... → https://lain.bgm.tv/r/100/pic/cover/l/...
 * 接口在 images 中返回的 small、common 等地址就是这种 /r/宽度/ 形式。原图可能有几百 KB，列表中的小图用缩略图即可。
 * 不符合该格式的地址原样返回。
 */
internal fun resizedImageUrl(url: String?, width: Int): String? {
    val original = imageUrl(url) ?: return null
    val prefix = "https://lain.bgm.tv/pic/"
    return if (original.startsWith(prefix)) {
        "https://lain.bgm.tv/r/$width/pic/" + original.removePrefix(prefix)
    } else {
        original
    }
}

/** 优先使用中文名；中文名为空或缺失时退回原名。 */
internal fun preferChineseName(name: String, chineseName: String?): String =
    if (chineseName.isNullOrBlank()) name else chineseName

/** Bangumi 条目类型编号对应的文字；未知编号显示“其他”。 */
internal fun subjectTypeLabel(type: Int?): String = SubjectType.fromId(type)?.label ?: "其他"

/**
 * 把 infobox（资料表）中的一个值转成一行文字。
 *
 * infobox 的 value 有两种形式：
 * - 字符串，例如 "O型"；
 * - 列表，例如 [{"v": "后藤独"}, {"k": "外号", "v": "波奇酱"}]，这里拼成“后藤独、外号：波奇酱”。
 * 所以 DTO 中把 value 声明为 JsonElement（任意 JSON），在这里按实际类型分别处理。
 */
internal fun infoboxValueText(value: JsonElement): String = when (value) {
    is JsonNull -> ""
    is JsonPrimitive -> value.content
    is JsonArray -> value.map { item -> infoboxListItemText(item) }
        .filter { it.isNotBlank() }
        .joinToString("、")
    is JsonObject -> infoboxListItemText(value)
}

private fun infoboxListItemText(item: JsonElement): String {
    if (item !is JsonObject) return infoboxValueText(item)
    val key = (item["k"] as? JsonPrimitive)?.content
    val text = (item["v"] as? JsonPrimitive)?.content ?: ""
    return if (key.isNullOrBlank()) text else "$key：$text"
}
