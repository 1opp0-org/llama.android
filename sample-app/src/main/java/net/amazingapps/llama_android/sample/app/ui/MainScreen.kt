package net.amazingapps.llama_android.sample.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import net.amazingapps.llama_android.sample.app.MainUiState
import net.amazingapps.llama_android.sample.app.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    MainScreenContent(
        uiState = uiState,
        onDownloadClick = { viewModel.downloadAndLoadModel() },
        onUserInputChange = { input -> viewModel.updateUserInput(input) },
        onSendClick = { viewModel.sendPrompt() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: MainUiState,
    onDownloadClick: () -> Unit,
    onUserInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Llama Android") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Hello Llama.cpp",
                style = MaterialTheme.typography.headlineMedium
            )

            ModelStateSection(
                uiState = uiState,
                onDownloadClick = onDownloadClick
            )

            ChatSection(
                uiState = uiState,
                onUserInputChange = onUserInputChange,
                onSendClick = onSendClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreenContent(
            uiState = MainUiState(status = "Idle"),
            onDownloadClick = {},
            onUserInputChange = {},
            onSendClick = {}
        )
    }
}
