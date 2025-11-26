package com.questua.app.presentation.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.common.uriToFile
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val name by viewModel.editName.collectAsState()
    val email by viewModel.editEmail.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val selectedAvatarUri by viewModel.selectedAvatarUri.collectAsState()

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val file = context.uriToFile(it)
            file?.let { f ->
                viewModel.onImageSelected(f, it.toString())
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading && state.user == null) {
            LoadingSpinner()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (onNavigateBack != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Meu Perfil",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            // --- Avatar ---
            Box(modifier = Modifier.size(120.dp)) {
                val avatarModel = selectedAvatarUri ?: state.user?.avatarUrl?.toFullImageUrl()

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(avatarModel ?: "https://via.placeholder.com/150")
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = state.isEditing) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentScale = ContentScale.Crop
                )

                if (state.isEditing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Alterar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Formulário / Exibição ---
            if (state.isEditing) {
                QuestuaTextField(
                    value = name,
                    onValueChange = { viewModel.editName.value = it },
                    label = "Nome",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                QuestuaTextField(
                    value = email,
                    onValueChange = { },
                    label = "E-mail (Fixo)",
                    leadingIcon = Icons.Default.Email,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                QuestuaTextField(
                    value = newPassword,
                    onValueChange = { viewModel.newPassword.value = it },
                    label = "Nova Senha (Opcional)",
                    placeholder = "Deixe vazio para manter",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = state.user?.displayName ?: "Usuário",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = state.user?.email ?: "carregando...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de Ação Principal
            QuestuaButton(
                text = if (state.isEditing) "Salvar Alterações" else "Editar Perfil",
                onClick = { viewModel.toggleEditMode() },
                isSecondary = !state.isEditing,
                isLoading = state.isLoading
            )

            // --- SEÇÕES DE CONFIGURAÇÃO E CONTA ---
            // Só aparecem se NÃO estiver editando
            if (!state.isEditing) {
                Spacer(modifier = Modifier.height(32.dp))

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

                SectionHeader("Conta")

                SettingsActionItem(
                    label = "Ajuda e Suporte",
                    icon = Icons.Outlined.Help,
                    onClick = onNavigateToHelp
                )

                if (state.user?.role == UserRole.ADMIN) {
                    SettingsActionItem(
                        label = "Painel do Administrador",
                        icon = Icons.Default.AdminPanelSettings,
                        onClick = onNavigateToAdmin,
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }

                SettingsActionItem(
                    label = "Sair",
                    icon = Icons.Outlined.Logout,
                    onClick = { viewModel.logout(onLogoutSuccess = onNavigateToLogin) },
                    textColor = MaterialTheme.colorScheme.error
                )
            }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onBackground
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