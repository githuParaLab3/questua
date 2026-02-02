package com.questua.app.presentation.admin.content.achievements

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questua.app.core.common.URIPathHelper
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.AchievementMetadata
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementFormDialog(
    achievement: Achievement? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        key: String, name: String, desc: String,
        icon: Any?, // String ou File
        rarity: RarityType, xp: Int, meta: AchievementMetadata?
    ) -> Unit
) {
    val context = LocalContext.current

    // Básico
    var keyName by remember { mutableStateOf(achievement?.keyName ?: "") }
    var name by remember { mutableStateOf(achievement?.name ?: "") }
    var description by remember { mutableStateOf(achievement?.description ?: "") }
    var xpReward by remember { mutableStateOf(achievement?.xpReward?.toString() ?: "50") }
    var rarity by remember { mutableStateOf(achievement?.rarity ?: RarityType.COMMON) }

    // Icon
    var selectedIcon by remember { mutableStateOf<Any?>(achievement?.iconUrl) }

    // Metadata
    var metaCategory by remember { mutableStateOf(achievement?.metadata?.category ?: "") }
    var metaDescExtra by remember { mutableStateOf(achievement?.metadata?.descriptionExtra ?: "") }

    // Auxiliares
    var showRarityPicker by remember { mutableStateOf(false) }
    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedIcon = URIPathHelper.getFileFromUri(context, it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (achievement == null) "Nova Conquista" else "Editar Conquista") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome da Conquista")
                QuestuaTextField(value = keyName, onValueChange = { keyName = it }, label = "Chave Única (ID interno)")

                OutlinedCard(onClick = { showRarityPicker = true }) {
                    ListItem(
                        headlineContent = { Text("Raridade: ${rarity.name}") },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                QuestuaTextField(value = xpReward, onValueChange = { xpReward = it }, label = "Recompensa (XP)")
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")

                Button(onClick = { iconPicker.launch("image/*") }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Image, null); Spacer(Modifier.width(8.dp))
                    Text(if(selectedIcon is File) "Ícone (Novo)" else if(selectedIcon != null) "Ícone (OK)" else "Upload Ícone")
                }

                HorizontalDivider()
                Text("Metadados (Opcional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                QuestuaTextField(value = metaCategory, onValueChange = { metaCategory = it }, label = "Categoria")
                QuestuaTextField(value = metaDescExtra, onValueChange = { metaDescExtra = it }, label = "Info Extra")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val metadata = if(metaCategory.isBlank() && metaDescExtra.isBlank()) null
                    else AchievementMetadata(metaCategory.ifBlank { null }, metaDescExtra.ifBlank { null })

                    onConfirm(
                        keyName, name, description, selectedIcon,
                        rarity, xpReward.toIntOrNull() ?: 0, metadata
                    )
                },
                enabled = name.isNotBlank() && keyName.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showRarityPicker) {
        AlertDialog(
            onDismissRequest = { showRarityPicker = false },
            title = { Text("Selecione a Raridade") },
            text = {
                Column {
                    RarityType.values().forEach { type ->
                        ListItem(
                            modifier = Modifier.clickable { rarity = type; showRarityPicker = false },
                            headlineContent = { Text(type.name) }
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}