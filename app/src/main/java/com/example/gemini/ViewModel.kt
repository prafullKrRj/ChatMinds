package com.example.gemini

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemini.constants.Constants
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModel: ViewModel() {
    private val _uiState: MutableStateFlow<State> =
        MutableStateFlow(State.Steady)

    val uiState: StateFlow<State> = _uiState.asStateFlow()
    private val generativeModel = GenerativeModel (
        modelName = "gemini-pro",
        apiKey = Constants.API_KEY
    )
    fun getAnswer(prompt: String) {
        viewModelScope.launch {
            _uiState.value = State.Loading
            try {
                val output = generativeModel.generateContent(prompt)
                output.text?.let {
                    _uiState.value = State.Success(output = it)
                }
            } catch (e: Exception) {
                _uiState.value = State.Error
            }
        }
    }
}
sealed interface State {
    data class Success(val output: String): State
    data object Error: State
    data object Loading: State
    data object Steady: State
}