package org.multipaz.lokalize.llm.strategy

import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import org.multipaz.lokalize.util.LLmModel

/**
 * Strategy interface for LLM provider-specific operations.
 * Each provider (OpenAI, Google, Anthropic) implements this to encapsulate
 * its model resolution and executor creation logic.
 */
interface ProviderStrategy {
    /**
     * Resolves the internal LLmModel to the provider-specific Koog LLModel.
     */
    fun resolveKoogModel(model: LLmModel): LLModel

    /**
     * Creates the appropriate SingleLLMPromptExecutor for this provider.
     */
    fun createExecutor(apiKey: String): SingleLLMPromptExecutor

    /**
     * Returns the provider name for logging/debugging.
     */
    val providerName: String
}
