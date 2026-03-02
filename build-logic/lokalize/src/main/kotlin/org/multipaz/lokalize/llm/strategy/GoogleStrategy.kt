package org.multipaz.lokalize.llm.strategy

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.llm.LLModel
import org.multipaz.lokalize.util.LLmModel

/**
 * Google (Gemini) provider strategy for model resolution and executor creation.
 */
class GoogleStrategy : ProviderStrategy {

    override val providerName: String = "Google"

    override fun resolveKoogModel(model: LLmModel): LLModel = when (model) {
        LLmModel.GEMINI2_0_FLASH -> GoogleModels.Gemini2_0Flash
        LLmModel.GEMINI2_0_FLASH_LITE -> GoogleModels.Gemini2_0FlashLite
        LLmModel.GEMINI2_5_PRO -> GoogleModels.Gemini2_5Pro
        LLmModel.GEMINI2_5_FLASH -> GoogleModels.Gemini2_5Flash
        LLmModel.GEMINI2_5_FLASH_LITE -> GoogleModels.Gemini2_5FlashLite
        LLmModel.GEMINI3_PRO_PREVIEW -> GoogleModels.Gemini2_5Pro
        LLmModel.GEMINI15FLASH -> GoogleModels.Gemini2_0Flash
        else -> GoogleModels.Gemini2_0Flash
    }

    override fun createExecutor(apiKey: String) = simpleGoogleAIExecutor(apiKey)
}
