package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.CardNetwork
import com.example.data.model.CardType
import com.example.data.model.WalletCard
import com.example.ui.components.InteractiveCreditCardView
import com.example.ui.theme.WalletTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleCard = WalletCard(
      title = "Sapphire Preferred",
      cardType = CardType.CREDIT_CARD,
      cardholderName = "ALEX MORGAN",
      cardNumber = "4111222233334589",
      lastFourDigits = "4589",
      expiryMonth = "08",
      expiryYear = "28",
      cvv = "384",
      bankOrIssuer = "Chase Bank",
      cardNetwork = CardNetwork.VISA,
      themePresetId = "sapphire_blue"
    )

    composeTestRule.setContent {
      WalletTheme {
        InteractiveCreditCardView(
          card = sampleCard,
          isRevealed = true,
          modifier = Modifier.padding(16.dp)
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

