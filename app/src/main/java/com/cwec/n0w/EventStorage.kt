package com.cwec.n0w

import android.content.Context
import android.provider.CalendarContract
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class EventStorage {
    private val events = mutableListOf<Event>()

    data class Event(
        val id: Long,
        val title: String,
        val start: Long,
        val end: Long,
    ) {
        fun getFormattedStartTime(context: Context): String {
            val now = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val eventTime = Calendar.getInstance().apply { timeInMillis = start }
            eventTime.set(Calendar.HOUR_OF_DAY, 0)
            eventTime.set(Calendar.MINUTE, 0)
            eventTime.set(Calendar.SECOND, 0)
            eventTime.set(Calendar.MILLISECOND, 0)

            val diffMillis = eventTime.timeInMillis - now.timeInMillis
            val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()

            val timeStr = DateFormat.getTimeFormat(context).format(Date(start))

            return when (diffDays) {
                0 -> timeStr
                1 -> "$timeStr, " + context.getString(R.string.tomorrow)
                in 2..Int.MAX_VALUE -> {
                    val daysStr = context.resources.getQuantityString(
                        R.plurals.event_in_days,
                        diffDays,
                        diffDays
                    )
                    daysStr
                }

                else -> timeStr
            }
        }
    }

    fun fetchCalendarEvents(context: Context): List<Event> {
        events.clear()

        if (!Helper().hasCalendarPermission(context)) {
            return emptyList()
        }

        val startMillis: Long
        val endMillis: Long

        val calendarStart = Calendar.getInstance()
        startMillis = calendarStart.timeInMillis

        val calendarEnd = Calendar.getInstance().apply {
            timeInMillis = startMillis
            add(Calendar.DAY_OF_YEAR, 7)
        }
        endMillis = calendarEnd.timeInMillis

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.CALENDAR_ID
        )

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(startMillis.toString())
            .appendPath(endMillis.toString())
            .build()

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                    ?: "No Title"
                val dtStart = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
                val dtEnd = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Instances.END))
                events.add(Event(id, title, dtStart, dtEnd))
            }
        }

        events.sortBy { it.start }

        return events.toList()
    }

    fun testEvents(): List<Event> {
        val startDate = GregorianCalendar(2025, Calendar.OCTOBER, 9, 9, 0).timeInMillis
        val maxEventsPerDay = 3
        val hourInMillis = 60 * 60 * 1000L

        val titles = listOf(
            "Team Meeting",
            "Project Discussion",
            "Code Review",
            "Design Workshop",
            "Customer Call",
            "Marketing Briefing",
            "Product Demo",
            "Strategy Meeting",
            "Release Planning",
            "Team Retrospective",
            "Quarterly Financial Review",
            "Annual Product Roadmap Analysis",
            "Client Feedback Session",
            "Cross Department Sync Up",
            "New Hire Orientation Workshop",
            "Product Launch Celebration",
            "Sales Pipeline Review",
            "Vendor Negotiation Meeting",
            "User Experience Testing",
            "Software Deployment Plan",
            "Customer Support Training",
            "Executive Board Meeting",
            "Security Protocol Update",
            "Social Media Campaign",
            "Technical Debt Prioritization",
            "Cloud Infrastructure Audit",
            "Innovation Brainstorm Session",
            "Employee Wellness Program",
            "Customer Onboarding Tutorial",
            "Performance Review Meeting"
        )

        val events = mutableListOf<Event>()
        var id = 1L

        for (i in titles.indices) {
            val day = i / maxEventsPerDay
            val eventInDayIndex = i % maxEventsPerDay
            val start = startDate + day * 24 * hourInMillis + eventInDayIndex * 3 * hourInMillis
            val duration = (30 + (i * 10) % 91) * 60 * 1000L
            val end = start + duration

            events.add(Event(id, titles[i], start, end))
            id++
        }

        return events
    }

    fun getEvents(): List<Event> = events.toList()

    fun addEvent(event: Event) {
        events.add(event)
    }
}