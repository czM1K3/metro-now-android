package dev.metronow.android.features.infotext.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.metronow.android.features.infotext.data.InfoTextRepository
import dev.metronow.android.features.infotext.domain.InfoText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InfoTextsListViewModel (
    private val infoTextRepository: InfoTextRepository,
): ViewModel() {
    private val _screenStateStream = MutableStateFlow<InfoTextsListState>(InfoTextsListState.Loading)
    val screenStateStream get() = _screenStateStream.asStateFlow()

    init {
        viewModelScope.launch {
            val infoTexts = infoTextRepository.getInfoTexts()
            _screenStateStream.value = InfoTextsListState.Loaded(infoTexts)
        }
    }
}

sealed interface InfoTextsListState {

    data object Loading : InfoTextsListState

    data class Loaded(val infoTexts: List<InfoText>) : InfoTextsListState
}
