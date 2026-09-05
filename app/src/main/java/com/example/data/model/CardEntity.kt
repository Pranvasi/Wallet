package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class CustomField(
    val label: String,
    val value: String
)

@Entity(
    tableName = "wallet_cards",
    indices = [
        Index(value = ["cardType"]),
        Index(value = ["isFavorite"]),
        Index(value = ["idCardType"]),
        Index(value = ["updatedAt"])
    ]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val cardType: String,
    val cardholderName: String,
    val encryptedCardNumber: String = "",
    val lastFourDigits: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val encryptedCvv: String = "",
    val encryptedPin: String = "",
    val bankOrIssuer: String = "",
    val cardNetwork: String = "OTHER",
    val idCardType: String? = null,
    val encryptedIdNumber: String = "",
    val issueDate: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val themePresetId: String = "midnight_black",
    val notes: String = "",
    val tags: String = "",
    val customFieldsJson: String = "[]",
    val frontPhotoUri: String? = null,
    val backPhotoUri: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class WalletCard(
    val id: Long = 0,
    val title: String = "",
    val cardType: CardType = CardType.CREDIT_CARD,
    val cardholderName: String = "",
    val cardNumber: String = "",
    val lastFourDigits: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val cvv: String = "",
    val pin: String = "",
    val bankOrIssuer: String = "",
    val cardNetwork: CardNetwork = CardNetwork.OTHER,
    val idCardType: IdCardType? = null,
    val idNumber: String = "",
    val issueDate: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val themePresetId: String = "midnight_black",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val customFields: List<CustomField> = emptyList(),
    val frontPhotoUri: String? = null,
    val backPhotoUri: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val maskedNumber: String
        get() {
            return if (cardNumber.isNotBlank()) {
                val clean = cardNumber.replace(" ", "")
                if (clean.length > 4) {
                    val stars = "*".repeat((clean.length - 4).coerceAtLeast(0))
                    val full = stars + clean.takeLast(4)
                    full.chunked(4).joinToString(" ")
                } else {
                    clean
                }
            } else if (lastFourDigits.isNotBlank()) {
                "•••• •••• •••• $lastFourDigits"
            } else {
                ""
            }
        }

    val formattedExpiry: String
        get() = if (expiryMonth.isNotBlank() && expiryYear.isNotBlank()) "$expiryMonth/$expiryYear" else ""

    val isExpired: Boolean
        get() {
            if (expiryMonth.isBlank() || expiryYear.isBlank()) return false
            return try {
                val month = expiryMonth.toIntOrNull() ?: return false
                var year = expiryYear.toIntOrNull() ?: return false
                if (year < 100) year += 2000
                val now = java.util.Calendar.getInstance()
                val currentYear = now.get(java.util.Calendar.YEAR)
                val currentMonth = now.get(java.util.Calendar.MONTH) + 1
                year < currentYear || (year == currentYear && month < currentMonth)
            } catch (e: Exception) {
                false
            }
        }
}
