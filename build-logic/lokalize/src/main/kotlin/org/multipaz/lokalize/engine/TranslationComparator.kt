package org.multipaz.lokalize.engine

import org.multipaz.lokalize.model.LocaleBundle

/**
 * Compares two locale bundles to find missing or inconsistent translations.
 */
class TranslationComparator {

    /**
     * Result of comparing two locale bundles.
     */
    data class ComparisonResult(
        val missingStrings: Set<String>,
        val missingPlurals: Set<String>,
        val missingArrays: Set<String>,
        val incompletePlurals: Map<String, List<String>> // key -> missing quantities
    ) {
        val hasMissing: Boolean
            get() = missingStrings.isNotEmpty() ||
                    missingPlurals.isNotEmpty() ||
                    missingArrays.isNotEmpty() ||
                    incompletePlurals.isNotEmpty()

        val totalMissing: Int
            get() = missingStrings.size + missingPlurals.size + missingArrays.size +
                    incompletePlurals.values.sumOf { it.size }
    }

    /**
     * Compares base bundle against target bundle.
     *
     * @param base The source-of-truth locale bundle (e.g., English)
     * @param target The bundle to check (e.g., Spanish)
     * @return Comparison result with all missing/incomplete entries
     */
    fun compare(base: LocaleBundle, target: LocaleBundle): ComparisonResult {
        // Find missing strings
        val missingStrings = base.strings.keys - target.strings.keys

        // Find missing plurals
        val missingPlurals = base.plurals.keys - target.plurals.keys

        // Find incomplete plurals (target has the plural but missing some quantities)
        val incompletePlurals = mutableMapOf<String, List<String>>()
        base.plurals.forEach { (key, basePlural) ->
            val targetPlural = target.plurals[key]
            if (targetPlural != null) {
                val missingQuantities = basePlural.items.keys - targetPlural.items.keys
                if (missingQuantities.isNotEmpty()) {
                    incompletePlurals[key] = missingQuantities.toList()
                }
            }
        }

        val missingArrays = base.arrays.keys - target.arrays.keys

        return ComparisonResult(
            missingStrings = missingStrings,
            missingPlurals = missingPlurals,
            missingArrays = missingArrays,
            incompletePlurals = incompletePlurals
        )
    }

    /**
     * Check if a specific quantity is required for a locale.
     * Based on CLDR plural rules.
     */
    fun requiredQuantities(locale: String): List<String> {
        return when (locale.lowercase().split("-")[0]) { // Get language code
            "en", "de", "es", "it", "pt" -> listOf("one", "other")
            "fr", "ca" -> listOf("one", "other") // French treats 0 as "one"
            "ru", "uk", "hr", "sr" -> listOf("one", "few", "many", "other")
            "pl" -> listOf("one", "few", "many", "other")
            "ar" -> listOf("zero", "one", "two", "few", "many", "other")
            "ja", "ko", "zh", "vi", "th" -> listOf("other") // No grammatical plurals
            else -> listOf("one", "other") // Default fallback
        }
    }

    /**
     * Compares base bundle against target bundle, accounting for locale-specific plural rules.
     * Only reports missing quantities that the target locale actually requires.
     *
     * @param base The source-of-truth locale bundle (e.g., English)
     * @param target The bundle to check (e.g., Thai)
     * @return Comparison result with all missing/incomplete entries
     */
    fun compareWithPluralRules(base: LocaleBundle, target: LocaleBundle): ComparisonResult {
        // Find missing strings
        val missingStrings = base.strings.keys - target.strings.keys

        // Find missing plurals (entire plural entry missing)
        val missingPlurals = base.plurals.keys - target.plurals.keys

        // Find incomplete plurals - only check quantities that target locale actually needs
        val targetLocale = target.locale
        val requiredTargetQuantities = requiredQuantities(targetLocale).toSet()

        val incompletePlurals = mutableMapOf<String, List<String>>()
        base.plurals.forEach { (key, basePlural) ->
            val targetPlural = target.plurals[key]
            if (targetPlural != null) {
                // Only report missing quantities that the target locale requires
                val missingQuantities = requiredTargetQuantities - targetPlural.items.keys
                if (missingQuantities.isNotEmpty()) {
                    incompletePlurals[key] = missingQuantities.toList()
                }
            }
        }

        val missingArrays = base.arrays.keys - target.arrays.keys

        return ComparisonResult(
            missingStrings = missingStrings,
            missingPlurals = missingPlurals,
            missingArrays = missingArrays,
            incompletePlurals = incompletePlurals
        )
    }
}
