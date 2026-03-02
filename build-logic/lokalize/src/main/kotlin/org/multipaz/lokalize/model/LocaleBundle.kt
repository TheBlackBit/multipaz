package org.multipaz.lokalize.model

/**
 * Represents a complete locale bundle with all resource types.
 */
data class LocaleBundle(
    val locale: String,
    val strings: Map<String, ResourceEntry.StringEntry>,
    val plurals: Map<String, ResourceEntry.PluralEntry>,
    val arrays: Map<String, ResourceEntry.StringArrayEntry>
) {
    /**
     * Get total entry count.
     */
    fun totalEntries(): Int = strings.size + plurals.size + arrays.size

    companion object {
        /**
         * Create an empty bundle.
         */
        fun empty(locale: String) = LocaleBundle(locale, emptyMap(), emptyMap(), emptyMap())
    }
}
