package dev.bangumi.demo.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bangumi.demo.ui.theme.AppDimensions

// 搜索筛选和索引页共用的选择控件。都是“从几个选项中选一个”：选项少时用一排筛选标签，选项多时用下拉菜单。
// <T> 是泛型参数：选项可以是枚举、数字或可空值，由调用方通过 label 决定每个选项显示什么文字。

/**
 * 一排筛选标签（单选），选中的标签带勾和底色。选项较多超出宽度时可以横向滚动。
 *
 * @param options 全部选项，按显示顺序。
 * @param selected 当前选中的选项。
 * @param label 选项显示的文字。
 * @param onSelect 点击某个选项时调用。
 */
@Composable
fun <T> OptionChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.smallSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

/**
 * 下拉选择：按钮上显示“名称：当前值 ▾”，点击后弹出选项列表，选中后关闭。
 * 适合年份这类选项很多的情况；菜单内容过长时可以滚动。
 *
 * @param title 选项的名称，例如“年份”。
 * @param enabled 为 false 时按钮不可点击（例如没有选年份时，月份不可选）。
 */
@Composable
fun <T> OptionDropdown(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
) {
    // 菜单是否展开只影响这个控件本身，用 remember 保存在界面中即可。
    var expanded by remember { mutableStateOf(false) }
    // Box 让菜单以按钮为锚点弹出在按钮下方。
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
            Text("$title：${label(selected)} ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
