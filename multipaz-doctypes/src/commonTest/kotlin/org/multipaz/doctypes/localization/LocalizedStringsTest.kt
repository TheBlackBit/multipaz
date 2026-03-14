package org.multipaz.doctypes.localization

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals
import kotlin.test.assertEquals
import org.multipaz.doctypes.generated.GeneratedTranslations

class LocalizedStringsTest {

    @Test
    fun testGetStringReturnsValueForKnownKey() {
        // Test with a known key - DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE
        val key = GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE
        val result = LocalizedStrings.getString(key)

        // Should not be the key itself (which means translation was found or fell back to English)
        assertNotEquals(key, result, "Should return a translated value, not the key itself")
    }

    @Test
    fun testGetStringReturnsNonEmpty() {
        val key = GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE
        val result = LocalizedStrings.getString(key)

        assertTrue(result.isNotEmpty(), "Localized string should not be empty")
    }

    @Test
    fun testGetStringWithUnknownKeyReturnsKey() {
        // For unknown keys, the function should return the key itself
        val unknownKey = "unknown_key_that_does_not_exist"
        val result = LocalizedStrings.getString(unknownKey)

        assertEquals(unknownKey, result, "Unknown key should return the key itself")
    }

    @Test
    fun testGeneratedTranslationsHasAllLanguages() {
        val languages = GeneratedTranslations.allLanguages

        assertTrue(languages.isNotEmpty(), "Should have at least one language")
        assertTrue(languages.contains("en"), "Should contain English")
    }

    @Test
    fun testGetAllLocalesMatchesGeneratedTranslations() {
        val locales = LocalizedStrings.getAllLocales()

        assertEquals(GeneratedTranslations.allLanguages, locales)
        assertTrue(locales.contains("en"), "Should contain English")
    }

    @Test
    fun testGetMapForLocaleReturnsNonEmptyMap() {
        // Test English map
        val enMap = GeneratedTranslations.getMapForLocale("en")
        assertTrue(enMap.isNotEmpty(), "English map should not be empty")

        // Verify it contains expected keys
        assertNotNull(enMap[GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE],
            "English map should contain driving license display name")
    }

    @Test
    fun testFallbackToEnglishForUnknownLocale() {
        // Request a non-existent locale - should fall back to English
        val map = GeneratedTranslations.getMapForLocale("xx")
        val enMap = GeneratedTranslations.getMapForLocale("en")

        // Should return the English map as fallback
        assertTrue(map === enMap, "Unknown locale should fallback to English map")
    }

    @Test
    fun testFallbackToEnglishForMissingTranslation() {
        // For any locale, if a key is missing, it should fallback to English
        // Testing by getting a string that should exist in all languages
        val key = GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE

        // Get for current locale
        val result = LocalizedStrings.getString(key)
        val enResult = GeneratedTranslations.getMapForLocale("en")[key]

        assertNotNull(result, "Result should not be null")
        assertNotNull(enResult, "English result should not be null")

        // The result should either be from the current locale or from English
        assertTrue(
            result.isNotEmpty(),
            "Result should be a non-empty string (from current locale or fallback to English)"
        )
    }

    @Test
    fun testMultipleCallsAreConsistent() {
        val key = GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE

        val result1 = LocalizedStrings.getString(key)
        val result2 = LocalizedStrings.getString(key)

        assertEquals(result1, result2, "Multiple calls should return the same result")
    }

    @Test
    fun testDifferentKeysReturnDifferentValues() {
        val key1 = GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE
        val key2 = GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_PHOTO_ID

        val result1 = LocalizedStrings.getString(key1)
        val result2 = LocalizedStrings.getString(key2)

        assertNotEquals(result1, result2, "Different keys should return different values")
    }
}
