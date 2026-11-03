package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.CharacterSummary
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.SubjectComment
import dev.bangumi.demo.model.SubjectDetail
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * 条目详情页的状态：基本信息（含评分分布）、角色列表、吐槽三块数据各自加载，互不影响。
 * 例如吐槽接口失败时，评分分布和角色仍然正常显示，只有吐槽区域显示“重试”。
 * 吐槽按页加载，可以“加载更多”。
 *
 * 与 SubjectListState 一样，通过构造参数接收“怎样加载”的函数：运行时传入 BangumiApi 的方法，测试时传入 Lambda。
 *
 * @param subjectId 条目编号。
 * @param title 详情加载完成前先显示的标题（来自上一个页面，例如搜索结果中的名称）。
 * @param loadComments 加载吐槽：传入条目编号和起始位置 offset，返回一页吐槽和总数。
 */
class SubjectDetailState(
    val subjectId: Int,
    val title: String,
    private val loadSubject: suspend (Int) -> SubjectDetail,
    private val loadCharacters: suspend (Int) -> List<CharacterSummary>,
    private val loadComments: suspend (subjectId: Int, offset: Int) -> Page<SubjectComment>,
) {
    var detail by mutableStateOf<LoadState<SubjectDetail>>(LoadState.Loading)
        private set

    var characters by mutableStateOf<LoadState<List<CharacterSummary>>>(LoadState.Loading)
        private set

    /** 吐槽的第一页及之后加载的各页（拼接在一起）。 */
    var comments by mutableStateOf<LoadState<Page<SubjectComment>>>(LoadState.Loading)
        private set

    /** 是否正在加载下一页吐槽。 */
    var isLoadingMoreComments by mutableStateOf(false)
        private set

    /** 最近一次加载下一页吐槽失败的原因；已显示的吐槽保留，只在底部提示并可重试。 */
    var moreCommentsError by mutableStateOf<String?>(null)
        private set

    /**
     * 加载所有尚未成功的部分；已经成功的不重复请求。
     *
     * 进入页面时调用一次；某一块失败后点“重试”也调用它，只会重新请求失败的那块。
     * 中途离开页面（协程被取消）时，未完成的部分保持“加载中”，回到页面时会再次加载。
     *
     * coroutineScope 内用 launch 同时发出三个请求，三者都结束后本函数才返回；
     * 类比 Java 中并行提交三个任务再等待全部完成。
     */
    suspend fun load(): Unit = coroutineScope {
        if (detail !is LoadState.Success) {
            launch {
                detail = LoadState.Loading
                detail = loadState { loadSubject(subjectId) }
            }
        }
        if (characters !is LoadState.Success) {
            launch {
                characters = LoadState.Loading
                characters = loadState { loadCharacters(subjectId) }
            }
        }
        if (comments !is LoadState.Success) {
            launch {
                comments = LoadState.Loading
                comments = loadState { loadComments(subjectId, 0) }
            }
        }
    }

    /**
     * 加载下一页吐槽并拼接到已显示的吐槽后面。
     * 第一页还没成功、已经全部加载或正在加载时不做任何事；失败时只记录 moreCommentsError，再次调用即重试。
     */
    suspend fun loadMoreComments() {
        // as? 在类型不符时得到 null：只有第一页已成功时才能加载下一页。
        val current = (comments as? LoadState.Success)?.data ?: return
        if (!current.hasMore || isLoadingMoreComments) return

        isLoadingMoreComments = true
        moreCommentsError = null
        try {
            val next = loadComments(subjectId, current.items.size)
            comments = LoadState.Success(current.appended(next) { it.id })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            moreCommentsError = errorMessageOf(e)
        } finally {
            isLoadingMoreComments = false
        }
    }
}
