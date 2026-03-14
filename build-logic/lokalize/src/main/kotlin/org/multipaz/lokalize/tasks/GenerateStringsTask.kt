package org.multipaz.lokalize.tasks

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.multipaz.lokalize.util.OutputFormat
import java.io.File
import java.util.Locale

/**
 * Gradle task that generates Kotlin constants from JSON string resources.
 *
 * Scans src/commonMain/resources for folders starting with "values" and generates
 * Kotlin files with embedded string maps. This allows strings to be baked into the
 * binary for platforms where file access is unreliable (e.g., iOS).
 */
abstract class GenerateStringsTask : DefaultTask() {

    @get:InputDirectory
    abstract val resourcesDir: DirectoryProperty

    @get:Input
    abstract val defaultLocale: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val generatedClassName: Property<String>

    @get:Input
    abstract val outputFormat: Property<OutputFormat>

    init {
        description = "Generates Kotlin constants from JSON string resources"
        group = "lokalize"
    }

    @TaskAction
    fun generate() {
        // Skip if output format is not JSON - this task only supports JSON format
        if (outputFormat.get() != OutputFormat.JSON) {
            logger.lifecycle("Skipping generateMultipazStrings task - output format is '${outputFormat.get().name}', only JSON format is supported")
            return
        }

        val resources = resourcesDir.get().asFile
        val output = outputDir.get().asFile

        if (!resources.exists()) {
            logger.lifecycle("Resources directory does not exist: ${resources.absolutePath}")
            return
        }

        // Clean and create output directory
        output.deleteRecursively()
        output.mkdirs()

        // Find all values* directories
        val valuesDirs = resources.listFiles { file ->
            file.isDirectory && file.name.startsWith("values")
        }?.sortedBy { it.name } ?: emptyList()

        logger.lifecycle("Found ${valuesDirs.size} value directories in ${resources.absolutePath}")

        val languageMaps = mutableMapOf<String, Map<String, String>>()
        var defaultLocaleMap: Map<String, String>? = null

        valuesDirs.forEach { dir ->
            val stringsFile = File(dir, "strings.json")
            if (stringsFile.exists()) {
                val lang = extractLanguageCode(dir.name)
                logger.lifecycle("Processing ${dir.name}/strings.json for language: $lang")

                try {
                    val jsonContent = stringsFile.readText()
                    val jsonObject = Json.parseToJsonElement(jsonContent).jsonObject
                    val map = mutableMapOf<String, String>()

                    jsonObject.forEach { (key, jsonElement) ->
                        map[key] = jsonElement.jsonPrimitive.content
                    }

                    languageMaps[lang] = map
                    if (lang == defaultLocale.get()) {
                        defaultLocaleMap = map
                    }

                    // Generate individual file for this language
                    generateLanguageFile(output, lang, map)
                } catch (e: Exception) {
                    logger.error("Failed to parse ${stringsFile.absolutePath}: ${e.message}")
                }
            } else {
                logger.lifecycle("No strings.json found in ${dir.name}")
            }
        }

        // Generate the main access object that contains all maps
        generateAccessObject(output, languageMaps)

        // Generate StringKeys object from default locale strings.
        val defaultMap = defaultLocaleMap ?: languageMaps[defaultLocale.get()]
        if (defaultMap != null) {
            generateStringKeysFile(output, defaultMap)
        } else {
            logger.warn("Default locale '${defaultLocale.get()}' not found, skipping StringKeys.kt generation")
        }

        logger.lifecycle("Generated ${languageMaps.size} string maps in ${output.absolutePath}")
    }

    private fun extractLanguageCode(dirName: String): String {
        return when {
            dirName == "values" -> defaultLocale.get()
            dirName.startsWith("values-") -> dirName.substringAfter("values-")
            else -> defaultLocale.get()
        }
    }

    private fun generateLanguageFile(outputDir: File, lang: String, map: Map<String, String>) {
        val fileName = "Strings_${lang.replace("-", "_")}.kt"
        val file = File(outputDir, fileName)
        val safeLang = lang.replace("-", "_")

        val content = buildString {
            appendLine("package ${packageName.get()}")
            appendLine()
            appendLine("/**")
            appendLine(" * Generated strings for language: $lang")
            appendLine(" * DO NOT EDIT - This file is auto-generated by the generateMultipazStrings task")
            appendLine(" */")
            appendLine("internal val strings_$safeLang = mapOf(")

            map.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
                val comma = if (index < map.size - 1) "," else ""
                appendLine("    \"${escapeString(entry.key)}\" to \"${escapeString(entry.value)}\"$comma")
            }

            appendLine(")")
        }

