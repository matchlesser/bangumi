package dev.bangumi.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.bangumi.demo.model.Subject
import dev.bangumi.demo.ui.components.SubjectCard
import dev.bangumi.demo.ui.theme.AppDimensions

/**
 * 只根据传入数据绘制页面，把用户操作通过回调交给状态对象。
 * subjects 是已经筛选好的列表；query 保留输入框原文。
 * (String) -> Unit 类似 Java Consumer<String>，() -> Unit 类似 Runnable；Unit 表示不返回有用值。
 */
@Composable
fun SubjectListScreen(
    subjects: List<Subject>,
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    // Box 负责水平居中；内层 Column 限制内容宽度，并从上到下排列标题、输入框和结果。
    Box(
        modifier = Modifier.fillMaxSize().padding(AppDimensions.largeSpacing),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = AppDimensions.contentMaxWidth).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.largeSpacing),
        ) {
            // 数量来自当前结果，输入变化后会与列表同步更新。
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing)) {
                Text(
                    text = "Bangumi",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "番剧列表 · ${subjects.size} 部作品",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "离线示例数据：以下名称、评分与简介均为虚构，未连接 Bangumi。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 受控输入：value 来自状态，onValueChange 类似 Java 文本监听器，只上报新文本。
            // 筛选由状态层完成；输入框本身不获取数据，也不修改列表。
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("按名称筛选") },
                placeholder = { Text("例如：书店") },
                supportingText = { Text("支持名称片段，忽略首尾空格与英文大小写。") },
                // 这里接受界面 Lambda，因此可以在输入框右侧放文字按钮，而不必使用图标。
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        TextButton(onClick = onClearQuery) {
                            Text("清空")
                        }
                    }
                },
            )

            // 同一状态只显示空提示或列表之一；weight(1f) 让结果区占据标题和搜索框之外的空间。
            if (subjects.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        space = AppDimensions.mediumSpacing,
                        alignment = Alignment.CenterVertically,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "没有找到匹配的番剧",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "换个关键词试试，或清空后查看全部示例。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    // 空状态中的恢复入口和输入框中的清空按钮使用同一个事件。
                    OutlinedButton(onClick = onClearQuery) {
                        Text("清空关键词")
                    }
                }
            } else {
                // LazyColumn 按需绘制可见条目；结果变少时仍保持原有卡片样式。
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumSpacing),
                    contentPadding = PaddingValues(bottom = AppDimensions.smallSpacing),
                ) {
                    // 稳定 id 用于识别条目，避免筛选改变位置后把其他条目当成原来的条目。
                    items(items = subjects, key = { subject -> subject.id }) { subject ->
                        SubjectCard(subject = subject)
                    }
                }
            }
        }
    }
}
