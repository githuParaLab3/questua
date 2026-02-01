package com.questua.app.presentation.admin.content.cities

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questua.app.core.common.URIPathHelper
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityFormDialog(
    city: City? = null,
    languages: List<Language>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, desc: String, langId: String, poly: BoundingPolygon?, lat: Double, lon: Double, img: File?, icon: File?, prem: Boolean, unlock: UnlockRequirement?, ai: Boolean, pub: Boolean) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(city?.name ?: "") }
    var code by remember { mutableStateOf(city?.countryCode ?: "") }
    var desc by remember { mutableStateOf(city?.description ?: "") }
    var langId by remember { mutableStateOf(city?.languageId ?: "") }
    var lat by remember { mutableStateOf(city?.lat?.toString() ?: "") }
    var lon by remember { mutableStateOf(city?.lon?.toString() ?: "") }
    var isPremium by remember { mutableStateOf(city?.isPremium ?: false) }
    var isPublished by remember { mutableStateOf(city?.isPublished ?: true) }

    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var selectedIconFile by remember { mutableStateOf<File?>(null) }

    var polyText by remember { mutableStateOf(city?.boundingPolygon?.let { Json.encodeToString(it.coordinates) } ?: "[]") }

    // Estados amigáveis para UnlockRequirement
    var reqPremium by remember { mutableStateOf(city?.unlockRequirement?.premiumAccess ?: false) }
    var reqLevel by remember { mutableStateOf(city?.unlockRequirement?.requiredGamificationLevel?.toString() ?: "") }
    var reqCefr by remember { mutableStateOf(city?.unlockRequirement?.requiredCefrLevel ?: "") }

    var showLangPicker by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedImageFile = URIPathHelper.getFileFromUri(context, it) }
    }
    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedIconFile = URIPathHelper.getFileFromUri(context, it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (city == null) "Nova Cidade" else "Editar Cidade") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome da Cidade")

                OutlinedCard(onClick = { showLangPicker = true }) {
                    val current = languages.find { it.id == langId }
                    ListItem(
                        headlineContent = { Text(current?.name ?: "Selecionar Idioma") },
                        supportingContent = { Text(if(langId.isEmpty()) "Obrigatório" else "ID: $langId") }
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Image, null); Spacer(Modifier.width(4.dp)); Text(if(selectedImageFile != null) "OK" else "Imagem")
                    }
                    Button(onClick = { iconPicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Image, null); Spacer(Modifier.width(4.dp)); Text(if(selectedIconFile != null) "OK" else "Ícone")
                    }
                }

                QuestuaTextField(value = code, onValueChange = { code = it }, label = "Código País")
                QuestuaTextField(value = desc, onValueChange = { desc = it }, label = "Descrição")
                QuestuaTextField(value = lat, onValueChange = { lat = it }, label = "Latitude")
                QuestuaTextField(value = lon, onValueChange = { lon = it }, label = "Longitude")
                QuestuaTextField(value = polyText, onValueChange = { polyText = it }, label = "Polígono (JSON)")

                // Seção Amigável de Requisitos
                HorizontalDivider()
                Text("Requisitos de Desbloqueio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reqPremium, onCheckedChange = { reqPremium = it })
                    Text("Exigir Assinatura Premium")
                }

                QuestuaTextField(
                    value = reqLevel,
                    onValueChange = { reqLevel = it },
                    label = "Nível de Gamificação Mínimo"
                )

                QuestuaTextField(
                    value = reqCefr,
                    onValueChange = { reqCefr = it },
                    label = "Nível CEFR Mínimo (ex: B1)"
                )

                HorizontalDivider()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPublished, onCheckedChange = { isPublished = it })
                    Text("Publicado")
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isPremium, onCheckedChange = { isPremium = it })
                    Text("Premium")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val poly = try { BoundingPolygon(Json.decodeFromString(polyText)) } catch (e: Exception) { null }

                // Monta o objeto UnlockRequirement a partir dos campos individuais
                val unlock = UnlockRequirement(
                    premiumAccess = reqPremium,
                    requiredGamificationLevel = reqLevel.toIntOrNull(),
                    requiredCefrLevel = reqCefr.ifEmpty { null }
                )

                onConfirm(name, code, desc, langId, poly, lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0, selectedImageFile, selectedIconFile, isPremium, unlock, false, isPublished)
            }) { Text("Confirmar") }
        }
    )

    // O BLOCO QUE FALTAVA PARA O SELETOR FUNCIONAR:
    if (showLangPicker) {
        AlertDialog(
            onDismissRequest = { showLangPicker = false },
            title = { Text("Selecione o Idioma") },
            text = {
                Box(Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        items(languages) { lang ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    langId = lang.id
                                    showLangPicker = false
                                },
                                headlineContent = { Text(lang.name) },
                                supportingContent = { Text(lang.code) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangPicker = false }) { Text("Fechar") }
            }
        )
    }
}