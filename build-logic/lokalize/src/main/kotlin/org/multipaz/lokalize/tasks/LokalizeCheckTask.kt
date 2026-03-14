package org.multipaz.lokalize.tasks

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.multipaz.lokalize.util.LokalizeExtension
import org.multipaz.lokalize.util.OutputFormat
import org.multipaz.lokalize.engine.ResourceScanner
import org.multipaz.lokalize.engine.TranslationComparator
import java.io.File

/**
 * Validates that all target locales have complete and consistent translations.
 *
 * This task is cacheable - it will be skipped if inputs haven't changed.
 * Use `--rerun-tasks` to force re-validation.
 */
@CacheableTask
abstract class LokalizeCheckTask : DefaultTask() {

    @get:Internal
    abstract val extension: Property<LokalizeExtension>

    @get:Input
    abstract val defaultLocale: Property<String>

    @get:Input
    abstract val targetLocales: ListProperty<String>

    @get:Input
    abstract val failOnMissing: Property<Boolean>

    @get:Input
    abstract val resourcesDir: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baseStringsFile: RegularFileProperty

    @get:Input
    abstract val outputFormat: Property<OutputFormat>

    @TaskAction
    fun run() = runBlocking {
        val scanner = ResourceScanner()
        val comparator = TranslationComparator()

        val baseFile = baseStringsFile.get().asFile
        if (!baseFile.exists()) {
            throw GradleException("Base strings file not found at: ${baseFile.absolutePath}")
        }

        val baseBundle = scanner.scan(baseFile)
        logger.info("Found ${baseBundle.totalEntries()} entries in base locale '${defaultLocale.get()}'")
        logger.info("  - Strings: ${baseBundle.strings.size}")
        logger.info("  - Plurals: ${baseBundle.plurals.size}")
        logger.info("  - Arrays: ${baseBundle.arrays.size}")

        val resDir = resourcesDir.get()
        val format = outputFormat.getOrElse(OutputFormat.XML)
        var totalMissingCount = 0
        val allMissingByLocale = mutableMapOf<String, TranslationComparator.ComparisonResult>()

        targetLocales.get().forEach { locale ->
            val targetFile = when (format) {
                OutputFormat.XML -> File(project.projectDir, "$resDir/values-$locale/strings.xml")
                OutputFormat.JSON -> File(project.projectDir, "$resDir/values-$locale/strings.json")
            }
            val targetBundle = if (targetFile.exists()) {
                scanner.scan(targetFile)
            } else {
                // File doesn't exist - everything is missing
                val emptyComparison = TranslationComparator.ComparisonResult(
                    missingStrings = baseBundle.strings.keys,
                    missingPlurals = baseBundle.plurals.keys,
                    missingArrays = baseBundle.arrays.keys,
                    incompletePlurals = emptyMap()
                )
                allMissingByLocale[locale] = emptyComparison
                totalMissingCount += emptyComparison.totalMissing
                return@forEach
            }

            val comparison = comparator.compareWithPluralRules(baseBundle, targetBundle)

            if (comparison.hasMissing) {
                allMissingByLocale[locale] = comparison
                totalMissingCount += comparison.totalMissing
            } else {
                logger.lifecycle("✓ Locale '$locale' is complete (${targetBundle.totalEntries()} entries)")
            }
        }

        if (allMissingByLocale.isNotEmpty()) {
            val message = buildString {
                appendLine()
                appendLine("=".repeat(60))
                appendLine("Lokalize check failed: $totalMissingCount missing translations")
                appendLine("=".repeat(60))
                appendLine()

                allMissingByLocale.toSortedMap().forEach { (locale, comparison) ->
                    appendLine("[$locale] Missing ${comparison.totalMissing} translations:")

                    comparison.missingStrings.forEach { key ->
                        val baseValue = baseBundle.strings[key]?.value ?: ""
                        appendLine("  • $key = \"$baseValue\"")
                    }

                    comparison.missingPlurals.forEach { key ->
                        val quantities =
                            baseBundle.plurals[key]?.items?.keys?.sorted() ?: emptyList()
                        appendLine("  • $key (plural, needs: ${quantities.joinToString()})")
                    }

                    comparison.incompletePlurals.forEach { (key, missingQuantities) ->
                        appendLine("  • $key (incomplete plural, missing: ${missingQuantities.joinToString()})")
                    }

                    comparison.missingArrays.forEach { key ->
                        val size = baseBundle.arrays[key]?.items?.size ?: 0
                        appendLine("  • $key (array, size: $size)")
                    }

                    appendLine()
                }

                appendLine("-".repeat(60))
                appendLine("Run './gradlew lokalizeFix' to generate missing translations")
                appendLine("-".repeat(60))
            }

            if (failOnMissing.getOrElse(true)) {
                throw GradleException(message)
            } else {
                logger.warn(message)
            }
        } else {
            logger.lifecycle("✓ All ${targetLocales.get().size} locales are complete!")
        }
    }
}
