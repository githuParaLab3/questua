package com.questua.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions // <--- IMPORTANTE
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction // <--- IMPORTANTE
import androidx.compose.ui.text.input.KeyboardType // <--- IMPORTANTE
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.core.ui.theme.Slate500
import com.questua.app.core.ui.theme.Slate900

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val name by viewModel.displayName.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    if (state.isRegistered) {
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
        // Header
        Column(modifier = Modifier.padding(24.dp)) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Slate500
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Comece sua busca",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            )
            Text(
                text = "Crie seu perfil de explorador.",
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
                value = name,
                onValueChange = { viewModel.displayName.value = it },
                label = "Nome",
                placeholder = "Seu nome",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuestuaTextField(
                value = email,
                onValueChange = { viewModel.email.value = it },
                label = "E-mail",
                placeholder = "seu@email.com",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuestuaTextField(
                value = password,
                onValueChange = { viewModel.password.value = it },
                label = "Senha",
                placeholder = "Crie uma senha",
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Checkbox simulado (Termos)
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Checkbox(
                    checked = true,
                    onCheckedChange = { },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Concordo com os Termos de Serviço do Questua.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            QuestuaButton(
                text = "Criar conta",
                onClick = { viewModel.register() },
                isLoading = state.isLoading,
                enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}