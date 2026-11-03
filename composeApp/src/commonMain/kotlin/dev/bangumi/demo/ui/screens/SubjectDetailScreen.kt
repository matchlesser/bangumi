package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.components.ScoreLabel
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 显示一部番剧的详情，并通过回调报告返回操作。
 *
 * 与列表页一样只负责绘制：不保存“当前选中谁”，也不自己切换页面。
 * 点击返回时只调用 onBack，由 App 交给状态对象处理，因此关键词和筛选结果不会因进入详情而丢失。
 *
 * @param subject 当前需要显示的番剧，由调用方保证非空。
 * @param onBack 用户点击返回按钮时执行的操作，类似 Java 中传入的 Runnable 监听器。
 */
@Composable
fun SubjectDetailScreen(
    subject: Subject,
    onBack: () -> Unit,
) {
    // 外层与列表页使用相同的边距和最大内容宽度，切换页面时内容位置保持一致。
    // verticalScroll 让简介较长或窗口较矮时可以用鼠标滚轮滚动；
    // rememberScrollState 在重组之间记住滚动位置，作用类似 remember 记住状态对象。
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppDimensions.largeSpacing),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = AppDimensions.contentMaxWidth).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.largeSpacing),
        ) {
            // 文字按钮可用鼠标点击，也可用 Tab 聚焦后按 Enter 返回；未引入图标库，因此用文字箭头。
            TextButton(onClick = onBack) {
                Text("← 返回列表")
            }

            // 标题区：名称、评分，以及数据来源说明和 Bangumi 条目编号。
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.headlineMedium,
                )
                // 与列表卡片共用评分组件，“暂无评分”等规则只维护一处。
                ScoreLabel(score = subject.score)
                Text(
                    text = "数据来自 Bangumi 番组计划（bgm.tv），条目编号 ${subject.id}。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 简介区：列表卡片只显示两行预览，这里显示全文；没有简介时明确提示。
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
                Text(
                    text = "简介",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (subject.summary.isBlank()) "暂无简介" else subject.summary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
