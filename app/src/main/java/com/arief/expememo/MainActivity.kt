package com.arief.expememo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App()
                }
            }
        }
    }
}

data class Txn(
    val date: String,
    val account: String,
    val category: String,
    val note: String,
    val amount: Double,
    val incomeExpense: String
)

object Api {
    private const val BASE =
        "https://script.google.com/macros/s/REDACTED/exec"
    private const val TOKEN = "c6dae53f243b358acf4839514ff0d01f"

    fun fetch(): String {
        val conn = URL("$BASE?token=$TOKEN&months=24").openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 45000
        try {
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}

fun parseRows(json: String): List<Txn> {
    val obj = JSONObject(json)
    if (!obj.optBoolean("ok", false)) {
        throw Exception(obj.optString("error", "respon tidak valid"))
    }
    val arr = obj.getJSONArray("rows")
    return (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        Txn(
            date = o.getString("date"),
            account = o.getString("account"),
            category = o.getString("category"),
            note = o.getString("note"),
            amount = o.getDouble("amount"),
            incomeExpense = o.getString("incomeExpense")
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

fun monthLabel(key: String): String {
    val parts = key.split("-")
    if (parts.size != 2) return key
    val idx = (parts[1].toIntOrNull() ?: 0) - 1
    if (idx < 0 || idx > 11) return key
    return MONTH_NAMES[idx] + " " + parts[0]
}

fun dayLabel(iso: String): String {
    if (iso.length < 16) return iso
    val monthIdx = (iso.substring(5, 7).toIntOrNull() ?: 1) - 1
    val month = MONTH_NAMES.getOrElse(monthIdx) { "" }
    return iso.substring(8, 10) + " " + month + " " + iso.substring(11, 16)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var rows by remember { mutableStateOf<List<Txn>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        loading = true
        error = null
        scope.launch {
            try {
                val body = withContext(Dispatchers.IO) { Api.fetch() }
                val parsed = parseRows(body)
                rows = parsed
                if (selected == null && parsed.isNotEmpty()) {
                    selected = parsed.maxOf { it.date.substring(0, 7) }
                }
            } catch (e: Exception) {
                error = e.message ?: "gagal memuat data"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    val months = rows
        ?.map { it.date.substring(0, 7) }
        ?.distinct()
        ?.sortedDescending()
        ?: emptyList()

    val shown = rows
        ?.filter { selected == null || it.date.substring(0, 7) == selected }
        ?: emptyList()

    val totalExpense = shown
        .filter { it.incomeExpense.equals("Expense", true) }
        .sumOf { it.amount }

    val totalIncome = shown
        .filter { it.incomeExpense.equals("Income", true) }
        .sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ExpeMemo") },
                actions = {
                    TextButton(onClick = { load() }, enabled = !loading) {
                        Text(if (loading) "..." else "Refresh")
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
            MonthFilter(
                months = months,
                selected = selected,
                onSelect = { selected = it }
            )

            if (shown.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Pengeluaran",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            rupiah(totalExpense),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC62828)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Pemasukan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            rupiah(totalIncome),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                HorizontalDivider()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading && rows == null -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(shown) { t ->
                            TxnRow(t)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthFilter(
    months: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    if (months.isEmpty()) return
    var open by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = true,
            onClick = { open = true },
            label = { Text(monthLabel(selected ?: months.first()) + "  ▾") },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            months.forEach { m ->
                DropdownMenuItem(
                    text = { Text(monthLabel(m)) },
                    onClick = {
                        onSelect(m)
                        open = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Semua bulan") },
                onClick = {
                    onSelect(null)
                    open = false
                }
            )
        }
    }
}

@Composable
fun TxnRow(t: Txn) {
    val income = t.incomeExpense.equals("Income", true)
    val meta = listOf(t.account, t.category.ifBlank { "Tanpa kategori" })
        .joinToString("  •  ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                t.note.ifBlank { "(tanpa catatan)" },
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                dayLabel(t.date) + "   " + meta,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            (if (income) "+" else "−") + rupiah(t.amount),
            fontWeight = FontWeight.SemiBold,
            color = if (income) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
    }
}
