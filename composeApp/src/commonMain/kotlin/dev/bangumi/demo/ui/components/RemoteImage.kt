package dev.bangumi.demo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * 网络图片：封面、头像、立绘等都用它显示，保证占位和裁切方式一致。
 *
 * 外层 Box 先画一块浅色底作为占位：图片加载中、加载失败或没有图片（url 为 null）时都显示这块底色，
 * 布局大小不会因为图片是否到达而跳动。AsyncImage（Coil）在后台下载和解码，完成后淡入覆盖底色。
 *
 * @param url 图片地址；null 表示没有图片，只显示占位底色。
 * @param contentDescription 给读屏软件的说明；图片旁边已有文字说明时传 null，表示装饰性图片。
 * @param modifier 由调用方决定大小（例如封面 64×90dp、头像 48dp）。
 * @param shape 裁切形状，例如圆角矩形或圆形（头像）。
 * @param alignment 裁切时保留哪一部分；角色立绘传 TopCenter，优先保留上方的脸部。
 */
@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    alignment: Alignment = Alignment.Center,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        // ContentScale.Crop：等比缩放填满区域，超出部分裁掉，不会把图片拉伸变形。
        // matchParentSize 是 Box 内可用的修饰符：图片与外层占位底色大小相同。
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = alignment,
        )
    }
}
