package com.example.gemini

import androidx.compose.material3.TextButton
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.gemini.ui.theme.GeminiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiTheme(darkTheme = true) {
                App()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(viewModel: ViewModel = ViewModel()) {
    var question by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = Modifier.padding(bottom = 8.dp),
        bottomBar = {
            Prompt(modifier = Modifier) {
                question = it
                focusManager.clearFocus()
                viewModel.getAnswer(prompt = it)
            }
        }
    ) { paddingValues ->
        val state: State by viewModel.uiState.collectAsState()
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .pointerInteropFilter(
                onTouchEvent = {
                    focusManager.clearFocus()
                    false
                }
            )
            .padding(paddingValues), contentAlignment = Alignment.BottomCenter
        ) {
            MainScreen(state = state, question = question) {
                viewModel.getAnswer(prompt = question)
            }
        }
    }
}

@Composable
fun MainScreen(state: State, question: String, retry: () -> Unit) {
    Chat(state = state, question = question) {
        retry()
    }
}
@Composable
fun Chat(state: State, question: String, retry: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        QuestionCard(question = question)
        when (state) {
            is State.Steady -> {}
            is State.Success -> AnswerCard(answer = state.output)
            is State.Error -> {
                ErrorState {
                    retry()
                }
            }
            is State.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ErrorState(retry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = {
                retry()
            }
        ) {
            Text("Retry")
        }
    }
}
@Composable
fun QuestionCard(question: String) {
    if (question.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_access_time_24),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center),
            )
        }
    } else {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        ) {
            Text(text = "Q. $question", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun AnswerCard(answer: String) {
    Card(modifier = Modifier
        .fillMaxWidth()
    ) {
        Text(text = answer, modifier = Modifier.padding(16.dp))
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Prompt(modifier: Modifier, prompt: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var text by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        value = text,
        onValueChange = {
            text = it
        },
        label = { Text("Prompt") },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        prompt(text)
                        text = ""
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
            keyboardController?.hide()
            focusManager.clearFocus()
            prompt(text)
            text = ""

        }),
        shape = RoundedCornerShape(40)
    )
}