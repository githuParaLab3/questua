package com.questua.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaAsyncImage
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.core.ui.theme.*
import com.questua.app.domain.enums.UserRole

@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val name by viewModel.editName.collectAsState()
    val email by viewModel.editEmail.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        if (state.isLoading) {
            LoadingSpinner()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Avatar ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (state.user?.avatarUrl != null) {
                    QuestuaAsyncImage(
                        imageUrl = state.user!!.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Slate200
                    )
                }

                // Botão de editar foto (visual)
                if (state.isEditing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Amber500, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Dados Cadastrais ---
            if (state.isEditing) {
                QuestuaTextField(
                    value = name,
                    onValueChange = { viewModel.editName.value = it },
                    label = "Nome",
                    leadingIcon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(16.dp))
                QuestuaTextField(
                    value = email,
                    onValueChange = { viewModel.editEmail.value = it },
                    label = "E-mail",
                    leadingIcon = Icons.Default.Email
                )
            } else {
                Text(
                    text = state.user?.displayName ?: "Usuário",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = state.user?.email ?: "carregando...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Editar / Salvar
            QuestuaButton(
                text = if (state.isEditing) "Salvar Alterações" else "Editar Perfil",
                onClick = { viewModel.toggleEditMode() },
                isSecondary = !state.isEditing
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Configurações ---
            SectionHeader("Configurações")

            SettingsItemSwitch(
                label = "Notificações",
                checked = state.notificationsEnabled,
                onCheckedChange = { viewModel.toggleNotifications(it) }
            )

            SettingsItemSwitch(
                label = "Tema Escuro",
                checked = state.darkThemeEnabled,
                onCheckedChange = { viewModel.toggleTheme(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Ações ---
            SectionHeader("Conta")

            SettingsActionItem(
                label = "Ajuda e Suporte",
                icon = Icons.Outlined.Help,
                onClick = onNavigateToHelp
            )

            // Botão Admin (Condicional)
            // Exibe se o usuário já é admin para permitir acesso ao painel,
            // OU um toggle para fins de demonstração/debug se quiser simular a promoção.
            // Assumindo que o botão "Modo Administrador" leva ao Painel Admin.
            if (state.user?.role == UserRole.ADMIN) {
                SettingsActionItem(
                    label = "Painel do Administrador",
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = onNavigateToAdmin,
                    textColor = Amber600
                )
            }

            SettingsActionItem(
                label = "Sair",
                icon = Icons.Outlined.Logout,
                onClick = { viewModel.logout(onLogoutSuccess = onNavigateToLogin) },
                textColor = Rose500
            )
        }

        state.error?.let {
            ErrorDialog(message = it, onDismiss = { viewModel.clearError() })
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsItemSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(color = Slate900)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Amber500
            )
        )
    }
}

@Composable
fun SettingsActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = Slate900
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
}