package org.multipaz.lokalize

import org.gradle.api.Plugin
import org.gradle.api.Project
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
 * }
 * ```
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

        // Register check task - validates translations
        target.tasks.register("lokalizeCheck", LokalizeCheckTask::class.java) { task ->
            task.description = "Validates that all target locales have complete translations"
            task.group = "lokalize"

            // Wire extension values to task inputs lazily
            task.defaultLocale.convention(ext.defaultLocale)
            task.targetLocales.convention(ext.targetLocales)
            task.failOnMissing.convention(ext.failOnMissing)
            task.resourcesDir.convention(ext.resourcesDir)
            task.extension.convention(ext)

            // Compute base strings file path from resourcesDir
            task.baseStringsFile.convention(
                target.layout.projectDirectory.file(
                    ext.resourcesDir.map { "$it/values/strings.xml" }
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
            task.extension.convention(ext)

            // Output directory for temporary translation results (in build/, not source)
            task.outputDirectory.convention(
                target.layout.buildDirectory.dir("lokalize/translations")
            )
        }

        // Hook check task into build lifecycle if enabled
        target.afterEvaluate {
            if (ext.failOnMissing) {
                // Run lokalizeCheck before compilation tasks
                target.tasks.findByName("compileKotlin")?.dependsOn("lokalizeCheck")
                target.tasks.findByName("compileCommonMainKotlinMetadata")?.dependsOn("lokalizeCheck")
            }
        }
    }
}
