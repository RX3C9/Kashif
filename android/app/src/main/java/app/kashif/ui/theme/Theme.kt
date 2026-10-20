package app.kashif.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Colour logic from docs/DESIGN.md. Brand colours are ours; state colours follow the familiar caller-ID convention. */
object CallStateColors {
    val NormalTop = Color(0xFF1565FF); val NormalMid = Color(0xFF0B2A6B); val NormalBottom = Color(0xFF08152F)
    val SpamTop = Color(0xFFFF3B30); val SpamMid = Color(0xFFC0182A); val SpamBottom = Color(0xFF2A0E1A)
    val BizTop = Color(0xFF1FA35B); val BizMid = Color(0xFF0E6B3D); val BizBottom = Color(0xFF0A2A22)
    val Accept = Color(0xFF2BB55A); val Decline = Color(0xFFFF5A4E)
}

private val Primary = Color(0xFF0A84FF)
private val Light = lightColorScheme(primary = Primary, background = Color(0xFFF3F6FB), surface = Color.White)
private val Dark = darkColorScheme(primary = Primary, background = Color(0xFF0E1116), surface = Color(0xFF161B22))

@Composable
fun KashifTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
