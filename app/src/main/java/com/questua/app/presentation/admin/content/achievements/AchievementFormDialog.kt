package com.questua.app.presentation.admin.content.achievements

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questua.app.core.common.URIPathHelper
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement
import com.questua.app.domain.model.AchievementMetadata
import com.questua.app.presentation.admin.content.dialogues.MediaPickerField
import com.questua.app.presentation.admin.content.dialogues.SectionTitle
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
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (achievement == null) "Nova Conquista" else "Editar Conquista", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle("Informações Básicas")
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome da Conquista")
                QuestuaTextField(value = keyName, onValueChange = { keyName = it }, label = "Chave Única (ID interno)")

                OutlinedCard(
                    onClick = { showRarityPicker = true },
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ListItem(
                        headlineContent = { Text("Raridade: ${rarity.name}", fontWeight = FontWeight.Bold) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                QuestuaTextField(value = xpReward, onValueChange = { xpReward = it }, label = "Recompensa (XP)")
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")

                SectionTitle("Visual")
                MediaPickerField(
                    label = "Ícone",
                    value = selectedIcon,
                    icon = Icons.Default.Image,
                    onPick = { iconPicker.launch("image/*") },
                    onClear = { selectedIcon = null }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SectionTitle("Metadados (Opcional)")

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
                enabled = name.isNotBlank() && keyName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = QuestuaGold, contentColor = Color.Black)
            ) { Text("Salvar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )

    if (showRarityPicker) {
        AlertDialog(
            onDismissRequest = { showRarityPicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Selecione a Raridade", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    RarityType.values().forEach { type ->
                        val color = when (type) {
                            RarityType.LEGENDARY -> Color(0xFFFFD700)
                            RarityType.EPIC -> Color(0xFF9C27B0)
                            RarityType.RARE -> Color(0xFF2196F3)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        ListItem(
                            modifier = Modifier.clickable { rarity = type; showRarityPicker = false },
                            headlineContent = { Text(type.name, color = color, fontWeight = FontWeight.Bold) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showRarityPicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) { Text("Fechar") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}