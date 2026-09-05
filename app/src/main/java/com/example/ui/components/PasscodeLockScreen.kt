package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PasscodeLockScreen(
    title: String = "Wallet Locked",
    subtitle: String = "Authenticate with biometrics or device credentials to unlock",
    isBiometricAvailable: Boolean = true,
    hasCustomPasscode: Boolean = false,
    expectedLength: Int = 4,
    onPasscodeEntered: suspend (String) -> Boolean = { true },
    onBiometricClick: () -> Unit = {},
    onResetPasscode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AppLockScreen(
        title = title,
        subtitle = subtitle,
        onUnlockClick = onBiometricClick,
        modifier = modifier
    )
}
