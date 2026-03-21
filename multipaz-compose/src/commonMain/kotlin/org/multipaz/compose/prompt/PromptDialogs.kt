package org.multipaz.compose.prompt

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import coil3.ImageLoader
import org.multipaz.prompt.ConvertToHumanReadableFn
import org.multipaz.prompt.PromptDialogModel
import org.multipaz.prompt.PromptModel

/**
 * Composable which can show prompts from [PromptModel].
 *
 * If an application wishes to implement a prompts itself but still wants other models,
 * it can pass this the [PromptDialogModel.DialogType] in [excludeTypes].
 *
 * @param promptModel the [PromptModel] to show prompts from.
 * @param imageLoader an [ImageLoader] to load images from the network or `null`.
 * @param maxHeight the maximum height of a dialog or `null` if not restricted.
 * @param excludeTypes a list of prompt model dialog types to not show.
 * @param toHumanReadable a function to convert a [org.multipaz.prompt.Reason] to a
 *   [org.multipaz.prompt.Reason.HumanReadable] for display in prompts. Defaults to
 *   [defaultToHumanReadable] which uses translated strings from multipaz-compose.
 */
@Composable
expect fun PromptDialogs(
    promptModel: PromptModel,
    imageLoader: ImageLoader? = null,
    maxHeight: Dp? = null,
    excludeTypes: List<PromptDialogModel.DialogType<*>> = emptyList(),
    toHumanReadable: ConvertToHumanReadableFn = ::defaultToHumanReadable
)
