package com.example.data.repository

import com.example.data.local.WalletDao
import com.example.data.model.CardEntity
import com.example.data.model.CardNetwork
import com.example.data.model.CardType
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import com.example.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class WalletRepository(private val walletDao: WalletDao) {

    companion object {
        // Process-level in-memory cache for zero-latency screen transitions
        private val inMemoryCardsCache = ConcurrentHashMap<Long, WalletCard>()
    }

    val allCards: Flow<List<WalletCard>> = walletDao.getAllCards()
        .map { entities ->
            val domainList = entities.map { entityToDomain(it) }
            domainList.forEach { card -> inMemoryCardsCache[card.id] = card }
            domainList
        }
        .flowOn(Dispatchers.Default)

    val cardCount: Flow<Int> = walletDao.getCardCount()

    fun getCachedCard(id: Long): WalletCard? = inMemoryCardsCache[id]

    suspend fun getCardById(id: Long): WalletCard? {
        inMemoryCardsCache[id]?.let { return it }
        return withContext(Dispatchers.IO) {
            val entity = walletDao.getCardById(id) ?: return@withContext null
            val domain = entityToDomain(entity)
            inMemoryCardsCache[id] = domain
            domain
        }
    }

    suspend fun insertCard(card: WalletCard): Long = withContext(Dispatchers.IO) {
        val entity = domainToEntity(card)
        val id = walletDao.insertCard(entity)
        val inserted = card.copy(id = id)
        inMemoryCardsCache[id] = inserted
        id
    }

    suspend fun updateCard(card: WalletCard) = withContext(Dispatchers.IO) {
        val entity = domainToEntity(card)
        walletDao.updateCard(entity)
        inMemoryCardsCache[card.id] = card
    }

    suspend fun toggleFavorite(cardId: Long, currentFav: Boolean) = withContext(Dispatchers.IO) {
        val entity = walletDao.getCardById(cardId) ?: return@withContext
        val updatedEntity = entity.copy(isFavorite = !currentFav, updatedAt = System.currentTimeMillis())
        walletDao.updateCard(updatedEntity)
        inMemoryCardsCache[cardId]?.let {
            inMemoryCardsCache[cardId] = it.copy(isFavorite = !currentFav)
        }
    }

    suspend fun deleteCard(id: Long) = withContext(Dispatchers.IO) {
        walletDao.deleteCardById(id)
        inMemoryCardsCache.remove(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        walletDao.clearAll()
    }

    suspend fun importCards(cards: List<WalletCard>, replaceExisting: Boolean = false) = withContext(Dispatchers.IO) {
        if (replaceExisting) {
            walletDao.clearAll()
        }
        val entities = cards.map { domainToEntity(it.copy(id = 0)) }
        walletDao.insertAll(entities)
    }

    suspend fun seedSampleDataIfEmpty() {
        // Vault starts completely clean with 0 cards per user request
    }

    private fun domainToEntity(card: WalletCard): CardEntity {
        val cleanNumber = card.cardNumber.replace(" ", "").replace("-", "")
        val last4 = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else cleanNumber
        val detectedNetwork = if (card.cardNetwork == CardNetwork.OTHER && cleanNumber.isNotBlank()) {
            CardNetwork.detectNetwork(cleanNumber)
        } else {
            card.cardNetwork
        }

        val customFieldsJsonStr = if (card.customFields.isNotEmpty()) {
            val jsonArray = org.json.JSONArray()
            for (field in card.customFields) {
                if (field.label.isNotBlank() || field.value.isNotBlank()) {
                    val obj = org.json.JSONObject()
                    obj.put("label", field.label)
                    obj.put("value", field.value)
                    jsonArray.put(obj)
                }
            }
            jsonArray.toString()
        } else {
            "[]"
        }

        return CardEntity(
            id = card.id,
            title = card.title.ifBlank {
                when (card.cardType) {
                    CardType.CREDIT_CARD -> "${card.bankOrIssuer.ifBlank { detectedNetwork.displayName }} Credit"
                    CardType.DEBIT_CARD -> "${card.bankOrIssuer.ifBlank { detectedNetwork.displayName }} Debit"
                    CardType.ID_CARD -> card.idCardType?.displayName ?: "ID Card"
                }
            },
            cardType = card.cardType.name,
            cardholderName = card.cardholderName,
            encryptedCardNumber = CryptoManager.encrypt(cleanNumber),
            lastFourDigits = last4,
            expiryMonth = card.expiryMonth,
            expiryYear = card.expiryYear,
            encryptedCvv = CryptoManager.encrypt(card.cvv),
            encryptedPin = CryptoManager.encrypt(card.pin),
            bankOrIssuer = card.bankOrIssuer,
            cardNetwork = detectedNetwork.name,
            idCardType = card.idCardType?.name,
            encryptedIdNumber = CryptoManager.encrypt(card.idNumber),
            issueDate = card.issueDate,
            address = card.address,
            dateOfBirth = card.dateOfBirth,
            themePresetId = card.themePresetId,
            notes = card.notes,
            tags = card.tags.joinToString(","),
            customFieldsJson = customFieldsJsonStr,
            frontPhotoUri = card.frontPhotoUri,
            backPhotoUri = card.backPhotoUri,
            isFavorite = card.isFavorite,
            createdAt = if (card.createdAt > 0) card.createdAt else System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun entityToDomain(entity: CardEntity): WalletCard {
        val cardType = try {
            CardType.valueOf(entity.cardType)
        } catch (e: Exception) {
            CardType.CREDIT_CARD
        }

        val cardNetwork = try {
            CardNetwork.valueOf(entity.cardNetwork)
        } catch (e: Exception) {
            CardNetwork.OTHER
        }

        val idCardType = if (!entity.idCardType.isNullOrBlank()) {
            try {
                IdCardType.valueOf(entity.idCardType)
            } catch (e: Exception) {
                null
            }
        } else null

        val tags = if (entity.tags.isNotBlank()) {
            entity.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else emptyList()

        val customFields = if (entity.customFieldsJson.isNotBlank() && entity.customFieldsJson != "[]") {
            try {
                val jsonArray = org.json.JSONArray(entity.customFieldsJson)
                val list = mutableListOf<com.example.data.model.CustomField>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        com.example.data.model.CustomField(
                            label = obj.optString("label", ""),
                            value = obj.optString("value", "")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        return WalletCard(
            id = entity.id,
            title = entity.title,
            cardType = cardType,
            cardholderName = entity.cardholderName,
            cardNumber = CryptoManager.decrypt(entity.encryptedCardNumber),
            lastFourDigits = entity.lastFourDigits,
            expiryMonth = entity.expiryMonth,
            expiryYear = entity.expiryYear,
            cvv = CryptoManager.decrypt(entity.encryptedCvv),
            pin = CryptoManager.decrypt(entity.encryptedPin),
            bankOrIssuer = entity.bankOrIssuer,
            cardNetwork = cardNetwork,
            idCardType = idCardType,
            idNumber = CryptoManager.decrypt(entity.encryptedIdNumber),
            issueDate = entity.issueDate,
            address = entity.address,
            dateOfBirth = entity.dateOfBirth,
            themePresetId = entity.themePresetId,
            notes = entity.notes,
            tags = tags,
            customFields = customFields,
            frontPhotoUri = entity.frontPhotoUri,
            backPhotoUri = entity.backPhotoUri,
            isFavorite = entity.isFavorite,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
