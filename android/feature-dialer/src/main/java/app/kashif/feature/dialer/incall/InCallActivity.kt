package app.kashif.feature.dialer.incall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class InCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val call by CallState.current.collectAsState()
            IncomingCallScreen(
                name = call?.details?.handle?.schemeSpecificPart ?: "رقم غير معروف",
                subtitle = "", verdict = Verdict.NORMAL,
                onAccept = { call?.answer(0) }, onDecline = { call?.disconnect(); finish() })
        }
    }
}

/** Full-screen incoming call — colour-coded per docs/DESIGN.md §3. */
@Composable
fun IncomingCallScreen(name: String, subtitle: String, verdict: Verdict, onAccept: () -> Unit, onDecline: () -> Unit) {
    val g = when (verdict) {
        Verdict.SPAM -> listOf(Color(0xFFFF3B30), Color(0xFFC0182A), Color(0xFF2A0E1A))
        Verdict.VERIFIED_BUSINESS -> listOf(Color(0xFF1FA35B), Color(0xFF0E6B3D), Color(0xFF0A2A22))
        Verdict.NORMAL -> listOf(Color(0xFF1565FF), Color(0xFF0B2A6B), Color(0xFF08152F))
    }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(g)).systemBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(48.dp))
        Box(Modifier.size(128.dp).background(Color.Black.copy(alpha = .25f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(if (verdict == Verdict.SPAM) Icons.Default.GppMaybe else Icons.Default.Person, null,
                tint = Color.White, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        if (subtitle.isNotBlank()) Text(subtitle, color = Color.White.copy(alpha = .85f))
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FilledIconButton(onDecline, Modifier.size(72.dp), colors = IconButtonDefaults.filledIconButtonColors(Color(0xFFFF5A4E))) {
                Icon(Icons.Default.CallEnd, "رفض", tint = Color.White) }
            FilledIconButton(onAccept, Modifier.size(72.dp), colors = IconButtonDefaults.filledIconButtonColors(Color(0xFF2BB55A))) {
                Icon(Icons.Default.Call, "رد", tint = Color.White) }
        }
        Spacer(Modifier.height(32.dp))
    }
}
