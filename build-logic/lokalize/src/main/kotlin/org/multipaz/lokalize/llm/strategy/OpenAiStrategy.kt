package org.multipaz.lokalize.llm.strategy

import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.llm.LLModel
import org.multipaz.lokalize.util.LLmModel

/**
 * OpenAI provider strategy for model resolution and executor creation.
 */
class OpenAiStrategy : ProviderStrategy {

    override val providerName: String = "OpenAI"

    override fun resolveKoogModel(model: LLmModel): LLModel = when (model) {
        LLmModel.GPT4O -> OpenAIModels.Chat.GPT4o
        LLmModel.GPT4O_MINI -> OpenAIModels.CostOptimized.GPT4oMini
        LLmModel.GPT4_1 -> OpenAIModels.Chat.GPT4_1
        LLmModel.GPT5 -> OpenAIModels.Chat.GPT5
        LLmModel.GPT5_MINI -> OpenAIModels.Chat.GPT5Mini
        LLmModel.GPT5_NANO -> OpenAIModels.Chat.GPT5Nano
        LLmModel.GPT4_1_NANO -> OpenAIModels.Chat.GPT4_1
        LLmModel.GPT4_1_MINI -> OpenAIModels.Chat.GPT4_1
        LLmModel.GPT5_PRO -> OpenAIModels.Chat.GPT5
        LLmModel.GPT5_1 -> OpenAIModels.Chat.GPT5
        LLmModel.GPT5_2 -> OpenAIModels.Chat.GPT5
        LLmModel.O3_MINI -> OpenAIModels.Reasoning.O1
        LLmModel.O4_MINI -> OpenAIModels.Reasoning.O1
        else -> OpenAIModels.Chat.GPT4o
    }

    override fun createExecutor(apiKey: String) = simpleOpenAIExecutor(apiKey)
}
