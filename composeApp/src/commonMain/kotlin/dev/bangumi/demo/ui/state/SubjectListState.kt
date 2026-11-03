package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.Subject
import kotlin.coroutines.cancellation.CancellationException

/**
 * 保存列表页的输入、搜索结果和选中条目，并处理事件；不负责布局，也不自己发网络请求。
 *
 * @param searchSubjects 真正执行搜索的函数。类型 suspend (String) -> List<Subject> 和 onClick 的 () -> Unit 一样是函数类型，
 *   只是多了 suspend（可挂起，见 BangumiApi.searchSubjects）。程序运行时传入 BangumiApi 的方法，
 *   测试时传入一段返回固定结果的 Lambda，因此不联网也能测试这里的状态变化。
 */
class SubjectListState(private val searchSubjects: suspend (String) -> List<Subject>) {

    // by 是属性委托：读写交给 Compose 状态；值变化时，读取它的界面会自动刷新。
    // private set：外部只能读，只能通过下面的方法修改，类似 Java 的公开 getter 与私有 setter。

    /** 输入框中的原文。 */
    var query by mutableStateOf("")
        private set

    /** 最近一次成功搜索的结果；null 表示还没有搜索过，空列表表示搜过但没有匹配。 */
    var results by mutableStateOf<List<Subject>?>(null)
        private set

    /** 是否正在等待网络响应；为 true 时界面显示加载提示，并忽略重复的搜索请求。 */
    var isLoading by mutableStateOf(false)
        private set

    /** 最近一次搜索失败的原因；null 表示没有错误。 */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** 当前查看详情的条目；null 表示显示列表。 */
    var selectedSubject by mutableStateOf<Subject?>(null)
        private set

    /** 接收输入框原文；只记录文字，不会立即发请求，按回车或点“搜索”时才调用 search。 */
    fun updateQuery(query: String) {
        this.query = query
    }

    /**
     * 用当前关键词搜索。suspend 函数，由界面在协程中调用（见 App 中的 scope.launch）。
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
            results = searchSubjects(keyword)
        } catch (e: CancellationException) {
            // 协程被取消（例如关闭窗口）不是搜索失败，不显示错误，继续向外抛出让协程正常结束。
            throw e
        } catch (e: Exception) {
            // 有些网络异常（例如域名无法解析）没有文字说明，这时显示异常类型名，方便判断原因。
            // ?: 是 Elvis 运算符：左边为 null 时取右边，类似 Java 的 x != null ? x : y。
            errorMessage = e.message ?: e::class.simpleName ?: "未知错误"
        } finally {
            // 无论成功、失败还是取消，都结束加载状态。
            isLoading = false
        }
    }

    /** 清空关键词并回到初始提示：同时清除结果和错误。 */
    fun clearQuery() {
        query = ""
        results = null
        errorMessage = null
    }

    /** 保存用户选择的条目，界面据此显示详情；关键词和搜索结果不变。 */
    fun selectSubject(subject: Subject) {
        selectedSubject = subject
    }

    /** 清除选中的条目，界面返回列表，原来的搜索结果仍然保留。 */
    fun clearSelectedSubject() {
        selectedSubject = null
    }
}
