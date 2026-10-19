package fr.marine.routine.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Couleurs : violet pour Marine, vert pour Parent. Contrastes ≥ 4,5:1 pour le texte. */
object MarineColors {
    val Violet = Color(0xFF5E2B97)
    val VioletDark = Color(0xFF4A2370)
    val VioletLight = Color(0xFFF1E8FA)
    val VioletTrack = Color(0xFFDCCBEE)

    val ParentGreen = Color(0xFF1F6E4A)
    val ParentGreenLight = Color(0xFFE4F2EA)

    /** Activité confirmée « J'ai terminé ». */
    val Done = Color(0xFF2E7D32)
    val DoneLight = Color(0xFFE3F4E4)

    /** Aide : couleur distincte, chaleureuse, non alarmante. */
    val Help = Color(0xFFFFE2B0)
    val OnHelp = Color(0xFF5A3A00)

    val Upcoming = Color(0xFFE6E1EC)
    val Text = Color(0xFF221B2B)
    val TextSoft = Color(0xFF4F4659)
}

@Composable
fun MarineTheme(parent: Boolean = false, content: @Composable () -> Unit) {
    val primary = if (parent) MarineColors.ParentGreen else MarineColors.Violet
    val container = if (parent) MarineColors.ParentGreenLight else MarineColors.VioletLight
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = container,
            onPrimaryContainer = MarineColors.Text,
            secondary = primary,
            onSecondary = Color.White,
            background = Color.White,
            onBackground = MarineColors.Text,
            surface = Color.White,
            onSurface = MarineColors.Text,
            onSurfaceVariant = MarineColors.TextSoft,
        ),
        content = content,
    )
}
