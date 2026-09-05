package com.example.ui.screens.home

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WalletDatabase
import com.example.data.model.CardType
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import com.example.data.repository.WalletRepository
import com.example.data.security.SecurityPreferences
import com.example.data.security.SecuritySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HomeTab(val displayName: String) {
    CARDS("Cards"),
    IDS("IDs")
}

enum class PaymentSubFilter(val label: String) {
    ALL("All Cards"),
    CREDIT("Credit Cards"),
    DEBIT("Debit Cards"),
    FAVORITES("Favorites"),
    EXPIRING("Expired / Expiring")
}

enum class IdSubFilter(val label: String) {
    ALL("All IDs"),
    AADHAAR("Aadhaar"),
    PAN("PAN"),
    DRIVING_LICENSE("Driving Licence"),
    VOTER_ID("Voter ID"),
    PASSPORT("Passport"),
    VEHICLE_RC("Vehicle RC"),
    ABHA_HEALTH("ABHA Health"),
    RATION_CARD("Ration Card"),
    FAVORITES("Favorites")
}

private data class FilterResult(
    val allCards: List<WalletCard>,
    val filteredCards: List<WalletCard>,
    val selectedTab: HomeTab,
    val paymentSubFilter: PaymentSubFilter,
    val idSubFilter: IdSubFilter,
    val searchQuery: String,
    val paymentCardsCount: Int,
    val idCardsCount: Int,
    val expiringCardsCount: Int
)

data class HomeUiState(
    val cards: List<WalletCard> = emptyList(),
    val filteredCards: List<WalletCard> = emptyList(),
    val selectedTab: HomeTab = HomeTab.CARDS,
    val paymentSubFilter: PaymentSubFilter = PaymentSubFilter.ALL,
    val idSubFilter: IdSubFilter = IdSubFilter.ALL,
    val searchQuery: String = "",
    val revealedCardIds: Set<Long> = emptySet(),
    val paymentCardsCount: Int = 0,
    val idCardsCount: Int = 0,
    val expiringCardsCount: Int = 0,
    val securitySettings: SecuritySettings = SecuritySettings(),
    val copyFeedbackMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WalletRepository
    private val securityPreferences: SecurityPreferences = SecurityPreferences(application)

    private val _selectedTab = MutableStateFlow(HomeTab.CARDS)
    private val _paymentSubFilter = MutableStateFlow(PaymentSubFilter.ALL)
    private val _idSubFilter = MutableStateFlow(IdSubFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _revealedCardIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _copyFeedbackMessage = MutableStateFlow<String?>(null)

    init {
        val db = WalletDatabase.getDatabase(application)
        repository = WalletRepository(db.walletDao())
    }

    // Combine 5 flows for filter calculation
    private val _filterResultFlow = combine(
        repository.allCards,
        _selectedTab,
        _paymentSubFilter,
        _idSubFilter,
        _searchQuery
    ) { cards, tab, paymentFilter, idFilter, query ->
        val paymentCards = cards.filter { it.cardType == CardType.CREDIT_CARD || it.cardType == CardType.DEBIT_CARD }
        val idCards = cards.filter { it.cardType == CardType.ID_CARD }

        val activeList = if (tab == HomeTab.CARDS) paymentCards else idCards

        val filtered = activeList.filter { card ->
            val matchesSubFilter = if (tab == HomeTab.CARDS) {
                when (paymentFilter) {
                    PaymentSubFilter.ALL -> true
                    PaymentSubFilter.CREDIT -> card.cardType == CardType.CREDIT_CARD
                    PaymentSubFilter.DEBIT -> card.cardType == CardType.DEBIT_CARD
                    PaymentSubFilter.FAVORITES -> card.isFavorite
                    PaymentSubFilter.EXPIRING -> card.isExpired
                }
            } else {
                when (idFilter) {
                    IdSubFilter.ALL -> true
                    IdSubFilter.AADHAAR -> card.idCardType == IdCardType.AADHAAR
                    IdSubFilter.PAN -> card.idCardType == IdCardType.PAN
                    IdSubFilter.DRIVING_LICENSE -> card.idCardType == IdCardType.DRIVING_LICENSE
                    IdSubFilter.VOTER_ID -> card.idCardType == IdCardType.VOTER_ID
                    IdSubFilter.PASSPORT -> card.idCardType == IdCardType.PASSPORT
                    IdSubFilter.VEHICLE_RC -> card.idCardType == IdCardType.VEHICLE_RC
                    IdSubFilter.ABHA_HEALTH -> card.idCardType == IdCardType.ABHA_HEALTH
                    IdSubFilter.RATION_CARD -> card.idCardType == IdCardType.RATION_CARD
                    IdSubFilter.FAVORITES -> card.isFavorite
                }
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                card.title.lowercase().contains(q) ||
                        card.cardholderName.lowercase().contains(q) ||
                        card.bankOrIssuer.lowercase().contains(q) ||
                        card.lastFourDigits.contains(q) ||
                        card.idNumber.lowercase().contains(q) ||
                        card.notes.lowercase().contains(q) ||
                        card.tags.any { it.lowercase().contains(q) } ||
                        card.customFields.any {
                            it.label.lowercase().contains(q) || it.value.lowercase().contains(q)
                        }
            }

            matchesSubFilter && matchesQuery
        }

        val expiringCount = paymentCards.count { it.isExpired }

        FilterResult(
            allCards = cards,
            filteredCards = filtered,
            selectedTab = tab,
            paymentSubFilter = paymentFilter,
            idSubFilter = idFilter,
            searchQuery = query,
            paymentCardsCount = paymentCards.size,
            idCardsCount = idCards.size,
            expiringCardsCount = expiringCount
        )
    }.flowOn(Dispatchers.Default)

    // Combine 4 flows for UI State
    val uiState: StateFlow<HomeUiState> = combine(
        _filterResultFlow,
        _revealedCardIds,
        securityPreferences.settingsFlow,
        _copyFeedbackMessage
    ) { filterResult, revealedIds, settings, feedback ->
        HomeUiState(
            cards = filterResult.allCards,
            filteredCards = filterResult.filteredCards,
            selectedTab = filterResult.selectedTab,
            paymentSubFilter = filterResult.paymentSubFilter,
            idSubFilter = filterResult.idSubFilter,
            searchQuery = filterResult.searchQuery,
            revealedCardIds = revealedIds,
            paymentCardsCount = filterResult.paymentCardsCount,
            idCardsCount = filterResult.idCardsCount,
            expiringCardsCount = filterResult.expiringCardsCount,
            securitySettings = settings,
            copyFeedbackMessage = feedback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    fun setPaymentSubFilter(filter: PaymentSubFilter) {
        _paymentSubFilter.value = filter
    }

    fun setIdSubFilter(filter: IdSubFilter) {
        _idSubFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleRevealCard(cardId: Long) {
        val current = _revealedCardIds.value
        if (current.contains(cardId)) {
            _revealedCardIds.value = current - cardId
        } else {
            _revealedCardIds.value = current + cardId
        }
    }

    fun toggleFavorite(card: WalletCard) {
        viewModelScope.launch {
            repository.toggleFavorite(card.id, card.isFavorite)
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
        }
    }

    fun copyToClipboard(label: String, value: String) {
        if (value.isBlank()) return
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        clipboard.setPrimaryClip(clip)
        _copyFeedbackMessage.value = "Copied $label to clipboard"
    }

    fun clearFeedbackMessage() {
        _copyFeedbackMessage.value = null
    }
}
