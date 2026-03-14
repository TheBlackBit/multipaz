package org.multipaz.lokalize

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.multipaz.lokalize.tasks.GenerateStringsTask
import org.multipaz.lokalize.tasks.LokalizeCheckTask
import org.multipaz.lokalize.tasks.LokalizeTranslateTask
import org.multipaz.lokalize.util.LLMProvider
import org.multipaz.lokalize.util.LLmModel
import org.multipaz.lokalize.util.LokalizeExtension

/**
 * Lokalize Gradle Plugin for internationalization enforcement and AI-assisted translation.
 *
 * Provides tasks:
 * - lokalizeCheck: Validates that all target locales have complete translations
 * - lokalizeFix: Generates missing translations using AI
 *
 * Example configuration:
 * ```kotlin
 * lokalize {
 *     defaultLocale = "en"
 *     targetLocales = listOf("es", "fr", "de")
 *     failOnMissing = true
 *     llmApiKey.set("your-api-key") // or use LOKALIZE_API_KEY env var
 *     llmProvider.set(LLMProvider.GOOGLE)  // or OPENAI, ANTHROPIC
 *     llModel.set(LLmModel.GEMINI2_0_FLASH)  // see LLmModel enum for all options
 *     resourcesDir.set("src/commonMain/composeResources") // optional: custom path
 *     outputFormat.set(OutputFormat.XML) // or JSON for web/desktop projects
 * }
 * ```
 *
 * Output formats:
 * - XML: Android strings.xml format with values/values-locale folders
 * - JSON: JSON format with nested keys, stored as values/strings.json and values-locale/strings.json
 */
class LokalizePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val ext = target.extensions.create("lokalize", LokalizeExtension::class.java)

        // CRITICAL: Create worker configuration in the target project for classpath isolation
        // This ensures the worker JVM gets its own dependencies separate from Gradle's runtime
        // Note: Koog repository must be added in settings.gradle.kts dependencyResolutionManagement
        target.configurations.create("lokalizeWorker") { conf ->
            conf.isCanBeConsumed = false
            conf.isCanBeResolved = true
        }

        // Add dependencies to the worker configuration
        // Using correct artifact names from ai.koog (not the libs aliases)
        target.dependencies.apply {
            add("lokalizeWorker", "ai.koog:koog-agents:0.5.1")
            add("lokalizeWorker", "ai.koog:agents-ext:0.5.1")
            add("lokalizeWorker", "ai.koog:prompt-executor-openai-client:0.5.1")
            add("lokalizeWorker", "ai.koog:prompt-executor-google-client:0.5.1")
            add("lokalizeWorker", "ai.koog:prompt-executor-anthropic-client:0.5.1")
            add("lokalizeWorker", "ai.koog:prompt-executor-llms-all:0.5.1")
            add("lokalizeWorker", "ai.koog:prompt-model:0.5.1")
            add("lokalizeWorker", "org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
            add("lokalizeWorker", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
        }

        ext.llmApiKey.convention(target.providers.environmentVariable("LOKALIZE_API_KEY")
            .orElse(target.providers.environmentVariable("KOOG_API_KEY"))
            .orElse(target.providers.environmentVariable("OPENAI_API_KEY"))
            .orElse(target.providers.environmentVariable("GOOGLE_API_KEY"))
            .orElse(target.providers.environmentVariable("ANTHROPIC_API_KEY")))

        ext.llmProvider.convention(LLMProvider.GOOGLE)
        ext.llModel.convention(LLmModel.GEMINI2_0_FLASH)
        ext.resourcesDir.convention("src/commonMain/composeResources")
        ext.outputFormat.convention(org.multipaz.lokalize.util.OutputFormat.XML)

        // Register check task - validates translations
        target.tasks.register("lokalizeCheck", LokalizeCheckTask::class.java) { task ->
            task.description = "Validates that all target locales have complete translations"
            task.group = "lokalize"

            // Wire extension values to task inputs lazily
            task.defaultLocale.convention(ext.defaultLocale)
            task.targetLocales.convention(ext.targetLocales)
            task.failOnMissing.convention(ext.failOnMissing)
            task.resourcesDir.convention(ext.resourcesDir)
            task.outputFormat.convention(ext.outputFormat)
            task.extension.convention(ext)

            // Compute base strings file path from resourcesDir and outputFormat
            task.baseStringsFile.convention(
                target.layout.projectDirectory.file(
                    ext.outputFormat.zip(ext.resourcesDir) { format, resDir ->
                        when (format) {
                            org.multipaz.lokalize.util.OutputFormat.XML -> "$resDir/values/strings.xml"
                            org.multipaz.lokalize.util.OutputFormat.JSON -> "$resDir/values/strings.json"
                        }
                    }
                )
            )
        }

        // Register fix task - generates missing translations with AI
        target.tasks.register("lokalizeFix", LokalizeTranslateTask::class.java) { task ->
            task.description = "Generates missing translations using AI"
            task.group = "lokalize"

            // Wire extension values to task inputs lazily
            task.llmApiKey.convention(ext.llmApiKey)
            task.llmProvider.convention(ext.llmProvider)
            task.llModel.convention(ext.llModel)
            task.resourcesDir.convention(ext.resourcesDir)
            task.defaultLocale.convention(ext.defaultLocale)
            task.targetLocales.convention(ext.targetLocales)
            task.outputFormat.convention(ext.outputFormat)
            task.extension.convention(ext)

            // Output directory for temporary translation results (in build/, not source)
            task.outputDirectory.convention(
                target.layout.buildDirectory.dir("lokalize/translations")
            )
        }

        // Register the generate strings task - it will decide at execution time what to do
        val generateStringsTask = target.tasks.register("generateMultipazStrings", GenerateStringsTask::class.java) { task ->
            task.description = "Generates Kotlin constants from JSON string resources for embedded access (skips for XML format)"
            task.group = "lokalize"

            task.defaultLocale.convention(ext.defaultLocale)
            task.outputFormat.convention(ext.outputFormat)
            task.packageName.convention("org.multipaz.doctypes.generated")
            task.generatedClassName.convention("GeneratedTranslations")

            task.resourcesDir.convention(
                target.layout.projectDirectory.dir(ext.resourcesDir)
            )

            task.outputDir.convention(
                target.layout.buildDirectory.dir("generated/kmp/kotlin/org/multipaz/doctypes/generated")
            )

            // Configure source directory EARLY (during configuration phase, not afterEvaluate)
            // KMP source sets are frozen after afterEvaluate, so we must do this now
            target.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java)?.let { kotlinExt ->
                kotlinExt.sourceSets.findByName("commonMain")?.let { sourceSet ->
                    // Use the task's output directory provider for lazy resolution
                    sourceSet.kotlin.srcDir(task.outputDir)
                    target.logger.lifecycle("Configured generated strings source dir for ${target.name}: ${task.outputDir.get().asFile.absolutePath}")
                }
            }
        }

        // Configure task dependencies during normal configuration phase (not afterEvaluate)
        // This ensures all compile tasks depend on string generation
        target.tasks.configureEach { compileTask ->
            val taskName = compileTask.name
            if (taskName in setOf(
                    "compileKotlinJvm",
                    "compileKotlinAndroid",
                    "compileDebugKotlinAndroid",
                    "compileReleaseKotlinAndroid",
                    "compileKotlinIosX64",
                    "compileKotlinIosArm64",
                    "compileKotlinIosSimulatorArm64",
                    "compileKotlinJs",
                    "compileKotlinWasmJs",
                    "compileKotlinMetadata",
                    "compileCommonMainKotlinMetadata"
                ) || (taskName.startsWith("compile") && taskName.contains("Kotlin"))
            ) {
                compileTask.dependsOn(generateStringsTask)
                target.logger.lifecycle("Added dependency: $taskName -> generateMultipazStrings for ${target.name}")
            }
        }

        target.afterEvaluate {
            // Only log the configuration status in afterEvaluate
            if (ext.outputFormat.get() == org.multipaz.lokalize.util.OutputFormat.JSON) {
                target.logger.lifecycle("JSON format configured for generateMultipazStrings task in ${target.name}")
            } else {
                target.logger.lifecycle("XML format detected for ${target.name} - generateMultipazStrings will skip execution")
            }

            // Hook check task into build lifecycle if enabled
            if (ext.failOnMissing) {
                target.tasks.findByName("compileKotlin")?.dependsOn("lokalizeCheck")
                target.tasks.findByName("compileCommonMainKotlinMetadata")?.dependsOn("lokalizeCheck")
            }
        }
    }
}
