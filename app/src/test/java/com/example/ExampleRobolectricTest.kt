package com.example

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CardNetwork
import com.example.data.model.CardType
import com.example.data.model.CustomField
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import com.example.data.security.BackupCrypto
import com.example.ui.util.CardNumberVisualTransformation
import com.example.ui.util.CardValidationUtils
import com.example.ui.util.DateVisualTransformation
import com.example.ui.util.ExpiryDateVisualTransformation
import android.graphics.Bitmap
import com.example.ui.util.CardShareHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Wallet", appName)
  }

  @Test
  fun `detect card network from card number`() {
    val visa = CardNetwork.detectNetwork("4111222233334444")
    assertEquals(CardNetwork.VISA, visa)

    val mastercard = CardNetwork.detectNetwork("5112222233334444")
    assertEquals(CardNetwork.MASTERCARD, mastercard)

    val amex = CardNetwork.detectNetwork("378282246310005")
    assertEquals(CardNetwork.AMEX, amex)
  }

  @Test
  fun `card number visual transformation formats accurately`() {
    val standardTransform = CardNumberVisualTransformation(isAmex = false)
    val res16 = standardTransform.filter(AnnotatedString("4111222233334444"))
    assertEquals("4111 2222 3333 4444", res16.text.text)

    val amexTransform = CardNumberVisualTransformation(isAmex = true)
    val resAmex = amexTransform.filter(AnnotatedString("378282246310005"))
    assertEquals("3782 822463 10005", resAmex.text.text)
  }

  @Test
  fun `expiry date visual transformation formats accurately`() {
    val expiryTransform = ExpiryDateVisualTransformation()
    val res = expiryTransform.filter(AnnotatedString("1228"))
    assertEquals("12/28", res.text.text)

    val resPartial = expiryTransform.filter(AnnotatedString("05"))
    assertEquals("05", resPartial.text.text)
  }

  @Test
  fun `date visual transformation formats accurately and preserves monotonic offset mapping`() {
    val dateTransform = DateVisualTransformation()
    
    // 8 digits full date
    val resFull = dateTransform.filter(AnnotatedString("15081995"))
    assertEquals("15/08/1995", resFull.text.text)
    
    // Verify offset mapping monotonicity and round trips
    for (i in 0..8) {
      val transformedOffset = resFull.offsetMapping.originalToTransformed(i)
      val originalOffset = resFull.offsetMapping.transformedToOriginal(transformedOffset)
      assertEquals("Original offset $i should round trip", i, originalOffset)
    }

    // Partial 2 digits
    val resDay = dateTransform.filter(AnnotatedString("15"))
    assertEquals("15/", resDay.text.text)
    assertEquals(3, resDay.offsetMapping.originalToTransformed(2))

    // Partial 4 digits
    val resMonth = dateTransform.filter(AnnotatedString("1508"))
    assertEquals("15/08/", resMonth.text.text)
    assertEquals(6, resMonth.offsetMapping.originalToTransformed(4))
  }

  @Test
  fun `card validation utils luhn and expiry verification`() {
    assertTrue(CardValidationUtils.isValidLuhn("4532015112830366"))
    assertFalse(CardValidationUtils.isValidLuhn("4532015112830367"))

    assertTrue(CardValidationUtils.isExpiryValid("12", "29"))
    assertFalse(CardValidationUtils.isExpiryValid("13", "29"))
    assertFalse(CardValidationUtils.isExpiryValid("00", "29"))
    assertFalse(CardValidationUtils.isExpiryValid("01", "20"))
  }

  @Test
  fun `backup encryption and decryption with Indian IDs and custom fields`() {
    val cards = listOf(
      WalletCard(
        title = "My Aadhaar Card",
        cardType = CardType.ID_CARD,
        idCardType = IdCardType.AADHAAR,
        cardholderName = "Rajesh Sharma",
        idNumber = "9876 5432 1098",
        bankOrIssuer = "UIDAI",
        customFields = listOf(
          CustomField(label = "VID", value = "9191 8282 7373 6464"),
          CustomField(label = "Father's Name", value = "Mohan Sharma")
        )
      )
    )

    val password = "SuperSecretPassword123!"
    val backupJson = BackupCrypto.createEncryptedBackup(cards, password)
    assertNotNull(backupJson)
    assertTrue(backupJson.contains("data"))

    val decryptedCards = BackupCrypto.decryptBackup(backupJson, password)
    assertEquals(1, decryptedCards.size)
    assertEquals("My Aadhaar Card", decryptedCards[0].title)
    assertEquals(IdCardType.AADHAAR, decryptedCards[0].idCardType)
    assertEquals("9876 5432 1098", decryptedCards[0].idNumber)
    assertEquals(2, decryptedCards[0].customFields.size)
    assertEquals("VID", decryptedCards[0].customFields[0].label)
    assertEquals("9191 8282 7373 6464", decryptedCards[0].customFields[0].value)
  }

  @Test
  fun `security preferences set and verify passcode`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val securityPrefs = com.example.data.security.SecurityPreferences(context)
    securityPrefs.setPasscode("1357")
    assertTrue(securityPrefs.verifyPasscode("1357"))
    assertFalse(securityPrefs.verifyPasscode("0000"))
    assertFalse(securityPrefs.verifyPasscode("1358"))
  }

  @Test
  fun `hasCardPhotos returns false when no attachment photos present`() {
    val cardNoPhotos = WalletCard(
      title = "Test Card",
      frontPhotoUri = null,
      backPhotoUri = ""
    )
    assertFalse(CardShareHelper.hasCardPhotos(cardNoPhotos))
  }

  @Test
  fun `hasCardPhotos returns true when front or back photo present`() {
    val cardWithFront = WalletCard(
      title = "Card with Front",
      frontPhotoUri = "content://media/external/images/1",
      backPhotoUri = null
    )
    assertTrue(CardShareHelper.hasCardPhotos(cardWithFront))

    val cardWithBack = WalletCard(
      title = "Card with Back",
      frontPhotoUri = null,
      backPhotoUri = "file:///storage/emulated/0/back.jpg"
    )
    assertTrue(CardShareHelper.hasCardPhotos(cardWithBack))

    val cardWithBoth = WalletCard(
      title = "Card with Both",
      frontPhotoUri = "file:///storage/emulated/0/front.jpg",
      backPhotoUri = "file:///storage/emulated/0/back.jpg"
    )
    assertTrue(CardShareHelper.hasCardPhotos(cardWithBoth))
  }

  @Test
  fun `formatCardDetails generates plain text representation correctly`() {
    val idCard = WalletCard(
      title = "Aadhaar Card",
      cardType = CardType.ID_CARD,
      idCardType = IdCardType.AADHAAR,
      cardholderName = "Amit Kumar",
      idNumber = "9988 7766 5544",
      bankOrIssuer = "UIDAI",
      notes = "Verified document"
    )
    val text = CardShareHelper.formatCardDetails(idCard)
    assertTrue(text.contains("Aadhaar Card"))
    assertTrue(text.contains("Amit Kumar"))
    assertTrue(text.contains("9988 7766 5544"))
    assertTrue(text.contains("UIDAI"))
    assertTrue(text.contains("Verified document"))
  }

  @Test
  fun `saveBitmapToCache saves card preview bitmap and returns file provider uri`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val bitmap = Bitmap.createBitmap(200, 120, Bitmap.Config.ARGB_8888)
    val uri = CardShareHelper.saveBitmapToCache(context, bitmap, "Test Card")
    assertNotNull(uri)
    assertEquals("content", uri?.scheme)
    assertTrue(uri?.authority?.endsWith(".fileprovider") == true)
  }

  @Test
  fun `backup and restore with photos and walletbackup file format`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Create dummy photo files in context.filesDir
    val photosDir = java.io.File(context.filesDir, "card_photos")
    photosDir.mkdirs()
    val frontFile = java.io.File(photosDir, "test_front.jpg")
    frontFile.writeBytes("dummy_front_image_content_bytes_123".toByteArray())
    val backFile = java.io.File(photosDir, "test_back.jpg")
    backFile.writeBytes("dummy_back_image_content_bytes_456".toByteArray())

    val cardWithPhotos = WalletCard(
      title = "Passport Card",
      cardType = CardType.ID_CARD,
      idCardType = IdCardType.PASSPORT,
      cardholderName = "Ananya Roy",
      idNumber = "Z9876543",
      frontPhotoUri = android.net.Uri.fromFile(frontFile).toString(),
      backPhotoUri = android.net.Uri.fromFile(backFile).toString()
    )

    val password = "SecureWalletPassword99!"

    // Test creating .walletbackup file
    val backupFile = BackupCrypto.createCacheBackupFile(context, listOf(cardWithPhotos), password)
    assertNotNull(backupFile)
    assertTrue(backupFile.exists())
    assertTrue(backupFile.name.endsWith(".walletbackup"))

    // Test reading and decrypting from stream
    val decryptedCards = BackupCrypto.readBackupFromStream(context, backupFile.inputStream(), password)
    assertEquals(1, decryptedCards.size)
    val restored = decryptedCards[0]
    assertEquals("Passport Card", restored.title)
    assertEquals("Ananya Roy", restored.cardholderName)
    assertEquals("Z9876543", restored.idNumber)

    // Verify restored photos exist on disk
    assertNotNull(restored.frontPhotoUri)
    assertNotNull(restored.backPhotoUri)
    val restoredFrontFile = java.io.File(android.net.Uri.parse(restored.frontPhotoUri!!).path!!)
    assertTrue(restoredFrontFile.exists())
    assertEquals("dummy_front_image_content_bytes_123", String(restoredFrontFile.readBytes()))

    val restoredBackFile = java.io.File(android.net.Uri.parse(restored.backPhotoUri!!).path!!)
    assertTrue(restoredBackFile.exists())
    assertEquals("dummy_back_image_content_bytes_456", String(restoredBackFile.readBytes()))
  }
}

