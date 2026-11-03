package dev.bangumi.demo.model

// 搜索筛选和索引（浏览）用到的类型、分类、排序和查询条件。编号与 Bangumi 接口一致（2026-11-03 核对 OpenAPI 文档）。
// enum class 是 Kotlin 的枚举，和 Java 的 enum 一样，每个值可以带构造参数。

/** 条目类型。id 是接口中的编号（没有 5）。 */
enum class SubjectType(val id: Int, val label: String) {
    ANIME(2, "动画"),
    BOOK(1, "书籍"),
    MUSIC(3, "音乐"),
    GAME(4, "游戏"),
    REAL(6, "三次元"),
    ;

    /** companion object 中的函数相当于 Java 的静态方法：SubjectType.fromId(2) 得到 ANIME。 */
    companion object {
        /** 按接口编号查找类型；未知编号返回 null。 */
        fun fromId(id: Int?): SubjectType? = entries.firstOrNull { it.id == id }
    }
}

/**
 * 条目分类（接口参数 cat），每种分类属于一个条目类型；音乐没有分类。
 * 不同类型的“其他”编号都是 0，所以 id 只在同一类型内唯一。
 */
enum class SubjectCategory(val type: SubjectType, val id: Int, val label: String) {
    ANIME_TV(SubjectType.ANIME, 1, "TV"),
    ANIME_WEB(SubjectType.ANIME, 5, "WEB"),
    ANIME_OVA(SubjectType.ANIME, 2, "OVA"),
    ANIME_MOVIE(SubjectType.ANIME, 3, "剧场版"),
    ANIME_OTHER(SubjectType.ANIME, 0, "其他"),
    BOOK_COMIC(SubjectType.BOOK, 1001, "漫画"),
    BOOK_NOVEL(SubjectType.BOOK, 1002, "小说"),
    BOOK_ILLUSTRATION(SubjectType.BOOK, 1003, "画集"),
    BOOK_OTHER(SubjectType.BOOK, 0, "其他"),
    GAME_GAMES(SubjectType.GAME, 4001, "游戏"),
    GAME_SOFTWARE(SubjectType.GAME, 4002, "软件"),
    GAME_DLC(SubjectType.GAME, 4003, "扩展包"),
    GAME_TABLETOP(SubjectType.GAME, 4005, "桌游"),
    GAME_OTHER(SubjectType.GAME, 0, "其他"),
    REAL_JP_DRAMA(SubjectType.REAL, 1, "日剧"),
    REAL_WESTERN_DRAMA(SubjectType.REAL, 2, "欧美剧"),
    REAL_CN_DRAMA(SubjectType.REAL, 3, "华语剧"),
    REAL_TV_SERIES(SubjectType.REAL, 6001, "电视剧"),
    REAL_FILM(SubjectType.REAL, 6002, "电影"),
    REAL_SHOW(SubjectType.REAL, 6003, "演出"),
    REAL_VARIETY(SubjectType.REAL, 6004, "综艺"),
    REAL_OTHER(SubjectType.REAL, 0, "其他"),
    ;

    companion object {
        /** 某个类型下的全部分类，按上面声明的顺序；音乐返回空列表。 */
        fun of(type: SubjectType): List<SubjectCategory> = entries.filter { it.type == type }
    }
}

/** 搜索结果的排序方式（接口参数 sort）。 */
enum class SearchSort(val apiValue: String, val label: String) {
    MATCH("match", "匹配度"),
    HEAT("heat", "收藏人数"),
    RANK("rank", "排名"),
    SCORE("score", "评分"),
}

/** 索引（浏览）的排序方式：按排名从高到低，或按日期从新到旧。 */
enum class BrowseSort(val apiValue: String, val label: String) {
    RANK("rank", "按排名"),
    DATE("date", "按日期"),
}

/**
 * 一次搜索的条件。
 * @param type 只搜某一类型；null 表示全部类型。
 */
data class SearchQuery(
    val keyword: String,
    val type: SubjectType? = null,
    val sort: SearchSort = SearchSort.MATCH,
)

/**
 * 一次索引（浏览）的条件。
 * @param category 分类；null 表示该类型下的全部分类。必须属于 type。
 * @param year 年份；null 表示不限。
 * @param month 月份 1–12；null 表示不限。只在指定年份时有意义。
 */
data class BrowseQuery(
    val type: SubjectType = SubjectType.ANIME,
    val category: SubjectCategory? = null,
    val year: Int? = null,
    val month: Int? = null,
    val sort: BrowseSort = BrowseSort.RANK,
)
