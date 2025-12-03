package com.questua.app.presentation.admin.feedback

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.questua.app.presentation.admin.components.AdminBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeedbackScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Feedbacks & Reports") }) },
        bottomBar = { AdminBottomNavBar(navController) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(5) { index ->
                ListItem(
                    headlineContent = { Text("Erro no diálogo da cidade X") },
                    supportingContent = { Text("Reportado por: user123 • Tipo: BUG") },
                    trailingContent = {
                        IconButton(onClick = { /* Resolve */ }) {
                            Icon(Icons.Default.Check, contentDescription = "Resolver")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}