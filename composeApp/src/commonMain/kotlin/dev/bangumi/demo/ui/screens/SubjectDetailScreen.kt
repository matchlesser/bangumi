package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bangumi.demo.model.CharacterSummary
import dev.bangumi.demo.model.Page
import dev.bangumi.demo.model.SubjectComment
import dev.bangumi.demo.model.SubjectDetail
import dev.bangumi.demo.ui.components.ClickableCard
import dev.bangumi.demo.ui.components.DetailPage
import dev.bangumi.demo.ui.components.DetailSection
import dev.bangumi.demo.ui.components.LoadMoreFooter
import dev.bangumi.demo.ui.components.LoadStateContent
import dev.bangumi.demo.ui.components.RatingDistribution
import dev.bangumi.demo.ui.components.RemoteImage
import dev.bangumi.demo.ui.components.ScoreLabel
import dev.bangumi.demo.ui.components.SecondaryText
import dev.bangumi.demo.ui.components.TagLabel
import dev.bangumi.demo.ui.components.currentEpochSeconds
import dev.bangumi.demo.ui.components.formatCount
import dev.bangumi.demo.ui.components.formatRelativeTime
import dev.bangumi.demo.ui.state.LoadState
import dev.bangumi.demo.ui.state.SubjectDetailState
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 条目详情页：标题与基本信息、评分分布、简介、主要人物、吐槽。
 *
 * 页面只读取 state 中的数据并上报事件；什么时候加载、怎样重试由 App 和状态对象负责。
 * 三块数据（基本信息、角色、吐槽）各自显示加载中或失败，一块失败不影响其他部分。
 *
 * @param scrollState 滚动位置，由返回栈中的页面对象保存。
 * @param onRetry 某一块加载失败后点“重试”时调用；只会重新请求失败的部分。
 * @param onLoadMoreComments 点击吐槽底部的“加载更多”或其“重试”时调用。
 * @param onCharacterClick 点击角色卡片时调用，打开角色详情。
 */
@Composable
fun SubjectDetailScreen(
    state: SubjectDetailState,
    scrollState: ScrollState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onLoadMoreComments: () -> Unit,
    onCharacterClick: (CharacterSummary) -> Unit,
) {
    DetailPage(scrollState = scrollState, onBack = onBack) {
        // 详情加载完成前先显示上一页带来的标题和封面占位；as? 在类型不符时得到 null，而不是抛出异常。
        val detail = (state.detail as? LoadState.Success)?.data

        // 头部：左侧封面，右侧标题、原名和基本信息。
        Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.largeSpacing)) {
            RemoteImage(
                url = detail?.imageUrl,
                contentDescription = "封面",
                modifier = Modifier.size(AppDimensions.detailCoverWidth, AppDimensions.detailCoverHeight),
                shape = MaterialTheme.shapes.medium,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
            ) {
                Text(
                    text = detail?.name ?: state.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (detail != null && detail.originalName != detail.name) {
                    SecondaryText(detail.originalName)
                }
                if (detail != null) {
                    SubjectInfo(detail)
                }
            }
        }

        // 评分分布和简介与头部信息来自同一个接口，一起显示加载中或失败。
        LoadStateContent(state = state.detail, onRetry = onRetry) { loaded ->
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.largeSpacing)) {
                DetailSection(title = "评分分布") {
                    if (loaded.ratingTotal == 0) {
                        SecondaryText("还没有人评分。")
                    } else {
                        RatingDistribution(counts = loaded.ratingCounts, modifier = Modifier.fillMaxWidth())
                    }
                }
                DetailSection(title = "简介") {
                    Text(
                        text = if (loaded.summary.isBlank()) "暂无简介" else loaded.summary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        DetailSection(title = "主要人物") {
            LoadStateContent(state = state.characters, onRetry = onRetry) { characters ->
                CharacterList(characters = characters, onCharacterClick = onCharacterClick)
            }
        }

        DetailSection(title = "吐槽") {
            LoadStateContent(state = state.comments, onRetry = onRetry) { comments ->
                CommentList(comments = comments)
                if (comments.items.isNotEmpty()) {
                    LoadMoreFooter(
                        shown = comments.items.size,
                        total = comments.total,
                        unit = "条",
                        isLoading = state.isLoadingMoreComments,
                        error = state.moreCommentsError,
                        onLoadMore = onLoadMoreComments,
                    )
                }
            }
        }
    }
}

/** 评分、排名、放送信息、收藏人数和数据来源。 */
@Composable
private fun SubjectInfo(detail: SubjectDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreLabel(score = detail.score)
            if (detail.rank != null) {
                Text(text = "Bangumi 排名 #${detail.rank}", style = MaterialTheme.typography.bodyMedium)
            }
            if (detail.ratingTotal > 0) {
                SecondaryText("${formatCount(detail.ratingTotal)} 人评分")
            }
        }
        // listOfNotNull 只保留非 null 的项，缺少的信息不会留下多余的分隔符。
        val meta = listOfNotNull(detail.platform, detail.date, detail.episodes?.let { "$it 话" })
        if (meta.isNotEmpty()) {
            Text(text = meta.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
        }
        val collection = detail.collection
        SecondaryText(
            "看过 ${formatCount(collection.collect)} · 在看 ${formatCount(collection.doing)} · " +
                "想看 ${formatCount(collection.wish)} · 搁置 ${formatCount(collection.onHold)} · " +
                "抛弃 ${formatCount(collection.dropped)}",
        )
        SecondaryText("数据来自 Bangumi 番组计划（bgm.tv），条目编号 ${detail.id}。")
    }
}

