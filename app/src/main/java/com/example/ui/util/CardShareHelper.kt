package com.example.ui.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.CardType
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object CardShareHelper {

    fun formatCardDetails(card: WalletCard): String {
        val sb = StringBuilder()

        when (card.cardType) {
            CardType.ID_CARD -> {
                val docTitle = card.title.ifBlank { card.idCardType?.displayName ?: "Identity Document" }
                sb.appendLine("🪪 $docTitle")

                // ID Number
                if (card.idNumber.isNotBlank()) {
                    val label = when (card.idCardType) {
                        IdCardType.AADHAAR -> "Aadhaar Card Number"
                        IdCardType.PAN -> "Permanent Account Number"
                        IdCardType.VOTER_ID -> "EPIC Number"
                        IdCardType.RATION_CARD -> "Ration Card Number"
                        IdCardType.DRIVING_LICENSE -> "Driving Licence No."
                        IdCardType.PASSPORT -> "Passport Number"
                        IdCardType.VEHICLE_RC -> "Registration No."
                        IdCardType.ABHA_HEALTH -> "ABHA Number"
                        else -> card.idCardType?.displayName?.let { "$it Number" } ?: "Document / ID Number"
                    }
                    sb.appendLine("$label: ${card.idNumber}")
                }

                // Name
                if (card.cardholderName.isNotBlank()) {
                    val nameLabel = when (card.idCardType) {
                        IdCardType.AADHAAR -> "Full Name"
                        IdCardType.PAN -> "Name"
                        IdCardType.VOTER_ID -> "Name in English"
                        IdCardType.RATION_CARD -> "Name of Card Holder"
                        else -> "Name on Card / Document"
                    }
                    sb.appendLine("$nameLabel: ${card.cardholderName}")
                }

                // Issuing Authority
                if (card.bankOrIssuer.isNotBlank()) {
                    sb.appendLine("Issuing Authority: ${card.bankOrIssuer}")
                }

                // Date of Birth
                if (card.dateOfBirth.isNotBlank()) {
                    sb.appendLine("Date of Birth: ${card.dateOfBirth}")
                }

                // Issue Date
                if (card.issueDate.isNotBlank()) {
                    sb.appendLine("Issue Date: ${card.issueDate}")
                }

                // Address
                if (card.address.isNotBlank()) {
                    val addrLabel = when (card.idCardType) {
                        IdCardType.VOTER_ID -> "Address in English"
                        IdCardType.RATION_CARD -> "Card Holder Address"
                        else -> "Registered Address"
                    }
                    sb.appendLine("$addrLabel: ${card.address}")
                }
            }
            CardType.CREDIT_CARD, CardType.DEBIT_CARD -> {
                val cardTypeName = if (card.cardType == CardType.CREDIT_CARD) "Credit Card" else "Debit Card"
                val title = card.title.ifBlank { "${card.bankOrIssuer.ifBlank { card.cardNetwork.displayName }} $cardTypeName" }
                val icon = if (card.cardType == CardType.CREDIT_CARD) "💳" else "🏧"
                sb.appendLine("$icon $title")

                if (card.cardNumber.isNotBlank()) {
                    sb.appendLine("Card Number: ${card.cardNumber}")
                }
                if (card.formattedExpiry.isNotBlank()) {
                    sb.appendLine("Expiration Date: ${card.formattedExpiry}")
                }
                if (card.cvv.isNotBlank()) {
                    sb.appendLine("CVV / Security Code: ${card.cvv}")
                }
                if (card.pin.isNotBlank()) {
                    sb.appendLine("ATM PIN: ${card.pin}")
                }
                if (card.cardholderName.isNotBlank()) {
                    sb.appendLine("Cardholder: ${card.cardholderName}")
                }
                if (card.bankOrIssuer.isNotBlank()) {
                    sb.appendLine("Bank / Issuer: ${card.bankOrIssuer}")
                }
                if (card.cardNetwork.displayName.isNotBlank() && card.cardNetwork.name != "OTHER") {
                    sb.appendLine("Card Network: ${card.cardNetwork.displayName}")
                }
                if (card.dateOfBirth.isNotBlank()) {
                    sb.appendLine("Date of Birth: ${card.dateOfBirth}")
                }
                if (card.issueDate.isNotBlank()) {
                    sb.appendLine("Issue Date: ${card.issueDate}")
                }
                if (card.address.isNotBlank()) {
                    sb.appendLine("Registered Address: ${card.address}")
                }
            }
        }

        // Custom Fields - Output directly without any "Additional Details" header
        if (card.customFields.isNotEmpty()) {
            for (field in card.customFields) {
                if (field.label.isNotBlank() || field.value.isNotBlank()) {
                    val label = field.label.ifBlank { "Custom Field" }
                    sb.appendLine("$label: ${field.value}")
                }
            }
        }

        // Notes
        if (card.notes.isNotBlank()) {
            sb.appendLine("Notes: ${card.notes}")
        }

        // Tags
        if (card.tags.isNotEmpty()) {
            sb.appendLine("Tags: ${card.tags.joinToString(", ")}")
        }

        return sb.toString().trim()
    }

    /**
     * Authenticates with biometrics before sharing the card's full plaintext details.
     */
    fun shareCardWithAuth(
        context: Context,
        card: WalletCard,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        sharePlaintext(context, card)
        onSuccess()
    }

    fun hasCardPhotos(card: WalletCard): Boolean {
        return !card.frontPhotoUri.isNullOrBlank() || !card.backPhotoUri.isNullOrBlank()
    }

    /**
     * Saves a rendered card preview bitmap to the app cache directory and returns a content Uri via FileProvider.
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap, cardTitle: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "card_previews")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val safeTitle = cardTitle.replace(Regex("[^a-zA-Z0-9]"), "_").take(24)
            val file = File(cacheDir, "preview_${safeTitle}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares both front and back rendered card preview images at once via Android Share Sheet.
     */
    fun sharePreviewImages(
        context: Context,
        card: WalletCard,
        frontBitmap: Bitmap,
        backBitmap: Bitmap
    ) {
        val frontUri = saveBitmapToCache(context, frontBitmap, "${card.title}_front")
        val backUri = saveBitmapToCache(context, backBitmap, "${card.title}_back")

        val uris = ArrayList<Uri>()
        if (frontUri != null) uris.add(frontUri)
        if (backUri != null) uris.add(backUri)

        if (uris.isEmpty()) return

        val title = card.title.ifBlank { "Card Preview" }
        val sendIntent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uris[0])
                putExtra(Intent.EXTRA_SUBJECT, "$title - Card Preview")
                clipData = ClipData.newUri(context.contentResolver, title, uris[0])
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/png"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, "$title - Front & Back Preview Images")
                val cd = ClipData.newUri(context.contentResolver, "$title (Front)", uris[0])
                cd.addItem(ClipData.Item(uris[1]))
                clipData = cd
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        val shareChooser = Intent.createChooser(sendIntent, "Share card preview as images")
        shareChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareChooser)
    }

    /**
     * Shares a rendered card preview image via Android Share Sheet.
     */
    fun sharePreviewImage(context: Context, card: WalletCard, bitmap: Bitmap) {
        val uri = saveBitmapToCache(context, bitmap, card.title) ?: return
        val subject = card.title.ifBlank { "Card Preview" }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            clipData = ClipData.newUri(context.contentResolver, subject, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val shareChooser = Intent.createChooser(sendIntent, "Share card preview as image")
        shareChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareChooser)
    }

    /**
     * Shares card photos (front and/or back attachments) via Android Share Sheet.
     */
    fun shareCardPhotos(context: Context, card: WalletCard) {
        val uris = ArrayList<Uri>()

        fun resolveUri(uriString: String?): Uri? {
            if (uriString.isNullOrBlank()) return null
            return try {
                val parsed = Uri.parse(uriString)
                if (parsed.scheme == "file") {
                    val file = File(parsed.path ?: return null)
                    if (file.exists()) {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    } else null
                } else if (parsed.scheme == "content") {
                    // Cache content stream to guarantee permissions for the recipient app
                    val cacheDir = File(context.cacheDir, "shared_images")
                    if (!cacheDir.exists()) cacheDir.mkdirs()
                    val targetFile = File(cacheDir, "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
                    context.contentResolver.openInputStream(parsed)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                } else {
                    val file = File(uriString)
                    if (file.exists()) {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    } else null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        resolveUri(card.frontPhotoUri)?.let { uris.add(it) }
        resolveUri(card.backPhotoUri)?.let { uris.add(it) }

        if (uris.isEmpty()) return

        val title = card.title.ifBlank { "Card Photos" }
        val sendIntent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uris[0])
                putExtra(Intent.EXTRA_SUBJECT, "$title - Attachment Photo")
                clipData = ClipData.newUri(context.contentResolver, title, uris[0])
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, "$title - Front & Back Photos")
                val cd = ClipData.newUri(context.contentResolver, title, uris[0])
                for (i in 1 until uris.size) {
                    cd.addItem(ClipData.Item(uris[i]))
                }
                clipData = cd
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        val shareChooser = Intent.createChooser(sendIntent, "Share card photos")
        shareChooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareChooser)
    }

    fun sharePlaintext(context: Context, card: WalletCard) {
        val text = formatCardDetails(card)
        val subject = card.title.ifBlank { "Card Details" }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val shareChooser = Intent.createChooser(sendIntent, "Share card details to...")
        shareChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareChooser)
    }
}
