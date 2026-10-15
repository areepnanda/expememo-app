package com.arief.expememo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

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
private val DarkBg = Color(0xFF1E1E1E)
private val DarkSurface = Color(0xFF2A2A2A)
private val DarkBadge = Color(0xFF3A3A3A)
private val CardBorder = Color(0xFF3D3D3D)
private val Maroon = Color(0xFF800000)
private val White = Color.White

private fun makeIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = addPathNodes(pathData),
            fill = SolidColor(Color.Black)
        )
    }.build()

private val IconReceipt = makeIcon(
    "Receipt",
    "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z"
)

private val IconBarChart = makeIcon(
    "BarChart",
    "M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z"
)

private val IconBank = makeIcon(
    "Bank",
    "M4 10h3v7H4zm6.5 0h3v7h-3zM2 19h20v3H2zm15.5-9h3v7h-3zM12 1L2 6v2h20V6z"
)

private val CHART_COLORS = listOf(
    Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFDD835),
    Color(0xFF43A047), Color(0xFF00ACC1), Color(0xFF1E88E5),
    Color(0xFF8E24AA), Color(0xFFD81B60), Color(0xFF6D4C41),
    Color(0xFF7CB342), Color(0xFF00897B), Color(0xFF3949AB),
    Color(0xFFF4511E), Color(0xFFC0CA33)
)

private val AppColors = darkColorScheme(
    primary = RedPrimary,
    onPrimary = White,
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

data class DateGroup(val label: String, val items: List<Txn>)

data class CategoryStat(
    val name: String,
    val icon: String,
    val total: Double,
    val count: Int,
    val txs: List<Txn>
)

data class BankStat(
    val name: String,
    val total: Double,
    val count: Int
)

data class BankAvatar(val letter: String, val bg: Color, val fg: Color)

fun bankAvatar(account: String): BankAvatar = when {
    account.contains("bca", true) -> BankAvatar("B", Color(0xFF0A5CA8), White)
    account.contains("mandiri", true) -> BankAvatar("M", Color(0xFFF7B600), Color(0xFF0B2E6B))
    account.contains("jenius", true) -> BankAvatar("J", Color(0xFF00AAB8), White)
    else -> BankAvatar(
        account.trim().take(1).uppercase().ifBlank { "?" },
        Color(0xFF616161),
        White
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

private fun shortNum(n: Double): String {
    val rounded = (n * 10).roundToInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) rounded.toInt().toString()
    else rounded.toString().replace('.', ',')
}

fun shortRupiah(value: Double): String = when {
    value >= 1_000_000 -> shortNum(value / 1_000_000) + "jt"
    value >= 1_000 -> shortNum(value / 1_000) + "rb"
    else -> value.roundToInt().toString()
}

private fun niceCeil(v: Double): Double {
    if (v <= 0.0) return 1.0
    val base = 10.0.pow(floor(log10(v)))
    val f = v / base
    val nice = when {
        f <= 1.0 -> 1.0
        f <= 2.0 -> 2.0
        f <= 2.5 -> 2.5
        f <= 5.0 -> 5.0
        else -> 10.0
    }
    return nice * base
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

fun categoryStats(txs: List<Txn>): List<CategoryStat> =
    txs.groupBy { it.category.ifBlank { "Tanpa kategori" } }
        .map { (name, items) ->
            CategoryStat(
                name = name,
                icon = items.firstOrNull { it.icon.isNotBlank() }?.icon ?: "",
                total = items.sumOf { it.amount },
                count = items.size,
                txs = items.sortedByDescending { it.dt }
            )
        }
        .sortedByDescending { it.total }

@Composable
fun CompactTopBar(
    onBack: (() -> Unit)? = null,
    title: @Composable () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    Surface(color = Maroon) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val step = 8.dp.toPx()
                val stroke = 1.dp.toPx()
                val h = size.height
                var x = -h
                while (x < size.width + h) {
                    drawLine(
                        color = White.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x + h, h),
                        strokeWidth = stroke
                    )
                    x += step
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = if (onBack != null) 4.dp else 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) { title() }
                }
                Box { actions() }
            }
        }
    }
    HorizontalDivider(color = CardBorder)
}

