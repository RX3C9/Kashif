package app.kashif.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dialer {
    /** Places an outgoing call through Telecom; returns false if permission is missing or it fails. */
    fun call(ctx: Context, number: String): Boolean {
        val n = number.trim()
        if (n.isEmpty()) return false
        return try {
            val tm = ctx.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            tm.placeCall(Uri.fromParts("tel", n, null), Bundle())
            true
        } catch (e: SecurityException) { false }
    }
}

private val KEYS = listOf(
    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
    listOf("*" to "", "0" to "+", "#" to "")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialpadSheet(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var number by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(number.ifEmpty { " " }, fontSize = 32.sp, maxLines = 1)
            Spacer(Modifier.height(12.dp))
            KEYS.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { (d, l) ->
                        Column(Modifier.size(76.dp).clickable { number += d },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(d, fontSize = 28.sp)
                            if (l.isNotEmpty()) Text(l, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.size(56.dp))
                FilledIconButton(onClick = { if (Dialer.call(ctx, number)) onDismiss() }, modifier = Modifier.size(64.dp),
                    shape = CircleShape, colors = IconButtonDefaults.filledIconButtonColors(Color(0xFF0A84FF))) {
                    Icon(Icons.Default.Call, "اتصال", tint = Color.White)
                }
                IconButton(onClick = { number = number.dropLast(1) }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.Backspace, "مسح")
                }
            }
        }
    }
}
