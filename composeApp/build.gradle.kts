plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            // 以下版本都以 Kotlin 2.2.x 构建；更新的版本需要 Kotlin 2.3 以上，升级 Kotlin 时再一起升级。
            // 协程：在后台执行网络请求而不卡住界面（launch 等函数来自这里）。
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            // HTTP 客户端的共享接口，在 commonMain 中发请求；具体收发由各平台的引擎完成。
            implementation("io.ktor:ktor-client-core:3.3.3")
            // JSON 文本与 Kotlin 对象互相转换，配合上面的 serialization 插件使用。
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            // 图片加载（Coil）：异步下载、解码、内存与磁盘缓存；network-ktor3 让它复用上面的 Ktor 下载图片。
            // 3.3.0 以 Kotlin 2.2.0 构建，依赖的 Compose 1.8.2、Ktor 3.1.0 都不高于本项目版本；3.4.0 起需要 Kotlin 2.3。
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
        }
        commonTest.dependencies {
            // 使用与项目 Kotlin 插件同版本的测试库；共享测试会在当前 desktop JVM 目标上运行。
            implementation(kotlin("test"))
            // runTest：在测试中调用 suspend 函数。
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                // 桌面平台的网络引擎，真正负责收发数据；类似 JDBC 接口之下的具体数据库驱动。
                implementation("io.ktor:ktor-client-cio:3.3.3")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.bangumi.demo.MainKt"
    }
}
