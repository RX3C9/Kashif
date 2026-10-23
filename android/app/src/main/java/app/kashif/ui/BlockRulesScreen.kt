package app.kashif.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.kashif.core.data.BlockRule
import app.kashif.core.data.KashifDatabase
import app.kashif.core.data.RuleAction
import app.kashif.core.data.RuleType
import app.kashif.core.data.SpamSeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun typeLabel(t: RuleType) = when (t) {
    RuleType.EXACT -> "رقم محدد"
    RuleType.PREFIX -> "يبدأ بـ"
    RuleType.COUNTRY -> "كود دولة"
    RuleType.REGEX -> "نمط (Regex)"
    RuleType.HIDDEN -> "أرقام مخفية"
    RuleType.NOT_IN_CONTACTS -> "غير موجود في جهاتي"
}

/** Protection tab: manage block rules (Room) and load the bundled offline spam pack once. */
@Composable
fun BlockRulesScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val db = remember { KashifDatabase.get(ctx) }
    val rules by db.blockRules().observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var type by remember { mutableStateOf(RuleType.PREFIX) }
    var pattern by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    val needsPattern = type != RuleType.HIDDEN && type != RuleType.NOT_IN_CONTACTS

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (db.spam().count() == 0) {
                val text = runCatching { ctx.assets.open("spam_seed.csv").bufferedReader().use { it.readText() } }.getOrDefault("")
                val rows = SpamSeed.parse(text, System.currentTimeMillis())
                if (rows.isNotEmpty()) db.spam().upsertAll(rows)
            }
        }
    }

    Column(modifier.padding(12.dp)) {
        Text("قواعد الحظر", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { menu = true }) { Text(typeLabel(type)) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                RuleType.entries.forEach { t ->
                    DropdownMenuItem(text = { Text(typeLabel(t)) }, onClick = { type = t; menu = false })
                }
            }
        }
        if (needsPattern) OutlinedTextField(pattern, { pattern = it }, Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("النمط (مثال: +2015 أو 91)") })
        Spacer(Modifier.height(8.dp))
        Button(enabled = !needsPattern || pattern.isNotBlank(), onClick = {
            val r = BlockRule(type = type.name, pattern = if (needsPattern) pattern.trim() else "",
                action = RuleAction.REJECT.name, label = typeLabel(type), createdAt = System.currentTimeMillis())
            scope.launch(Dispatchers.IO) { db.blockRules().insert(r) }
            pattern = ""
        }) { Text("إضافة قاعدة") }
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(rules, key = { it.id }) { r ->
                ListItem(
                    headlineContent = { Text(r.label.ifEmpty { r.type } + if (r.pattern.isNotEmpty()) "  " + r.pattern else "") },
                    leadingContent = {
                        Switch(checked = r.enabled, onCheckedChange = { on ->
                            scope.launch(Dispatchers.IO) { db.blockRules().upsert(r.copy(enabled = on)) }
                        })
                    },
                    trailingContent = {
                        IconButton(onClick = { scope.launch(Dispatchers.IO) { db.blockRules().delete(r) } }) {
                            Icon(Icons.Default.Delete, "حذف")
                        }
                    }
                )
            }
        }
    }
}
