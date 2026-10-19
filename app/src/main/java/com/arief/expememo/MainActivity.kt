package com.arief.expememo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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

private val IconLock = makeIcon(
    "Lock",
    "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zM9 8V6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9z"
)

private val IconLockOpen = makeIcon(
    "LockOpen",
    "M12 17c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm6-9h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6h2c0-1.66 1.34-3 3-3s3 1.34 3 3v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm0 12H6V10h12v10z"
)

private val IconBackspace = makeIcon(
    "Backspace",
    "M22 3H7c-.69 0-1.23.35-1.59.88L0 12l5.41 8.11c.36.53.9.89 1.59.89h15c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-3 12.59L17.59 17 14 13.41 10.41 17 9 15.59 12.59 12 9 8.41 10.41 7 14 10.59 17.59 7 19 8.41 15.41 12 19 15.59z"
)

private val IconEdit = makeIcon(
    "Edit",
    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
)

private val IconLabel = makeIcon(
    "Label",
    "M17.63 5.84C17.27 5.33 16.67 5 16 5L5 5.01C3.9 5.01 3 5.9 3 7v10c0 1.1.9 1.99 2 1.99L16 19c.67 0 1.27-.33 1.63-.84L22 12l-4.37-6.16z"
)

private const val PREFS_NAME = "expememo_prefs"
private const val KEY_BIOMETRIC = "biometric_enabled"
private const val KEY_LOCK_METHOD = "lock_method"
private const val KEY_PIN_HASH = "pin_hash"
private const val KEY_PIN_SALT = "pin_salt"
private const val KEY_PIN_LENGTH = "pin_length"
private const val LOCK_METHOD_BIOMETRIC = "biometric"
private const val LOCK_METHOD_PIN = "pin"
private const val LOCK_GRACE_MS = 2 * 60 * 1000L

private val AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

fun biometricAvailable(context: Context): Boolean =
    BiometricManager.from(context)
        .canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

fun biometricEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_BIOMETRIC, true)

fun setBiometricEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_BIOMETRIC, enabled)
        .apply()
}

fun lockMethod(context: Context): String =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LOCK_METHOD, LOCK_METHOD_BIOMETRIC) ?: LOCK_METHOD_BIOMETRIC

fun setLockMethod(context: Context, method: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_LOCK_METHOD, method)
        .apply()
}

fun savedPin(context: Context): Pair<String, Int>? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val hash = prefs.getString(KEY_PIN_HASH, null)
    val len = prefs.getInt(KEY_PIN_LENGTH, 0)
    return if (hash.isNullOrEmpty() || len < 4) null else hash to len
}

fun savePin(context: Context, pin: String) {
    val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        .joinToString("") { "%02x".format(it) }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_PIN_SALT, salt)
        .putString(KEY_PIN_HASH, sha256Hex(salt + pin))
        .putInt(KEY_PIN_LENGTH, pin.length)
        .apply()
}

fun verifyPin(context: Context, pin: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val hash = prefs.getString(KEY_PIN_HASH, null) ?: return false
    val salt = prefs.getString(KEY_PIN_SALT, "") ?: ""
    return hash == sha256Hex(salt + pin)
}

fun lockActive(context: Context): Boolean =
    biometricEnabled(context) && when (lockMethod(context)) {
        LOCK_METHOD_PIN -> savedPin(context) != null
        else -> biometricAvailable(context)
    }

private fun sha256Hex(value: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }

private const val KEY_API_URL = "api_url"
private const val KEY_API_TOKEN = "api_token"

data class ApiConfig(val url: String, val token: String)

private fun apiPrefs(context: Context) =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

fun apiValueSaved(context: Context, key: String): Boolean =
    !apiPrefs(context).getString(key, "")?.trim().isNullOrEmpty()

fun loadApiConfig(context: Context): ApiConfig? {
    val url = apiPrefs(context).getString(KEY_API_URL, "")?.trim().orEmpty()
    val token = apiPrefs(context).getString(KEY_API_TOKEN, "")?.trim().orEmpty()
    return if (url.isBlank() || token.isBlank()) null else ApiConfig(url, token)
}

fun saveApiConfig(context: Context, url: String, token: String) {
    apiPrefs(context).edit()
        .putString(KEY_API_URL, url)
        .putString(KEY_API_TOKEN, token)
        .apply()
}

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