@Composable
fun CompactBottomBar(tab: Int, onTabChange: (Int) -> Unit) {
    Surface(color = DarkSurface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            BottomBarItem(IconReceipt, "Transaksi", tab == 0) { onTabChange(0) }
            BottomBarItem(IconBarChart, "Statistik", tab == 1) { onTabChange(1) }
            BottomBarItem(IconBank, "Bank", tab == 2) { onTabChange(2) }
        }
    }
}

@Composable
fun RowScope.BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) RedPrimary else GrayText,
            modifier = Modifier.size(20.dp)
        )
        Text(
            label,
            fontSize = 9.sp,
            color = if (selected) RedPrimary else GrayText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var rows by remember { mutableStateOf<List<Txn>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }
    var detail by remember { mutableStateOf<Txn?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var updateUrl by remember { mutableStateOf<String?>(null) }
    var updateCode by remember { mutableStateOf<Int?>(null) }
    var downloadProgress by remember { mutableStateOf<Int?>(null) }
    var pendingInstall by remember { mutableStateOf<File?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var bankFilter by remember { mutableStateOf<String?>(null) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
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
    BackHandler(enabled = detail == null && selectedCategory != null) { selectedCategory = null }

    val shown = rows
        ?.filter { selected == null || periodStart(it.dt).toString() == selected }
        ?.sortedByDescending { it.dt }
        ?: emptyList()

    if (detail != null) {
        DetailScreen(detail!!) { detail = null }
    } else if (selectedCategory != null) {
        CategoryScreen(
            stat = categoryStats(shown).firstOrNull { it.name == selectedCategory }
                ?: CategoryStat(selectedCategory ?: "", "", 0.0, 0, emptyList()),
            onBack = { selectedCategory = null },
            onOpenTxn = { detail = it }
        )
    } else {
        MainScreen(
            tab = tab,
            onTabChange = { tab = it },
            loading = loading,
            error = error,
            rowsLoaded = rows != null,
            shown = shown,
            periods = rows
                ?.map { periodStart(it.dt).toString() }
                ?.distinct()
                ?.sortedDescending()
                ?: emptyList(),
            selectedPeriod = selected,
            onPeriodChange = { selected = it },
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            bankFilter = bankFilter,
            onBankFilterChange = { bankFilter = it },
            categoryFilter = categoryFilter,
            onCategoryFilterChange = { categoryFilter = it },
            onReload = { load() },
            onOpenTxn = { detail = it },
            onOpenCategory = { selectedCategory = it }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tab: Int,
    onTabChange: (Int) -> Unit,
    loading: Boolean,
    error: String?,
    rowsLoaded: Boolean,
    shown: List<Txn>,
    periods: List<String>,
    selectedPeriod: String?,
    onPeriodChange: (String?) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    bankFilter: String?,
    onBankFilterChange: (String?) -> Unit,
    categoryFilter: String?,
    onCategoryFilterChange: (String?) -> Unit,
    onReload: () -> Unit,
    onOpenTxn: (Txn) -> Unit,
    onOpenCategory: (String) -> Unit
) {
    val stats = categoryStats(shown)

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            CompactTopBar(
                title = {
                    var open by remember { mutableStateOf(false) }

                    Box {
                        Row(
                            modifier = Modifier.clickable { open = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedPeriod == null) "Semua periode"
                                else periodLabel(LocalDate.parse(selectedPeriod)),
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Pilih periode",
                                tint = White
                            )
                        }

                        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                            periods.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(periodLabel(LocalDate.parse(p))) },
                                    onClick = {
                                        onPeriodChange(p)
                                        open = false
                                    }
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Semua periode") },
                                onClick = {
                                    onPeriodChange(null)
                                    open = false
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            CompactBottomBar(tab = tab, onTabChange = onTabChange)
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                isRefreshing = loading,
                onRefresh = onReload,
                modifier = Modifier.fillMaxSize()
            ) {
                when (tab) {
                    0 -> TransactionTab(
                        loading = loading,
                        error = error,
                        rowsLoaded = rowsLoaded,
                        shown = shown,
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange,
                        bankFilter = bankFilter,
                        onBankFilterChange = onBankFilterChange,
                        categoryFilter = categoryFilter,
                        onCategoryFilterChange = onCategoryFilterChange,
                        onReload = onReload,
                        onOpenTxn = onOpenTxn
                    )
                    1 -> StatistikTab(
                        shown = shown,
                        stats = stats,
                        onOpenCategory = onOpenCategory
                    )
                    2 -> BankTab(shown = shown)
                }
            }
        }
    }
}

@Composable
fun TransactionTab(
    loading: Boolean,
    error: String?,
    rowsLoaded: Boolean,
    shown: List<Txn>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    bankFilter: String?,
    onBankFilterChange: (String?) -> Unit,
    categoryFilter: String?,
    onCategoryFilterChange: (String?) -> Unit,
    onReload: () -> Unit,
    onOpenTxn: (Txn) -> Unit
) {
    val banks = remember(shown) {
        shown.map { it.account }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val categories = remember(shown) {
        shown.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val catIconOf = remember(shown) {
        shown.groupBy { it.category }.mapValues { (_, v) ->
            v.firstOrNull { it.icon.isNotBlank() }?.icon ?: ""
        }
    }

    val filtered = shown.filter { t ->
        (searchQuery.isBlank() || t.note.contains(searchQuery, ignoreCase = true)) &&
            (bankFilter == null || t.account == bankFilter) &&
            (categoryFilter == null || t.category == categoryFilter)
    }
    val totalExpense = filtered.sumOf { it.amount }
    val dateGroups = filtered
        .groupBy { it.dt.toLocalDate() }
        .map { (_, items) ->
            DateGroup(dayHeaderLabel(items.first().dt), items)
        }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading && !rowsLoaded -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = RedPrimary
            )
            error != null -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                OutlinedButton(onClick = onReload) { Text("Coba lagi") }
            }
            rowsLoaded && shown.isEmpty() -> Text(
                "Tidak ada transaksi",
                modifier = Modifier.align(Alignment.Center),
                color = GrayText
            )
            else -> Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    singleLine = true,
                    placeholder = {
                        Text("Cari catatan…", fontSize = 13.sp, color = GrayText)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Cari",
                            tint = GrayText,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    textStyle = TextStyle(fontSize = 13.sp, color = White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterDropdownChip(
                        label = "Bank",
                        value = bankFilter,
                        options = banks,
                        onSelect = onBankFilterChange
                    )
                    FilterDropdownChip(
                        label = "Kategori",
                        value = categoryFilter,
                        options = categories,
                        optionLabel = { c ->
                            val ic = catIconOf[c] ?: ""
                            if (ic.isBlank()) c else "$ic $c"
                        },
                        onSelect = onCategoryFilterChange
                    )
                }
                HorizontalDivider(
                    color = CardBorder,
                    modifier = Modifier.padding(top = 6.dp)
                )
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchQuery.isNotBlank() || bankFilter != null || categoryFilter != null)
                                "Tidak ada yang cocok dengan filter"
                            else "Tidak ada transaksi",
                            color = GrayText
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Pengeluaran",
                                fontSize = 10.sp,
                                color = GrayText
                            )
                            Text(
                                rupiah(totalExpense),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = RedAmount
                            )
                        }
                        Text(
                            "${filtered.size} transaksi",
                            fontSize = 10.sp,
                            color = GrayText
                        )
                    }
                    HorizontalDivider(color = CardBorder)
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(dateGroups) { group ->
                            DateGroupCard(group) { onOpenTxn(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDropdownChip(
    label: String,
    value: String?,
    options: List<String>,
    optionLabel: (String) -> String = { it },
    onSelect: (String?) -> Unit
) {
    var open by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = value != null,
            onClick = { open = true },
            label = {
                Text(
                    if (value == null) label else optionLabel(value),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("$label: Semua") },
                onClick = {
                    onSelect(null)
                    open = false
                }
            )
            if (options.isNotEmpty()) HorizontalDivider()
            options.forEach { o ->
                DropdownMenuItem(
                    text = { Text(optionLabel(o), fontSize = 13.sp) },
                    onClick = {
                        onSelect(o)
                        open = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikTab(
    shown: List<Txn>,
    stats: List<CategoryStat>,
    onOpenCategory: (String) -> Unit
) {
    if (shown.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "Tidak ada transaksi",
                modifier = Modifier.align(Alignment.Center),
                color = GrayText
            )
        }
        return
    }

    val totalExpense = shown.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Total Pengeluaran",
                        fontSize = 10.sp,
                        color = GrayText,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        rupiah(totalExpense),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RedAmount
                    )
                    Text(
                        "${shown.size} transaksi",
                        fontSize = 10.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    BarChart(
                        stats = stats,
                        onOpenCategory = onOpenCategory
                    )
                }
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Column {
                    stats.forEachIndexed { i, s ->
                        if (i > 0) HorizontalDivider(color = CardBorder)
                        CategoryRow(
                            stat = s,
                            percent = (s.total / totalExpense * 100).toInt(),
                            color = CHART_COLORS[i % CHART_COLORS.size],
                            onClick = { onOpenCategory(s.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarChart(
    stats: List<CategoryStat>,
    onOpenCategory: (String) -> Unit
) {
    if (stats.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = GrayText)
    val niceMax = niceCeil(stats.maxOf { it.total })
    val leftPad = 38.dp
    val gridCount = 4

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .pointerInput(stats) {
                    detectTapGestures { pos ->
                        val pad = leftPad.toPx()
                        if (pos.x < pad) return@detectTapGestures
                        val slot = (size.width - pad) / stats.size
                        val idx = ((pos.x - pad) / slot)
                            .toInt()
                            .coerceIn(0, stats.size - 1)
                        onOpenCategory(stats[idx].name)
                    }
                }
        ) {
            val pad = leftPad.toPx()
            val bottomPad = 6.dp.toPx()
            val chartW = size.width - pad
            val chartH = size.height - bottomPad

            for (i in 0..gridCount) {
                val y = chartH - chartH * i / gridCount
                drawLine(
                    color = CardBorder,
                    start = Offset(pad, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                val value = niceMax * i / gridCount
                val text = textMeasurer.measure(shortRupiah(value), labelStyle)
                drawText(
                    textLayoutResult = text,
                    topLeft = Offset(
                        pad - 6.dp.toPx() - text.size.width,
                        y - text.size.height / 2f
                    )
                )
            }

            val slot = chartW / stats.size
            val barWidth = (slot * 0.55f).coerceAtMost(32.dp.toPx())
            stats.forEachIndexed { i, s ->
                val barHeight = ((s.total / niceMax).toFloat() * chartH).coerceAtLeast(2.dp.toPx())
                val left = pad + slot * i + (slot - barWidth) / 2f
                drawRoundRect(
                    color = CHART_COLORS[i % CHART_COLORS.size],
                    topLeft = Offset(left, chartH - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = leftPad)
        ) {
            stats.forEach { s ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        s.icon.ifBlank { s.name.take(1) },
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun BankTab(shown: List<Txn>) {
    if (shown.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "Tidak ada transaksi",
                modifier = Modifier.align(Alignment.Center),
                color = GrayText
            )
        }
        return
    }

    val total = shown.sumOf { it.amount }
    val bankStats = remember(shown) {
        shown.groupBy { it.account.trim().ifBlank { "Tanpa nama" } }
            .map { (name, items) -> BankStat(name, items.sumOf { it.amount }, items.size) }
            .sortedByDescending { it.total }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Total Pengeluaran",
                    fontSize = 10.sp,
                    color = GrayText
                )
                Text(
                    rupiah(total),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = RedAmount
                )
            }
            Text(
                "${shown.size} transaksi",
                fontSize = 10.sp,
                color = GrayText
            )
        }
        HorizontalDivider(color = CardBorder)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Column {
                        bankStats.forEachIndexed { i, b ->
                            if (i > 0) HorizontalDivider(color = CardBorder)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val avatar = bankAvatar(b.name)
                                Surface(
                                    shape = CircleShape,
                                    color = avatar.bg,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            avatar.letter,
                                            color = avatar.fg,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        b.name,
                                        fontWeight = FontWeight.Medium,
                                        color = White,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${b.count} transaksi",
                                        fontSize = 10.sp,
                                        color = GrayText
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        rupiah(b.total),
                                        fontWeight = FontWeight.SemiBold,
                                        color = RedAmount,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "${(b.total / total * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        color = GrayText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRow(
    stat: CategoryStat,
    percent: Int,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    stat.icon.ifBlank { stat.name.take(1).uppercase() },
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stat.name,
                fontWeight = FontWeight.Medium,
                color = White,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "$percent% • ${stat.count} transaksi",
                fontSize = 10.sp,
                color = GrayText
            )
        }
        Text(
            rupiah(stat.total),
            fontWeight = FontWeight.SemiBold,
            color = RedAmount,
            fontSize = 13.sp
        )
    }
}

@Composable
fun DateGroupCard(group: DateGroup, onOpenDetail: (Txn) -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    group.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = RedOnContainer
                )
                Text(
                    rupiah(group.items.sumOf { it.amount }),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = RedAmount
                )
            }
            group.items.forEachIndexed { i, t ->
                if (i > 0) HorizontalDivider(color = CardBorder)
                TxnRow(t) { onOpenDetail(t) }
            }
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
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = avatar.bg,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    avatar.letter,
                    color = avatar.fg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                t.note.ifBlank { "(tanpa catatan)" },
                fontWeight = FontWeight.Medium,
                color = White,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    timeLabel(t.dt),
                    fontSize = 10.sp,
                    color = GrayText
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (t.icon.isNotBlank()) {
                    Text(
                        t.icon,
                        fontSize = 12.sp
                    )
                } else if (t.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkBadge
                    ) {
                        Text(
                            t.category,
                            fontSize = 10.sp,
                            color = GrayText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
        Text(
            rupiah(t.amount),
            fontWeight = FontWeight.SemiBold,
            color = RedAmount,
            fontSize = 13.sp
        )
    }
}

@Composable
fun CategoryScreen(
    stat: CategoryStat,
    onBack: () -> Unit,
    onOpenTxn: (Txn) -> Unit
) {
    val dateGroups = stat.txs
        .groupBy { it.dt.toLocalDate() }
        .map { (date, items) ->
            DateGroup(dayHeaderLabel(items.first().dt), items)
        }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            CompactTopBar(
                onBack = onBack,
                title = {
                    Text(
                        (if (stat.icon.isNotBlank()) stat.icon + " " else "") + stat.name,
                        color = White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Pengeluaran",
                        fontSize = 10.sp,
                        color = GrayText
                    )
                    Text(
                        rupiah(stat.total),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RedAmount
                    )
                }
                Text(
                    "${stat.count} transaksi",
                    fontSize = 10.sp,
                    color = GrayText
                )
            }
            HorizontalDivider(color = CardBorder)
            if (stat.txs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "Tidak ada transaksi",
                        modifier = Modifier.align(Alignment.Center),
                        color = GrayText
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(dateGroups) { group ->
                        DateGroupCard(group) { onOpenTxn(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreen(t: Txn, onBack: () -> Unit) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            CompactTopBar(
                onBack = onBack,
                title = {
                    Text(
                        "Detail Transaksi",
                        color = White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
                fontSize = 26.sp,
                color = RedAmount
            )
            Text(
                dayHeaderLabel(t.dt) + " • " + timeLabel(t.dt),
                fontSize = 11.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            color = GrayText,
            fontSize = 11.sp,
            modifier = Modifier.width(96.dp)
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = White,
            modifier = Modifier.weight(1f)
        )
    }
}
