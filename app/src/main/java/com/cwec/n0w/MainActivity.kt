package com.cwec.n0w

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.cwec.n0w.ui.theme.N0WTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.material.icons.Icons
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.material.icons.filled.DateRange

class MainActivity : ComponentActivity() {
    private var storage = EventStorage()
    private var accessGranted = false
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadEvents()
        }
        accessGranted = isGranted
    }

    val dotsFontFamily = FontFamily(
        Font(R.font.n_dot77, FontWeight.Normal),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        when {
            Helper().hasCalendarPermission(applicationContext) -> {
                accessGranted = true
                loadEvents()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
            }
        }
        setContent {
            N0WTheme {
                CalendarContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Helper().hasCalendarPermission(applicationContext)) {
            accessGranted = true
            loadEvents()
        } else {
            accessGranted = false
        }
    }

    private fun loadEvents() {
        storage.fetchCalendarEvents(applicationContext)
        lifecycleScope.launch {
            CalendarWidget().updateAll(applicationContext)
        }
        setContent {
            N0WTheme {
                CalendarContent()
            }
        }
    }

    @Composable
    fun CalendarContent() {
        val groupedEvents = storage.getEvents()
            .groupBy { event ->
                Instant.ofEpochMilli(event.start).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .toSortedMap()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            if (!accessGranted || groupedEvents.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!accessGranted) {
                        Text(text = applicationContext.getString(R.string.no_access_to_the_calendar))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { openAppSettings() }) {
                            Text(text = applicationContext.getString(R.string.allow_access))
                        }
                    } else if (groupedEvents.isEmpty()) {
                        Text(
                            applicationContext.getString(R.string.no_events),
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = applicationContext.getString(R.string.upcoming_events)
                                .uppercase(),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = dotsFontFamily
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                        val context = LocalContext.current
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                setPackage("com.google.android.calendar")
                                addCategory(Intent.CATEGORY_APP_CALENDAR)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = CalendarContract.CONTENT_URI
                                }
                                context.startActivity(intent)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = applicationContext.getString(R.string.open_calendar_app),
                                tint = Color.White
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF2F3035))
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            groupedEvents.forEach { (date, eventsForDay) ->
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp, 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.Gray, shape = RectangleShape)
                                        )
                                        Text(
                                            text = date.format(
                                                DateTimeFormatter.ofLocalizedDate(
                                                    FormatStyle.LONG
                                                )
                                            ),
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                color = Color.Gray,
                                                fontFamily = dotsFontFamily
                                            ),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                                items(eventsForDay) { event ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(start = 10.dp),
                                            text = event.title,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                color = Color.LightGray
                                            )
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = Instant.ofEpochMilli(event.start)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalTime()
                                                .format(DateTimeFormatter.ofPattern("HH:mm")),
                                            style = TextStyle(
                                                color = Color.Gray,
                                                fontSize = 16.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
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

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}