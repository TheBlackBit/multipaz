package org.multipaz.lokalize.llm.strategy

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.llm.LLModel
import org.multipaz.lokalize.util.LLmModel

/**
 * Anthropic (Claude) provider strategy for model resolution and executor creation.
 */
class AnthropicStrategy : ProviderStrategy {

    override val providerName: String = "Anthropic"

    override fun resolveKoogModel(model: LLmModel): LLModel = when (model) {
        LLmModel.CLAUDE_OPUS_3 -> AnthropicModels.Opus_3
        LLmModel.CLAUDE_HAIKU_3 -> AnthropicModels.Haiku_3
        LLmModel.CLAUDE_SONNET_3_5 -> AnthropicModels.Sonnet_3_5
        LLmModel.CLAUDE_HAIKU_3_5 -> AnthropicModels.Haiku_3_5
        LLmModel.CLAUDE_SONNET_3_7 -> AnthropicModels.Sonnet_3_7
        LLmModel.CLAUDE_SONNET_4 -> AnthropicModels.Sonnet_4
        LLmModel.CLAUDE_OPUS_4 -> AnthropicModels.Opus_4
        LLmModel.CLAUDE_OPUS_4_1 -> AnthropicModels.Opus_4
        LLmModel.CLAUDE_OPUS_4_5 -> AnthropicModels.Opus_4
        LLmModel.CLAUDE_SONNET_4_5 -> AnthropicModels.Sonnet_4
        LLmModel.CLAUDE_HAIKU_4_5 -> AnthropicModels.Haiku_3_5
        else -> AnthropicModels.Sonnet_3_5
    }

    override fun createExecutor(apiKey: String) = simpleAnthropicExecutor(apiKey)
}
