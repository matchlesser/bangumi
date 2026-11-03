package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.Page
import kotlin.coroutines.cancellation.CancellationException

/**
 * 分页列表的加载状态：第一页、加载更多、失败与重试、丢弃过期结果。
 * 搜索页和索引页的结果列表行为完全相同，都使用这个类，规则只写一处。
 *
 * <R, T> 是两个泛型参数：R 是请求条件（例如 SearchQuery、BrowseQuery），T 是条目类型（例如 Subject）。
 *
 * @param load 加载一页：传入请求条件和起始位置 offset，返回一页结果和总数。
 * @param idOf 取条目唯一编号，拼接下一页时用于去重。
 */
class PagedResults<R, T>(
    private val load: suspend (request: R, offset: Int) -> Page<T>,
    private val idOf: (T) -> Any,
) {
    /** 当前结果；null 表示还没有加载过（或已清空）。 */
    var page by mutableStateOf<Page<T>?>(null)
        private set

    /** 是否正在加载第一页。 */
    var isLoading by mutableStateOf(false)
        private set

    /** 第一页加载失败的原因；null 表示没有错误。 */
    var error by mutableStateOf<String?>(null)
        private set

    /** 是否正在加载下一页。 */
    var isLoadingMore by mutableStateOf(false)
        private set

    /** 下一页加载失败的原因；已显示的结果保留。 */
    var loadMoreError by mutableStateOf<String?>(null)
        private set

    /**
     * 每次换成新的第一页（或清空）时加一；“加载更多”不改变它。
     * 界面以它为键保存列表滚动位置：新结果回到顶部，加载更多时保持原位置。
     */
    var version by mutableStateOf(0)
        private set

    /** 当前 page 对应的请求条件；加载下一页时沿用它，而不是界面上可能已经改动的条件。 */
    private var pageRequest: R? = null

    /** 每次请求第一页时加一，用来判断返回的结果是不是最新那次请求的（普通变量，不需要界面观察）。 */
    private var latestRequestId = 0

    /**
     * 按新条件加载第一页，替换当前结果。
     *
     * 最新的请求优先：如果等待期间又发出了新的请求（例如连续切换筛选条件），旧请求返回的结果直接丢弃，
     * 只显示最后一次请求的结果。失败时记录 error，再次调用即重试。
     */
    suspend fun loadFirst(request: R) {
        val requestId = ++latestRequestId
        isLoading = true
        error = null
        try {
            val first = load(request, 0)
            if (requestId == latestRequestId) {
                page = first
                pageRequest = request
                loadMoreError = null
                isLoadingMore = false
                version++
            }
        } catch (e: CancellationException) {
            // 协程被取消（例如离开页面、关闭窗口）不是失败，不显示错误，继续向外抛出。
            throw e
        } catch (e: Exception) {
            if (requestId == latestRequestId) error = errorMessageOf(e)
        } finally {
            // 只有最新的请求结束时才结束加载状态；被新请求取代的旧请求不影响界面。
            if (requestId == latestRequestId) isLoading = false
        }
    }

    /**
     * 加载下一页并拼接到当前结果后面（按 id 去重）。
     * 没有更多、正在加载或还没有结果时不做任何事；失败时只记录 loadMoreError，再次调用即重试。
     * 等待期间换了新的第一页或被清空时，这一页作废。
     */
    suspend fun loadMore() {
        val current = page ?: return
        val request = pageRequest ?: return
        if (!current.hasMore || isLoadingMore || isLoading) return

        val versionAtStart = version
        isLoadingMore = true
        loadMoreError = null
        try {
            val next = load(request, current.items.size)
            if (versionAtStart == version) page = current.appended(next, idOf)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (versionAtStart == version) loadMoreError = errorMessageOf(e)
        } finally {
            isLoadingMore = false
        }
    }

    /** 清空结果回到初始状态；已经发出但还没返回的请求结果都会被丢弃。 */
    fun clear() {
        latestRequestId++
        page = null
        pageRequest = null
        isLoading = false
        error = null
        isLoadingMore = false
        loadMoreError = null
        version++
    }
}