        file.writeText(content)
        logger.lifecycle("Generated $fileName with ${map.size} strings")
    }

    private fun generateAccessObject(outputDir: File, languageMaps: Map<String, Map<String, String>>) {
        val file = File(outputDir, "${generatedClassName.get()}.kt")
        val default = defaultLocale.get()
        val safeDefault = default.replace("-", "_")

        val content = buildString {
            appendLine("package ${packageName.get()}")
            appendLine()
            appendLine("/**")
            appendLine(" * Central access point for localized strings.")
            appendLine(" * DO NOT EDIT - This file is auto-generated by the generateMultipazStrings task")
            appendLine(" */")
            appendLine("object ${generatedClassName.get()} {")
            appendLine()
            appendLine("    private val defaultMap = strings_$safeDefault")
            appendLine()

            // Add language map entries
            languageMaps.keys.sorted().forEach { lang ->
                val safeLang = lang.replace("-", "_")
                appendLine("    private val map_$safeLang = strings_$safeLang")
            }
            appendLine()

            // Add allLanguages list
            appendLine("    val allLanguages: List<String> = listOf(")
            val sortedLangs = languageMaps.keys.sorted()
            sortedLangs.forEachIndexed { index, lang ->
                val comma = if (index < sortedLangs.size - 1) "," else ""
                appendLine("        \"$lang\"$comma")
            }
            appendLine("    )")
            appendLine()

            // getMapForLocale function - matches the API expected by LocalizedStrings
            appendLine("    /**")
            appendLine("     * Returns the string map for the given locale.")
            appendLine("     * Falls back to '$default' if the locale is not available.")
            appendLine("     *")
            appendLine("     * @param locale The locale code (e.g., \"en\", \"es\", \"de\")")
            appendLine("     * @return Map of string keys to localized values")
            appendLine("     */")
            appendLine("    fun getMapForLocale(locale: String): Map<String, String> {")
            appendLine("        return when (locale) {")

            sortedLangs.forEach { lang ->
                val safeLang = lang.replace("-", "_")
                appendLine("            \"$lang\" -> map_$safeLang")
            }

            appendLine("            else -> defaultMap")
            appendLine("        }")
            appendLine("    }")
            appendLine()

            // getString function for convenience
            appendLine("    /**")
            appendLine("     * Returns the localized string for the given key and language.")
            appendLine("     * Falls back to the default language, then to the key itself if not found.")
            appendLine("     *")
            appendLine("     * @param key The string key")
            appendLine("     * @param lang The language code (e.g., \"en\", \"es\", \"de\")")
            appendLine("     * @return The localized string or the key as fallback")
            appendLine("     */")
            appendLine("    fun getString(key: String, lang: String): String {")
            appendLine("        val map = getMapForLocale(lang)")
            appendLine("        return map[key] ?: defaultMap[key] ?: key")
            appendLine("    }")
            appendLine()

            // containsKey function
            appendLine("    /**")
            appendLine("     * Checks if the given key exists in the specified language.")
            appendLine("     *")
            appendLine("     * @param key The string key to check")
            appendLine("     * @param lang The language code")
            appendLine("     * @return true if the key exists in the language map")
            appendLine("     */")
            appendLine("    fun containsKey(key: String, lang: String): Boolean {")
            appendLine("        return getMapForLocale(lang).containsKey(key)")
            appendLine("    }")

            appendLine("}")
        }

        file.writeText(content)
        logger.lifecycle("Generated ${generatedClassName.get()}.kt with ${languageMaps.size} languages")
    }

    private fun generateStringKeysFile(outputDir: File, defaultMap: Map<String, String>) {
        val file = File(outputDir, "GeneratedStringKeys.kt")
        val entries = defaultMap.keys.sorted().map { key ->
            val constName = key
                .uppercase(Locale.US)
                .replace(Regex("[^A-Z0-9]+"), "_")
                .trim('_')
            constName to key
        }

        val content = buildString {
            appendLine("package org.multipaz.doctypes.localization")
            appendLine()
            appendLine("/**")
            appendLine(" * Generated string resource keys from values/strings.json.")
            appendLine(" * DO NOT EDIT - This file is auto-generated by the generateMultipazStrings task")
            appendLine(" */")
            appendLine("internal object GeneratedStringKeys {")
            appendLine()
            entries.forEach { (constName, key) ->
                appendLine("    const val $constName = \"${escapeString(key)}\"")
            }
            appendLine("}")
        }

        file.writeText(content)
        logger.lifecycle("Generated GeneratedStringKeys.kt with ${entries.size} keys")
    }

    private fun escapeString(str: String): String {
        return str
            .replace("\\", "\\\\")  // Backslash
            .replace("\"", "\\\"")   // Double quotes
            .replace("$", "\$")       // Dollar sign for string interpolation
            .replace("\n", "\\n")      // Newline
            .replace("\r", "\\r")      // Carriage return
            .replace("\t", "\\t")      // Tab
    }
}