class MainActivity : FragmentActivity() {
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
    val id: String,
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

data class CategoryDef(
    val name: String,
    val icon: String,
    val keywords: List<String>,
    val used: Int
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
    private fun enc(s: String): String =
        try {
            URLEncoder.encode(s, "UTF-8")
        } catch (_: Exception) {
            s
        }

    fun fetch(url: String, token: String, query: String): String {
        val sep = if (url.contains("?")) "&" else "?"
        val full = url + sep + "token=" + enc(token) + "&" + query
        val conn = URL(full).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 45000
        try {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    fun update(
        url: String,
        token: String,
        id: String,
        category: String,
        subcategory: String,
        note: String,
        amount: Double,
        description: String
    ): String {
        val query = "action=update" +
            "&id=" + enc(id) +
            "&category=" + enc(category) +
            "&subcategory=" + enc(subcategory) +
            "&note=" + enc(note) +
            "&amount=" + amount +
            "&description=" + enc(description)
        return fetch(url, token, query)
    }

    fun rulesList(url: String, token: String): String =
        fetch(url, token, "action=rules.list")

    fun rulesSave(
        url: String,
        token: String,
        old: String,
        name: String,
        icon: String,
        keywords: String
    ): String {
        val query = "action=rules.save" +
            "&name=" + enc(name) +
            "&icon=" + enc(icon) +
            "&keywords=" + enc(keywords) +
            (if (old.isNotBlank()) "&old=" + enc(old) else "")
        return fetch(url, token, query)
    }

    fun rulesDelete(url: String, token: String, name: String): String =
        fetch(url, token, "action=rules.delete&name=" + enc(name))
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
                id = o.optString("id", "").trim(),
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

fun parseCategoryDefs(json: String): List<CategoryDef> {
    val obj = JSONObject(json)
    if (!obj.optBoolean("ok", false)) {
        throw Exception(obj.optString("error", "respon tidak valid"))
    }
    val arr = obj.getJSONArray("categories")
    return (0 until arr.length())
        .map { arr.getJSONObject(it) }
        .mapNotNull { o ->
            val name = o.optString("name", "").trim()
            if (name.isBlank()) return@mapNotNull null
            val kwArr = o.optJSONArray("keywords")
            CategoryDef(
                name = name,
                icon = o.optString("icon", "").trim(),
                keywords = if (kwArr == null) emptyList()
                else (0 until kwArr.length())
                    .map { kwArr.optString(it, "").trim() }
                    .filter { it.isNotBlank() },
                used = o.optInt("used", 0)
            )
        }
        .sortedBy { it.name.lowercase() }
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

sealed interface PeriodSelection {
    data class Monthly(val start: LocalDate) : PeriodSelection
    data class Custom(val start: LocalDate, val end: LocalDate) : PeriodSelection
    data object All : PeriodSelection
}

fun periodLabel(start: LocalDate): String =
    rangeLabel(start, start.plusMonths(1).minusDays(1))

fun rangeLabel(start: LocalDate, end: LocalDate): String {
    val startPart = start.dayOfMonth.toString() + " " + MONTH_NAMES[start.monthValue - 1] +
        (if (start.year != end.year) " " + start.year else "")
    val endPart = end.dayOfMonth.toString() + " " + MONTH_NAMES[end.monthValue - 1] + " " + end.year
    return "$startPart – $endPart"
}

fun selectionLabel(sel: PeriodSelection): String = when (sel) {
    is PeriodSelection.Monthly -> periodLabel(sel.start)
    is PeriodSelection.Custom -> rangeLabel(sel.start, sel.end)
    PeriodSelection.All -> "Semua periode"
}

fun recentPeriodOptions(count: Int = 3): List<LocalDate> {
    val cur = periodStart(LocalDate.now().atStartOfDay())
    return (0 until count).map { cur.minusMonths(it.toLong()) }
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
    onMenu: (() -> Unit)? = null,
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
                    .padding(start = if (onBack != null || onMenu != null) 4.dp else 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onMenu != null) {
                    IconButton(onClick = onMenu, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (onBack != null) {
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
    var selected: PeriodSelection by remember {
        mutableStateOf<PeriodSelection>(
            PeriodSelection.Monthly(periodStart(LocalDate.now().atStartOfDay()))
        )
    }
    var showCustomPeriod by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }
    var detail by remember { mutableStateOf<Txn?>(null) }
    var editingTxn by remember { mutableStateOf<Txn?>(null) }
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
    var locked by remember { mutableStateOf(lockActive(context)) }
    var lastStoppedAt by remember { mutableStateOf(0L) }
    var showSettings by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(false) }
    var categoryDefs by remember { mutableStateOf<List<CategoryDef>?>(null) }
    var catLoading by remember { mutableStateOf(false) }
    var catError by remember { mutableStateOf<String?>(null) }
    var catAddOpen by remember { mutableStateOf(false) }
    var catEditTarget by remember { mutableStateOf<CategoryDef?>(null) }
    var catDeleteTarget by remember { mutableStateOf<CategoryDef?>(null) }
    var hasConfig by remember { mutableStateOf(loadApiConfig(context) != null) }
    var reloadTick by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val txnListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val catListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                lastStoppedAt = SystemClock.elapsedRealtime()
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                val f = pendingInstall
                if (f != null) {
                    pendingInstall = null
                    installApk(context, f)
                }
                val stopped = lastStoppedAt
                if (stopped > 0) {
                    lastStoppedAt = 0
                    if (SystemClock.elapsedRealtime() - stopped > LOCK_GRACE_MS &&
                        lockActive(context)
                    ) {
                        locked = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var fetchSeq by remember { mutableStateOf(0) }

    fun load() {
        val cfg = loadApiConfig(context)
        if (cfg == null) {
            error = "URL & token API belum diatur. Buka menu ☰ → Pengaturan."
            return
        }
        loading = true
        error = null
        val sel = selected
        val seq = ++fetchSeq
        scope.launch {
            try {
                val query = when (sel) {
                    is PeriodSelection.All -> "months=24"
                    is PeriodSelection.Monthly ->
                        "from=${sel.start}&to=${sel.start.plusMonths(1).minusDays(1)}"
                    is PeriodSelection.Custom -> "from=${sel.start}&to=${sel.end}"
                }
                val body = withContext(Dispatchers.IO) { Api.fetch(cfg.url, cfg.token, query) }
                val parsed = parseRows(body)
                if (seq == fetchSeq) rows = parsed
            } catch (e: Exception) {
                if (seq == fetchSeq) error = e.message ?: "gagal memuat data"
            } finally {
                if (seq == fetchSeq) loading = false
            }
        }
    }

    fun saveSettings(newUrl: String, newToken: String): String? {
        val cur = loadApiConfig(context)
        val url = newUrl.ifBlank { cur?.url ?: "" }
        val token = newToken.ifBlank { cur?.token ?: "" }
        if (url.isBlank() || token.isBlank()) return "URL dan token wajib diisi"
        if (!url.startsWith("https://", true)) return "URL harus diawali https://"
        val changed = cur?.url != url || cur?.token != token
        saveApiConfig(context, url, token)
        hasConfig = true
        showSettings = false
        if (changed) reloadTick++
        return null
    }

    fun loadCategoryDefs() {
        val cfg = loadApiConfig(context)
        if (cfg == null) {
            catError = "URL & token API belum diatur"
            return
        }
        catLoading = true
        catError = null
        scope.launch {
            try {
                val body = withContext(Dispatchers.IO) { Api.rulesList(cfg.url, cfg.token) }
                categoryDefs = parseCategoryDefs(body)
            } catch (e: Exception) {
                catError = e.message ?: "gagal memuat kategori"
            } finally {
                catLoading = false
            }
        }
    }

    val saveCategoryDef: suspend (String?, String, String, List<String>) -> String? = { old, name, icon, keywords ->
        val cfg = loadApiConfig(context)
        when {
            cfg == null -> "URL & token API belum diatur"
            else -> try {
                val body = withContext(Dispatchers.IO) {
                    Api.rulesSave(
                        url = cfg.url,
                        token = cfg.token,
                        old = old ?: "",
                        name = name,
                        icon = icon,
                        keywords = keywords.joinToString(",")
                    )
                }
                val obj = JSONObject(body)
                if (!obj.optBoolean("ok", false)) {
                    obj.optString("error", "").ifBlank { "gagal menyimpan" }
                } else null
            } catch (e: Exception) {
                e.message?.ifBlank { null } ?: "gagal menyimpan"
            }
        }
    }

    val deleteCategoryDef: suspend (CategoryDef) -> String? = { def ->
        val cfg = loadApiConfig(context)
        when {
            cfg == null -> "URL & token API belum diatur"
            else -> try {
                val body = withContext(Dispatchers.IO) {
                    Api.rulesDelete(cfg.url, cfg.token, def.name)
                }
                val obj = JSONObject(body)
                if (!obj.optBoolean("ok", false)) {
                    obj.optString("error", "").ifBlank { "gagal menghapus" }
                } else null
            } catch (e: Exception) {
                e.message?.ifBlank { null } ?: "gagal menghapus"
            }
        }
    }

    LaunchedEffect(Unit) {
        val update = runCatching {
            withContext(Dispatchers.IO) { Updater.check(BuildConfig.VERSION_CODE) }
        }.getOrNull()
        if (update != null) {
            updateCode = update.first
            updateUrl = update.second
        }
    }

    LaunchedEffect(hasConfig) {
        if (!hasConfig) showSettings = true
    }

    LaunchedEffect(hasConfig, reloadTick) {
        if (hasConfig) load()
    }

    LaunchedEffect(showCategories, hasConfig, reloadTick) {
        if (showCategories && hasConfig) loadCategoryDefs()
    }

    BackHandler(enabled = detail != null) { detail = null }
    BackHandler(enabled = detail == null && selectedCategory != null) { selectedCategory = null }
    BackHandler(enabled = showCategories && hasConfig) { showCategories = false }
    BackHandler(enabled = showSettings && hasConfig) { showSettings = false }

    val saveTxnEdit: suspend (Txn) -> String? = { updated ->
        val cfg = loadApiConfig(context)
        when {
            cfg == null -> "URL & token API belum diatur"
            else -> try {
                val body = withContext(Dispatchers.IO) {
                    Api.update(
                        url = cfg.url,
                        token = cfg.token,
                        id = updated.id,
                        category = updated.category,
                        subcategory = updated.subcategory,
                        note = updated.note,
                        amount = updated.amount,
                        description = updated.description
                    )
                }
                val obj = JSONObject(body)
                if (!obj.optBoolean("ok", false)) {
                    obj.optString("error", "").ifBlank { "gagal menyimpan" }
                } else null
            } catch (e: Exception) {
                e.message?.ifBlank { null } ?: "gagal menyimpan"
            }
        }
    }

    val sel = selected
    val shown = rows
        ?.filter {
            val d = it.dt.toLocalDate()
            when (sel) {
                is PeriodSelection.All -> true
                is PeriodSelection.Monthly ->
                    !d.isBefore(sel.start) && d.isBefore(sel.start.plusMonths(1))
                is PeriodSelection.Custom ->
                    !d.isBefore(sel.start) && !d.isAfter(sel.end)
            }
        }
        ?.sortedByDescending { it.dt }
        ?: emptyList()

    if (locked) {
        LockScreen(onUnlocked = { locked = false })
    } else if (showSettings) {
        SettingsScreen(
            urlSaved = apiValueSaved(context, KEY_API_URL),
            tokenSaved = apiValueSaved(context, KEY_API_TOKEN),
            allowBack = hasConfig,
            onBack = { showSettings = false },
            onSave = { u, t -> saveSettings(u, t) }
        )
    } else if (showCategories) {
        CategoryManageScreen(
            categories = categoryDefs,
            loading = catLoading,
            error = catError,
            onBack = { showCategories = false },
            onRetry = { loadCategoryDefs() },
            onAdd = { catAddOpen = true },
            onEdit = { catEditTarget = it },
            onDelete = { catDeleteTarget = it }
        )
    } else if (detail != null) {
        DetailScreen(
            t = detail!!,
            onBack = { detail = null },
            onEdit = if (detail!!.id.isNotBlank()) {
                { editingTxn = detail }
            } else null
        )
    } else if (selectedCategory != null) {
        CategoryScreen(
            stat = categoryStats(shown).firstOrNull { it.name == selectedCategory }
                ?: CategoryStat(selectedCategory ?: "", "", 0.0, 0, emptyList()),
            listState = catListState,
            onBack = { selectedCategory = null },
            onOpenTxn = { detail = it }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(240.dp),
                    drawerContainerColor = DarkSurface
                ) {
                    Text(
                        "ExpeMemo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = White,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider(color = CardBorder)
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                IconLabel,
                                contentDescription = null,
                                tint = GrayText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Kategori", fontSize = 13.sp, color = White) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showCategories = true
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = null,
                                tint = GrayText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Pengaturan", fontSize = 13.sp, color = White) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showSettings = true
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        ) {
            MainScreen(
                tab = tab,
                txnListState = txnListState,
                onTabChange = { tab = it },
                loading = loading,
                error = error,
                rowsLoaded = rows != null,
                shown = shown,
                periods = recentPeriodOptions(),
                selectedPeriod = selected,
                onPeriodChange = {
                    selected = it
                    load()
                },
                onPickCustomPeriod = { showCustomPeriod = true },
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                bankFilter = bankFilter,
                onBankFilterChange = { bankFilter = it },
                categoryFilter = categoryFilter,
                onCategoryFilterChange = { categoryFilter = it },
                onReload = { load() },
                onOpenTxn = { detail = it },
                onOpenCategory = { selectedCategory = it },
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
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

    if (editingTxn != null) {
        EditTxnDialog(
            txn = editingTxn!!,
            categories = rows
                ?.map { it.category }
                ?.filter { it.isNotBlank() }
                ?.distinct()
                ?.sorted()
                ?: emptyList(),
            onDismiss = { editingTxn = null },
            onSave = saveTxnEdit,
            onSaved = { updated ->
                val newIcon = rows
                    ?.filter { it.category.equals(updated.category, ignoreCase = true) }
                    ?.firstOrNull { it.icon.isNotBlank() }?.icon ?: ""
                val fixed = updated.copy(icon = newIcon)
                rows = rows?.map { if (it.id == fixed.id) fixed else it }
                if (detail?.id == fixed.id) detail = fixed
                editingTxn = null
                load()
            }
        )
    }

    if (showCategories && catAddOpen) {
        CategoryEditDialog(
            original = null,
            onDismiss = { catAddOpen = false },
            onSave = saveCategoryDef,
            onSaved = {
                catAddOpen = false
                loadCategoryDefs()
                load()
            }
        )
    }

    if (showCategories && catEditTarget != null) {
        CategoryEditDialog(
            original = catEditTarget,
            onDismiss = { catEditTarget = null },
            onSave = saveCategoryDef,
            onSaved = {
                catEditTarget = null
                loadCategoryDefs()
                load()
            }
        )
    }

    if (showCategories && catDeleteTarget != null) {
        CategoryDeleteDialog(
            target = catDeleteTarget!!,
            onDismiss = { catDeleteTarget = null },
            onDelete = deleteCategoryDef,
            onDeleted = {
                catDeleteTarget = null
                loadCategoryDefs()
                load()
            }
        )
    }

    if (showCustomPeriod) {
        val current = selected
        CustomPeriodDialog(
            initialStart = (current as? PeriodSelection.Custom)?.start,
            initialEnd = (current as? PeriodSelection.Custom)?.end,
            onDismiss = { showCustomPeriod = false },
            onConfirm = { start, end ->
                showCustomPeriod = false
                selected = PeriodSelection.Custom(start, end)
                load()
            }
        )
    }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPeriodDialog(
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val minDate = today.minusYears(3)
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart?.toUtcMillis(),
        initialSelectedEndDateMillis = initialEnd?.toUtcMillis(),
        initialDisplayedMonthMillis = (initialStart ?: today).toUtcMillis(),
        yearRange = IntRange(minDate.year, today.year + 1),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= minDate.toUtcMillis()
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null &&
                    state.selectedEndDateMillis != null,
                onClick = {
                    val s = state.selectedStartDateMillis
                    val e = state.selectedEndDateMillis
                    if (s != null && e != null) onConfirm(s.toLocalDateUtc(), e.toLocalDateUtc())
                }
            ) { Text("Pilih") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    ) {
        DateRangePicker(
            state = state,
            showModeToggle = false,
            title = {
                Text(
                    "Pilih rentang periode",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tab: Int,
    txnListState: LazyListState,
    onTabChange: (Int) -> Unit,
    loading: Boolean,
    error: String?,
    rowsLoaded: Boolean,
    shown: List<Txn>,
    periods: List<LocalDate>,
    selectedPeriod: PeriodSelection,
    onPeriodChange: (PeriodSelection) -> Unit,
    onPickCustomPeriod: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    bankFilter: String?,
    onBankFilterChange: (String?) -> Unit,
    categoryFilter: String?,
    onCategoryFilterChange: (String?) -> Unit,
    onReload: () -> Unit,
    onOpenTxn: (Txn) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val stats = categoryStats(shown)

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            CompactTopBar(
                onMenu = onOpenDrawer,
                title = {
                    var open by remember { mutableStateOf(false) }

                    Box {
                        Row(
                            modifier = Modifier.clickable { open = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                selectionLabel(selectedPeriod),
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
                                    text = { Text(periodLabel(p)) },
                                    onClick = {
                                        onPeriodChange(PeriodSelection.Monthly(p))
                                        open = false
                                    }
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Periode custom") },
                                onClick = {
                                    open = false
                                    onPickCustomPeriod()
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Semua periode") },
                                onClick = {
                                    onPeriodChange(PeriodSelection.All)
                                    open = false
                                }
                            )
                        }
                    }
                },
                actions = {
                    val ctx = LocalContext.current
                    var lockOn by remember { mutableStateOf(biometricEnabled(ctx)) }
                    IconButton(
                        onClick = {
                            if (lockOn) {
                                setBiometricEnabled(ctx, false)
                                lockOn = false
                            } else {
                                if (!lockActive(ctx) && biometricAvailable(ctx)) {
                                    setLockMethod(ctx, LOCK_METHOD_BIOMETRIC)
                                }
                                val ok = lockActive(ctx)
                                setBiometricEnabled(ctx, ok)
                                lockOn = ok
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            if (lockOn) IconLock else IconLockOpen,
                            contentDescription =
                                if (lockOn) "Matikan kunci aplikasi"
                                else "Aktifkan kunci aplikasi",
                            tint = if (lockOn) White else GrayText,
                            modifier = Modifier.size(18.dp)
                        )
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
                        listState = txnListState,
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
    listState: LazyListState,
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
                        state = listState,
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
    listState: LazyListState,
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
                    state = listState,
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
fun CategoryManageScreen(
    categories: List<CategoryDef>?,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (CategoryDef) -> Unit,
    onDelete: (CategoryDef) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            CompactTopBar(
                onBack = onBack,
                title = {
                    Text(
                        "Kategori",
                        color = White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = RedPrimary,
                contentColor = White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah kategori")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            when {
                categories == null && loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            color = RedPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                categories == null && error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedButton(onClick = onRetry) { Text("Coba lagi") }
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = {
                            Text("Cari kategori…", fontSize = 13.sp, color = GrayText)
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
                    val defs = categories ?: emptyList()
                    val filtered = if (query.isBlank()) defs
                    else defs.filter {
                        it.name.contains(query, true) ||
                            it.keywords.any { k -> k.contains(query, true) }
                    }
                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                if (defs.isEmpty()) "Belum ada kategori"
                                else "Kategori tidak ditemukan",
                                modifier = Modifier.align(Alignment.Center),
                                color = GrayText
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 10.dp, end = 10.dp, top = 4.dp, bottom = 88.dp
                            )
                        ) {
                            items(filtered, key = { it.name }) { def ->
                                CategoryDefRow(
                                    def = def,
                                    onEdit = { onEdit(def) },
                                    onDelete = { onDelete(def) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDefRow(
    def: CategoryDef,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RedContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = White,
                    modifier = Modifier
                        .padding(end = 18.dp)
                        .size(20.dp)
                )
            }
        }
    ) {
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = DarkBadge) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
                    Text(
                        def.icon.ifBlank { "📝" },
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    def.name,
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${def.keywords.size} keyword · ${def.used} trx",
                    color = GrayText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        tint = GrayText,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                }
            }
        }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun CategoryEditDialog(
    original: CategoryDef?,
    onDismiss: () -> Unit,
    onSave: suspend (old: String?, name: String, icon: String, keywords: List<String>) -> String?,
    onSaved: () -> Unit
) {
    val editing = original != null
    var name by remember(original) { mutableStateOf(original?.name ?: "") }
    var icon by remember(original) { mutableStateOf(original?.icon ?: "") }
    var keywords by remember(original) {
        mutableStateOf(original?.keywords?.joinToString(", ") ?: "")
    }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmRename by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun keywordList(): List<String> =
        keywords.split(",", "|", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    fun doSave(old: String?) {
        saving = true
        error = null
        scope.launch {
            val err = onSave(
                old,
                name.trim().uppercase(),
                icon.trim(),
                keywordList()
            )
            saving = false
            if (err == null) onSaved() else error = err
        }
    }

    fun submit() {
        when {
            saving -> return
            name.trim().isEmpty() -> error = "Nama kategori wajib diisi"
            editing && name.trim().uppercase() != original!!.name.uppercase() -> confirmRename = true
            else -> doSave(if (editing) original!!.name else null)
        }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = {
            Text(
                if (editing) "Edit Kategori" else "Tambah Kategori",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = CircleShape, color = DarkBadge) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                            Text(
                                icon.trim().ifBlank { "📝" },
                                fontSize = 20.sp
                            )
                        }
                    }
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { if (it.length <= 8) icon = it },
                        label = { Text("Icon (emoji)", fontSize = 12.sp, color = GrayText) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = White),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Nama kategori", fontSize = 12.sp, color = GrayText) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = White),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = keywords,
                    onValueChange = {
                        keywords = it
                        error = null
                    },
                    label = {
                        Text("Keyword (pisahkan dengan koma)", fontSize = 12.sp, color = GrayText)
                    },
                    placeholder = {
                        Text("gofood, resto, sambal", fontSize = 12.sp, color = GrayText)
                    },
                    textStyle = TextStyle(fontSize = 13.sp, color = White),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Keyword dipakai untuk kategori otomatis dari email bank.",
                    fontSize = 11.sp,
                    color = GrayText
                )
                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = { submit() }
            ) {
                Text(
                    if (saving) "Menyimpan…" else "Simpan",
                    color = RedPrimary
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) { Text("Batal") }
        }
    )

    if (confirmRename && original != null) {
        AlertDialog(
            onDismissRequest = { if (!saving) confirmRename = false },
            title = {
                Text("Ubah nama kategori?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Nama \"" + original.name + "\" diubah menjadi \"" +
                        name.trim().uppercase() + "\". " +
                        original.used + " transaksi lama akan diperbarui mengikuti nama baru.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        confirmRename = false
                        doSave(original.name)
                    }
                ) {
                    Text("Ya, perbarui", color = RedPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !saving,
                    onClick = { confirmRename = false }
                ) { Text("Batal") }
            }
        )
    }
}

@Composable
fun CategoryDeleteDialog(
    target: CategoryDef,
    onDismiss: () -> Unit,
    onDelete: suspend (CategoryDef) -> String?,
    onDeleted: () -> Unit
) {
    var deleting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        title = { Text("Hapus kategori?", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Kategori \"" + target.name + "\" beserta " +
                        target.keywords.size + " keyword-nya akan dihapus.",
                    fontSize = 13.sp
                )
                if (target.used > 0) {
                    Text(
                        "Kategori ini masih dipakai " + target.used +
                            " transaksi dan tidak dapat dihapus.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !deleting && target.used == 0,
                onClick = {
                    deleting = true
                    error = null
                    scope.launch {
                        val err = onDelete(target)
                        deleting = false
                        if (err == null) onDeleted() else error = err
                    }
                }
            ) {
                Text(
                    if (deleting) "Menghapus…" else "Hapus",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !deleting,
                onClick = onDismiss
            ) { Text("Batal") }
        }
    )
}

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val bioAvailable = remember { biometricAvailable(context) }
    var usePin by remember {
        mutableStateOf(lockMethod(context) == LOCK_METHOD_PIN && savedPin(context) != null)
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun showPrompt() {
        val frag = context as? FragmentActivity ?: return
        val prompt = BiometricPrompt(
            frag,
            ContextCompat.getMainExecutor(frag),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    errorText = null
                    onUnlocked()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        errorText = errString.toString()
                    }
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Buka ExpeMemo")
            .setSubtitle("Gunakan sidik jari atau kunci layar Anda")
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(usePin) {
        if (!usePin) showPrompt()
    }

    Scaffold(containerColor = DarkBg) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = Maroon) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        IconLock,
                        contentDescription = "Terkunci",
                        tint = White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "ExpeMemo terkunci",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (usePin) {
                Text(
                    "Masukkan PIN untuk membuka",
                    fontSize = 11.sp,
                    color = GrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )
                PinEntryPad(
                    pinLength = savedPin(context)?.second ?: 6,
                    onError = { errorText = it },
                    onUnlocked = onUnlocked
                )
            } else {
                Text(
                    "Gunakan sidik jari atau kunci layar untuk membuka",
                    fontSize = 11.sp,
                    color = GrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (errorText != null) {
                Text(
                    errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (usePin) {
                if (bioAvailable) {
                    TextButton(
                        onClick = {
                            errorText = null
                            usePin = false
                        }
                    ) {
                        Text("Gunakan sidik jari", fontSize = 13.sp, color = RedPrimary)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        errorText = null
                        showPrompt()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Text("Coba buka lagi", fontSize = 13.sp)
                }
                if (savedPin(context) != null) {
                    TextButton(
                        onClick = {
                            errorText = null
                            usePin = true
                        }
                    ) {
                        Text("Gunakan PIN", fontSize = 13.sp, color = GrayText)
                    }
                }
            }
        }
    }
}

@Composable
fun PinEntryPad(pinLength: Int, onUnlocked: () -> Unit, onError: (String) -> Unit) {
    val context = LocalContext.current
    var entered by remember { mutableStateOf("") }

    fun onKey(digit: String) {
        if (entered.length >= pinLength) return
        entered += digit
        if (entered.length == pinLength) {
            if (verifyPin(context, entered)) {
                onUnlocked()
            } else {
                entered = ""
                onError("PIN salah")
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(pinLength) { i ->
                val filled = i < entered.length
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (filled) RedPrimary else Color.Transparent, CircleShape)
                        .border(
                            1.5.dp,
                            if (filled) RedPrimary else GrayText,
                            CircleShape
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            ).forEach { rowKeys ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    rowKeys.forEach { key ->
                        when {
                            key.isEmpty() -> Spacer(modifier = Modifier.size(62.dp))
                            key == "DEL" -> Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .clickable { entered = entered.dropLast(1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    IconBackspace,
                                    contentDescription = "Hapus",
                                    tint = GrayText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            else -> Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .clickable { onKey(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreen(t: Txn, onBack: () -> Unit, onEdit: (() -> Unit)? = null) {
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
                },
                actions = {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                            Icon(
                                IconEdit,
                                contentDescription = "Edit transaksi",
                                tint = White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
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

@Composable
fun EditTxnDialog(
    txn: Txn,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: suspend (Txn) -> String?,
    onSaved: (Txn) -> Unit
) {
    var category by remember(txn.id) { mutableStateOf(txn.category) }
    var subcategory by remember(txn.id) { mutableStateOf(txn.subcategory) }
    var note by remember(txn.id) { mutableStateOf(txn.note) }
    var amountStr by remember(txn.id) {
        mutableStateOf(
            if (txn.amount % 1.0 == 0.0) txn.amount.toLong().toString()
            else txn.amount.toString()
        )
    }
    var description by remember(txn.id) { mutableStateOf(txn.description) }
    var catOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        val amount = amountStr.toDoubleOrNull()
        when {
            saving -> return
            amount == null || amount <= 0 -> error = "Nominal tidak valid"
            else -> {
                saving = true
                error = null
                val updated = txn.copy(
                    category = category.trim(),
                    subcategory = subcategory.trim(),
                    note = note.trim(),
                    amount = amount,
                    description = description.trim()
                )
                scope.launch {
                    val err = onSave(updated)
                    saving = false
                    if (err == null) onSaved(updated) else error = err
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Edit Transaksi", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                            error = null
                        },
                        label = { Text("Kategori", fontSize = 12.sp, color = GrayText) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = White),
                        trailingIcon = {
                            if (categories.isNotEmpty()) {
                                IconButton(
                                    onClick = { catOpen = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = "Pilih kategori",
                                        tint = GrayText
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = catOpen,
                        onDismissRequest = { catOpen = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c, fontSize = 13.sp) },
                                onClick = {
                                    category = c
                                    catOpen = false
                                    error = null
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = subcategory,
                    onValueChange = {
                        subcategory = it
                        error = null
                    },
                    label = { Text("Subkategori", fontSize = 12.sp, color = GrayText) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = White),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                        error = null
                    },
                    label = { Text("Catatan", fontSize = 12.sp, color = GrayText) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = White),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        if (it.length <= 15 && it.all { ch -> ch.isDigit() }) {
                            amountStr = it
                            error = null
                        }
                    },
                    label = { Text("Nominal (Rp)", fontSize = 12.sp, color = GrayText) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        error = null
                    },
                    label = { Text("Keterangan", fontSize = 12.sp, color = GrayText) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = White),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = { submit() }
            ) {
                Text(
                    if (saving) "Menyimpan…" else "Simpan",
                    color = RedPrimary
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) { Text("Batal") }
        }
    )
}

@Composable
fun SettingsScreen(
    urlSaved: Boolean,
    tokenSaved: Boolean,
    allowBack: Boolean,
    onBack: () -> Unit,
    onSave: (String, String) -> String?
) {
    var url by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    val bioAvailable = remember { biometricAvailable(ctx) }
    var lockOn by remember { mutableStateOf(biometricEnabled(ctx)) }
    var method by remember { mutableStateOf(lockMethod(ctx)) }
    var pendingEnable by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            CompactTopBar(
                onBack = if (allowBack) onBack else null,
                title = {
                    Text(
                        "Pengaturan",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Koneksi Google Apps Script",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            ConfigStatusRow("URL", urlSaved)
            ConfigStatusRow("Token", tokenSaved)
            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    error = null
                },
                label = { Text("URL", fontSize = 12.sp, color = GrayText) },
                placeholder = {
                    Text(
                        "https://script.google.com/macros/s/…/exec",
                        fontSize = 12.sp,
                        color = GrayText
                    )
                },
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = White),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = token,
                onValueChange = {
                    token = it
                    error = null
                },
                label = { Text("Token", fontSize = 12.sp, color = GrayText) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = White),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = {
                    val err = onSave(url.trim(), token.trim())
                    if (err != null) error = err
                },
                enabled = url.isNotBlank() || token.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan", fontSize = 13.sp)
            }
            Text(
                "Isi minimal satu kolom untuk menyimpan. Kolom yang dikosongkan tidak diubah. " +
                    "Nilai yang tersimpan tidak dapat dilihat kembali.",
                fontSize = 11.sp,
                color = GrayText
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CardBorder)
            Text(
                "Kunci Aplikasi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aktifkan kunci aplikasi", fontSize = 13.sp, color = White)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = lockOn,
                    onCheckedChange = { want ->
                        when {
                            !want -> {
                                setBiometricEnabled(ctx, false)
                                lockOn = false
                            }
                            lockActive(ctx) -> {
                                setBiometricEnabled(ctx, true)
                                lockOn = true
                            }
                            else -> {
                                pendingEnable = true
                                showPinDialog = true
                            }
                        }
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Metode buka:", fontSize = 12.sp, color = GrayText)
                FilterChip(
                    selected = method == LOCK_METHOD_BIOMETRIC,
                    enabled = bioAvailable,
                    onClick = {
                        method = LOCK_METHOD_BIOMETRIC
                        setLockMethod(ctx, LOCK_METHOD_BIOMETRIC)
                    },
                    label = { Text("Sidik jari", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = method == LOCK_METHOD_PIN,
                    onClick = {
                        if (savedPin(ctx) != null) {
                            method = LOCK_METHOD_PIN
                            setLockMethod(ctx, LOCK_METHOD_PIN)
                        } else {
                            showPinDialog = true
                        }
                    },
                    label = { Text("PIN", fontSize = 12.sp) }
                )
            }
            if (method == LOCK_METHOD_PIN && savedPin(ctx) != null) {
                OutlinedButton(
                    onClick = { showPinDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedPrimary)
                ) {
                    Text("Ubah PIN", fontSize = 13.sp)
                }
            }
        }
    }
    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = {
                showPinDialog = false
                pendingEnable = false
            },
            onSave = { pin ->
                savePin(ctx, pin)
                setLockMethod(ctx, LOCK_METHOD_PIN)
                if (pendingEnable) {
                    setBiometricEnabled(ctx, true)
                    lockOn = true
                    pendingEnable = false
                }
                method = LOCK_METHOD_PIN
                showPinDialog = false
            }
        )
    }
}

@Composable
fun ConfigStatusRow(label: String, saved: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = GrayText)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            if (saved) "tersimpan" else "belum diatur",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (saved) RedOnContainer else GrayText
        )
    }
}

@Composable
fun PinSetupDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "PIN terdiri dari 4–6 angka dan dipakai untuk membuka aplikasi.",
                    fontSize = 12.sp,
                    color = GrayText
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                            pin = it
                            error = null
                        }
                    },
                    label = { Text("PIN baru", fontSize = 12.sp, color = GrayText) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = White),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = {
                        if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                            confirm = it
                            error = null
                        }
                    },
                    label = { Text("Konfirmasi PIN", fontSize = 12.sp, color = GrayText) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = White),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        pin.length < 4 -> error = "PIN minimal 4 angka"
                        pin != confirm -> error = "Konfirmasi PIN tidak sama"
                        else -> onSave(pin)
                    }
                }
            ) { Text("Simpan", color = RedPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
