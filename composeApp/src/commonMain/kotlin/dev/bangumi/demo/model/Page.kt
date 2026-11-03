package dev.bangumi.demo.model

/**
 * 分页数据：目前已取得的条目 items，以及服务器上的总数 total。
 * 搜索结果和吐槽都按页（每页 20 条）请求，点击“加载更多”时把下一页拼接到 items 后面。
 *
 * <T> 是泛型参数，与 Java 的 Page<T> 写法相同：Page<Subject> 是搜索结果，Page<SubjectComment> 是吐槽。
 */
data class Page<T>(
    val items: List<T>,
    val total: Int,
) {
    /** 是否还有没取回的条目。 */
    val hasMore: Boolean get() = items.size < total

    /**
     * 把下一页拼接到当前页后面，返回新的 Page（原对象不变）。
     *
     * - 按 idOf 去重：两次请求之间数据可能变化（例如有人发了新吐槽，后面的条目整体后移），
     *   同一条目可能在两页都出现。列表以 id 作为每一行的 key，重复的 key 会导致界面出错，所以必须去重。
     * - 下一页为空时说明已经取完，把 total 改为实际数量，避免“加载更多”一直显示。
     *
     * @param idOf 取出条目唯一编号的函数，例如 { it.id }。
     */
    fun appended(next: Page<T>, idOf: (T) -> Any): Page<T> {
        val merged = (items + next.items).distinctBy(idOf)
        val newTotal = if (next.items.isEmpty()) merged.size else maxOf(next.total, merged.size)
        return Page(items = merged, total = newTotal)
    }
}
