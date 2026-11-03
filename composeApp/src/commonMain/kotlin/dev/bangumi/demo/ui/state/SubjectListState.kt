package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.SearchQuery
import dev.bangumi.demo.model.SearchSort
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.model.SubjectType

/**
 * 搜索标签页的状态：输入框文字、类型筛选、排序，以及搜索结果（含加载更多）。
 * 不负责布局，也不自己发网络请求；点击结果后打开哪个页面由 AppNavigator 负责。
 *
 * @param searchSubjects 真正执行搜索的函数：传入搜索条件和起始位置 offset，返回一页结果和总数。
 *   类型 suspend (SearchQuery, Int) -> Page<Subject> 和 onClick 的 () -> Unit 一样是函数类型，只是多了 suspend（可挂起）。
 *   程序运行时传入 BangumiApi 的方法，测试时传入一段返回固定结果的 Lambda，因此不联网也能测试这里的状态变化。
 */
class SubjectListState(searchSubjects: suspend (query: SearchQuery, offset: Int) -> Page<Subject>) {

    // by 是属性委托：读写交给 Compose 状态；值变化时，读取它的界面会自动刷新。
    // private set：外部只能读，只能通过下面的方法修改，类似 Java 的公开 getter 与私有 setter。

    /** 输入框中的原文。 */
    var query by mutableStateOf("")
        private set

    /** 只搜某一类型；null 表示全部类型。 */
    var typeFilter by mutableStateOf<SubjectType?>(null)
        private set

    /** 排序方式。 */
    var sort by mutableStateOf(SearchSort.MATCH)
        private set

    /** 搜索结果及其加载状态（第一页、加载更多、错误），规则见 PagedResults。 */
    val results = PagedResults(load = searchSubjects, idOf = { it.id })

    /** 接收输入框原文；只记录文字，不会立即发请求，按回车或点“搜索”时才调用 search。 */
    fun updateQuery(query: String) {
        this.query = query
    }

    /**
     * 用当前关键词、类型筛选和排序搜索第一页。suspend 函数，由界面在协程中调用（见 App 中的 scope.launch）。
     * 关键词去掉首尾空白后为空时不发请求。失败后再次调用就是“重试”。
     */
    suspend fun search() {
        val keyword = query.trim()
        if (keyword.isEmpty()) return
        results.loadFirst(SearchQuery(keyword = keyword, type = typeFilter, sort = sort))
    }

    /** 切换类型筛选；输入框有关键词时立即按新条件重新搜索。 */
    suspend fun changeType(type: SubjectType?) {
        if (type == typeFilter) return
        typeFilter = type
        search()
    }

    /** 切换排序；输入框有关键词时立即按新条件重新搜索。 */
    suspend fun changeSort(sort: SearchSort) {
        if (sort == this.sort) return
        this.sort = sort
        search()
    }

    /** 加载下一页，沿用当前结果对应的搜索条件。 */
    suspend fun loadMore() {
        results.loadMore()
    }

    /** 清空关键词并回到初始提示：同时清除结果和错误；筛选和排序保留。 */
    fun clearQuery() {
        query = ""
        results.clear()
    }
}
