package com.example.ui.screens.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WalletDatabase
import com.example.data.model.CardNetwork
import com.example.data.model.CardThemePreset
import com.example.data.model.CardThemes
import com.example.data.model.CardType
import com.example.data.model.CustomField
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import com.example.data.repository.WalletRepository
import com.example.ui.util.CardValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardEditorUiState(
    val cardId: Long = 0,
    val title: String = "",
    val cardType: CardType = CardType.CREDIT_CARD,
    val cardholderName: String = "",
    val cardNumber: String = "", // Pure digits
    val rawExpiry: String = "", // Pure digits MMYY (up to 4)
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val formattedExpiry: String = "",
    val cvv: String = "",
    val pin: String = "",
    val bankOrIssuer: String = "",
    val cardNetwork: CardNetwork = CardNetwork.OTHER,
    val idCardType: IdCardType = IdCardType.AADHAAR,
    val idNumber: String = "",
    val issueDate: String = "",
    val rawIssueDate: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val themePresetId: String = "midnight_black",
    val notes: String = "",
    val tagsString: String = "",
    val customFields: List<CustomField> = emptyList(),
    val frontPhotoUri: String? = null,
    val backPhotoUri: String? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val validationError: String? = null,

    // Aadhaar Card Specific Fields
    val aadhaarNumber: String = "", // 12 digits only
    val aadhaarFullName: String = "",
    val aadhaarDob: String = "", // DD/MM/YYYY
    val aadhaarGender: String = "Male", // "Male", "Female", "Others"
    val aadhaarGenderCustom: String = "",
    val aadhaarAddress: String = "",
    val aadhaarIssueDate: String = "", // DD/MM/YYYY
    val aadhaarMobile: String = "", // 10 digits only

    // PAN Card Specific Fields
    val panNumber: String = "", // 10 chars block letters & digits
    val panName: String = "", // BLOCK LETTERS ONLY
    val panFatherName: String = "", // BLOCK LETTERS ONLY
    val panDob: String = "", // DD/MM/YYYY

    // Voter Card Specific Fields
    val voterEpicNumber: String = "", // Block letters, digits, '/'
    val voterNameEn: String = "",
    val voterNameRegional: String = "",
    val voterRelationType: String = "Father", // "Father", "Husband", "Guardian"
    val voterRelationNameEn: String = "",
    val voterRelationNameRegional: String = "",
    val voterGender: String = "Male", // "Male", "Female", "Others"
    val voterGenderCustom: String = "",
    val voterAgeType: String = "DOB", // "DOB" or "AGE"
    val voterDob: String = "", // DD/MM/YYYY
    val voterAge: String = "", // 2-3 digits
    val voterAddressEn: String = "",
    val voterAddressRegional: String = "",
    val voterSerialNo: String = "", // Max 4 digits
    val voterAcNo: String = "", // Max 4 digits
    val voterAcName: String = "",
    val voterPartNo: String = "", // Max 4 digits
    val voterPartName: String = "",
    val voterPollingStation: String = "",

    // Ration Card Specific Fields
    val rationCategory: String = "AAY", // "AAY", "PHH", "SPHH", "RKSY-I", "RKSY-II", "NPHH", "Others"
    val rationCategoryCustom: String = "",
    val rationCardNumber: String = "",
    val rationCardholderName: String = "",
    val rationRelationType: String = "Father", // "Father", "Husband", "Guardian"
    val rationFatherOrHusbandName: String = "",
    val rationHeadOfFamily: String = "",
    val rationAge: String = "",
    val rationDealerName: String = "",
    val rationDealerAddress: String = "",
    val rationAddress: String = ""
) {
    val isMonthInvalid: Boolean
        get() = expiryMonth.length == 2 && (expiryMonth.toIntOrNull() ?: 0) !in 1..12

    fun toWalletCard(): WalletCard {
        if (cardType == CardType.ID_CARD) {
            when (idCardType) {
                IdCardType.AADHAAR -> {
                    val resolvedGender = if (aadhaarGender == "Others") aadhaarGenderCustom.trim() else aadhaarGender
                    val cFields = listOf(
                        CustomField("Gender", resolvedGender),
                        CustomField("Mobile Number", aadhaarMobile)
                    )
                    return WalletCard(
                        id = cardId,
                        title = title.ifBlank { "Aadhaar Card" },
                        cardType = CardType.ID_CARD,
                        cardholderName = aadhaarFullName.trim(),
                        idCardType = IdCardType.AADHAAR,
                        idNumber = aadhaarNumber.trim(),
                        dateOfBirth = aadhaarDob.trim(),
                        issueDate = aadhaarIssueDate.trim(),
                        address = aadhaarAddress.trim(),
                        bankOrIssuer = "UIDAI",
                        themePresetId = themePresetId,
                        notes = notes,
                        tags = if (tagsString.isNotBlank()) tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
                        customFields = cFields,
                        frontPhotoUri = frontPhotoUri,
                        backPhotoUri = backPhotoUri,
                        isFavorite = isFavorite,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                IdCardType.PAN -> {
                    val cFields = listOf(
                        CustomField("Father's Name", panFatherName.uppercase().trim())
                    )
                    return WalletCard(
                        id = cardId,
                        title = title.ifBlank { "PAN Card" },
                        cardType = CardType.ID_CARD,
                        cardholderName = panName.uppercase().trim(),
                        idCardType = IdCardType.PAN,
                        idNumber = panNumber.uppercase().trim(),
                        dateOfBirth = panDob.trim(),
                        bankOrIssuer = "INCOME TAX DEPARTMENT",
                        themePresetId = themePresetId,
                        notes = notes,
                        tags = if (tagsString.isNotBlank()) tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
                        customFields = cFields,
                        frontPhotoUri = frontPhotoUri,
                        backPhotoUri = backPhotoUri,
                        isFavorite = isFavorite,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                IdCardType.VOTER_ID -> {
                    val resolvedGender = if (voterGender == "Others") voterGenderCustom.trim() else voterGender
                    val cFields = mutableListOf(
                        CustomField("Relation Type", voterRelationType),
                        CustomField("Name (Regional)", voterNameRegional.trim()),
                        CustomField("${voterRelationType}'s Name (English)", voterRelationNameEn.trim()),
                        CustomField("${voterRelationType}'s Name (Regional)", voterRelationNameRegional.trim()),
                        CustomField("Father / Husband / Guardian Name (English)", voterRelationNameEn.trim()),
                        CustomField("Father / Husband / Guardian Name (Regional)", voterRelationNameRegional.trim()),
                        CustomField("Gender", resolvedGender),
                        CustomField("Address (Regional)", voterAddressRegional.trim()),
                        CustomField("Serial Number", voterSerialNo.trim()),
                        CustomField("Assembly Constituency No.", voterAcNo.trim()),
                        CustomField("Assembly Constituency Name", voterAcName.trim()),
                        CustomField("Part No.", voterPartNo.trim()),
                        CustomField("Part Name", voterPartName.trim()),
                        CustomField("Polling Station", voterPollingStation.trim())
                    )
                    if (voterAgeType == "AGE") {
                        cFields.add(CustomField("Age", voterAge.trim()))
                    }

                    return WalletCard(
                        id = cardId,
                        title = title.ifBlank { "Voter Card" },
                        cardType = CardType.ID_CARD,
                        cardholderName = voterNameEn.trim(),
                        idCardType = IdCardType.VOTER_ID,
                        idNumber = voterEpicNumber.uppercase().trim(),
                        dateOfBirth = if (voterAgeType == "DOB") voterDob.trim() else "",
                        address = voterAddressEn.trim(),
                        bankOrIssuer = "ELECTION COMMISSION OF INDIA",
                        themePresetId = themePresetId,
                        notes = notes,
                        tags = if (tagsString.isNotBlank()) tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
                        customFields = cFields,
                        frontPhotoUri = frontPhotoUri,
                        backPhotoUri = backPhotoUri,
                        isFavorite = isFavorite,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                IdCardType.RATION_CARD -> {
                    val resolvedCategory = if (rationCategory == "Others") rationCategoryCustom.trim() else rationCategory.trim()
                    val fullCardId = if (resolvedCategory.isNotBlank() && rationCardNumber.isNotBlank()) {
                        "$resolvedCategory ${rationCardNumber.trim()}"
                    } else {
                        rationCardNumber.trim()
                    }
                    val cFields = listOf(
                        CustomField("Category", resolvedCategory),
                        CustomField("Card Number", rationCardNumber.trim()),
                        CustomField("Relation Type", rationRelationType),
                        CustomField("Name of $rationRelationType", rationFatherOrHusbandName.trim()),
                        CustomField("Name of Father / Husband", rationFatherOrHusbandName.trim()),
                        CustomField("Head of the Family", rationHeadOfFamily.trim()),
                        CustomField("Age", rationAge.trim()),
                        CustomField("Dealer Name", rationDealerName.trim()),
                        CustomField("Dealer Address", rationDealerAddress.trim())
                    )
                    return WalletCard(
                        id = cardId,
                        title = title.ifBlank { "Ration Card" },
                        cardType = CardType.ID_CARD,
                        cardholderName = rationCardholderName.trim(),
                        idCardType = IdCardType.RATION_CARD,
                        idNumber = fullCardId,
                        address = rationAddress.trim(),
                        bankOrIssuer = "FOOD & SUPPLIES DEPARTMENT",
                        themePresetId = themePresetId,
                        notes = notes,
                        tags = if (tagsString.isNotBlank()) tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
                        customFields = cFields,
                        frontPhotoUri = frontPhotoUri,
                        backPhotoUri = backPhotoUri,
                        isFavorite = isFavorite,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                else -> {
                    // Standard / generic ID card fallback
                    val validCustomFields = customFields.filter { it.label.isNotBlank() || it.value.isNotBlank() }
                    return WalletCard(
                        id = cardId,
                        title = title.ifBlank { idCardType.displayName },
                        cardType = CardType.ID_CARD,
                        cardholderName = cardholderName,
                        idCardType = idCardType,
                        idNumber = idNumber,
                        issueDate = issueDate,
                        address = address,
                        dateOfBirth = dateOfBirth,
                        bankOrIssuer = bankOrIssuer.ifBlank { idCardType.defaultIssuer },
                        themePresetId = themePresetId,
                        notes = notes,
                        tags = if (tagsString.isNotBlank()) tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
                        customFields = validCustomFields,
                        frontPhotoUri = frontPhotoUri,
                        backPhotoUri = backPhotoUri,
                        isFavorite = isFavorite,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            }
        }

        // Credit / Debit cards
        val cleanNumber = cardNumber.filter { it.isDigit() }
        val last4 = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else cleanNumber
        val detectedNetwork = if (cardNetwork == CardNetwork.OTHER && cleanNumber.isNotBlank()) {
            CardNetwork.detectNetwork(cleanNumber)
        } else {
            cardNetwork
        }

        val tags = if (tagsString.isNotBlank()) {
            tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else emptyList()

        val validCustomFields = customFields.filter { it.label.isNotBlank() || it.value.isNotBlank() }

        return WalletCard(
            id = cardId,
            title = title.ifBlank {
                when (cardType) {
                    CardType.CREDIT_CARD -> "${bankOrIssuer.ifBlank { detectedNetwork.displayName }} Credit"
                    CardType.DEBIT_CARD -> "${bankOrIssuer.ifBlank { detectedNetwork.displayName }} Debit"
                    CardType.ID_CARD -> idCardType.displayName
                }
            },
            cardType = cardType,
            cardholderName = cardholderName,
            cardNumber = cleanNumber,
            lastFourDigits = last4,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvv = cvv,
            pin = pin,
            bankOrIssuer = bankOrIssuer,
            cardNetwork = detectedNetwork,
            idCardType = null,
            idNumber = "",
            issueDate = issueDate,
            address = address,
            dateOfBirth = dateOfBirth,
            themePresetId = themePresetId,
            notes = notes,
            tags = tags,
            customFields = validCustomFields,
            frontPhotoUri = frontPhotoUri,
            backPhotoUri = backPhotoUri,
            isFavorite = isFavorite,
            updatedAt = System.currentTimeMillis()
        )
    }
}

class CardEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WalletRepository
    private val _uiState = MutableStateFlow(CardEditorUiState())
    val uiState: StateFlow<CardEditorUiState> = _uiState.asStateFlow()

    private var initializedKey: String? = null

    init {
        val db = WalletDatabase.getDatabase(application)
        repository = WalletRepository(db.walletDao())
    }

    fun initialize(cardId: Long, initialType: CardType?, initialIdType: IdCardType? = null) {
        val key = "$cardId-${initialType?.name}-${initialIdType?.name}"
        if (initializedKey == key) return
        initializedKey = key

        if (cardId > 0) {
            val cached = repository.getCachedCard(cardId)
            if (cached != null) {
                populateFromCard(cached)
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }
            viewModelScope.launch {
                val card = repository.getCardById(cardId)
                if (card != null && card != cached) {
                    populateFromCard(card)
                } else if (card == null) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        } else {
            val type = initialType ?: CardType.CREDIT_CARD
            val idType = initialIdType ?: IdCardType.AADHAAR
            val defaultIssuer = if (type == CardType.ID_CARD) {
                when (idType) {
                    IdCardType.AADHAAR -> "UIDAI"
                    IdCardType.PAN -> "INCOME TAX DEPARTMENT"
                    IdCardType.VOTER_ID -> "ELECTION COMMISSION OF INDIA"
                    IdCardType.RATION_CARD -> "FOOD & SUPPLIES DEPARTMENT"
                    else -> idType.defaultIssuer
                }
            } else {
                ""
            }
            val defaultTitle = if (type == CardType.ID_CARD) idType.displayName else ""

            _uiState.value = CardEditorUiState(
                cardType = type,
                idCardType = idType,
                bankOrIssuer = defaultIssuer,
                title = defaultTitle
            )
        }
    }

    private fun populateFromCard(card: WalletCard) {
        val rawNum = card.cardNumber.filter { it.isDigit() }
        val rawM = card.expiryMonth.filter { it.isDigit() }
        val rawY = card.expiryYear.filter { it.isDigit() }
        val combinedExpiry = "$rawM$rawY"
        val expFormatted = if (rawM.isNotBlank() && rawY.isNotBlank()) {
            "$rawM/$rawY"
        } else combinedExpiry

        val customMap = card.customFields.associate { it.label to it.value }

        val idType = card.idCardType ?: IdCardType.AADHAAR

        // Parse Aadhaar
        val aNum = if (idType == IdCardType.AADHAAR) card.idNumber else ""
        val aName = if (idType == IdCardType.AADHAAR) card.cardholderName else ""
        val aDob = if (idType == IdCardType.AADHAAR) card.dateOfBirth else ""
        val aAddress = if (idType == IdCardType.AADHAAR) card.address else ""
        val aIssue = if (idType == IdCardType.AADHAAR) card.issueDate else ""
        val aMobile = customMap["Mobile Number"] ?: ""
        val aGenRaw = customMap["Gender"] ?: "Male"
        val aGen = if (aGenRaw in listOf("Male", "Female")) aGenRaw else if (aGenRaw.isNotBlank()) "Others" else "Male"
        val aGenCustom = if (aGenRaw !in listOf("Male", "Female")) aGenRaw else ""

        // Parse PAN
        val pNum = if (idType == IdCardType.PAN) card.idNumber else ""
        val pName = if (idType == IdCardType.PAN) card.cardholderName else ""
        val pFather = customMap["Father's Name"] ?: ""
        val pDob = if (idType == IdCardType.PAN) card.dateOfBirth else ""

        // Parse Voter
        val vNum = if (idType == IdCardType.VOTER_ID) card.idNumber else ""
        val vNameEn = if (idType == IdCardType.VOTER_ID) card.cardholderName else ""
        val vNameReg = customMap["Name (Regional)"] ?: ""
        val vRelTypeRaw = customMap["Relation Type"] ?: ""
        val vRelType = when {
            vRelTypeRaw in listOf("Father", "Husband", "Guardian") -> vRelTypeRaw
            customMap.keys.any { it.contains("Husband", ignoreCase = true) } -> "Husband"
            customMap.keys.any { it.contains("Guardian", ignoreCase = true) } -> "Guardian"
            else -> "Father"
        }
        val vRelEn = customMap["${vRelType}'s Name (English)"]
            ?: customMap["Father / Husband / Guardian Name (English)"]
            ?: customMap["Relation Name (English)"]
            ?: customMap["Father's Name (English)"]
            ?: customMap["Husband's Name (English)"]
            ?: customMap["Guardian's Name (English)"]
            ?: ""
        val vRelReg = customMap["${vRelType}'s Name (Regional)"]
            ?: customMap["Father / Husband / Guardian Name (Regional)"]
            ?: customMap["Relation Name (Regional)"]
            ?: customMap["Father's Name (Regional)"]
            ?: customMap["Husband's Name (Regional)"]
            ?: customMap["Guardian's Name (Regional)"]
            ?: ""
        val vGenRaw = customMap["Gender"] ?: "Male"
        val vGen = if (vGenRaw in listOf("Male", "Female")) vGenRaw else if (vGenRaw.isNotBlank()) "Others" else "Male"
        val vGenCustom = if (vGenRaw !in listOf("Male", "Female")) vGenRaw else ""
        val vDob = if (idType == IdCardType.VOTER_ID) card.dateOfBirth else ""
        val vAge = customMap["Age"] ?: ""
        val vAgeType = if (vDob.isNotBlank()) "DOB" else if (vAge.isNotBlank()) "AGE" else "DOB"
        val vAddrEn = if (idType == IdCardType.VOTER_ID) card.address else ""
        val vAddrReg = customMap["Address (Regional)"] ?: ""
        val vSerial = customMap["Serial Number"] ?: ""
        val vAcNo = customMap["Assembly Constituency No."] ?: ""
        val vAcName = customMap["Assembly Constituency Name"] ?: ""
        val vPartNo = customMap["Part No."] ?: ""
        val vPartName = customMap["Part Name"] ?: ""
        val vPoll = customMap["Polling Station"] ?: ""

        // Parse Ration Card
        val rCatRaw = customMap["Category"] ?: ""
        val rCat = if (rCatRaw in listOf("AAY", "PHH", "SPHH", "RKSY-I", "RKSY-II", "NPHH")) rCatRaw else if (rCatRaw.isNotBlank()) "Others" else "AAY"
        val rCatCustom = if (rCatRaw !in listOf("AAY", "PHH", "SPHH", "RKSY-I", "RKSY-II", "NPHH")) rCatRaw else ""
        val rCardNo = customMap["Card Number"] ?: if (card.idNumber.contains(" ")) card.idNumber.substringAfter(" ") else card.idNumber
        val rHolder = if (idType == IdCardType.RATION_CARD) card.cardholderName else ""
        val rRelTypeRaw = customMap["Relation Type"] ?: ""
        val rRelType = when {
            rRelTypeRaw in listOf("Father", "Husband", "Guardian") -> rRelTypeRaw
            customMap.keys.any { it.contains("Husband", ignoreCase = true) } -> "Husband"
            customMap.keys.any { it.contains("Guardian", ignoreCase = true) } -> "Guardian"
            else -> "Father"
        }
        val rFather = customMap["Name of $rRelType"]
            ?: customMap["Name of Father / Husband"]
            ?: customMap["Name of Father"]
            ?: customMap["Name of Husband"]
            ?: customMap["Name of Guardian"]
            ?: customMap["Father / Husband Name"]
            ?: ""
        val rHead = customMap["Head of the Family"] ?: ""
        val rAge = customMap["Age"] ?: ""
        val rDealer = customMap["Dealer Name"] ?: ""
        val rDealerAddr = customMap["Dealer Address"] ?: ""
        val rAddr = if (idType == IdCardType.RATION_CARD) card.address else ""

        _uiState.value = CardEditorUiState(
            cardId = card.id,
            title = card.title,
            cardType = card.cardType,
            cardholderName = card.cardholderName,
            cardNumber = rawNum,
            rawExpiry = combinedExpiry,
            expiryMonth = rawM,
            expiryYear = rawY,
            formattedExpiry = expFormatted,
            cvv = card.cvv,
            pin = card.pin,
            bankOrIssuer = card.bankOrIssuer,
            cardNetwork = card.cardNetwork,
            idCardType = idType,
            idNumber = card.idNumber,
            issueDate = card.issueDate,
            rawIssueDate = card.issueDate.filter { it.isDigit() },
            address = card.address,
            dateOfBirth = card.dateOfBirth,
            themePresetId = card.themePresetId,
            notes = card.notes,
            tagsString = card.tags.joinToString(", "),
            customFields = card.customFields,
            frontPhotoUri = card.frontPhotoUri,
            backPhotoUri = card.backPhotoUri,
            isFavorite = card.isFavorite,

            // Aadhaar
            aadhaarNumber = aNum,
            aadhaarFullName = aName,
            aadhaarDob = aDob,
            aadhaarGender = aGen,
            aadhaarGenderCustom = aGenCustom,
            aadhaarAddress = aAddress,
            aadhaarIssueDate = aIssue,
            aadhaarMobile = aMobile,

            // PAN
            panNumber = pNum,
            panName = pName,
            panFatherName = pFather,
            panDob = pDob,

            // Voter
            voterEpicNumber = vNum,
            voterNameEn = vNameEn,
            voterNameRegional = vNameReg,
            voterRelationType = vRelType,
            voterRelationNameEn = vRelEn,
            voterRelationNameRegional = vRelReg,
            voterGender = vGen,
            voterGenderCustom = vGenCustom,
            voterAgeType = vAgeType,
            voterDob = vDob,
            voterAge = vAge,
            voterAddressEn = vAddrEn,
            voterAddressRegional = vAddrReg,
            voterSerialNo = vSerial,
            voterAcNo = vAcNo,
            voterAcName = vAcName,
            voterPartNo = vPartNo,
            voterPartName = vPartName,
            voterPollingStation = vPoll,

            // Ration
            rationCategory = rCat,
            rationCategoryCustom = rCatCustom,
            rationCardNumber = rCardNo,
            rationCardholderName = rHolder,
            rationRelationType = rRelType,
            rationFatherOrHusbandName = rFather,
            rationHeadOfFamily = rHead,
            rationAge = rAge,
            rationDealerName = rDealer,
            rationDealerAddress = rDealerAddr,
            rationAddress = rAddr
        )
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, validationError = null) }
    fun onCardholderNameChange(value: String) = _uiState.update { it.copy(cardholderName = value) }

    fun onCardNumberChange(input: String) {
        val digits = input.filter { it.isDigit() }.take(19)
        val network = CardNetwork.detectNetwork(digits)
        _uiState.update {
            it.copy(
                cardNumber = digits,
                cardNetwork = network,
                validationError = null
            )
        }
    }

    fun onExpiryChange(input: String) {
        var digits = input.filter { it.isDigit() }.take(4)
        if (digits.length == 1 && digits[0] in '2'..'9') {
            digits = "0$digits"
        }
        val month = digits.take(2)
        val year = if (digits.length > 2) digits.substring(2) else ""
        val formatted = if (digits.length > 2) "${digits.take(2)}/${digits.substring(2)}" else digits

        _uiState.update {
            it.copy(
                rawExpiry = digits,
                expiryMonth = month,
                expiryYear = year,
                formattedExpiry = formatted,
                validationError = null
            )
        }
    }

    fun onCvvChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(cvv = digits) }
    }

    fun onPinChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(pin = digits) }
    }

    fun onBankOrIssuerChange(value: String) = _uiState.update { it.copy(bankOrIssuer = value) }
    fun onCardNetworkChange(network: CardNetwork) = _uiState.update { it.copy(cardNetwork = network) }
    
    fun onIdCardTypeChange(type: IdCardType) {
        val defaultIssuer = when (type) {
            IdCardType.AADHAAR -> "UIDAI"
            IdCardType.PAN -> "INCOME TAX DEPARTMENT"
            IdCardType.VOTER_ID -> "ELECTION COMMISSION OF INDIA"
            IdCardType.RATION_CARD -> "FOOD & SUPPLIES DEPARTMENT"
            else -> type.defaultIssuer
        }
        _uiState.update {
            it.copy(
                idCardType = type,
                bankOrIssuer = defaultIssuer,
                title = type.displayName,
                validationError = null
            )
        }
    }
    
    fun onIdNumberChange(value: String) = _uiState.update { it.copy(idNumber = value, validationError = null) }

    fun onIssueDateChange(value: String) {
        val formatted = CardValidationUtils.formatFullDate(value)
        _uiState.update {
            it.copy(
                rawIssueDate = formatted.filter { ch -> ch.isDigit() },
                issueDate = formatted,
                validationError = null
            )
        }
    }

    fun onDateOfBirthChange(value: String) = _uiState.update {
        it.copy(dateOfBirth = CardValidationUtils.formatFullDate(value), validationError = null)
    }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onThemePresetChange(preset: CardThemePreset) = _uiState.update { it.copy(themePresetId = preset.id) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onTagsStringChange(value: String) = _uiState.update { it.copy(tagsString = value) }
    fun onFavoriteChange(fav: Boolean) = _uiState.update { it.copy(isFavorite = fav) }

    // --- AADHAAR Specific Handlers ---
    fun onAadhaarNumberChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(aadhaarNumber = digits, validationError = null) }
    }
    fun onAadhaarFullNameChange(value: String) = _uiState.update { it.copy(aadhaarFullName = value, validationError = null) }
    fun onAadhaarDobChange(value: String) = _uiState.update {
        it.copy(aadhaarDob = CardValidationUtils.formatFullDate(value), validationError = null)
    }
    fun onAadhaarGenderChange(value: String) = _uiState.update { it.copy(aadhaarGender = value, validationError = null) }
    fun onAadhaarGenderCustomChange(value: String) = _uiState.update { it.copy(aadhaarGenderCustom = value, validationError = null) }
    fun onAadhaarAddressChange(value: String) = _uiState.update { it.copy(aadhaarAddress = value, validationError = null) }
    fun onAadhaarIssueDateChange(value: String) = _uiState.update {
        it.copy(aadhaarIssueDate = CardValidationUtils.formatFullDate(value), validationError = null)
    }
    fun onAadhaarMobileChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(aadhaarMobile = digits, validationError = null) }
    }

    // --- PAN Specific Handlers ---
    fun onPanNumberChange(value: String) {
        val clean = value.uppercase().filter { it.isLetterOrDigit() }.take(10)
        _uiState.update { it.copy(panNumber = clean, validationError = null) }
    }
    fun onPanNameChange(value: String) = _uiState.update { it.copy(panName = value.uppercase(), validationError = null) }
    fun onPanFatherNameChange(value: String) = _uiState.update { it.copy(panFatherName = value.uppercase(), validationError = null) }
    fun onPanDobChange(value: String) = _uiState.update {
        it.copy(panDob = CardValidationUtils.formatFullDate(value), validationError = null)
    }

    // --- Voter Card Specific Handlers ---
    fun onVoterEpicNumberChange(value: String) {
        val clean = value.uppercase().filter { it.isLetterOrDigit() || it == '/' }
        _uiState.update { it.copy(voterEpicNumber = clean, validationError = null) }
    }
    fun onVoterNameEnChange(value: String) = _uiState.update { it.copy(voterNameEn = value, validationError = null) }
    fun onVoterNameRegionalChange(value: String) = _uiState.update { it.copy(voterNameRegional = value, validationError = null) }
    fun onVoterRelationTypeChange(value: String) = _uiState.update { it.copy(voterRelationType = value, validationError = null) }
    fun onVoterRelationNameEnChange(value: String) = _uiState.update { it.copy(voterRelationNameEn = value, validationError = null) }
    fun onVoterRelationNameRegionalChange(value: String) = _uiState.update { it.copy(voterRelationNameRegional = value, validationError = null) }
    fun onVoterGenderChange(value: String) = _uiState.update { it.copy(voterGender = value, validationError = null) }
    fun onVoterGenderCustomChange(value: String) = _uiState.update { it.copy(voterGenderCustom = value, validationError = null) }
    fun onVoterAgeTypeChange(value: String) = _uiState.update { it.copy(voterAgeType = value, validationError = null) }
    fun onVoterDobChange(value: String) = _uiState.update {
        it.copy(voterDob = CardValidationUtils.formatFullDate(value), validationError = null)
    }
    fun onVoterAgeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(voterAge = digits, validationError = null) }
    }
    fun onVoterAddressEnChange(value: String) = _uiState.update { it.copy(voterAddressEn = value, validationError = null) }
    fun onVoterAddressRegionalChange(value: String) = _uiState.update { it.copy(voterAddressRegional = value, validationError = null) }
    fun onVoterSerialNoChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(voterSerialNo = digits, validationError = null) }
    }
    fun onVoterAcNoChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(voterAcNo = digits, validationError = null) }
    }
    fun onVoterAcNameChange(value: String) = _uiState.update { it.copy(voterAcName = value, validationError = null) }
    fun onVoterPartNoChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(voterPartNo = digits, validationError = null) }
    }
    fun onVoterPartNameChange(value: String) = _uiState.update { it.copy(voterPartName = value, validationError = null) }
    fun onVoterPollingStationChange(value: String) = _uiState.update { it.copy(voterPollingStation = value, validationError = null) }

    // --- Ration Card Specific Handlers ---
    fun onRationCategoryChange(value: String) = _uiState.update { it.copy(rationCategory = value, validationError = null) }
    fun onRationCategoryCustomChange(value: String) = _uiState.update { it.copy(rationCategoryCustom = value, validationError = null) }
    fun onRationCardNumberChange(value: String) = _uiState.update { it.copy(rationCardNumber = value, validationError = null) }
    fun onRationCardholderNameChange(value: String) = _uiState.update { it.copy(rationCardholderName = value, validationError = null) }
    fun onRationRelationTypeChange(value: String) = _uiState.update { it.copy(rationRelationType = value, validationError = null) }
    fun onRationFatherOrHusbandNameChange(value: String) = _uiState.update { it.copy(rationFatherOrHusbandName = value, validationError = null) }
    fun onRationHeadOfFamilyChange(value: String) = _uiState.update { it.copy(rationHeadOfFamily = value, validationError = null) }
    fun onRationAgeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(rationAge = digits, validationError = null) }
    }
    fun onRationDealerNameChange(value: String) = _uiState.update { it.copy(rationDealerName = value, validationError = null) }
    fun onRationDealerAddressChange(value: String) = _uiState.update { it.copy(rationDealerAddress = value, validationError = null) }
    fun onRationAddressChange(value: String) = _uiState.update { it.copy(rationAddress = value, validationError = null) }

    fun onFrontPhotoChanged(uri: String?) = _uiState.update { it.copy(frontPhotoUri = uri) }
    fun onBackPhotoChanged(uri: String?) = _uiState.update { it.copy(backPhotoUri = uri) }

    // Custom Fields Management
    fun addCustomField(label: String = "", value: String = "") {
        val updated = _uiState.value.customFields + CustomField(label, value)
        _uiState.update { it.copy(customFields = updated) }
    }

    fun updateCustomFieldLabel(index: Int, newLabel: String) {
        val list = _uiState.value.customFields.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(label = newLabel)
            _uiState.update { it.copy(customFields = list) }
        }
    }

    fun updateCustomFieldValue(index: Int, newValue: String) {
        val list = _uiState.value.customFields.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(value = newValue)
            _uiState.update { it.copy(customFields = list) }
        }
    }

    fun removeCustomField(index: Int) {
        val list = _uiState.value.customFields.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _uiState.update { it.copy(customFields = list) }
        }
    }

    fun saveCard() {
        val state = _uiState.value

        // Perform strict validation based on card type
        if (state.cardType == CardType.ID_CARD) {
            when (state.idCardType) {
                IdCardType.AADHAAR -> {
                    if (state.aadhaarNumber.length != 12 || !state.aadhaarNumber.all { it.isDigit() }) {
                        _uiState.update { it.copy(validationError = "Aadhaar Card Number must be exactly 12 digits (numbers only)") }
                        return
                    }
                    if (state.aadhaarFullName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Full Name is mandatory for Aadhaar Card") }
                        return
                    }
                    if (state.aadhaarDob.isBlank()) {
                        _uiState.update { it.copy(validationError = "Date of Birth (DD/MM/YYYY) is mandatory") }
                        return
                    }
                    if (state.aadhaarGender == "Others" && state.aadhaarGenderCustom.isBlank()) {
                        _uiState.update { it.copy(validationError = "Please specify Gender") }
                        return
                    }
                    if (state.aadhaarAddress.isBlank()) {
                        _uiState.update { it.copy(validationError = "Address is mandatory for Aadhaar Card") }
                        return
                    }
                    if (state.aadhaarIssueDate.isBlank()) {
                        _uiState.update { it.copy(validationError = "Issue Date (DD/MM/YYYY) is mandatory") }
                        return
                    }
                    if (state.aadhaarMobile.length != 10 || !state.aadhaarMobile.all { it.isDigit() }) {
                        _uiState.update { it.copy(validationError = "Mobile Number must be exactly 10 digits (numbers only)") }
                        return
                    }
                }
                IdCardType.PAN -> {
                    if (state.panNumber.length != 10 || !state.panNumber.all { it.isLetterOrDigit() }) {
                        _uiState.update { it.copy(validationError = "Permanent Account Number must be exactly 10 characters (Block letters & digits)") }
                        return
                    }
                    if (state.panName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Name is mandatory (BLOCK LETTERS)") }
                        return
                    }
                    if (state.panFatherName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Father's Name is mandatory (BLOCK LETTERS)") }
                        return
                    }
                    if (state.panDob.isBlank()) {
                        _uiState.update { it.copy(validationError = "Date of Birth (DD/MM/YYYY) is mandatory") }
                        return
                    }
                }
                IdCardType.VOTER_ID -> {
                    if (state.voterEpicNumber.isBlank()) {
                        _uiState.update { it.copy(validationError = "EPIC Number is mandatory") }
                        return
                    }
                    if (state.voterNameEn.isBlank()) {
                        _uiState.update { it.copy(validationError = "Name (English) is mandatory") }
                        return
                    }
                    if (state.voterNameRegional.isBlank()) {
                        _uiState.update { it.copy(validationError = "Name (Regional language) is mandatory") }
                        return
                    }
                    if (state.voterRelationNameEn.isBlank()) {
                        _uiState.update { it.copy(validationError = "${state.voterRelationType}'s Name (English) is mandatory") }
                        return
                    }
                    if (state.voterRelationNameRegional.isBlank()) {
                        _uiState.update { it.copy(validationError = "${state.voterRelationType}'s Name (Regional language) is mandatory") }
                        return
                    }
                    if (state.voterGender == "Others" && state.voterGenderCustom.isBlank()) {
                        _uiState.update { it.copy(validationError = "Please specify Gender") }
                        return
                    }
                    if (state.voterAgeType == "DOB" && state.voterDob.isBlank()) {
                        _uiState.update { it.copy(validationError = "Date of Birth (DD/MM/YYYY) is mandatory") }
                        return
                    }
                    if (state.voterAgeType == "AGE" && (state.voterAge.length !in 2..3 || !state.voterAge.all { it.isDigit() })) {
                        _uiState.update { it.copy(validationError = "Age must be a 2 or 3 digit number") }
                        return
                    }
                    if (state.voterAddressEn.isBlank()) {
                        _uiState.update { it.copy(validationError = "Address (English) is mandatory") }
                        return
                    }
                    if (state.voterAddressRegional.isBlank()) {
                        _uiState.update { it.copy(validationError = "Address (Regional language) is mandatory") }
                        return
                    }
                    if (state.voterSerialNo.isBlank() || !state.voterSerialNo.all { it.isDigit() }) {
                        _uiState.update { it.copy(validationError = "Serial Number is mandatory (Max 4 digits)") }
                        return
                    }
                    if (state.voterAcNo.isBlank() || !state.voterAcNo.all { it.isDigit() }) {
                        _uiState.update { it.copy(validationError = "Assembly Constituency No. is mandatory (Max 4 digits)") }
                        return
                    }
                    if (state.voterAcName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Assembly Constituency Name is mandatory") }
                        return
                    }
                    if (state.voterPartNo.isBlank() || !state.voterPartNo.all { it.isDigit() }) {
                        _uiState.update { it.copy(validationError = "Part No. is mandatory (Max 4 digits)") }
                        return
                    }
                    if (state.voterPartName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Part Name is mandatory") }
                        return
                    }
                    if (state.voterPollingStation.isBlank()) {
                        _uiState.update { it.copy(validationError = "Polling Station is mandatory") }
                        return
                    }
                }
                IdCardType.RATION_CARD -> {
                    if (state.rationCategory == "Others" && state.rationCategoryCustom.isBlank()) {
                        _uiState.update { it.copy(validationError = "Please specify Ration Card Category") }
                        return
                    }
                    if (state.rationCardNumber.isBlank()) {
                        _uiState.update { it.copy(validationError = "Card Number is mandatory") }
                        return
                    }
                    if (state.rationCardholderName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Name of Card Holder is mandatory") }
                        return
                    }
                    if (state.rationFatherOrHusbandName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Name of ${state.rationRelationType} is mandatory") }
                        return
                    }
                    if (state.rationHeadOfFamily.isBlank()) {
                        _uiState.update { it.copy(validationError = "Head of the Family is mandatory") }
                        return
                    }
                    if (state.rationAge.isBlank() || !state.rationAge.all { it.isDigit() }) {
                        _uiState.update { it.copy(validationError = "Age is mandatory (numbers only)") }
                        return
                    }
                    if (state.rationDealerName.isBlank()) {
                        _uiState.update { it.copy(validationError = "Dealer Name is mandatory") }
                        return
                    }
                    if (state.rationDealerAddress.isBlank()) {
                        _uiState.update { it.copy(validationError = "Dealer Address is mandatory") }
                        return
                    }
                    if (state.rationAddress.isBlank()) {
                        _uiState.update { it.copy(validationError = "Card Holder Address is mandatory") }
                        return
                    }
                }
                else -> {
                    if (state.title.isBlank() && state.cardholderName.isBlank() && state.idNumber.isBlank()) {
                        _uiState.update { it.copy(validationError = "Please enter document number or title") }
                        return
                    }
                }
            }
        } else {
            if (state.title.isBlank() && state.bankOrIssuer.isBlank() && state.cardNumber.isBlank()) {
                _uiState.update { it.copy(validationError = "Please enter at least a title, bank name, or card number") }
                return
            }

            if (state.isMonthInvalid) {
                _uiState.update { it.copy(validationError = "Invalid expiry month. Month must be between 01 and 12.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationError = null) }
            val card = state.toWalletCard()
            if (state.cardId > 0) {
                repository.updateCard(card)
            } else {
                repository.insertCard(card)
            }
            _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
        }
    }
}
