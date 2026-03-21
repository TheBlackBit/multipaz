package org.multipaz.prompt

import androidx.biometric.BiometricPrompt.CryptoObject
import org.multipaz.securearea.UserAuthenticationType

/**
 * Prompts user for authentication through biometrics.
 *
 * To dismiss the prompt programmatically, cancel the job the coroutine was launched in.
 *
 * @param cryptoObject optional [CryptoObject] to be associated with the authentication.
 * @param reason a [Reason] describing why authentication is needed.
 * @param userAuthenticationTypes the set of allowed user authentication types, must contain at least one element.
 * @param requireConfirmation set to `true` to require explicit user confirmation after presenting passive biometric.
 * @return `true` if authentication succeed, `false` if the user dismissed the prompt.
 */
suspend fun AndroidPromptModel.showBiometricPrompt(
    cryptoObject: CryptoObject?,
    reason: Reason,
    userAuthenticationTypes: Set<UserAuthenticationType>,
    requireConfirmation: Boolean
): Boolean {
    return getDialogModel(BiometricPromptDialogModel.DialogType).displayPrompt(
        BiometricPromptDialogModel.BiometricPromptState(
            cryptoObject,
            reason,
            userAuthenticationTypes,
            requireConfirmation
        )
    )
}
