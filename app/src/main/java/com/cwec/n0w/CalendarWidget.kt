package com.cwec.n0w

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import java.time.LocalDate
import java.util.Locale

class CalendarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hasPermission = Helper().hasCalendarPermission(context)
        val storage = EventStorage()
        if (hasPermission) {
            storage.fetchCalendarEvents(context)
        }
        provideContent {
            CalendarWidgetContent(
                context,
                storage.getEvents(),
                hasPermission,
                actionStartActivity<MainActivity>()
            )
        }
    }

    @Composable
    fun CalendarWidgetContent(
        context: Context,
        events: List<EventStorage.Event>?,
        hasPermission: Boolean,
        openActivityAction: Action
    ) {
        val currentDate = LocalDate.now()
        val dayOfMonth = currentDate.dayOfMonth
        val dayOfWeek = currentDate.dayOfWeek
        val dayOfWeekName =
            dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())

        val bitmaps = remember(dayOfWeek) {
            val day =
                Helper().createTextBitmap(
                    context,
                    dayOfMonth.toString(),
                    48f,
                    android.graphics.Color.WHITE
                )
            val name =
                Helper().createTextBitmap(context, dayOfWeekName, 22f, android.graphics.Color.GRAY)
            Pair(day, name)
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(Color(0xFF191C21))
                .cornerRadius(24.dp)
                .clickable(openActivityAction),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(bitmaps.first),
                        contentDescription = "Renderer dayOfMonth"
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Image(
                        provider = ImageProvider(bitmaps.second),
                        contentDescription = "Renderer dayOfWeek"
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF2F3035))
                        .cornerRadius(24.dp)
                        .padding(16.dp, 4.dp),
                ) {
                    if (!hasPermission) {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = context.getString(R.string.no_access_to_the_calendar),
                                style = TextStyle(
                                    color = ColorProvider(Color.White, Color.White),
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = GlanceModifier.fillMaxWidth()
                            )
                            Spacer(GlanceModifier.height(8.dp))
                            Button(
                                text = context.getString(R.string.allow_access),
                                onClick = openActivityAction,
                                modifier = GlanceModifier.fillMaxWidth()
                            )
                        }
                    } else if (events.isNullOrEmpty()) {
                        Text(
                            text = context.getString(R.string.no_events),
                            style = TextStyle(
                                color = ColorProvider(Color.White, Color.White),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = GlanceModifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.Start
                        ) {
                            events.take(3).forEach { event ->
                                Text(
                                    text = event.getFormattedStartTime(context),
                                    style = TextStyle(
                                        color = ColorProvider(Color.Gray, Color.Gray),
                                        fontSize = 8.sp,
                                        textAlign = TextAlign.Start
                                    ),
                                    modifier = GlanceModifier.fillMaxWidth()
                                )
                                Text(
                                    text = event.title,
                                    style = TextStyle(
                                        color = ColorProvider(Color.White, Color.White),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Start
                                    ),
                                    modifier = GlanceModifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
