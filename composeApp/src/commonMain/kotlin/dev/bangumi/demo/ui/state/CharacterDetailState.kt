package dev.bangumi.demo.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bangumi.demo.model.CharacterAppearance
import dev.bangumi.demo.model.CharacterDetail
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 角色详情页的状态：资料、出演作品、声优三块数据各自加载，互不影响。
 * 加载规则与 SubjectDetailState.load 相同。
 *
 * @param characterId 角色编号。
 * @param title 资料加载完成前先显示的标题（来自条目页的角色名）。
 */
class CharacterDetailState(
    val characterId: Int,
    val title: String,
    private val loadCharacter: suspend (Int) -> CharacterDetail,
    private val loadAppearances: suspend (Int) -> List<CharacterAppearance>,
    private val loadVoiceActors: suspend (Int) -> List<String>,
) {
    var detail by mutableStateOf<LoadState<CharacterDetail>>(LoadState.Loading)
        private set

    var appearances by mutableStateOf<LoadState<List<CharacterAppearance>>>(LoadState.Loading)
        private set

    var voiceActors by mutableStateOf<LoadState<List<String>>>(LoadState.Loading)
        private set

    /** 加载所有尚未成功的部分；进入页面和点“重试”时调用。 */
    suspend fun load(): Unit = coroutineScope {
        if (detail !is LoadState.Success) {
            launch {
                detail = LoadState.Loading
                detail = loadState { loadCharacter(characterId) }
            }
        }
        if (appearances !is LoadState.Success) {
            launch {
                appearances = LoadState.Loading
                appearances = loadState { loadAppearances(characterId) }
            }
        }
        if (voiceActors !is LoadState.Success) {
            launch {
                voiceActors = LoadState.Loading
                voiceActors = loadState { loadVoiceActors(characterId) }
            }
        }
    }
}
