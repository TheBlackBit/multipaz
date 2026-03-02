package org.multipaz.lokalize.service

/**
 * Service interface for text translation.
 * Allows for easy testing via dependency injection.
 */
interface TranslationService {
    /**
     * Translates a batch of texts from source to target language.
     *
     * @param texts List of texts to translate
     * @param keys Associated keys for each text (for context)
     * @param sourceLocale Source language locale (e.g., "en")
     * @param targetLocale Target language locale (e.g., "es")
     * @return Map of key to translated text
     */
    suspend fun translate(
        texts: List<String>,
        keys: List<String>,
        sourceLocale: String,
        targetLocale: String
    ): Map<String, String>
}
