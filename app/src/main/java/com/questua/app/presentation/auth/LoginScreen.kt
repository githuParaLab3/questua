package com.questua.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.core.ui.theme.Slate500
import com.questua.app.core.ui.theme.Slate900

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    if (state.isLoggedIn) {
        onNavigateToHome()
    }

    state.error?.let { error ->
        ErrorDialog(message = error, onDismiss = { viewModel.clearError() })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header (Botão Voltar + Título)
        Column(modifier = Modifier.padding(24.dp)) {
            IconButton(
                onClick = { /* Navegar para trás se necessário */ },
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Slate500)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bem-vindo de volta",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Text(
                text = "Continue sua jornada de aprendizado.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
            )
        }

        // Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            QuestuaTextField(
                value = email,
                onValueChange = { viewModel.email.value = it },
                label = "E-mail",
                placeholder = "aventureiro@questua.com",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuestuaTextField(
                value = password,
                onValueChange = { viewModel.password.value = it },
                label = "Senha",
                placeholder = "••••••••",
                isPassword = true,
                leadingIcon = Icons.Default.Lock
            )

            // Botão "Esqueci senha"
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { /* TODO */ }) {
                    Text(
                        "Esqueci minha senha",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            QuestuaButton(
                text = "Entrar",
                onClick = { viewModel.login() },
                isLoading = state.isLoading,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ainda não tem uma conta? ", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Cadastre-se", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate900))
                }
            }
        }
    }
}