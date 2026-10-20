package app.kashif.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class LogRow(val name: String, val sub: String, val spam: Boolean)

/** Home / Calls tab — layout per docs/DESIGN.md §1 (search pill, call log, square dialpad FAB, bottom nav). */
@Composable
fun HomeScreen() {
    var tab by remember { mutableIntStateOf(0) }
    val rows = remember { listOf(
        LogRow("أحمد علي", "↙ 11:30 ص", false),
        LogRow("احتيال بنكي", "احتيال · 587 بلاغ · 11:10 ص", true),
        LogRow("ريم حسن", "↗ أمس", false)) }
    Scaffold(
        topBar = {
            Surface(Modifier.statusBarsPadding().padding(12.dp).fillMaxWidth(), shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("ابحث بالأسماء والأرقام…", Modifier.weight(1f), color = Color.Gray)
                    Icon(Icons.Default.MoreVert, null)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Dialpad, "Dialpad") }
        },
        bottomBar = {
            NavigationBar {
                listOf(Icons.Default.History to "المكالمات", Icons.Default.Chat to "الرسائل",
                    Icons.Default.Contacts to "جهات الاتصال", Icons.Default.Star to "المزايا",
                    Icons.Default.Shield to "الحماية").forEachIndexed { i, (ic, l) ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(ic, l) }, label = { Text(l) })
                }
            }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad)) {
            items(rows) { r ->
                ListItem(
                    leadingContent = {
                        Box(Modifier.size(44.dp).background(if (r.spam) Color(0xFFFFE5E3) else Color(0xFFE3EDFF), CircleShape),
                            contentAlignment = Alignment.Center) {
                            Icon(if (r.spam) Icons.Default.GppMaybe else Icons.Default.Person, null,
                                tint = if (r.spam) Color(0xFFE5332A) else Color(0xFF0A84FF))
                        }
                    },
                    headlineContent = { Text(r.name) },
                    supportingContent = { Text(r.sub, color = if (r.spam) Color(0xFFE5332A) else Color.Gray) },
                    trailingContent = { Icon(Icons.Default.Call, null) }
                )
            }
        }
    }
}
