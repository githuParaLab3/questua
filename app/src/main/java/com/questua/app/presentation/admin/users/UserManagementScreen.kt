package com.questua.app.presentation.admin.users

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.common.uriToFile
import com.questua.app.core.ui.components.ErrorDialog
import com.questua.app.core.ui.components.LoadingSpinner
import com.questua.app.core.ui.components.QuestuaAsyncImage
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserAccount
import com.questua.app.presentation.admin.components.AdminBottomNavBar
import com.questua.app.presentation.admin.feedback.EmptyState
import com.questua.app.presentation.navigation.Screen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showCreateDialog by remember { mutableStateOf(false) }

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // RECARREGAMENTO DINÂMICO AO ENTRAR EM FOCO
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUsers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciamento de Usuários") },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        },
        bottomBar = { AdminBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Usuário")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestuaTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = "Buscar por nome, email ou ID...",
                    label = null,
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (state.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar")
                            }
                        }
                    } else null,
                    modifier = Modifier.weight(1f)
                )

                val hasActiveFilters = state.roleFilter != null
                FilledTonalIconButton(
                    onClick = { showFilterSheet = true },
                    colors = if (hasActiveFilters) IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) else IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Box {
                        Icon(Icons.Default.Tune, contentDescription = "Filtros")
                        if (hasActiveFilters) {
                            Badge(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp),
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && state.users.isEmpty()) {
                    LoadingSpinner()
                } else if (state.users.isEmpty()) {
                    EmptyState(isSearching = state.searchQuery.isNotEmpty())
                } else {
                    LazyColumn {
                        items(state.users) { user ->
                            UserListItem(
                                user = user,
                                onClick = { navController.navigate(Screen.AdminUserDetail.passId(user.id)) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            UserFilterSheetContent(
                state = state,
                onRoleSelected = viewModel::onRoleFilterChange,
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            languages = state.availableLanguages,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, email, pass, langId, role, file ->
                viewModel.createUser(name, email, pass, langId, role, file)
                showCreateDialog = false
            }
        )
    }

    state.error?.let {
        ErrorDialog(message = it, onDismiss = { viewModel.loadUsers() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFilterSheetContent(
    state: UserManagementState,
    onRoleSelected: (UserRole?) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
            .fillMaxWidth()
    ) {
        Text(
            "Filtros",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Função (Role)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = state.roleFilter == null,
                    onClick = { onRoleSelected(null) },
                    label = { Text("Todos") }
                )
            }
            items(UserRole.entries) { role ->
                val isSelected = state.roleFilter == role
                FilterChip(
                    selected = isSelected,
                    onClick = { onRoleSelected(role) },
                    label = { Text(role.name) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onRoleSelected(null)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpar")
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Ver Resultados")
            }
        }
    }
}

@Composable
fun UserListItem(user: UserAccount, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(user.displayName, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            Column {
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionChip(
                    onClick = { },
                    label = { Text(user.role.name, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if(user.role == UserRole.ADMIN) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        },
        leadingContent = {
            if (!user.avatarUrl.isNullOrBlank()) {
                QuestuaAsyncImage(
                    imageUrl = user.avatarUrl.toFullImageUrl(),
                    contentDescription = "Avatar de ${user.displayName}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.displayName.firstOrNull()?.toString()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    languages: List<Language>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, UserRole, File?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf<Language?>(languages.firstOrNull()) }

    var role by remember { mutableStateOf(UserRole.USER) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Usuário") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                Box(modifier = Modifier.size(80.dp).clickable {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar Preview",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Adicionar Foto", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Senha") }, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLanguage?.let { "${it.code.uppercase()} - ${it.name}" } ?: "Selecione o idioma",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Idioma Nativo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text("${language.code.uppercase()} - ${language.name}") },
                                onClick = {
                                    selectedLanguage = language
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text("Função:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp).fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = role == UserRole.USER,
                        onClick = { role = UserRole.USER },
                        label = { Text("USER") }
                    )
                    FilterChip(
                        selected = role == UserRole.ADMIN,
                        onClick = { role = UserRole.ADMIN },
                        label = { Text("ADMIN") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedLanguage != null) {
                        val file = selectedImageUri?.let { context.uriToFile(it) }
                        onConfirm(name, email, password, selectedLanguage!!.id, role, file)
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && selectedLanguage != null
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}