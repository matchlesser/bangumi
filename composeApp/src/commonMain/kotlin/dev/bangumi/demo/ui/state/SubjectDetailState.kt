package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.CharacterSummary
import dev.bangumi.demo.model.SubjectComments
import dev.bangumi.demo.model.SubjectDetail
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 条目详情页的状态：基本信息（含评分分布）、角色列表、吐槽三块数据各自加载，互不影响。
 * 例如吐槽接口失败时，评分分布和角色仍然正常显示，只有吐槽区域显示“重试”。
 *
 * 与 SubjectListState 一样，通过构造参数接收“怎样加载”的函数：运行时传入 BangumiApi 的方法，测试时传入 Lambda。
 *
 * @param subjectId 条目编号。
 * @param title 详情加载完成前先显示的标题（来自上一个页面，例如搜索结果中的名称）。
 */
class SubjectDetailState(
    val subjectId: Int,
    val title: String,
    private val loadSubject: suspend (Int) -> SubjectDetail,
    private val loadCharacters: suspend (Int) -> List<CharacterSummary>,
    private val loadComments: suspend (Int) -> SubjectComments,
) {
    var detail by mutableStateOf<LoadState<SubjectDetail>>(LoadState.Loading)
        private set

    var characters by mutableStateOf<LoadState<List<CharacterSummary>>>(LoadState.Loading)
        private set

    var comments by mutableStateOf<LoadState<SubjectComments>>(LoadState.Loading)
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
                comments = loadState { loadComments(subjectId) }
            }
        }
    }
}
