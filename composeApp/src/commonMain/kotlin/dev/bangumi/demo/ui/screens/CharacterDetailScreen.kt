package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bangumi.demo.model.CharacterAppearance
import dev.bangumi.demo.model.CharacterDetail
import dev.bangumi.demo.model.ProfileEntry
import dev.bangumi.demo.ui.components.ClickableCard
import dev.bangumi.demo.ui.components.DetailPage
import dev.bangumi.demo.ui.components.DetailSection
import dev.bangumi.demo.ui.components.LoadStateContent
import dev.bangumi.demo.ui.components.RemoteImage
import dev.bangumi.demo.ui.components.SecondaryText
import dev.bangumi.demo.ui.components.TagLabel
import dev.bangumi.demo.ui.components.formatCount
import dev.bangumi.demo.ui.state.CharacterDetailState
import dev.bangumi.demo.ui.state.LoadState
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 角色详情页：立绘、名字、收藏统计、基本资料、简介、声优、出演作品。
 * 点击出演作品会打开该作品的条目详情，因此可以在条目和角色之间来回浏览。
 *
 * @param onSubjectClick 点击出演作品时调用。
 */
@Composable
fun CharacterDetailScreen(
    state: CharacterDetailState,
    scrollState: ScrollState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSubjectClick: (CharacterAppearance) -> Unit,
) {
    DetailPage(scrollState = scrollState, onBack = onBack) {
        // 有中文名时以中文名为标题、原名为副标题；资料加载前先显示条目页带来的名字和立绘占位。
        val detail = (state.detail as? LoadState.Success)?.data

        // 头部：左侧立绘，右侧名字、收藏统计和基本资料。
        Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.largeSpacing)) {
            // 立绘多为竖长图，裁切时保留上方（TopCenter），避免把脸裁掉。
            RemoteImage(
                url = detail?.imageUrl,
                contentDescription = "角色图片",
                modifier = Modifier.size(AppDimensions.detailCoverWidth, AppDimensions.detailCoverHeight),
                shape = MaterialTheme.shapes.medium,
                alignment = Alignment.TopCenter,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
            ) {
                Text(
                    text = detail?.chineseName ?: detail?.name ?: state.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (detail?.chineseName != null) {
                    SecondaryText(detail.name)
                }
                if (detail != null) {
                    CharacterInfo(detail)
                }
            }
        }

        // 简介与头部资料来自同一个接口，一起显示加载中或失败。
        LoadStateContent(state = state.detail, onRetry = onRetry) { loaded ->
            DetailSection(title = "简介") {
                Text(
                    text = if (loaded.summary.isBlank()) "暂无简介" else loaded.summary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        DetailSection(title = "声优") {
            LoadStateContent(state = state.voiceActors, onRetry = onRetry) { names ->
                Text(
                    text = if (names.isEmpty()) "暂无声优信息" else names.joinToString("、"),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        DetailSection(title = "出演作品") {
            LoadStateContent(state = state.appearances, onRetry = onRetry) { appearances ->
                AppearanceList(appearances = appearances, onSubjectClick = onSubjectClick)
            }
        }
    }
}

/** 收藏统计、数据来源和基本资料，显示在立绘右侧。 */
@Composable
private fun CharacterInfo(detail: CharacterDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing)) {
        SecondaryText(
            "收藏 ${formatCount(detail.collects)} 人 · 吐槽 ${formatCount(detail.comments)} 条 · " +
                "数据来自 Bangumi 番组计划（bgm.tv），角色编号 ${detail.id}。",
        )
        if (detail.profile.isNotEmpty()) {
            ProfileTable(detail.profile)
        }
    }
}

/** 两列资料表：左列是项目名（固定宽度），右列是内容（过长时自动换行）。 */
@Composable
private fun ProfileTable(entries: List<ProfileEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        entries.forEach { entry ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing)) {
                Text(
                    text = entry.label,
                    modifier = Modifier.width(96.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = entry.value,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/** 出演作品列表；每项可点击，打开对应条目的详情。 */
@Composable
private fun AppearanceList(
    appearances: List<CharacterAppearance>,
    onSubjectClick: (CharacterAppearance) -> Unit,
) {
    if (appearances.isEmpty()) {
        SecondaryText("Bangumi 尚未登记出演作品。")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
        appearances.forEach { appearance ->
            ClickableCard(
                onClick = { onSubjectClick(appearance) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = AppDimensions.mediumSpacing,
                        vertical = AppDimensions.smallSpacing,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 作品名就在旁边，小封面是装饰性图片。
                    RemoteImage(
                        url = appearance.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.thumbnailCoverWidth, AppDimensions.thumbnailCoverHeight),
                    )
                    Text(
                        text = appearance.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    TagLabel(text = appearance.type)
                    if (appearance.relation.isNotBlank()) {
                        TagLabel(text = appearance.relation)
                    }
                }
            }
        }
    }
}
