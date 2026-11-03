package dev.bangumi.demo.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 测试共用解析工具中的图片地址处理。 */
class BangumiJsonTest {

    // 原图地址插入 /r/宽度/ 变成缩略图地址，与接口在 images 中返回的格式一致。
    @Test
    fun convertsOriginalImageUrlToResizedUrl() {
        assertEquals(
            "https://lain.bgm.tv/r/100/pic/cover/l/77/bd/217844_g7bSF.jpg",
            resizedImageUrl("https://lain.bgm.tv/pic/cover/l/77/bd/217844_g7bSF.jpg", width = 100),
        )
    }

    // 已经是缩略图或不是 lain.bgm.tv 原图格式的地址原样返回；空地址为 null。
    @Test
    fun keepsOtherUrlsAndTreatsBlankAsMissing() {
        val alreadyResized = "https://lain.bgm.tv/r/200/pic/cover/l/e2/e7/328609_2EHLJ.jpg"
        val otherHost = "https://example.com/pic/a.jpg"

        assertEquals(alreadyResized, resizedImageUrl(alreadyResized, width = 100))
        assertEquals(otherHost, resizedImageUrl(otherHost, width = 100))
        assertNull(resizedImageUrl("", width = 100))
        assertNull(resizedImageUrl(null, width = 100))
        assertNull(imageUrl("  "))
    }
}
