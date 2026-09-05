package com.example.data.security

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.model.CardNetwork
import com.example.data.model.CardType
import com.example.data.model.CustomField
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupEnvelope(
    val version: Int = 2,
    val saltBase64: String,
    val ivBase64: String,
    val ciphertextBase64: String,
    val cardCount: Int,
    val photoCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("app", "Wallet")
        json.put("version", version)
        json.put("salt", saltBase64)
        json.put("iv", ivBase64)
        json.put("data", ciphertextBase64)
        json.put("count", cardCount)
        json.put("photoCount", photoCount)
        json.put("timestamp", timestamp)
        return json.toString(2)
    }

    companion object {
        fun fromJson(jsonStr: String): BackupEnvelope {
            val json = JSONObject(jsonStr)
            return BackupEnvelope(
                version = json.optInt("version", 1),
                saltBase64 = json.getString("salt"),
                ivBase64 = json.getString("iv"),
                ciphertextBase64 = json.getString("data"),
                cardCount = json.optInt("count", 0),
                photoCount = json.optInt("photoCount", 0),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

object BackupCrypto {
    private const val ITERATIONS = 35000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    const val BACKUP_FILE_EXTENSION = "walletbackup"

    fun createEncryptedBackup(cards: List<WalletCard>, password: String): String {
        return createEncryptedBackup(null, cards, password)
    }

    fun createEncryptedBackup(context: Context?, cards: List<WalletCard>, password: String): String {
        val jsonArray = JSONArray()
        var photoCount = 0

        for (card in cards) {
            val obj = JSONObject().apply {
                put("title", card.title)
                put("cardType", card.cardType.name)
                put("cardholderName", card.cardholderName)
                put("cardNumber", card.cardNumber)
                put("lastFourDigits", card.lastFourDigits)
                put("expiryMonth", card.expiryMonth)
                put("expiryYear", card.expiryYear)
                put("cvv", card.cvv)
                put("pin", card.pin)
                put("bankOrIssuer", card.bankOrIssuer)
                put("cardNetwork", card.cardNetwork.name)
                put("idCardType", card.idCardType?.name ?: "")
                put("idNumber", card.idNumber)
                put("issueDate", card.issueDate)
                put("address", card.address)
                put("dateOfBirth", card.dateOfBirth)
                put("themePresetId", card.themePresetId)
                put("notes", card.notes)
                put("tags", card.tags.joinToString(","))

                val cfArray = JSONArray()
                for (cf in card.customFields) {
                    val cfObj = JSONObject()
                    cfObj.put("label", cf.label)
                    cfObj.put("value", cf.value)
                    cfArray.put(cfObj)
                }
                put("customFields", cfArray)
                put("isFavorite", card.isFavorite)
                put("createdAt", card.createdAt)

                // Backup attached photos by encoding image bytes to Base64 in encrypted payload
                if (context != null) {
                    val frontBytesBase64 = readUriAsBase64(context, card.frontPhotoUri)
                    if (frontBytesBase64 != null) {
                        put("frontPhotoBase64", frontBytesBase64)
                        photoCount++
                    }

                    val backBytesBase64 = readUriAsBase64(context, card.backPhotoUri)
                    if (backBytesBase64 != null) {
                        put("backPhotoBase64", backBytesBase64)
                        photoCount++
                    }
                }
            }
            jsonArray.put(obj)
        }

        val plainJson = JSONObject().apply {
            put("cards", jsonArray)
            put("exportedAt", System.currentTimeMillis())
            put("photoCount", photoCount)
        }.toString()

        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)

        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val cipherBytes = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))

        val envelope = BackupEnvelope(
            version = 2,
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
            ciphertextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            cardCount = cards.size,
            photoCount = photoCount
        )

        return envelope.toJson()
    }

    fun decryptBackup(backupJson: String, password: String): List<WalletCard> {
        return decryptBackup(null, backupJson, password)
    }

    fun decryptBackup(context: Context?, backupJson: String, password: String): List<WalletCard> {
        val envelope = BackupEnvelope.fromJson(backupJson)
        val salt = Base64.decode(envelope.saltBase64, Base64.NO_WRAP)
        val iv = Base64.decode(envelope.ivBase64, Base64.NO_WRAP)
        val cipherBytes = Base64.decode(envelope.ciphertextBase64, Base64.NO_WRAP)

        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val plainBytes = cipher.doFinal(cipherBytes)
        val decryptedJson = String(plainBytes, Charsets.UTF_8)

        val rootObj = JSONObject(decryptedJson)
        val jsonArray = rootObj.getJSONArray("cards")
        val restoredCards = mutableListOf<WalletCard>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val cardType = try {
                CardType.valueOf(obj.optString("cardType", "CREDIT_CARD"))
            } catch (e: Exception) {
                CardType.CREDIT_CARD
            }
            val cardNetwork = try {
                CardNetwork.valueOf(obj.optString("cardNetwork", "OTHER"))
            } catch (e: Exception) {
                CardNetwork.OTHER
            }
            val idTypeStr = obj.optString("idCardType", "")
            val idCardType = if (idTypeStr.isNotBlank()) {
                try { IdCardType.valueOf(idTypeStr) } catch (e: Exception) { null }
            } else null

            val tagsStr = obj.optString("tags", "")
            val tags = if (tagsStr.isNotBlank()) tagsStr.split(",").map { it.trim() } else emptyList()

            val customFields = mutableListOf<CustomField>()
            val cfArray = obj.optJSONArray("customFields")
            if (cfArray != null) {
                for (j in 0 until cfArray.length()) {
                    val cfObj = cfArray.getJSONObject(j)
                    customFields.add(
                        CustomField(
                            label = cfObj.optString("label", ""),
                            value = cfObj.optString("value", "")
                        )
                    )
                }
            }

            // Restore photos if present in backup payload
            val frontPhotoBase64 = obj.optString("frontPhotoBase64", null)
            val backPhotoBase64 = obj.optString("backPhotoBase64", null)

            val restoredFrontUri: String? = if (!frontPhotoBase64.isNullOrBlank() && context != null) {
                saveBase64ToInternalStorage(context, frontPhotoBase64)
            } else {
                val existing = obj.optString("frontPhotoUri", "")
                if (existing.isNotBlank()) existing else null
            }

            val restoredBackUri: String? = if (!backPhotoBase64.isNullOrBlank() && context != null) {
                saveBase64ToInternalStorage(context, backPhotoBase64)
            } else {
                val existing = obj.optString("backPhotoUri", "")
                if (existing.isNotBlank()) existing else null
            }

            restoredCards.add(
                WalletCard(
                    id = 0,
                    title = obj.optString("title", "Imported Card"),
                    cardType = cardType,
                    cardholderName = obj.optString("cardholderName", ""),
                    cardNumber = obj.optString("cardNumber", ""),
                    lastFourDigits = obj.optString("lastFourDigits", ""),
                    expiryMonth = obj.optString("expiryMonth", ""),
                    expiryYear = obj.optString("expiryYear", ""),
                    cvv = obj.optString("cvv", ""),
                    pin = obj.optString("pin", ""),
                    bankOrIssuer = obj.optString("bankOrIssuer", ""),
                    cardNetwork = cardNetwork,
                    idCardType = idCardType,
                    idNumber = obj.optString("idNumber", ""),
                    issueDate = obj.optString("issueDate", ""),
                    address = obj.optString("address", ""),
                    dateOfBirth = obj.optString("dateOfBirth", ""),
                    themePresetId = obj.optString("themePresetId", "midnight_black"),
                    notes = obj.optString("notes", ""),
                    tags = tags,
                    customFields = customFields,
                    frontPhotoUri = restoredFrontUri,
                    backPhotoUri = restoredBackUri,
                    isFavorite = obj.optBoolean("isFavorite", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        return restoredCards
    }

    fun generateBackupFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "wallet_backup_$timeStamp.$BACKUP_FILE_EXTENSION"
    }

    fun createCacheBackupFile(context: Context, cards: List<WalletCard>, password: String): File {
        val encryptedContent = createEncryptedBackup(context, cards, password)
        val backupsDir = File(context.cacheDir, "backups")
        if (!backupsDir.exists()) {
            backupsDir.mkdirs()
        }
        val file = File(backupsDir, generateBackupFileName())
        file.writeText(encryptedContent, Charsets.UTF_8)
        return file
    }

    fun writeBackupToStream(context: Context, cards: List<WalletCard>, password: String, outputStream: OutputStream) {
        val encryptedContent = createEncryptedBackup(context, cards, password)
        outputStream.bufferedWriter(Charsets.UTF_8).use {
            it.write(encryptedContent)
            it.flush()
        }
    }

    fun readBackupFromStream(context: Context?, inputStream: InputStream, password: String): List<WalletCard> {
        val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return decryptBackup(context, content, password)
    }

    private fun readUriAsBase64(context: Context, uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            val bytes = if (uri.scheme == "file") {
                val path = uri.path ?: return null
                val file = File(path)
                if (file.exists()) file.readBytes() else null
            } else {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            if (bytes != null && bytes.isNotEmpty()) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBase64ToInternalStorage(context: Context, base64Str: String): String? {
        return try {
            val bytes = Base64.decode(base64Str, Base64.NO_WRAP)
            val photosDir = File(context.filesDir, "card_photos")
            if (!photosDir.exists()) {
                photosDir.mkdirs()
            }
            val fileName = "card_photo_restored_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val destFile = File(photosDir, fileName)
            destFile.writeBytes(bytes)
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
