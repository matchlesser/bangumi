package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.Subject
import kotlin.coroutines.cancellation.CancellationException

/**
 * 保存搜索页的输入和搜索结果，并处理搜索、加载更多等事件；不负责布局，也不自己发网络请求。
 * 点击结果后打开哪个页面由 AppNavigator 负责。
 *
 * @param searchSubjects 真正执行搜索的函数：传入关键词和起始位置 offset，返回一页结果和总数。
 *   类型 suspend (String, Int) -> Page<Subject> 和 onClick 的 () -> Unit 一样是函数类型，只是多了 suspend（可挂起）。
 *   程序运行时传入 BangumiApi 的方法，测试时传入一段返回固定结果的 Lambda，因此不联网也能测试这里的状态变化。
 */
class SubjectListState(private val searchSubjects: suspend (keyword: String, offset: Int) -> Page<Subject>) {

    // by 是属性委托：读写交给 Compose 状态；值变化时，读取它的界面会自动刷新。
    // private set：外部只能读，只能通过下面的方法修改，类似 Java 的公开 getter 与私有 setter。

    /** 输入框中的原文。 */
    var query by mutableStateOf("")
        private set

    /** 当前搜索结果（已取得的条目和总数）；null 表示还没有搜索过，items 为空表示搜过但没有匹配。 */
    var results by mutableStateOf<Page<Subject>?>(null)
        private set

    /**
     * 每次新的搜索成功后加一；“加载更多”不改变它。
     * 界面以它为键保存列表滚动位置：新搜索回到顶部，加载更多时保持在原来的位置。
     */
    var resultsVersion by mutableStateOf(0)
        private set

    /** 是否正在等待第一页结果；为 true 时界面显示加载提示，并忽略重复的搜索请求。 */
    var isLoading by mutableStateOf(false)
        private set

    /** 最近一次搜索失败的原因；null 表示没有错误。 */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** 是否正在加载下一页；为 true 时列表底部显示加载中。 */
    var isLoadingMore by mutableStateOf(false)
        private set

    /** 最近一次加载下一页失败的原因；已显示的结果保留，只在列表底部提示并可重试。 */
    var loadMoreError by mutableStateOf<String?>(null)
        private set

    /** 当前结果对应的关键词。输入框里的文字可能已经改了，加载下一页时仍要用这次搜索的关键词。 */
    private var searchedKeyword = ""

    /** 接收输入框原文；只记录文字，不会立即发请求，按回车或点“搜索”时才调用 search。 */
    fun updateQuery(query: String) {
        this.query = query
    }

    /**
     * 用当前关键词搜索第一页。suspend 函数，由界面在协程中调用（见 App 中的 scope.launch）。
     *
     * 过程：开始时标记加载中并清除旧错误 → 等待 searchSubjects 返回 → 成功保存结果，失败保存原因 → 结束加载。
     * 失败后再次调用本函数就是“重试”。
     */
    suspend fun search() {
        val keyword = query.trim()
        // 空关键词不发请求；正在加载时也不重复发送。
        if (keyword.isEmpty() || isLoading) return

        isLoading = true
        errorMessage = null
        try {
            val page = searchSubjects(keyword, 0)
            searchedKeyword = keyword
            results = page
            loadMoreError = null
            resultsVersion++
        } catch (e: CancellationException) {
            // 协程被取消（例如关闭窗口）不是搜索失败，不显示错误，继续向外抛出让协程正常结束。
            throw e
        } catch (e: Exception) {
            errorMessage = errorMessageOf(e)
        } finally {
            // 无论成功、失败还是取消，都结束加载状态。
            isLoading = false
        }
    }

    /**
     * 加载下一页并拼接到当前结果后面。没有更多结果、正在加载或正在重新搜索时不做任何事。
     * 失败时已有结果保留，只记录 loadMoreError；再次调用即重试。
     */
    suspend fun loadMore() {
        val current = results ?: return
        if (!current.hasMore || isLoadingMore || isLoading) return

        // 记下发出请求时的搜索版本；如果等待期间用户又搜索了别的关键词，这一页就作废，不拼到新结果上。
        val version = resultsVersion
        isLoadingMore = true
        loadMoreError = null
        try {
            val next = searchSubjects(searchedKeyword, current.items.size)
            if (version == resultsVersion) {
                results = current.appended(next) { it.id }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (version == resultsVersion) {
                loadMoreError = errorMessageOf(e)
            }
        } finally {
            isLoadingMore = false
        }
    }

    /** 清空关键词并回到初始提示：同时清除结果和错误。 */
    fun clearQuery() {
        query = ""
        results = null
        errorMessage = null
        loadMoreError = null
        // 版本加一，让清空前已经发出的“加载更多”请求返回后被丢弃，不会把结果又拼回来。
        resultsVersion++
    }
}
