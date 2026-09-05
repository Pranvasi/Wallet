package com.example

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.CardType
import com.example.data.model.IdCardType
import com.example.data.security.BiometricAuthHelper
import com.example.data.security.SecurityPreferences
import com.example.data.security.SecuritySettings
import com.example.ui.components.AppLockScreen
import com.example.ui.screens.detail.CardDetailScreen
import com.example.ui.screens.editor.CardEditorScreen
import com.example.ui.screens.editor.CardEditorViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.welcome.WelcomeSetupScreen
import com.example.ui.theme.WalletTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var securityPreferences: SecurityPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        securityPreferences = SecurityPreferences(applicationContext)

        setContent {
            val settingsState by securityPreferences.settingsFlow.collectAsStateWithLifecycle(
                initialValue = null
            )

            val settings = settingsState

            var isLocked by rememberSaveable { mutableStateOf<Boolean?>(null) }
            val context = LocalContext.current

            // Initialize lock state once security settings are loaded
            LaunchedEffect(settings) {
                if (settings != null && isLocked == null) {
                    if (settings.isSetupCompleted) {
                        isLocked = settings.isAppLockEnabled || settings.isBiometricEnabled
                    } else {
                        isLocked = false
                    }
                }
            }

            // Dynamic FLAG_SECURE handling
            LaunchedEffect(settings?.isSecureScreenEnabled) {
                if (settings?.isSecureScreenEnabled == true) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            val coroutineScope = rememberCoroutineScope()

            // Lock only upon turn off screen or screen lock (and complete exit / launch).
            // Normal app switching does not trigger the lock screen.
            DisposableEffect(lifecycleOwner, settings, context) {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context?, intent: Intent?) {
                        val action = intent?.action
                        if (action == Intent.ACTION_SCREEN_OFF || action == Intent.ACTION_USER_PRESENT) {
                            if (settings != null && settings.isSetupCompleted && (settings.isAppLockEnabled || settings.isBiometricEnabled)) {
                                val keyguardManager = ctx?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                                if (action == Intent.ACTION_SCREEN_OFF || keyguardManager?.isKeyguardLocked == true) {
                                    isLocked = true
                                }
                            }
                        }
                    }
                }

                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_USER_PRESENT)
                }
                context.registerReceiver(receiver, filter)

                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        if (settings != null && settings.isSetupCompleted && (settings.isAppLockEnabled || settings.isBiometricEnabled)) {
                            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                            if (keyguardManager?.isKeyguardLocked == true) {
                                isLocked = true
                            }
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    try {
                        context.unregisterReceiver(receiver)
                    } catch (_: Exception) {}
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            fun triggerBiometricUnlock() {
                if (settings != null && (settings.isBiometricEnabled || settings.isAppLockEnabled)) {
                    BiometricAuthHelper.showBiometricPrompt(
                        activity = this@MainActivity,
                        title = "Unlock Wallet",
                        subtitle = "Authenticate to access your stored cards",
                        onSuccess = { isLocked = false },
                        onError = { /* Biometric prompt dismissed or failed */ }
                    )
                }
            }

            // Auto prompt biometrics on app launch or when locked if enabled
            LaunchedEffect(isLocked, settings?.isSetupCompleted, settings?.isBiometricEnabled) {
                if (settings != null && settings.isSetupCompleted && isLocked == true && (settings.isBiometricEnabled || settings.isAppLockEnabled)) {
                    kotlinx.coroutines.delay(250)
                    triggerBiometricUnlock()
                }
            }

            val activeThemeMode = settings?.themeMode ?: "system"

            WalletTheme(
                themeMode = activeThemeMode
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (settings == null) {
                        // Loading preferences on initial startup
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (!settings.isSetupCompleted) {
                        // First-time Welcome & Security Setup Screen
                        WelcomeSetupScreen(
                            securityPreferences = securityPreferences,
                            onSetupFinished = {
                                isLocked = false
                            }
                        )
                    } else {
                        val currentlyLocked = isLocked ?: (settings.isAppLockEnabled || settings.isBiometricEnabled)
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Keep navigation mounted in hierarchy so state (such as editing a card or changing settings) is preserved
                            WalletAppNavigation(
                                onLockApp = { isLocked = true }
                            )

                            // Overlay Lock Screen when locked
                            AnimatedVisibility(
                                visible = currentlyLocked,
                                enter = fadeIn(animationSpec = tween(350, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))) +
                                        scaleIn(initialScale = 0.96f, animationSpec = tween(350, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))),
                                exit = fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                        scaleOut(targetScale = 1.04f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                            ) {
                                AppLockScreen(
                                    title = "Wallet Locked",
                                    subtitle = "Authenticate with biometrics or device credentials to unlock",
                                    onUnlockClick = { triggerBiometricUnlock() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletAppNavigation(
    onLockApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Material 3 Emphasized Decelerate & Standard Easings for ultra-smooth motion
    val smoothEntranceEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) // Emphasized decelerate
    val smoothExitEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)     // Emphasized accelerate
    val standardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)        // Fluid standard

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable(
            route = "home",
            exitTransition = {
                if (targetState.destination.route?.startsWith("add_card") == true) {
                    fadeOut(animationSpec = tween(400, easing = standardEasing)) + 
                    scaleOut(targetScale = 0.92f, animationSpec = tween(550, easing = smoothEntranceEasing))
                } else {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(450, easing = standardEasing)
                    ) + fadeOut(animationSpec = tween(350))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route?.startsWith("add_card") == true) {
                    fadeIn(animationSpec = tween(450, easing = standardEasing)) + 
                    scaleIn(initialScale = 0.92f, animationSpec = tween(500, easing = smoothEntranceEasing))
                } else {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(450, easing = standardEasing)
                    ) + fadeIn(animationSpec = tween(400))
                }
            }
        ) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToAddCard = { type, idType ->
                    val idParam = idType?.name ?: ""
                    navController.navigate("add_card?type=${type.name}&idCardType=$idParam")
                },
                onNavigateToCardDetail = { cardId ->
                    navController.navigate("card_detail/$cardId")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onLockApp = onLockApp
            )
        }

        composable(
            route = "add_card?type={type}&idCardType={idCardType}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = CardType.CREDIT_CARD.name
                },
                navArgument("idCardType") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = 550, easing = smoothEntranceEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 400, easing = standardEasing))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(durationMillis = 450, easing = smoothExitEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 350))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(durationMillis = 480, easing = smoothExitEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 380))
            }
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: CardType.CREDIT_CARD.name
            val idTypeStr = backStackEntry.arguments?.getString("idCardType") ?: ""

            val cardType = try { CardType.valueOf(typeStr) } catch (_: Exception) { CardType.CREDIT_CARD }
            val idCardType = if (idTypeStr.isNotBlank()) {
                try { IdCardType.valueOf(idTypeStr) } catch (_: Exception) { null }
            } else null

            val editorViewModel: CardEditorViewModel = viewModel()

            CardEditorScreen(
                viewModel = editorViewModel,
                cardId = 0L,
                initialType = cardType,
                initialIdType = idCardType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_card/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = 550, easing = smoothEntranceEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 400, easing = standardEasing))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(durationMillis = 450, easing = smoothExitEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 350))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(durationMillis = 480, easing = smoothExitEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 380))
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            val editorViewModel: CardEditorViewModel = viewModel()

            CardEditorScreen(
                viewModel = editorViewModel,
                cardId = cardId,
                initialType = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "card_detail/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(450, easing = standardEasing)
                ) + fadeIn(animationSpec = tween(380))
            },
            exitTransition = {
                if (targetState.destination.route?.startsWith("edit_card") == true) {
                    fadeOut(animationSpec = tween(350, easing = standardEasing)) + 
                    scaleOut(targetScale = 0.92f, animationSpec = tween(500, easing = smoothEntranceEasing))
                } else {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(450, easing = standardEasing)
                    ) + fadeOut(animationSpec = tween(350))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route?.startsWith("edit_card") == true) {
                    fadeIn(animationSpec = tween(400, easing = standardEasing)) + 
                    scaleIn(initialScale = 0.92f, animationSpec = tween(480, easing = smoothEntranceEasing))
                } else {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(450, easing = standardEasing)
                    ) + fadeIn(animationSpec = tween(400))
                }
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(450, easing = standardEasing)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            CardDetailScreen(
                cardId = cardId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("edit_card/$id")
                }
            )
        }

        composable(
            route = "settings",
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(450, easing = standardEasing)
                ) + fadeIn(animationSpec = tween(380))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(450, easing = standardEasing)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
