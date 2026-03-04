package com.patientapp.health.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.patientapp.health.data.UserRole

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onSignUp: (email: String, password: String, role: UserRole, displayName: String?) -> Unit,
    onSendPhoneCode: (phoneNumber: String, activity: Activity) -> Unit,
    onSignUpWithPhoneCode: (verificationId: String, code: String, role: UserRole, displayName: String?) -> Unit,
    onSignUpWithPhoneCredential: (role: UserRole, displayName: String?) -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearError: () -> Unit,
    onClearPhoneState: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.DOCTOR) }
    val context = LocalContext.current
    val activity = context as? Activity

    val isEmail = emailOrPhone.contains("@")
    val phoneCodeSent = uiState.phoneVerificationId != null
    val phoneInstantReady = uiState.phoneInstantCredentialReady

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create account",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("I am registering as", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = role == UserRole.DOCTOR,
                        onClick = { role = UserRole.DOCTOR }
                    )
                    Text("Doctor")
                    Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                    RadioButton(
                        selected = role == UserRole.PATIENT,
                        onClick = { role = UserRole.PATIENT }
                    )
                    Text("Patient (must be added by doctor first)")
                }
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("Email or phone number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = if (isEmail) KeyboardType.Email else KeyboardType.Phone)
                )
                if (isEmail) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )
                } else {
                    if (!phoneCodeSent && !phoneInstantReady) {
                        Button(
                            onClick = {
                                if (activity != null) onSendPhoneCode(emailOrPhone.trim(), activity)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading && emailOrPhone.isNotBlank()
                        ) {
                            Text(if (uiState.isLoading) "Sending…" else "Send verification code")
                        }
                    }
                    if (phoneCodeSent) {
                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = { verificationCode = it },
                            label = { Text("Verification code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                if (role == UserRole.DOCTOR) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display name (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )
                }
                val canRegisterWithEmail = isEmail && emailOrPhone.isNotBlank() && password.isNotBlank()
                val canRegisterWithPhone = !isEmail && (phoneCodeSent && verificationCode.isNotBlank() || phoneInstantReady)
                Button(
                    onClick = {
                        when {
                            isEmail -> onSignUp(
                                emailOrPhone,
                                password,
                                role,
                                displayName.ifBlank { null }
                            )
                            phoneInstantReady -> onSignUpWithPhoneCredential(role, displayName.ifBlank { null })
                            else -> uiState.phoneVerificationId?.let { id ->
                                onSignUpWithPhoneCode(id, verificationCode, role, displayName.ifBlank { null })
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && (canRegisterWithEmail || canRegisterWithPhone)
                ) {
                    Text(if (uiState.isLoading) "Creating…" else "Register")
                }
                TextButton(onClick = {
                    onClearPhoneState()
                    onNavigateToLogin()
                }) {
                    Text("Already have an account? Sign in")
                }
            }
        }
    }
}
