package dev.bangumi.demo.ui.state

import kotlin.coroutines.cancellation.CancellationException

/**
 * 一块需要从网络加载的数据当前处于什么阶段：加载中、成功（带数据）或失败（带原因）。
 *
 * sealed interface（密封接口）表示“只有这里列出的几种情况”：在 when 中把三种都处理完，
 * 编译器就知道没有遗漏，不需要 else。类比 Java 17 的 sealed interface + record。
 * out T 表示 LoadState<Nothing> 可以当作任意 LoadState<T> 使用，所以 Loading 和 Error 不需要类型参数。
 */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

/**
 * 执行一次加载并把结果包装成 LoadState：成功得到 Success，出错得到 Error。
 * 协程被取消（例如离开页面）不算失败，CancellationException 继续向外抛出。
 */
suspend fun <T> loadState(block: suspend () -> T): LoadState<T> =
    try {
        LoadState.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        LoadState.Error(errorMessageOf(e))
    }

/**
 * 把异常转成显示给用户的原因文字。
 * 有些网络异常（例如域名无法解析）没有文字说明，这时显示异常类型名，方便判断原因。
 * ?: 是 Elvis 运算符：左边为 null 时取右边，类似 Java 的 x != null ? x : y。
 */
fun errorMessageOf(e: Exception): String = e.message ?: e::class.simpleName ?: "未知错误"
