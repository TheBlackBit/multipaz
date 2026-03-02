package org.multipaz.lokalize.llm.strategy

import org.multipaz.lokalize.util.LLMProvider

/**
 * Factory for creating ProviderStrategy instances based on the LLM provider.
 * Uses the Factory Method pattern to decouple strategy creation from usage.
 */
object ProviderStrategyFactory {

    /**
     * Creates the appropriate ProviderStrategy for the given provider.
     *
     * @param provider The LLM provider enum
     * @return The corresponding ProviderStrategy implementation
     * @throws IllegalArgumentException if provider is unknown
     */
    fun create(provider: LLMProvider): ProviderStrategy = when (provider) {
        LLMProvider.OPENAI -> OpenAiStrategy()
        LLMProvider.GOOGLE -> GoogleStrategy()
        LLMProvider.ANTHROPIC -> AnthropicStrategy()
    }
}
