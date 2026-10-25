package com.example.initialsbox.engine

import com.example.initialsbox.model.InitialsResult

object ExportFormatter {

    fun toTxt(results: List<InitialsResult>, includeNumbering: Boolean = false): String {
        val sb = StringBuilder()
        sb.append("INITIALSBOX\n\n")

        results.forEachIndexed { index, item ->
            val prefix = if (includeNumbering) "${index + 1}. " else ""
            sb.append("$prefix${item.input} → ${item.initials}\n")
        }

        return sb.toString().trimEnd()
    }

    fun toInitialsOnly(results: List<InitialsResult>, includeNumbering: Boolean = false): String {
        return results.mapIndexed { index, item ->
            val prefix = if (includeNumbering) "${index + 1}. " else ""
            "$prefix${item.initials}"
        }.joinToString("\n")
    }

    fun toInputAndInitials(results: List<InitialsResult>, includeNumbering: Boolean = false): String {
        return results.mapIndexed { index, item ->
            val prefix = if (includeNumbering) "${index + 1}. " else ""
            "$prefix${item.input} → ${item.initials}"
        }.joinToString("\n")
    }

    fun toCsv(results: List<InitialsResult>): String {
        val sb = StringBuilder()
        sb.append("\"Input\",\"Initials\",\"Word Count\",\"Character Count\"\n")

        for (item in results) {
            val escapedInput = escapeCsv(item.input)
            val escapedInitials = escapeCsv(item.initials)
            sb.append("\"$escapedInput\",\"$escapedInitials\",${item.originalWordCount},${item.input.length}\n")
        }

        return sb.toString().trimEnd()
    }

    fun toJson(results: List<InitialsResult>): String {
        val sb = StringBuilder()
        sb.append("{\n  \"results\": [\n")

        results.forEachIndexed { index, item ->
            val isLast = index == results.size - 1
            sb.append("    {\n")
            sb.append("      \"input\": \"${escapeJson(item.input)}\",\n")
            sb.append("      \"initials\": \"${escapeJson(item.initials)}\",\n")
            sb.append("      \"wordCount\": ${item.originalWordCount}\n")
            sb.append("    }${if (isLast) "" else ","}\n")
        }

        sb.append("  ]\n}")
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    private fun escapeJson(value: String): String {
        val sb = StringBuilder()
        for (i in 0 until value.length) {
            val ch = value[i]
            when (ch) {
                '\"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (ch.code < 0x20) {
                        sb.append(String.format(java.util.Locale.ROOT, "\\u%04x", ch.code))
                    } else {
                        sb.append(ch)
                    }
                }
            }
        }
        return sb.toString()
    }
}
