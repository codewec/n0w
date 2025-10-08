package com.cwec.n0w

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap

class Helper {
    fun createTextBitmap(context: Context, text: String, fontSize: Float, fontColor: Int): Bitmap {
        val paint = Paint().apply {
            isAntiAlias = true
            typeface = ResourcesCompat.getFont(context, R.font.n_dot77)
            textSize = spToPx(context, fontSize)
            color = fontColor
        }

        val width = paint.measureText(text).toInt()
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val height = bounds.height()
        val padding = 10
        val bitmap = createBitmap(width + padding, height + padding)
        val canvas = Canvas(bitmap)
        val x = 0f
        val y = height.toFloat()
        canvas.drawText(text, x, y, paint)
        return bitmap
    }

    fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }

    fun hasCalendarPermission(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}