package dev.bangumi.demo.ui.state

import dev.bangumi.demo.model.CharacterAppearance
import dev.bangumi.demo.model.CharacterDetail
import dev.bangumi.demo.model.CharacterSummary
import dev.bangumi.demo.model.SubjectCollection
import dev.bangumi.demo.model.SubjectComment
import dev.bangumi.demo.model.SubjectComments
import dev.bangumi.demo.model.SubjectDetail
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测试详情页状态：三块数据各自加载，一块失败不影响其他；重试只请求失败的部分；取消不算失败。
 * 传入 Lambda 代替真实网络，所以不联网。
 */
class DetailStateTest {

    private val detail = SubjectDetail(
        id = 328609,
        name = "孤独摇滚！",
        originalName = "ぼっち・ざ・ろっく！",
        summary = "简介",
        score = 8.4,
        rank = 73,
        ratingTotal = 10,
        ratingCounts = listOf(0, 0, 0, 0, 0, 0, 0, 4, 3, 3),
        date = "2022-10-08",
        platform = "TV",
        episodes = 12,
        collection = SubjectCollection(wish = 1, collect = 2, doing = 3, onHold = 4, dropped = 5),
    )
    private val characters = listOf(CharacterSummary(id = 87968, name = "後藤ひとり", relation = "主角", voiceActor = "青山吉能"))
    private val comments = SubjectComments(
        total = 1,
        items = listOf(SubjectComment(id = 1, userName = "用户", status = "看过", rate = 9, text = "好看", updatedAt = 0)),
    )

    @Test
    fun allSectionsStartLoadingAndSucceedTogether() = runTest {
        val state = SubjectDetailState(
            subjectId = 328609,
            title = "孤独摇滚！",
            loadSubject = { id -> assertEquals(328609, id); detail },
            loadCharacters = { characters },
            loadComments = { comments },
        )
        assertEquals(LoadState.Loading, state.detail)

        state.load()

        assertEquals(LoadState.Success(detail), state.detail)
        assertEquals(LoadState.Success(characters), state.characters)
        assertEquals(LoadState.Success(comments), state.comments)
    }

    // 吐槽接口失败时，基本信息和角色照常成功；重试只重新请求吐槽。
    @Test
    fun failedSectionDoesNotAffectOthersAndRetryOnlyReloadsIt() = runTest {
        var subjectCalls = 0
        var commentCalls = 0
        val state = SubjectDetailState(
            subjectId = 1,
            title = "标题",
            loadSubject = { subjectCalls++; detail },
            loadCharacters = { characters },
            loadComments = {
                commentCalls++
                if (commentCalls == 1) throw IllegalStateException("Bangumi 返回 HTTP 502")
                comments
            },
        )

        state.load()

        assertEquals(LoadState.Success(detail), state.detail)
        assertEquals(LoadState.Success(characters), state.characters)
        assertEquals(LoadState.Error("Bangumi 返回 HTTP 502"), state.comments)

        state.load()

        assertEquals(LoadState.Success(comments), state.comments)
        assertEquals(1, subjectCalls)
        assertEquals(2, commentCalls)
    }

    // 加载被取消（例如中途离开页面）时保持“加载中”而不是显示错误；再次进入页面时补上。
    @Test
    fun cancelledSectionStaysLoadingAndLoadsNextTime() = runTest {
        var cancelFirst = true
        val state = SubjectDetailState(
            subjectId = 1,
            title = "标题",
            loadSubject = { detail },
            loadCharacters = {
                if (cancelFirst) throw CancellationException("离开页面")
                characters
            },
            loadComments = { comments },
        )

        state.load()

        assertEquals(LoadState.Loading, state.characters)
        assertEquals(LoadState.Success(detail), state.detail)

        cancelFirst = false
        state.load()

        assertEquals(LoadState.Success(characters), state.characters)
    }

    // 角色详情页的三块数据同样各自加载；异常没有文字说明时显示类型名。
    @Test
    fun characterSectionsLoadIndependently() = runTest {
        val character = CharacterDetail(
            id = 87968,
            name = "後藤ひとり",
            chineseName = "后藤一里",
            summary = "",
            profile = emptyList(),
            collects = 0,
            comments = 0,
        )
        val appearances = listOf(CharacterAppearance(subjectId = 328609, name = "孤独摇滚！", type = "动画", relation = "主角"))
        val state = CharacterDetailState(
            characterId = 87968,
            title = "後藤ひとり",
            loadCharacter = { character },
            loadAppearances = { appearances },
            loadVoiceActors = { throw UnsupportedOperationException() },
        )

        state.load()

        assertEquals(LoadState.Success(character), state.detail)
        assertEquals(LoadState.Success(appearances), state.appearances)
        assertEquals(LoadState.Error("UnsupportedOperationException"), state.voiceActors)
    }
}