/**
 * 角色列表，两列排列。默认只显示主角和配角；其余角色（客串、闲角等）点击按钮后展开。
 * 条目没有标注主角或配角时，直接显示全部角色。
 */
@Composable
private fun CharacterList(
    characters: List<CharacterSummary>,
    onCharacterClick: (CharacterSummary) -> Unit,
) {
    if (characters.isEmpty()) {
        SecondaryText("Bangumi 尚未登记角色。")
        return
    }
    // 是否展开全部角色只影响本区块的显示，用 remember 保存在界面中即可。
    var showAll by remember { mutableStateOf(false) }
    val mainCharacters = characters.filter { it.isMain }
    val visible = if (showAll || mainCharacters.isEmpty()) characters else mainCharacters

    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
        // chunked(2) 每两个角色分成一行；IntrinsicSize.Min 让同一行的两张卡片高度一致。
        visible.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
            ) {
                row.forEach { character ->
                    CharacterCard(
                        character = character,
                        onClick = { onCharacterClick(character) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                // 最后一行只有一个角色时，用空白占住右半边，保持卡片宽度一致。
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        if (mainCharacters.isNotEmpty() && mainCharacters.size < characters.size) {
            TextButton(onClick = { showAll = !showAll }) {
                Text(if (showAll) "只看主要人物" else "显示全部 ${characters.size} 位角色")
            }
        }
    }
}

/** 一个角色：圆形头像、名字、在本作中的定位、声优。 */
@Composable
private fun CharacterCard(
    character: CharacterSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ClickableCard(onClick = onClick, modifier = modifier) {
        Row(
            modifier = Modifier.padding(AppDimensions.mediumSpacing),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 名字就在旁边，头像是装饰性图片。CircleShape 把方形的脸部裁切图剪成圆形。
            RemoteImage(
                url = character.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(AppDimensions.avatarSize),
                shape = CircleShape,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = character.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (character.relation.isNotBlank()) {
                        TagLabel(text = character.relation)
                    }
                }
                SecondaryText(character.voiceActor?.let { "CV：$it" } ?: "暂无声优信息")
            }
        }
    }
}

/** 吐槽列表（按时间从新到旧）：用户、收藏状态、打分、相对时间和内容。 */
@Composable
private fun CommentList(comments: Page<SubjectComment>) {
    if (comments.items.isEmpty()) {
        SecondaryText("还没有人吐槽。")
        return
    }
    // “现在”在本区块第一次显示时读取一次；remember 避免每次重组都重新读取系统时间。
    val now = remember { currentEpochSeconds() }

    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
        SecondaryText("共 ${formatCount(comments.total)} 条，按时间从新到旧排列。")
        comments.items.forEach { comment ->
            HorizontalDivider()
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RemoteImage(
                        url = comment.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.smallAvatarSize),
                        shape = CircleShape,
                    )
                    Text(text = comment.userName, style = MaterialTheme.typography.titleSmall)
                    TagLabel(text = comment.status)
                    if (comment.rate != null) {
                        TagLabel(text = "${comment.rate} 分")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    SecondaryText(formatRelativeTime(comment.updatedAt, now))
                }
                if (comment.text.isNotBlank()) {
                    Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
