package org.multipaz.prompt

import org.multipaz.securearea.PassphraseConstraints

/**
 * [PromptDialogModel] to display a passphrase dialog.
 *
 * See [PromptModel.requestPassphrase] that is a thin wrapper over this class functionality for
 * more info.
 */
class PassphrasePromptDialogModel():
        PromptDialogModel<PassphrasePromptDialogModel.PassphraseRequest, String>() {
    override val dialogType: PromptDialogModel.DialogType<PassphrasePromptDialogModel>
        get() = DialogType

    object DialogType : PromptDialogModel.DialogType<PassphrasePromptDialogModel>

    /**
     * Data for the UI to display and run passphrase dialog.
     * @property reason the reason for the prompt, to be converted to human-readable `title` and
     *   `subtitle` text for UI.
     * @property passphraseConstraints the [PassphraseConstraints] for the passphrase.
     * @property passphraseEvaluator an optional function to evaluate the passphrase and give the user feedback.
     */
    class PassphraseRequest(
        val reason: Reason,
        val passphraseConstraints: PassphraseConstraints,
        val passphraseEvaluator: (suspend (enteredPassphrase: String) -> PassphraseEvaluation)?
    )
}


