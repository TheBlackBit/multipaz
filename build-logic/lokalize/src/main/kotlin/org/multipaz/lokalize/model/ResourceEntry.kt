package org.multipaz.lokalize.model

/**
 * Represents a resource entry in Android strings.xml files.
 * Can be a simple string, plural, or string array.
 */
sealed class ResourceEntry {
    abstract val key: String

    /**
     * Simple string entry.
     */
    data class StringEntry(
        override val key: String,
        val value: String
    ) : ResourceEntry()

    /**
     * Plural entry with quantity variants.
     * Android supports: zero, one, two, few, many, other
     */
    data class PluralEntry(
        override val key: String,
        val items: Map<String, String> // quantity -> value (one, other, etc.)
    ) : ResourceEntry() {
        companion object {
            val QUANTITIES = listOf("zero", "one", "two", "few", "many", "other")
        }
    }

    /**
     * String array entry.
     */
    data class StringArrayEntry(
        override val key: String,
        val items: List<String>
    ) : ResourceEntry()
}
