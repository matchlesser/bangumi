package dev.bangumi.demo.data

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

/** 优先使用中文名；中文名为空或缺失时退回原名。 */
internal fun preferChineseName(name: String, chineseName: String?): String =
    if (chineseName.isNullOrBlank()) name else chineseName

/** Bangumi 条目类型编号对应的文字。 */
internal fun subjectTypeLabel(type: Int?): String = when (type) {
    1 -> "书籍"
    2 -> "动画"
    3 -> "音乐"
    4 -> "游戏"
    6 -> "三次元"
    else -> "其他"
}

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
