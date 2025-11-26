package com.questua.app.core.ui.theme

import androidx.compose.ui.graphics.Color

// Royal Navy & Gold Palette

// Ambers (Gold/Action)
val Amber400 = Color(0xFFFBBF24)
val Amber500 = Color(0xFFF59E0B)
val Amber600 = Color(0xFFD97706)

// Slates (Neutral/Navy)
val Slate50 = Color(0xFFF8FAFC)
val Slate200 = Color(0xFFE2E8F0)
val Slate400 = Color(0xFF94A3B8) // <--- Adicionado
val Slate500 = Color(0xFF64748B)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)

// Accents
val Rose500 = Color(0xFFF43F5E)
val Green500 = Color(0xFF22C55E)

// Brand Legacy Colors (Usados em componentes específicos como Gems)
val QuestuaBlue = Color(0xFF3B82F6)      // <--- Adicionado (Blue-500)
val QuestuaBlueDark = Color(0xFF1D4ED8)  // <--- Adicionado (Blue-700)
val QuestuaOrange = Amber500             // Alias para compatibilidade

// Material 3 Mapping
val PrimaryLight = Amber500
val OnPrimaryLight = Color.White
val SecondaryLight = Slate900
val OnSecondaryLight = Color.White
val BackgroundLight = Slate50
val SurfaceLight = Color.White
val OnSurfaceLight = Slate800