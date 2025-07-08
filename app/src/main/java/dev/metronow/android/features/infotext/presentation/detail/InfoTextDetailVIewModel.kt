package dev.metronow.android.features.infotext.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.metronow.android.core.presentation.ui.Screens
import dev.metronow.android.features.infotext.data.InfoTextRepository
import dev.metronow.android.features.infotext.domain.InfoText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoTextDetailVIewModel(
    private val savedStateHandle: SavedStateHandle,
    private val infoTextRepository: InfoTextRepository,
) : ViewModel() {

    private val _screenStateStream = MutableStateFlow(InfoTextScreenState())
    val screenStateStream get() = _screenStateStream.asStateFlow()

   init {
       viewModelScope.launch {
           val infoTextId = savedStateHandle.toRoute<Screens.InfoText>().id
           val infoText = infoTextRepository.getInfoText(infoTextId)
           _screenStateStream.update { it.copy(infoText = infoText) }
       }
   }

}

data class InfoTextScreenState(val infoText: InfoText? = null)
