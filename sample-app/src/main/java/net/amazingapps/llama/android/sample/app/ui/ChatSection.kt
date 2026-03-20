package net.amazingapps.llama.android.sample.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.amazingapps.llama.android.sample.app.MainUiState

@Composable
fun ChatSection(
    uiState: MainUiState,
    onUserInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = uiState.userInput,
            onValueChange = onUserInputChange,
            label = { Text("Enter your prompt") },
            modifier = Modifier.Companion.fillMaxWidth(),
            enabled = uiState.modelLoaded,
            trailingIcon = {
                IconButton(
                    onClick = onSendClick,
                    enabled = uiState.modelLoaded && uiState.userInput.isNotBlank()
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (uiState.modelLoaded && uiState.userInput.isNotBlank()) {
                        onSendClick()
                    }
                }
            )
        )

        Card(
            modifier = Modifier.Companion
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.Companion.padding(16.dp)) {
                Text(
                    text = "AI Response:",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.Companion.height(8.dp))
                Text(
                    text = uiState.aiResponse,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatSectionPreview() {
    MaterialTheme {
        ChatSection(
            uiState = MainUiState(modelLoaded = true, aiResponse = "Hello!"),
            onUserInputChange = {},
            onSendClick = {}
        )
    }
}
