package com.patientapp.health.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.patientapp.health.data.AuthRepository
import com.patientapp.health.data.DailyFormRepository
import com.patientapp.health.data.UserRepository
import com.patientapp.health.ui.auth.AuthViewModel
import com.patientapp.health.ui.auth.LoginScreen
import com.patientapp.health.ui.auth.RegisterScreen
import com.patientapp.health.ui.doctor.AddPatientDialog
import com.patientapp.health.ui.doctor.DoctorHomeScreen
import com.patientapp.health.ui.doctor.DoctorViewModel
import com.patientapp.health.ui.doctor.PatientFormHistoryScreen
import com.patientapp.health.ui.patient.DailyFormScreen
import com.patientapp.health.ui.patient.PatientViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object DoctorHome : Screen("doctor_home")
    data object PatientHome : Screen("patient_home")
    data object PatientFormHistory : Screen("patient_form_history/{patientId}") {
        fun create(patientId: String) = "patient_form_history/$patientId"
    }
}

@Composable
fun NavGraph(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    dailyFormRepository: DailyFormRepository,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel = remember { AuthViewModel(authRepository) }
    val authState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val snackbarHostState = androidx.compose.material3.SnackbarHostState()
            androidx.compose.material3.Scaffold(
                snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
            ) {
                LoginScreen(
                    uiState = authState,
                    onSignIn = { email, password -> authViewModel.signIn(email, password) },
                    onSendPhoneCode = { phone, activity -> authViewModel.startPhoneVerification(phone, activity) },
                    onSignInWithPhoneCode = { id, code -> authViewModel.signInWithPhoneCode(id, code) },
                    onSignInWithPhoneCredential = { authViewModel.signInWithStoredPhoneCredential() },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onClearError = { authViewModel.clearError() },
                    onClearPhoneState = { authViewModel.clearPhoneVerificationState() },
                    snackbarHostState = snackbarHostState
                )
            }
        }

        composable(Screen.Register.route) {
            val snackbarHostState = androidx.compose.material3.SnackbarHostState()
            androidx.compose.material3.Scaffold(
                snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
            ) {
                RegisterScreen(
                    uiState = authState,
                    onSignUp = { email, pass, role, name ->
                        authViewModel.signUp(email, pass, role, name)
                    },
                    onSendPhoneCode = { phone, activity -> authViewModel.startPhoneVerification(phone, activity) },
                    onSignUpWithPhoneCode = { id, code, role, name ->
                        authViewModel.signUpWithPhoneCode(id, code, role, name)
                    },
                    onSignUpWithPhoneCredential = { role, name -> authViewModel.signUpWithPhoneCredential(role, name) },
                    onNavigateToLogin = { navController.popBackStack() },
                    onClearError = { authViewModel.clearError() },
                    onClearPhoneState = { authViewModel.clearPhoneVerificationState() },
                    snackbarHostState = snackbarHostState
                )
            }
        }

        composable(Screen.DoctorHome.route) {
            val doctorId = authState.currentUser?.id ?: return@composable
            val doctorViewModel = remember(doctorId) {
                DoctorViewModel(doctorId, userRepository, dailyFormRepository)
            }
            val doctorState by doctorViewModel.uiState.collectAsState()
            var showAddDialog by remember { mutableStateOf(false) }
            var selectedPatient by remember { mutableStateOf<com.patientapp.health.data.User?>(null) }

            if (showAddDialog) {
                AddPatientDialog(
                    onDismiss = {
                        showAddDialog = false
                        doctorViewModel.clearAddPatientState()
                    },
                    onConfirm = { email, displayName ->
                        doctorViewModel.addPatient(email, displayName)
                    },
                    error = doctorState.addPatientError,
                    success = doctorState.addPatientSuccess
                )
            }

            selectedPatient?.let { patient ->
                val forms = doctorViewModel.formsForPatient(patient.id)
                PatientFormHistoryScreen(
                    patient = patient,
                    forms = forms,
                    onBack = { selectedPatient = null }
                )
            } ?: run {
                DoctorHomeScreen(
                    uiState = doctorState,
                    onAddPatient = { showAddDialog = true },
                    onPatientClick = { selectedPatient = it },
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.PatientHome.route) {
            val patient = authState.currentUser ?: return@composable
            val doctorId = patient.doctorId ?: return@composable
            val patientViewModel = remember(patient.id) {
                PatientViewModel(patient.id, doctorId, dailyFormRepository, userRepository)
            }
            val patientState by patientViewModel.uiState.collectAsState()

            DailyFormScreen(
                uiState = patientState,
                onSubmit = { t, s, p -> patientViewModel.submitForm(t, s, p) },
                onClearSubmitState = { patientViewModel.clearSubmitState() },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }

    // Navigate to role home when logged in
    androidx.compose.runtime.LaunchedEffect(authState.isLoggedIn, authState.currentUser) {
        if (authState.isLoggedIn && authState.currentUser != null) {
            val user = authState.currentUser!!
            when (user.role) {
                com.patientapp.health.data.UserRole.DOCTOR ->
                    if (navController.currentBackStackEntry?.destination?.route != Screen.DoctorHome.route)
                        navController.navigate(Screen.DoctorHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                com.patientapp.health.data.UserRole.PATIENT ->
                    if (navController.currentBackStackEntry?.destination?.route != Screen.PatientHome.route)
                        navController.navigate(Screen.PatientHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
            }
        }
    }
}
