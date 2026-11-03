package dev.bangumi.demo.data

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent

/**
 * 创建全局的图片加载器（Coil）。App 启动时通过 setSingletonImageLoaderFactory 注册一次，
 * 之后所有 AsyncImage 都用它下载、解码和缓存图片。
 *
 * - 下载使用 Ktor（与接口请求相同的网络库），并带上项目的 User-Agent。
 * - crossfade：图片加载完成时淡入，而不是突然出现。
 * - Coil 默认带内存缓存和磁盘缓存：同一张图第二次显示时不再重新下载。
 *
 * @param context Coil 的平台上下文；桌面上不需要特别处理，由 Coil 传入。
 */
fun createImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .components {
            add(
                KtorNetworkFetcherFactory(
                    httpClient = {
                        // install(UserAgent) 让这个客户端发出的每个请求都带上同一个 User-Agent。
                        HttpClient { install(UserAgent) { agent = BANGUMI_USER_AGENT } }
                    },
                ),
            )
        }
        .crossfade(true)
        .build()
