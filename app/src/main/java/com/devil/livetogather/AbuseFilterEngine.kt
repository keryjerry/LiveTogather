package com.devil.livetogather

import java.util.regex.Pattern

/**
 * AbuseFilterEngine
 * Production-grade profanity, abuse, and harassment filter for:
 * 1. Personal 1-on-1 Chat
 * 2. Live Audio & Video Streaming Hangout Room Chat
 *
 * Supports multi-word phrase matching, regex word boundary checks,
 * character normalization (handling leetspeak, repeated letter flooding, punctuation evasion),
 * and instant blocking / asterisks masking.
 */
object AbuseFilterEngine {

    // Base Bad Words & Abusive Phrases Dictionary provided by Admin
    val BAD_WORDS = setOf(
        "aand",
        "aandu",
        "balatkar",
        "balatkari",
        "behen chod",
        "beti chod",
        "bhadva",
        "bhadve",
        "bhandve",
        "bhangi",
        "bhootni ke",
        "bhosad",
        "bhosadi ke",
        "boobe",
        "chakke",
        "chinaal",
        "chinki",
        "chod",
        "chodu",
        "chodu bhagat",
        "chooche",
        "choochi",
        "choope",
        "choot",
        "choot ke baal",
        "chootia",
        "chootiya",
        "chuche",
        "chuchi",
        "chudaap",
        "chudai khanaa",
        "chudam chudai",
        "chude",
        "chut",
        "chut ka chuha",
        "chut ka churan",
        "chut ka mail",
        "chut ke baal",
        "chut ke dhakkan",
        "chut maarli",
        "chutad",
        "chutadd",
        "chutan",
        "chutia",
        "chutiya",
        "gaand",
        "gaandfat",
        "gaandmasti",
        "gaandufad",
        "gandfattu",
        "gandu",
        "gashti",
        "gasti",
        "ghassa",
        "ghasti",
        "gucchi",
        "gucchu",
        "harami",
        "haramzade",
        "hawas",
        "hawas ke pujari",
        "hijda",
        "hijra",
        "jhant",
        "jhant chaatu",
        "jhant ka keeda",
        "jhant ke baal",
        "jhant ke pissu",
        "jhantu",
        "kamine",
        "kaminey",
        "kanjar",
        "kutta",
        "kutta kamina",
        "kutte ki aulad",
        "kutte ki jat",
        "kuttiya",
        "loda",
        "lodu",
        "lund",
        "lund choos",
        "lund ka bakkal",
        "lund khajoor",
        "lundtopi",
        "lundure",
        "maa ki chut",
        "maal",
        "madar chod",
        "madarchod",
        "madhavchod",
        "mooh mein le",
        "mutth",
        "mutthal",
        "najayaz",
        "najayaz aulaad",
        "najayaz paidaish",
        "paki",
        "pataka",
        "patakha",
        "raand",
        "randaap",
        "randi",
        "randi rona",
        "saala",
        "saala kutta",
        "saali kutti",
        "saali randi",
        "suar",
        "suar ke lund",
        "suar ki aulad",
        "tatte",
        "tatti",
        "teri maa ka bhosada",
        "teri maa ka boba chusu",
        "teri maa ki behenchod",
        "teri maa ki chut",
        "tharak",
        "tharki",
        "tu chuda"
    )

    // Pre-compiled multi-word phrases (sorted by length descending for greedy matching)
    private val multiWordPhrases = BAD_WORDS
        .filter { it.contains(" ") }
        .sortedByDescending { it.length }

    // Pre-compiled single words
    private val singleWords = BAD_WORDS
        .filter { !it.contains(" ") }
        .toSet()

    /**
     * Normalizes input text to defeat evasion techniques:
     * - Converts to lowercase
     * - Normalizes common leetspeak substitutions (@ -> a, 1/! -> i, 0 -> o, $ -> s, etc.)
     * - Collapses letter flooding (e.g. "chuuuutttiiya" -> "chutiya")
     */
    fun normalize(rawText: String): String {
        var text = rawText.lowercase().trim()
        text = text.replace("@", "a")
            .replace("0", "o")
            .replace("1", "i")
            .replace("!", "i")
            .replace("$", "s")
            .replace("3", "e")
            .replace("5", "s")

        // Collapse repeated characters (3+ identical consecutive characters down to 1)
        val sb = StringBuilder()
        var lastChar = ' '
        var repeatCount = 0
        for (c in text) {
            if (c == lastChar) {
                repeatCount++
                if (repeatCount < 2) {
                    sb.append(c)
                }
            } else {
                lastChar = c
                repeatCount = 1
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Checks if the given text contains any abusive word or phrase.
     * Returns a Pair: (isAbusive, offendingPhrase)
     */
    fun checkAbuse(rawText: String): Pair<Boolean, String?> {
        if (rawText.isBlank()) return Pair(false, null)
        val normalized = normalize(rawText)

        // 1. Check multi-word abusive phrases first (exact phrase presence)
        for (phrase in multiWordPhrases) {
            val pattern = Pattern.compile("(?i)\\b" + Pattern.quote(phrase) + "\\b")
            if (pattern.matcher(normalized).find() || pattern.matcher(rawText.lowercase()).find()) {
                return Pair(true, phrase)
            }
        }

        // 2. Check single words with token boundary matching
        val tokens = normalized.split(Regex("[\\s.,!?;:()\\-_/\\\\]+"))
        for (token in tokens) {
            val cleanedToken = token.trim()
            if (cleanedToken.isNotEmpty() && singleWords.contains(cleanedToken)) {
                return Pair(true, cleanedToken)
            }
        }

        return Pair(false, null)
    }

    /**
     * Convenient helper: Returns the abusive word/phrase if abusive, or NULL if clean text.
     */
    fun findAbusiveWord(rawText: String): String? {
        val (isAbuse, offendingWord) = checkAbuse(rawText)
        return if (isAbuse) (offendingWord ?: "Inappropriate content") else null
    }

    /**
     * Filters and masks abusive words with asterisks (e.g. "****")
     */
    fun maskAbusiveText(rawText: String): String {
        var result = rawText
        for (phrase in multiWordPhrases) {
            val pattern = Pattern.compile("(?i)\\b" + Pattern.quote(phrase) + "\\b")
            result = pattern.matcher(result).replaceAll("*".repeat(phrase.length))
        }
        val tokens = result.split(Regex("(?<=[\\s.,!?;:()\\-_/\\\\])|(?=[\\s.,!?;:()\\-_/\\\\])"))
        val maskedTokens = tokens.map { token ->
            val norm = normalize(token).trim()
            if (singleWords.contains(norm)) {
                "*".repeat(token.length)
            } else {
                token
            }
        }
        return maskedTokens.joinToString("")
    }
}
