package net.amazingapps.llama_android.sample.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.amazingapps.llama_android.sample.app.MainUiState

@Composable
fun ModelStateSection(
    uiState: MainUiState,
    onDownloadClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Status: ${uiState.status}",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onDownloadClick,
            enabled = !uiState.isDownloading && !uiState.modelLoaded,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            val buttonText = when {
                uiState.isDownloading -> "Downloading..."
                uiState.modelLoaded -> "Model Ready"
                else -> "Download & Load Model"
            }
            Text(buttonText)
        }

        // Placeholder for Models on Disk list
        Card(
            modifier = Modifier.Companion.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.Companion.padding(16.dp)) {
                Text(
                    text = "Models on Disk:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.Companion.height(8.dp))
                Text(
                    text = "No models found yet.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModelStateSectionPreview() {
    MaterialTheme {
        ModelStateSection(
            uiState = MainUiState(status = "Idle"),
            onDownloadClick = {}
        )
    }
}
