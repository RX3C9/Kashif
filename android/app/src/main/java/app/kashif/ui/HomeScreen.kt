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
import android.app.role.RoleManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import app.kashif.onboarding.RoleOnboarding
import app.kashif.core.data.CallDirection
import app.kashif.core.data.CallRow
import app.kashif.core.data.CallVerdict
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.launch


/** Home / Calls tab — layout per docs/DESIGN.md §1 (search pill, call log, square dialpad FAB, bottom nav). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var tab by remember { mutableIntStateOf(0) }
    val ctx = LocalContext.current
    var dialerHeld by remember { mutableStateOf(RoleOnboarding.isHeld(ctx, RoleManager.ROLE_DIALER)) }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        dialerHeld = RoleOnboarding.isHeld(ctx, RoleManager.ROLE_DIALER)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        RoleOnboarding.requestIntent(ctx, RoleManager.ROLE_DIALER)?.let { roleLauncher.launch(it) }
    }
    var rows by remember { mutableStateOf<List<CallRow>>(emptyList()) }
    var selected by remember { mutableStateOf<CallRow?>(null) }
    var showPad by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var reload by remember { mutableIntStateOf(0) }
    val hasLog = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    LaunchedEffect(reload, dialerHeld, hasLog) { rows = if (hasLog) CallLogLoader.load(ctx) else emptyList() }
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
            FloatingActionButton(onClick = { showPad = true }, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Dialpad, "Dialpad") }
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
            if (!dialerHeld) item {
                Card(Modifier.padding(12.dp).fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("اجعل كاشف تطبيق الاتصال الافتراضي", style = MaterialTheme.typography.titleMedium)
                        Text("لتظهر هوية المتصل وحظر السبام أثناء المكالمات.", color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { permLauncher.launch(RoleOnboarding.permissions) }) { Text("تفعيل") }
                    }
                }
            }
            items(rows) { r ->
                val spam = r.verdict == CallVerdict.SPAM
                ListItem(
                    modifier = Modifier.clickable { selected = r },
                    leadingContent = {
                        Box(Modifier.size(44.dp).background(if (spam) Color(0xFFFFE5E3) else Color(0xFFE3EDFF), CircleShape),
                            contentAlignment = Alignment.Center) {
                            Icon(if (spam) Icons.Default.GppMaybe else Icons.Default.Person, null,
                                tint = if (spam) Color(0xFFE5332A) else Color(0xFF0A84FF))
                        }
                    },
                    headlineContent = { Text(if (r.count > 1) r.title + " (" + r.count + ")" else r.title,
                        color = if (spam || r.direction == CallDirection.MISSED) Color(0xFFE5332A) else Color.Unspecified) },
                    supportingContent = {
                        val arrow = when (r.direction) {
                            CallDirection.OUTGOING -> "↗ "
                            CallDirection.MISSED -> "✕ "
                            CallDirection.REJECTED -> "⊘ "
                            else -> "↙ "
                        }
                        val extra = if (spam) "احتيال · " + r.reports + " بلاغ · " else ""
                        Text(arrow + extra + android.text.format.DateUtils.getRelativeTimeSpanString(r.date),
                            color = if (spam) Color(0xFFE5332A) else Color.Gray)
                    },
                    trailingContent = { Icon(Icons.Default.Call, null) }
                )
            }
        }
    }
    if (showPad) DialpadSheet { showPad = false }
    selected?.let { r ->
        ModalBottomSheet(onDismissRequest = { selected = null }) {
            Column(Modifier.padding(16.dp).navigationBarsPadding()) {
                Text(r.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    scope.launch { CallLogLoader.report(ctx, r.number, "spam", false); reload++ }; selected = null
                }, modifier = Modifier.fillMaxWidth()) { Text("تبليغ كسبام") }
                OutlinedButton(onClick = {
                    scope.launch { CallLogLoader.report(ctx, r.number, "spam", true); reload++ }; selected = null
                }, modifier = Modifier.fillMaxWidth()) { Text("تبليغ وحظر") }
            }
        }
    }
}
