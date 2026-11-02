package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.Subject

/**
 * 保存列表页的输入与选中条目并处理事件，不负责布局或获取数据。
 * 构造参数中的 private val 相当于 Java 的私有 final 字段及其构造赋值。
 * 当前数据源是固定的离线列表；筛选不会修改它。
 * query 决定筛选结果；selectedSubject 决定显示列表页还是详情页，两者互不影响。
 */
class SubjectListState(private val subjects: List<Subject>) {

    // by 是属性委托：把左边属性的读写交给右边的 Compose 状态；query 变更时，读取它的界面会刷新。
    var query by mutableStateOf("")
        // 外部可以读取，但只能通过下面的方法修改，类似 Java 的公开 getter 与私有 setter。
        private set

    // 选中条目，null 表示没有选中、显示列表；Compose 会在其变更时刷新界面。
    // 初始值是 null，必须写明 <Subject?>，否则类型会被推断为只能存 null 的 Nothing?。
    var selectedSubject by mutableStateOf<Subject?>(null)
        private set

    // 结果始终从完整列表计算，避免连续输入时丢失已经被筛掉的条目。
    val visibleSubjects: List<Subject>
        // 自定义 getter：每次读取时计算结果，类似 Java 的 getVisibleSubjects()。
        get() = filterSubjectsByName(subjects, query)

    /** 接收输入框原文；this.query 是属性，右侧 query 是方法参数，与 Java 的 this 用法相同。 */
    fun updateQuery(query: String) {
        this.query = query
    }

    /**
     * 保存用户选择的条目；读取该状态的界面可以据此显示对应详情。
     * 参数类型是非空的 Subject，调用方无法传入 null；“返回列表”使用单独的 clearSelectedSubject。
     */
    fun selectSubject(subject: Subject) {
        selectedSubject = subject
    }

    /** 清空筛选关键词并恢复完整列表，不改变当前选中的条目。 */
    fun clearQuery() {
        updateQuery("")
    }

    /** 清除选中的条目，让界面返回到列表，同时原来的筛选结果仍然保留。 */
    fun clearSelectedSubject() {
        selectedSubject = null
    }
}

/**
 * 纯筛选函数：相同输入得到相同输出，不修改原列表，也不依赖窗口。
 * 只匹配名称片段，忽略首尾空白和英文大小写；空关键词表示显示全部。
 * 独立于界面后，可以像测试 Java 的普通工具方法一样直接验证边界情况。
 */
fun filterSubjectsByName(subjects: List<Subject>, query: String): List<Subject> {
    // 只整理用于匹配的文字，保留输入框中的原文，避免干扰输入过程。
    val keyword = query.trim()
    if (keyword.isEmpty()) return subjects

    // filter 类似 Java Stream.filter(...).toList()；Lambda 返回 true 的条目按原顺序保留。
    return subjects.filter { subject ->
        subject.name.contains(keyword, ignoreCase = true)
    }
}
