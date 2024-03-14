package cd.ghost.safebox.data

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor


typealias TaskLauncher = (BiometricStatus) -> Unit

sealed class BiometricStatus {
    object Success : BiometricStatus()
    class Error(val message: String) : BiometricStatus()
    object Fail : BiometricStatus()
}

object BiometricUtils {

    private lateinit var executor: Executor
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var biometricPrompt: BiometricPrompt? = null
    private var taskLauncher: TaskLauncher? = null

    fun isBiometricAllowed(fragmentActivity: FragmentActivity) {
        if (!isBiometricEnabled(fragmentActivity)) return

        executor = ContextCompat.getMainExecutor(fragmentActivity)
        biometricPrompt = BiometricPrompt(fragmentActivity, executor, authCallback)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Unlock using your biometric credential")
            .setNegativeButtonText("Password")
            .setConfirmationRequired(false)
            .build()
    }

    private fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    }

    private fun isDeviceSecured(context: Context): Boolean {
        val keyguardManager =
            context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardSecure
    }

    private fun isBiometricEnabled(context: Context): Boolean {
        return isBiometricAvailable(context) && isDeviceSecured(context)
    }

    private val authCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(
            errorCode: Int, errString: CharSequence
        ) {
            super.onAuthenticationError(errorCode, errString)
            taskLauncher?.invoke(BiometricStatus.Error(errString.toString()))
        }

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(result)
            taskLauncher?.invoke(BiometricStatus.Success)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            taskLauncher?.invoke(BiometricStatus.Fail)
        }
    }

    fun launchBiometric(launcher: TaskLauncher) {
        biometricPrompt?.authenticate(promptInfo)
        taskLauncher = launcher
    }
}