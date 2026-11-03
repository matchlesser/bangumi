import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

// 应用版本号只在这里维护：安装包版本和代码中的 BuildInfo.VERSION（User-Agent、界面显示）都来自它。
// MSI 要求“主版本.次版本.修订号”三段数字，前两段不超过 255。
val appVersion = "1.1.0"

// 生成 BuildInfo.kt，让共享代码能读取版本号；类似 Android 项目中自动生成的 BuildConfig。
// 文件写在 build 目录下（不提交到 Git），每次构建按需重新生成。
val generateBuildInfo by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/buildInfo/kotlin")
    val version = appVersion
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("dev/bangumi/demo/BuildInfo.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package dev.bangumi.demo
            |
            |/** 由 composeApp/build.gradle.kts 中的 generateBuildInfo 任务生成，请勿手动修改。 */
            |object BuildInfo {
            |    /** 应用版本号，与安装包版本一致。 */
            |    const val VERSION = "$version"
            |}
            |
            """.trimMargin(),
        )
    }
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    jvmToolchain(17)

    sourceSets {
        commonMain {
            // 把生成的 BuildInfo.kt 加入共享代码；传入任务本身，Gradle 会在编译前先运行它。
            kotlin.srcDir(generateBuildInfo)
        }
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

        // 安装包配置：用 JDK 自带的 jpackage 生成 Windows 安装程序（MSI）。
        // 打包命令：.\gradlew.bat :composeApp:packageMsi，产物在 composeApp/build/compose/binaries/main/msi/。
        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            // 名称、说明、厂商使用英文：MSI 工具（WiX）默认的代码页不支持中文，写中文会导致打包失败。
            packageName = "Bangumi"
            packageVersion = appVersion
            description = "Bangumi anime search (learning project)"
            vendor = "matchlesser"

            // 安装包内置一个精简的 Java 运行环境，只包含程序用到的模块，用户电脑上不需要预先安装 Java。
            // 前三个来自 suggestRuntimeModules 任务的分析结果；jdk.crypto.ec 提供 HTTPS 握手所需的椭圆曲线算法，
            // 静态分析发现不了它，但缺少时连接 api.bgm.tv 会失败。
            modules("java.instrument", "java.management", "jdk.unsupported", "jdk.crypto.ec")

            windows {
                // 安装到当前用户目录，不需要管理员权限，安装时不弹出 UAC 提示。
                perUserInstall = true
                // 允许用户在安装时选择目录。
                dirChooser = true
                // 开始菜单中的“Bangumi”文件夹和桌面快捷方式。
                menu = true
                menuGroup = "Bangumi"
                shortcut = true
                // 固定的升级编号：以后安装新版本时会替换旧版本，而不是并存两份。生成后不要修改。
                upgradeUuid = "6DD1F889-527A-4CAA-8803-F89C711C905C"
                // 图标由 tools/MakeIcon.java 生成。
                iconFile.set(project.file("icons/app.ico"))
            }
        }
    }
}
