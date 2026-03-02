package org.multipaz.lokalize.worker

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.multipaz.lokalize.service.KoogTranslationService
import org.multipaz.lokalize.service.TranslationService
import org.multipaz.lokalize.util.LLMProvider
import org.multipaz.lokalize.util.LLmModel

/**
 * Parameters for the translation work action.
 * These parameters are passed to the isolated worker process.
 */
interface TranslationWorkParameters : WorkParameters {
    val apiKey: Property<String>
    val provider: Property<LLMProvider>
    val model: Property<LLmModel>
    val baseLocale: Property<String>
    val targetLocale: Property<String>
    val texts: ListProperty<String>
    val keys: ListProperty<String>
    val resultsFile: RegularFileProperty
}

/**
 * Work action that performs translation in an isolated worker process.
 *
 * Uses TranslationService abstraction for testability and clean architecture.
 */
abstract class TranslationWorkAction : WorkAction<TranslationWorkParameters> {

    override fun execute() {
        val apiKey = parameters.apiKey.get()
        val provider = parameters.provider.get()
        val model = parameters.model.get()
        val baseLocale = parameters.baseLocale.get()
        val targetLocale = parameters.targetLocale.get()
        val texts = parameters.texts.get()
        val keys = parameters.keys.get()

        // Check for empty API key
        if (apiKey.isBlank()) {
            println("[Worker] No API key provided - skipping AI translation")
            writeEmptyResults()
            return
        }

        println("[Worker] Translating ${texts.size} strings from $baseLocale to $targetLocale using ${provider.name}")

        // Create translation service
        val translationService: TranslationService = KoogTranslationService(
            apiKey = apiKey,
            provider = provider,
            model = model
        )

        // Translate - propagate errors to fail the task
        val translations = try {
            kotlinx.coroutines.runBlocking {
                translationService.translate(
                    texts = texts,
                    keys = keys,
                    sourceLocale = baseLocale,
                    targetLocale = targetLocale
                )
            }
        } catch (e: Exception) {
            val isRateLimit = e.message?.contains("429") == true ||
                             e.message?.contains("RESOURCE_EXHAUSTED") == true ||
                             e.message?.contains("quota") == true ||
                             e.message?.contains("limit: 0") == true

            if (isRateLimit) {
                println("[Worker] ⚠ API quota exhausted (429) - translation failed")
                println("[Worker]   Get a new API key or wait 24h for quota reset")
            } else {
                println("[Worker] ⚠ Translation failed: ${e.message}")
                e.printStackTrace()
            }
            // Re-throw to fail the task instead of silently using fallback
            throw RuntimeException("Translation failed for locale '$targetLocale': ${e.message}", e)
        }

        // If we got here, translations succeeded - use them directly
        val finalTranslations = mutableMapOf<String, String>()
        keys.forEachIndexed { index, key ->
            finalTranslations[key] = translations[key] ?: texts[index]
        }

        println("[Worker] Completed ${finalTranslations.size} entries (AI: ${translations.size}, fallback: ${texts.size - translations.size})")
        writeResults(finalTranslations)
    }

    private fun writeResults(translations: Map<String, String>) {
        val resultsFile = parameters.resultsFile.get().asFile
        resultsFile.parentFile?.mkdirs()

        val resultsJson = translations.entries.joinToString(
            prefix = "{\n",
            separator = ",\n",
            postfix = "\n}"
        ) { (key, value) ->
            val escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            "  \"$key\": \"$escapedValue\""
        }

        resultsFile.writeText(resultsJson)
    }

    private fun writeEmptyResults() {
        writeResults(emptyMap())
    }
}
