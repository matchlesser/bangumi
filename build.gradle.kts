plugins {
    kotlin("multiplatform") version "2.2.21" apply false
    id("org.jetbrains.compose") version "1.9.3" apply false
    // Compose 编译器与 Kotlin 使用相同版本。
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21" apply false
    // JSON 序列化插件：编译时为 @Serializable 类生成转换代码，版本同样必须与 Kotlin 相同。
    kotlin("plugin.serialization") version "2.2.21" apply false
}
