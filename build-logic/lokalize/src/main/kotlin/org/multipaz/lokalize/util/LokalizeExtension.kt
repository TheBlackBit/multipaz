package org.multipaz.lokalize.util

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configuration extension for the Lokalize plugin.
 */
abstract class LokalizeExtension @Inject constructor(objects: ObjectFactory) {

    /** The default/base locale (e.g., "en") */
    var defaultLocale: String = "en"

    /** List of target locales to validate/translate (e.g., listOf("es", "fr", "de")) */
    var targetLocales: List<String> = listOf()

    /** Whether to fail the build when missing translations are detected */
    var failOnMissing: Boolean = true

    /** API key for AI translation (can also use LOKALIZE_API_KEY env var) */
    val llmApiKey: Property<String> = objects.property(String::class.java)

    /** LLM provider to use for translation (OpenAI, Google, Anthropic) */
    val llmProvider: Property<LLMProvider> = objects.property(LLMProvider::class.java)

    /** LLM model to use for translation (e.g., GPT4o, Gemini2_5Flash, Opus_4_5) */
    val llModel: Property<LLmModel> = objects.property(LLmModel::class.java)

    /**
 * Base directory for string resources.
 * Default: "src/commonMain/composeResources"
 *
 * Example values:
 * - "src/commonMain/composeResources" (KMP Compose)
 * - "src/main/res" (Android only)
 * - "src/commonMain/resources" (Custom path)
 */
val resourcesDir: Property<String> = objects.property(String::class.java)

/**
 * Output format for generated resource files.
 * - XML: Android strings.xml format (default)
 * - JSON: JSON format with nested key support
 *
 * Default: XML
 */
val outputFormat: Property<OutputFormat> = objects.property(OutputFormat::class.java)
.apply { set(OutputFormat.XML) }
}
