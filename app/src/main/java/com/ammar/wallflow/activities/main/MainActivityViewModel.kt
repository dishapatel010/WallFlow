package com.ammar.wallflow.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.wallflow.data.preferences.Theme
import com.ammar.wallflow.data.repository.AppPreferencesRepository
import com.github.materiiapps.partial.Partialize
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {
    private val localUiState = MutableStateFlow(MainUiStatePartial())

    val uiState = combine(
        localUiState,
        appPreferencesRepository.appPreferencesFlow,
    ) { local, appPreferences ->
        local.merge(
            MainUiState(
                theme = appPreferences.lookAndFeelPreferences.theme,
                accentColor = appPreferences.lookAndFeelPreferences.accentColor,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(),
    )
}

@Partialize
data class MainUiState(
    val theme: Theme = Theme.SYSTEM,
    val accentColor: Int? = null,
)
