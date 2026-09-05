package com.example.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation for credit and debit card numbers.
 * Displays digits formatted with spaces (4-4-4-4 for standard cards, 4-6-5 for AMEX).
 * Preserves exact cursor position and smooth backspace behavior.
 */
class CardNumberVisualTransformation(private val isAmex: Boolean = false) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val maxLen = if (isAmex) 15 else 19
        val trimmed = raw.take(maxLen)
        val out = StringBuilder()

        if (isAmex) {
            for (i in trimmed.indices) {
                out.append(trimmed[i])
                if ((i == 3 || i == 9) && i != trimmed.lastIndex) {
                    out.append(' ')
                }
            }
        } else {
            for (i in trimmed.indices) {
                out.append(trimmed[i])
                if (i % 4 == 3 && i != trimmed.lastIndex) {
                    out.append(' ')
                }
            }
        }

        val transformedString = out.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(trimmed.length)
                return if (isAmex) {
                    when {
                        safeOffset <= 4 -> safeOffset
                        safeOffset <= 10 -> safeOffset + 1
                        else -> (safeOffset + 2).coerceAtMost(transformedString.length)
                    }
                } else {
                    val spaces = if (safeOffset > 0) (safeOffset - 1) / 4 else 0
                    (safeOffset + spaces).coerceAtMost(transformedString.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(transformedString.length)
                return if (isAmex) {
                    when {
                        safeOffset <= 4 -> safeOffset
                        safeOffset <= 11 -> (safeOffset - 1).coerceAtLeast(4)
                        else -> (safeOffset - 2).coerceAtLeast(10)
                    }.coerceAtMost(trimmed.length)
                } else {
                    val spaces = safeOffset / 5
                    (safeOffset - spaces).coerceIn(0, trimmed.length)
                }
            }
        }

        return TransformedText(AnnotatedString(transformedString), offsetMapping)
    }
}

/**
 * VisualTransformation for Expiry Date input.
 * Transforms raw 4-digit input "MMYY" into "MM/YY" format.
 */
class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(4)
        val out = StringBuilder()

        for (i in raw.indices) {
            out.append(raw[i])
            if (i == 1 && i != raw.lastIndex) {
                out.append('/')
            }
        }

        val transformed = out.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceIn(0, raw.length)
                return if (safeOffset <= 2) {
                    safeOffset
                } else {
                    (safeOffset + 1).coerceAtMost(transformed.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceIn(0, transformed.length)
                return if (safeOffset <= 2) {
                    safeOffset
                } else {
                    (safeOffset - 1).coerceIn(0, raw.length)
                }
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

/**
 * VisualTransformation for Full Date input (DD/MM/YYYY).
 * Transforms raw digits (up to 8 digits) into "DD/MM/YYYY" format.
 * Guarantees monotonic non-decreasing offset mapping, eliminates cursor jumping,
 * and ensures smooth backspace, selection, and insertion anywhere within the date.
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(8)
        val out = StringBuilder()

        for (i in raw.indices) {
            out.append(raw[i])
            if (i == 1 || i == 3) {
                out.append('/')
            }
        }

        val transformed = out.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceIn(0, raw.length)
                return when {
                    safeOffset <= 1 -> safeOffset
                    safeOffset <= 3 -> (safeOffset + 1).coerceAtMost(transformed.length)
                    else -> (safeOffset + 2).coerceAtMost(transformed.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceIn(0, transformed.length)
                return when {
                    safeOffset <= 2 -> safeOffset
                    safeOffset <= 5 -> (safeOffset - 1).coerceIn(0, raw.length)
                    else -> (safeOffset - 2).coerceIn(0, raw.length)
                }
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

/**
 * VisualTransformation for Issue Date input (MM/YYYY or MM/YY).
 */
class IssueDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(6)
        val out = StringBuilder()

        for (i in raw.indices) {
            out.append(raw[i])
            if (i == 1 && i != raw.lastIndex) {
                out.append('/')
            }
        }

        val transformed = out.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(raw.length)
                return if (safeOffset <= 2) {
                    safeOffset
                } else {
                    (safeOffset + 1).coerceAtMost(transformed.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(transformed.length)
                return if (safeOffset <= 2) {
                    safeOffset
                } else {
                    (safeOffset - 1).coerceIn(0, raw.length)
                }
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

object CardValidationUtils {
    /**
     * Standard Luhn algorithm / Mod 10 checksum check for credit/debit cards.
     */
    fun isValidLuhn(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        if (digits.length < 13 || digits.length > 19) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    /**
     * Validates MM and YY format and ensures expiry date is not in the past.
     */
    fun isExpiryValid(monthStr: String, yearStr: String): Boolean {
        val month = monthStr.toIntOrNull() ?: return false
        val year = yearStr.toIntOrNull() ?: return false
        if (month !in 1..12) return false

        val fullYear = if (year < 100) 2000 + year else year
        val now = java.util.Calendar.getInstance()
        val currentYear = now.get(java.util.Calendar.YEAR)
        val currentMonth = now.get(java.util.Calendar.MONTH) + 1

        if (fullYear < currentYear) return false
        if (fullYear == currentYear && month < currentMonth) return false
        if (fullYear > currentYear + 30) return false
        return true
    }

    /**
     * Checks if expiry month is invalid (e.g. > 12 or 00).
     */
    fun isMonthInvalid(monthStr: String): Boolean {
        if (monthStr.length < 2) return false
        val month = monthStr.toIntOrNull() ?: return true
        return month !in 1..12
    }

    /**
     * Formats numerical input into DD/MM/YYYY date format without requiring slashes or backslashes.
     * Purely numerical input from number pad auto-inserts slashes: e.g. 15081995 -> 15/08/1995.
     * Also gracefully accepts pasted or backslash-separated dates (e.g. 15\08\1995 or 15/08/1995).
     */
    fun formatFullDate(value: String): String {
        var normalized = value
        if (value.contains('/') || value.contains('\\') || value.contains('-')) {
            val rawParts = value.split(Regex("[/\\\\\\-]"))
            if (rawParts.size >= 2) {
                val d = rawParts[0].filter { it.isDigit() }
                val m = rawParts[1].filter { it.isDigit() }
                val rest = rawParts.drop(2).joinToString("").filter { it.isDigit() }
                val paddedD = if (d.length == 1) "0$d" else d
                val paddedM = if (rawParts.size >= 3 && m.length == 1) "0$m" else m
                normalized = "$paddedD$paddedM$rest"
            }
        }

        val digits = normalized.filter { it.isDigit() }.take(8)
        return when {
            digits.length > 4 -> "${digits.take(2)}/${digits.substring(2, 4)}/${digits.substring(4)}"
            digits.length > 2 -> "${digits.take(2)}/${digits.substring(2)}"
            else -> digits
        }
    }
}
