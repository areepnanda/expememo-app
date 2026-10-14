package com.arief.expememo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

private val DAY_NAMES = listOf(
    "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
)

private val RedPrimary = Color(0xFFE53935)
private val RedContainer = Color(0xFF93000A)
private val RedOnContainer = Color(0xFFFFDAD6)
private val RedAmount = Color(0xFFFF6E63)
private val GrayText = Color(0xFFA0A0A0)
private val DarkBg = Color(0xFF141414)
private val DarkSurface = Color(0xFF1F1F1F)
private val DarkBadge = Color(0xFF2C2C2C)

private val AppColors = darkColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    primaryContainer = RedContainer,
    onPrimaryContainer = RedOnContainer,
    secondaryContainer = RedContainer,
    onSecondaryContainer = RedOnContainer,
    background = DarkBg,
    surface = DarkBg,
    surfaceVariant = DarkSurface
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColors) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App()
                }
            }
        }
    }
}

data class Txn(
    val dt: LocalDateTime,
    val account: String,
    val category: String,
    val icon: String,
    val subcategory: String,
    val note: String,
    val description: String,
    val amount: Double
)

data class DateHeader(val label: String)

data class BankAvatar(val letter: String, val bg: Color, val fg: Color)

fun bankAvatar(account: String): BankAvatar = when {
    account.contains("bca", true) -> BankAvatar("B", Color(0xFF0A5CA8), Color.White)
    account.contains("mandiri", true) -> BankAvatar("M", Color(0xFFF7B600), Color(0xFF0B2E6B))
    account.contains("jenius", true) -> BankAvatar("J", Color(0xFF00AAB8), Color.White)
    else -> BankAvatar(
        account.trim().take(1).uppercase().ifBlank { "?" },
        Color(0xFF616161),
        Color.White
    )
}

object Api {
    private const val BASE =
        "https://script.google.com/macros/s/REDACTED/exec"

    fun fetch(): String {
        val conn = URL("$BASE?token=${BuildConfig.EXPEMEMO_TOKEN}&months=24").openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 45000
        try {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}

object Updater {
    private const val RELEASES_API =
        "https://api.github.com/repos/areepnanda/expememo-app/releases/latest"

    fun check(currentCode: Int): Pair<Int, String>? {
        val conn = URL(RELEASES_API).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val obj = JSONObject(body)
            val code = Regex("versionCode:\\s*(\\d+)")
                .find(obj.optString("body", ""))
                ?.groupValues?.get(1)?.toIntOrNull()
                ?: return null
            if (code <= currentCode) return null
            val assets = obj.getJSONArray("assets")
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.getString("name").endsWith(".apk")) {
                    return code to a.getString("browser_download_url")
                }
            }
            return null
        } finally {
            conn.disconnect()
        }
    }

    fun download(url: String, target: File, onProgress: (Int) -> Unit): File {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 120000
        val total = conn.contentLengthLong
        try {
            target.parentFile?.mkdirs()
            target.outputStream().use { out ->
                conn.inputStream.use { input ->
                    val buf = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        val read = input.read(buf)
                        if (read == -1) break
                        out.write(buf, 0, read)
                        done += read
                        onProgress(if (total > 0) (done * 100 / total).toInt() else -1)
                    }
                }
            }
        } finally {
            conn.disconnect()
        }
        return target
    }
}

fun canInstall(context: android.content.Context): Boolean =
    context.packageManager.canRequestPackageInstalls()

fun openUnknownSourcesSettings(context: android.content.Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:" + context.packageName)
        )
    )
}

fun installApk(context: android.content.Context, file: File): Boolean {
    if (!canInstall(context)) return false
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
        .onFailure { return false }
    return true
}

fun parseRows(json: String): List<Txn> {
    val obj = JSONObject(json)
    if (!obj.optBoolean("ok", false)) {
        throw Exception(obj.optString("error", "respon tidak valid"))
    }
    val arr = obj.getJSONArray("rows")
    return (0 until arr.length())
        .map { arr.getJSONObject(it) }
        .filter { it.getString("incomeExpense").equals("Expense", true) }
        .mapNotNull { o ->
            val dt = runCatching { LocalDateTime.parse(o.getString("date")) }.getOrNull()
                ?: return@mapNotNull null
            Txn(
                dt = dt,
                account = o.getString("account"),
                category = o.getString("category").trim(),
                icon = o.optString("icon", "").trim(),
                subcategory = o.optString("subcategory", "").trim(),
                note = o.getString("note").trim(),
                description = o.optString("description", "").trim(),
                amount = o.getDouble("amount")
            )
        }
}

