package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.BrowseQuery
import dev.bangumi.demo.model.BrowseSort
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectCategory
import dev.bangumi.demo.model.SubjectType

/**
 * 索引标签页的状态：按类型、分类、年份、月份浏览条目，结果可以加载更多。
 * 每次改变条件都立即重新加载第一页；条件之间的约束也在这里处理：
 * - 换类型时清除分类（分类属于某个类型，例如“漫画”只属于书籍）；
 * - 清除年份时同时清除月份（只有指定年份时，月份才有意义）。
 *
 * @param browseSubjects 加载一页的函数：传入浏览条件和起始位置 offset；运行时为 BangumiApi.browseSubjects。
 */
class BrowseState(browseSubjects: suspend (query: BrowseQuery, offset: Int) -> Page<Subject>) {

    /** 当前浏览条件；默认浏览全部动画，按排名排序。 */
    var query by mutableStateOf(BrowseQuery())
        private set

    /** 浏览结果及其加载状态，规则见 PagedResults。 */
    val results = PagedResults(load = browseSubjects, idOf = { it.id })

    /**
     * 第一次显示索引页时加载；已经有结果、正在加载或已经失败（等用户点重试）时不做任何事。
     * 从详情页返回时索引页重新显示，因此不会重复请求。
     */
    suspend fun loadIfNeeded() {
        if (results.page == null && !results.isLoading && results.error == null) reload()
    }

    /** 按当前条件重新加载第一页；也用于失败后的“重试”。 */
    suspend fun reload() {
        results.loadFirst(query)
    }

    /** 换类型：清除分类（旧分类不属于新类型），然后重新加载。 */
    suspend fun changeType(type: SubjectType) {
        if (type == query.type) return
        update(query.copy(type = type, category = null))
    }

    /** 换分类；null 表示该类型下全部分类。不属于当前类型的分类会被忽略。 */
    suspend fun changeCategory(category: SubjectCategory?) {
        if (category != null && category.type != query.type) return
        update(query.copy(category = category))
    }

    /** 换年份；null 表示不限年份，同时清除月份。 */
    suspend fun changeYear(year: Int?) {
        update(query.copy(year = year, month = if (year == null) null else query.month))
    }

    /** 换月份；null 表示不限。没有指定年份时忽略。 */
    suspend fun changeMonth(month: Int?) {
        if (query.year == null) return
        update(query.copy(month = month))
    }

    /** 换排序方式。 */
    suspend fun changeSort(sort: BrowseSort) {
        update(query.copy(sort = sort))
    }

    /** 加载下一页，沿用当前结果对应的条件。 */
    suspend fun loadMore() {
        results.loadMore()
    }

    /** 条件真的变化时才保存并重新加载；copy 生成修改了部分属性的新对象，原对象不变。 */
    private suspend fun update(newQuery: BrowseQuery) {
        if (newQuery == query) return
        query = newQuery
        reload()
    }
}
