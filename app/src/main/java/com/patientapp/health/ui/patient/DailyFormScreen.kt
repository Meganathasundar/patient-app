package com.patientapp.health.ui.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyFormScreen(
    uiState: PatientUiState,
    onSubmit: (temperature: Double, symptoms: String, painLevel: Int) -> Unit,
    onClearSubmitState: () -> Unit,
    onSignOut: () -> Unit
) {
    var temperature by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var painLevel by remember { mutableFloatStateOf(5f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Health Form") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Sign out")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.submitSuccess) {
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Form submitted successfully.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                TextButton(onClick = onClearSubmitState) {
                    Text("Dismiss")
                }
            }
            if (uiState.submitError != null) {
                Card(
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.submitError,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Please fill out your daily health form",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = temperature,
                        onValueChange = { temperature = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Body temperature (°C)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmitting
                    )
                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        label = { Text("Symptoms description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmitting
                    )
                    Column {
                        Text(
                            text = "Current pain level: ${painLevel.toInt()}/10",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = painLevel,
                            onValueChange = { painLevel = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )
                    }
                    Button(
                        onClick = {
                            val temp = temperature.toDoubleOrNull() ?: 0.0
                            if (temp in 30.0..45.0) {
                                onSubmit(temp, symptoms, painLevel.toInt())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmitting
                    ) {
                        Text(if (uiState.isSubmitting) "Submitting…" else "Submit")
                    }
                }
            }
        }
    }
}