fun rupiah(value: Double): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val df = DecimalFormat("#,##0", symbols)
    return "Rp " + df.format(value)
}

fun periodStart(dt: LocalDateTime): LocalDate {
    val base = LocalDate.of(dt.year, dt.month, 25)
    return if (dt.dayOfMonth >= 25) base else base.minusMonths(1)
}

fun periodLabel(start: LocalDate): String {
    val end = start.plusMonths(1).minusDays(1)
    val startPart = start.dayOfMonth.toString() + " " + MONTH_NAMES[start.monthValue - 1] +
        (if (start.year != end.year) " " + start.year else "")
    val endPart = end.dayOfMonth.toString() + " " + MONTH_NAMES[end.monthValue - 1] + " " + end.year
    return "$startPart – $endPart"
}

fun dayHeaderLabel(dt: LocalDateTime): String {
    val dayName = DAY_NAMES[dt.dayOfWeek.value - 1]
    return "$dayName, ${dt.dayOfMonth} ${MONTH_NAMES[dt.monthValue - 1]} ${dt.year}"
}

fun timeLabel(dt: LocalDateTime): String =
    "%02d:%02d".format(dt.hour, dt.minute)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var rows by remember { mutableStateOf<List<Txn>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var updateUrl by remember { mutableStateOf<String?>(null) }
    var updateCode by remember { mutableStateOf<Int?>(null) }
    var downloadProgress by remember { mutableStateOf<Int?>(null) }
    var pendingInstall by remember { mutableStateOf<File?>(null) }
    var detail by remember { mutableStateOf<Txn?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val f = pendingInstall
                if (f != null) {
                    pendingInstall = null
                    installApk(context, f)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                val body = withContext(Dispatchers.IO) { Api.fetch() }
                val parsed = parseRows(body)
                rows = parsed
                if (selected == null && parsed.isNotEmpty()) {
                    selected = parsed.maxOf { periodStart(it.dt) }.toString()
                }
            } catch (e: Exception) {
                error = e.message ?: "gagal memuat data"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        load()
        val update = runCatching {
            withContext(Dispatchers.IO) { Updater.check(BuildConfig.VERSION_CODE) }
        }.getOrNull()
        if (update != null) {
            updateCode = update.first
            updateUrl = update.second
        }
    }

    BackHandler(enabled = detail != null) { detail = null }

    if (detail != null) {
        DetailScreen(detail!!) { detail = null }
    } else {
        val periods = rows
            ?.map { periodStart(it.dt).toString() }
            ?.distinct()
            ?.sortedDescending()
            ?: emptyList()

        val shown = rows
            ?.filter { selected == null || periodStart(it.dt).toString() == selected }
            ?.sortedByDescending { it.dt }
            ?: emptyList()

        val totalExpense = shown.sumOf { it.amount }

        val listItems = buildList {
            shown.groupBy { it.dt.toLocalDate() }.forEach { (_, items) ->
                add(DateHeader(dayHeaderLabel(items.first().dt)))
                addAll(items)
            }
        }

        Scaffold(
            containerColor = DarkBg,
            topBar = {
                TopAppBar(
                    title = { Text("ExpeMemo") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = RedPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        TextButton(onClick = { load() }, enabled = !loading) {
                            Text(if (loading) "..." else "Refresh", color = Color.White)
                        }
                    }
                )
            }
        ) { pad ->
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
            ) {
                PeriodFilter(
                    periods = periods,
                    selected = selected,
                    onSelect = { selected = it }
                )

                if (shown.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Pengeluaran",
                                fontSize = 11.sp,
                                color = GrayText
                            )
                            Text(
                                rupiah(totalExpense),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RedAmount
                            )
                        }
                        Text(
                            "${shown.size} transaksi",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                    HorizontalDivider(color = DarkSurface)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        loading && rows == null -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = RedPrimary
                        )
                        error != null -> Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            OutlinedButton(onClick = { load() }) { Text("Coba lagi") }
                        }
                        rows != null && shown.isEmpty() -> Text(
                            "Tidak ada transaksi",
                            modifier = Modifier.align(Alignment.Center),
                            color = GrayText
                        )
                        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(listItems) { entry ->
                                when (entry) {
                                    is DateHeader -> Text(
                                        entry.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = RedOnContainer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                    is Txn -> TxnRow(entry) { detail = entry }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (updateUrl != null) {
        AlertDialog(
            onDismissRequest = { updateUrl = null },
            title = { Text("Update tersedia") },
            text = { Text("Versi baru (build #${updateCode}) tersedia. Download dan install sekarang?") },
            confirmButton = {
                TextButton(onClick = {
                    val url = updateUrl
                    updateUrl = null
                    if (url != null) {
                        downloadProgress = 0
                        scope.launch {
                            try {
                                val file = withContext(Dispatchers.IO) {
                                    val f = File(context.filesDir, "update/expememo-update.apk")
                                    if (f.exists()) f.delete()
                                    Updater.download(url, f) { p -> downloadProgress = p }
                                    f
                                }
                                downloadProgress = null
                                if (!installApk(context, file)) {
                                    pendingInstall = file
                                    openUnknownSourcesSettings(context)
                                }
                            } catch (e: Exception) {
                                downloadProgress = null
                                error = e.message ?: "download gagal"
                            }
                        }
                    }
                }) { Text("Download") }
            },
            dismissButton = {
                TextButton(onClick = { updateUrl = null }) { Text("Nanti") }
            }
        )
    }

    if (downloadProgress != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Mengunduh update") },
            text = {
                Column {
                    Text(
                        (if (downloadProgress ?: -1 < 0) "Mengunduh..." else "${downloadProgress}%"),
                        fontSize = 13.sp,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    LinearProgressIndicator(
                        progress = { (downloadProgress ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = RedPrimary
                    )
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
fun PeriodFilter(
    periods: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    if (periods.isEmpty()) return
    var open by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = true,
            onClick = { open = true },
            label = {
                Text(
                    (if (selected == null) "Semua periode" else periodLabel(LocalDate.parse(selected))) + "  ▾"
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            periods.forEach { p ->
                DropdownMenuItem(
                    text = { Text(periodLabel(LocalDate.parse(p))) },
                    onClick = {
                        onSelect(p)
                        open = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Semua periode") },
                onClick = {
                    onSelect(null)
                    open = false
                }
            )
        }
    }
}

@Composable
fun TxnRow(t: Txn, onClick: () -> Unit) {
    val avatar = bankAvatar(t.account)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = avatar.bg,
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    avatar.letter,
                    color = avatar.fg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                t.note.ifBlank { "(tanpa catatan)" },
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    timeLabel(t.dt),
                    fontSize = 12.sp,
                    color = GrayText
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (t.icon.isNotBlank()) {
                    Text(
                        t.icon,
                        fontSize = 15.sp
                    )
                } else if (t.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkBadge
                    ) {
                        Text(
                            t.category,
                            fontSize = 11.sp,
                            color = GrayText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        Text(
            rupiah(t.amount),
            fontWeight = FontWeight.SemiBold,
            color = RedAmount,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(t: Txn, onBack: () -> Unit) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Detail Transaksi") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", color = Color.White, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                rupiah(t.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = RedAmount
            )
            Text(
                dayHeaderLabel(t.dt) + " • " + timeLabel(t.dt),
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    DetailRow("Bank", t.account)
                    DetailRow(
                        "Kategori",
                        (if (t.icon.isNotBlank()) t.icon + " " else "") + t.category.ifBlank { "-" }
                    )
                    if (t.subcategory.isNotBlank()) {
                        DetailRow("Subkategori", t.subcategory)
                    }
                    DetailRow("Catatan", t.note.ifBlank { "-" })
                    if (t.description.isNotBlank()) {
                        DetailRow("Keterangan", t.description)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            label,
            color = GrayText,
            fontSize = 13.sp,
            modifier = Modifier.width(110.dp)
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}
