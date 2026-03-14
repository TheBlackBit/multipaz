package org.multipaz.lokalize.engine

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.multipaz.lokalize.model.LocaleBundle
import org.multipaz.lokalize.model.ResourceEntry
import java.io.File

/**
 * JSON resource scanner strategy.
 * Parses JSON localization files with support for nested keys and structured plurals/arrays.
 *
 * Example input structure:
 * ```
 * {
 *   "simpleKey": "Simple value",
 *   "nested": {
 *     "key": "Nested value"
 *   },
 *   "pluralKey": {
 *     "_type": "plural",
 *     "one": "%d item",
 *     "other": "%d items"
 *   },
 *   "arrayKey": {
 *     "_type": "array",
 *     "values": ["Item 1", "Item 2", "Item 3"]
 *   }
 * }
 * ```
 */
class JsonResourceScannerStrategy : ResourceScannerStrategy {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun scan(file: File): LocaleBundle {
        if (!file.exists()) {
            return LocaleBundle.empty(extractLocaleFromPath(file))
        }

        return try {
            val content = file.readText()
            val jsonObject = json.parseToJsonElement(content).jsonObject
            val entries = extractEntries(jsonObject)
            val locale = extractLocaleFromPath(file)

            LocaleBundle(
                locale = locale,
                strings = entries.strings,
                plurals = entries.plurals,
                arrays = entries.arrays
            )
        } catch (e: Exception) {
            // Return empty bundle on parse error
            LocaleBundle.empty(extractLocaleFromPath(file))
        }
    }

    override fun supports(file: File): Boolean {
        return file.exists() && file.name.endsWith(".json", ignoreCase = true)
    }

    /**
     * Container for all extracted resource entries.
     */
    private data class ResourceEntries(
        val strings: Map<String, ResourceEntry.StringEntry>,
        val plurals: Map<String, ResourceEntry.PluralEntry>,
        val arrays: Map<String, ResourceEntry.StringArrayEntry>
    )

    /**
     * Extracts all resource entries from the JSON object.
     */
    private fun extractEntries(jsonObject: JsonObject): ResourceEntries {
        val strings = mutableMapOf<String, ResourceEntry.StringEntry>()
        val plurals = mutableMapOf<String, ResourceEntry.PluralEntry>()
        val arrays = mutableMapOf<String, ResourceEntry.StringArrayEntry>()

        jsonObject.forEach { (key, element) ->
            when (element) {
                is JsonObject -> {
                    val typeMarker = element[TYPE_MARKER]?.jsonPrimitive?.content
                    when (typeMarker) {
                        PLURAL_TYPE -> {
                            // It's a plural
                            val items = element
                                .filter { it.key != TYPE_MARKER }
                                .mapValues { it.value.jsonPrimitive.content }
                            plurals[key] = ResourceEntry.PluralEntry(key, items)
                        }

                        ARRAY_TYPE -> {
                            // It's an array
                            val values = element[VALUES_KEY]?.let { valuesElement ->
                                if (valuesElement is JsonArray) {
                                    valuesElement.map { it.jsonPrimitive.content }
                                } else emptyList()
                            } ?: emptyList()
                            arrays[key] = ResourceEntry.StringArrayEntry(key, values)
                        }

                        else -> {
                            // It's a nested object - extract all leaf values
                            extractNestedStrings(key, element, strings)
                        }
                    }
                }

                is JsonPrimitive -> {
                    // It's a simple string
                    strings[key] = ResourceEntry.StringEntry(key, element.content)
                }

                else -> {} // Ignore other types
            }
        }

        return ResourceEntries(strings, plurals, arrays)
    }

    /**
     * Recursively extracts nested string values from a JSON object.
     */
    private fun extractNestedStrings(
        prefix: String,
        jsonObject: JsonObject,
        result: MutableMap<String, ResourceEntry.StringEntry>
    ) {
        jsonObject.forEach { (key, element) ->
            val fullKey = "$prefix/$key"
            when {
                element is JsonPrimitive -> {
                    result[fullKey] = ResourceEntry.StringEntry(fullKey, element.content)
                }
                element is JsonObject -> {
                    extractNestedStrings(fullKey, element, result)
                }
            }
        }
    }

    /**
     * Extracts locale from file path (e.g., values-es → es, values → default).
     */
    private fun extractLocaleFromPath(file: File): String {
        val parentName = file.parentFile?.name ?: "values"
        return when {
            parentName == "values" -> "default"
            parentName.startsWith("values-") -> parentName.removePrefix("values-")
            else -> "unknown"
        }
    }

    companion object {
        private const val TYPE_MARKER = "_type"
        private const val PLURAL_TYPE = "plural"
        private const val ARRAY_TYPE = "array"
        private const val VALUES_KEY = "values"
    }
}
