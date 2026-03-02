package org.multipaz.lokalize.engine

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.multipaz.lokalize.model.LocaleBundle
import org.multipaz.lokalize.model.ResourceEntry

class TranslationComparatorTest {

    @Test
    fun `compare should find missing strings`() {
        // Given: Base has strings A and B, target has only A
        val base = LocaleBundle(
            locale = "en",
            strings = mapOf(
                "key1" to ResourceEntry.StringEntry("key1", "Hello"),
                "key2" to ResourceEntry.StringEntry("key2", "World")
            ),
            plurals = emptyMap(),
            arrays = emptyMap()
        )
        val target = LocaleBundle(
            locale = "es",
            strings = mapOf(
                "key1" to ResourceEntry.StringEntry("key1", "Hola")
            ),
            plurals = emptyMap(),
            arrays = emptyMap()
        )

        // When
        val result = TranslationComparator().compare(base, target)

        // Then
        assertTrue(result.hasMissing)
        assertEquals(setOf("key2"), result.missingStrings)
        assertEquals(1, result.totalMissing)
    }

    @Test
    fun `compare should find missing plurals`() {
        // Given: Base has plural, target doesn't
        val base = LocaleBundle(
            locale = "en",
            strings = emptyMap(),
            plurals = mapOf(
                "items" to ResourceEntry.PluralEntry("items", mapOf("one" to "1 item", "other" to "%d items"))
            ),
            arrays = emptyMap()
        )
        val target = LocaleBundle(
            locale = "es",
            strings = emptyMap(),
            plurals = emptyMap(),
            arrays = emptyMap()
        )

        // When
        val result = TranslationComparator().compare(base, target)

        // Then
        assertTrue(result.hasMissing)
        assertEquals(setOf("items"), result.missingPlurals)
    }

    @Test
    fun `compare should find incomplete plurals`() {
        // Given: Base has quantities [one, other], target has only [other]
        val base = LocaleBundle(
            locale = "en",
            strings = emptyMap(),
            plurals = mapOf(
                "items" to ResourceEntry.PluralEntry("items", mapOf("one" to "1 item", "other" to "%d items"))
            ),
            arrays = emptyMap()
        )
        val target = LocaleBundle(
            locale = "es",
            strings = emptyMap(),
            plurals = mapOf(
                "items" to ResourceEntry.PluralEntry("items", mapOf("other" to "%d elementos"))
            ),
            arrays = emptyMap()
        )

        // When
        val result = TranslationComparator().compare(base, target)

        // Then
        assertTrue(result.hasMissing)
        assertTrue(result.missingPlurals.isEmpty()) // Not completely missing
        assertEquals(mapOf("items" to listOf("one")), result.incompletePlurals)
    }

    @Test
    fun `compare should find missing arrays`() {
        // Given
        val base = LocaleBundle(
            locale = "en",
            strings = emptyMap(),
            plurals = emptyMap(),
            arrays = mapOf(
                "colors" to ResourceEntry.StringArrayEntry("colors", listOf("Red", "Green", "Blue"))
            )
        )
        val target = LocaleBundle(
            locale = "es",
            strings = emptyMap(),
            plurals = emptyMap(),
            arrays = emptyMap()
        )

        // When
        val result = TranslationComparator().compare(base, target)

        // Then
        assertTrue(result.hasMissing)
        assertEquals(setOf("colors"), result.missingArrays)
    }

    @Test
    fun `compare should return empty when target is complete`() {
        // Given: Target has everything base has
        val base = LocaleBundle(
            locale = "en",
            strings = mapOf("key1" to ResourceEntry.StringEntry("key1", "Hello")),
            plurals = emptyMap(),
            arrays = emptyMap()
        )
        val target = LocaleBundle(
            locale = "es",
            strings = mapOf("key1" to ResourceEntry.StringEntry("key1", "Hola")),
            plurals = emptyMap(),
            arrays = emptyMap()
        )

        // When
        val result = TranslationComparator().compare(base, target)

        // Then
        assertFalse(result.hasMissing)
        assertEquals(0, result.totalMissing)
    }

    @Test
    fun `requiredQuantities should return correct quantities for English`() {
        assertEquals(listOf("one", "other"), TranslationComparator().requiredQuantities("en"))
    }

    @Test
    fun `requiredQuantities should return correct quantities for Russian`() {
        assertEquals(listOf("one", "few", "many", "other"), TranslationComparator().requiredQuantities("ru"))
    }

    @Test
    fun `requiredQuantities should handle locale with region code`() {
        assertEquals(listOf("one", "other"), TranslationComparator().requiredQuantities("en-US"))
        assertEquals(listOf("one", "few", "many", "other"), TranslationComparator().requiredQuantities("ru-RU"))
    }

    @Test
    fun `totalMissing should count all types of missing entries`() {
        // Given: Missing 1 string, 1 array, and 1 incomplete plural quantity
        // (the plural "incomplete_plural" exists in target but is missing the "one" quantity)
        val base = LocaleBundle(
            locale = "en",
            strings = mapOf("missing_string" to ResourceEntry.StringEntry("missing_string", "Value")),
            plurals = mapOf("incomplete_plural" to ResourceEntry.PluralEntry("incomplete_plural", mapOf("one" to "1", "other" to "many"))),
            arrays = mapOf("missing_array" to ResourceEntry.StringArrayEntry("missing_array", listOf("A", "B")))
        )
        val target = LocaleBundle(
            locale = "es",
            strings = emptyMap(),
            plurals = mapOf("incomplete_plural" to ResourceEntry.PluralEntry("incomplete_plural", mapOf("other" to "muchos"))),
            arrays = emptyMap()
        )

        // When
        val result = TranslationComparator().compare(base, target)

        // Then: 1 string + 1 array + 1 incomplete plural quantity = 3 total
        assertEquals(3, result.totalMissing)
        assertEquals(1, result.missingStrings.size)
        assertEquals(0, result.missingPlurals.size) // Plural key exists, not completely missing
        assertEquals(1, result.missingArrays.size)
        assertEquals(1, result.incompletePlurals.size)
    }
}
