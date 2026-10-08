package com.example.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PhysicsCyan
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

object PersianUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /**
     * Converts all ASCII digits 0-9 to Persian digits ۰-۹,
     * and converts decimal dots between digits to Persian momayyez (٫).
     */
    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (i in input.indices) {
            val c = input[i]
            if (c in '0'..'9') {
                sb.append(persianDigits[c - '0'])
            } else if (c == '.' && i > 0 && i < input.length - 1 && input[i - 1].isDigit() && input[i + 1].isDigit()) {
                sb.append('٫')
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Formats a float or double with standard rounding and Persian digits and momayyez.
     */
    fun formatNumber(value: Number, decimals: Int = 1): String {
        val d = value.toDouble()
        val formatStr = "%.${decimals}f"
        val formatted = String.format(Locale.US, formatStr, d)
        return toPersianDigits(formatted)
    }

    /**
     * Formats an integer to Persian digits.
     */
    fun formatInt(value: Int): String {
        return toPersianDigits(value.toString())
    }

    /**
     * Converts a millisecond timestamp to Solar Hijri (Jalali / شمسی) date & time string.
     * Example: ۱۴۰۵/۰۷/۱۳ ۲۲:۲۲
     */
    fun formatJalaliDateTime(timestamp: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.timeInMillis = timestamp

        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
        val rawStr = String.format(Locale.US, "%04d/%02d/%02d %02d:%02d", jy, jm, jd, hour, minute)
        return toPersianDigits(rawStr)
    }

    /**
     * Algorithm converting Gregorian Year/Month/Day to Jalali Year/Month/Day.
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy2 % 4 == 0 && gy2 % 100 != 0) || (gy2 % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0 until 11) {
            if (jDayNo < jDaysInMonth[i]) {
                jm = i
                break
            }
            jDayNo -= jDaysInMonth[i]
            jm = i + 1
        }
        val jd = jDayNo + 1
        return Triple(jy, jm + 1, jd)
    }
}

/**
 * A dedicated composable that displays mathematical formulas and equations strictly LTR
 * with dark sleek container, preventing BiDi layout scrambling.
 */
@Composable
fun LtrMathBox(
    formula: String,
    modifier: Modifier = Modifier,
    textColor: Color = PhysicsCyan,
    backgroundColor: Color = Color(0xFF0F172A),
    borderColor: Color = PhysicsCyan.copy(alpha = 0.35f),
    fontSize: Int = 14
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = formula,
                    color = textColor,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
