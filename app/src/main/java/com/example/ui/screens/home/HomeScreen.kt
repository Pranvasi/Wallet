package com.example.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.model.CardType
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import com.example.ui.components.CardShareBottomSheet
import com.example.ui.components.InteractiveCreditCardView
import com.example.ui.components.InteractiveIdCardView
import com.example.ui.util.CardShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddCard: (CardType, IdCardType?) -> Unit,
    onNavigateToCardDetail: (Long) -> Unit,
    onNavigateToBackup: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onLockApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddTypeSheet by remember { mutableStateOf(false) }
    var cardToShare by remember { mutableStateOf<WalletCard?>(null) }

    LaunchedEffect(uiState.copyFeedbackMessage) {
        uiState.copyFeedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearFeedbackMessage()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag("home_screen"),
        topBar = {
            LargeTopAppBar(
                modifier = Modifier.draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val newOffset = (scrollBehavior.state.heightOffset + delta)
                            .coerceIn(scrollBehavior.state.heightOffsetLimit, 0f)
                        scrollBehavior.state.heightOffset = newOffset
                    },
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            val target = if (velocity < -200f || (velocity <= 200f && scrollBehavior.state.collapsedFraction > 0.5f)) {
                                scrollBehavior.state.heightOffsetLimit
                            } else {
                                0f
                            }
                            Animatable(scrollBehavior.state.heightOffset).animateTo(
                                targetValue = target,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) {
                                scrollBehavior.state.heightOffset = this.value
                            }
                        }
                    }
                ),
                title = {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    Column {
                        Text(
                            text = "Wallet",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitleAlpha = (1f - collapsedFraction * 2.5f).coerceIn(0f, 1f)
                        if (subtitleAlpha > 0.01f) {
                            Text(
                                text = if (uiState.cards.isEmpty()) "No items stored" else "${uiState.cards.size} items stored locally",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = subtitleAlpha),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }

                    IconButton(
                        onClick = onLockApp,
                        modifier = Modifier.testTag("action_lock")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Lock Wallet",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("home_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == HomeTab.CARDS,
                    onClick = { viewModel.selectTab(HomeTab.CARDS) },
                    icon = {
                        if (uiState.paymentCardsCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(uiState.paymentCardsCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (uiState.selectedTab == HomeTab.CARDS) Icons.Filled.CreditCard else Icons.Outlined.CreditCard,
                                    contentDescription = "Cards"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (uiState.selectedTab == HomeTab.CARDS) Icons.Filled.CreditCard else Icons.Outlined.CreditCard,
                                contentDescription = "Cards"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Cards",
                            fontWeight = if (uiState.selectedTab == HomeTab.CARDS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_payment_cards")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == HomeTab.IDS,
                    onClick = { viewModel.selectTab(HomeTab.IDS) },
                    icon = {
                        if (uiState.idCardsCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(uiState.idCardsCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (uiState.selectedTab == HomeTab.IDS) Icons.Filled.Badge else Icons.Outlined.Badge,
                                    contentDescription = "IDs"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (uiState.selectedTab == HomeTab.IDS) Icons.Filled.Badge else Icons.Outlined.Badge,
                                contentDescription = "IDs"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "IDs",
                            fontWeight = if (uiState.selectedTab == HomeTab.IDS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_id_cards")
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTypeSheet = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Card") },
                text = { Text("Add Item", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_card")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // PROPER SEARCHBAR (Requirement #4)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            if (uiState.selectedTab == HomeTab.CARDS)
                                "Search Cards"
                            else
                                "Search IDs"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar")
                )
            }

            // Category Sub-Filter Chips with smooth transition
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(
                                initialOffsetX = { if (targetState == HomeTab.CARDS) -it / 4 else it / 4 },
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                    slideOutHorizontally(
                                        targetOffsetX = { if (targetState == HomeTab.CARDS) it / 4 else -it / 4 },
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    )
                        )
                },
                label = "filter_chips_transition"
            ) { activeTab ->
                if (activeTab == HomeTab.CARDS) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PaymentSubFilter.entries) { filter ->
                            val isSelected = uiState.paymentSubFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setPaymentSubFilter(filter) },
                                label = { Text(filter.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("filter_pay_${filter.name}")
                            )
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(IdSubFilter.entries) { filter ->
                            val isSelected = uiState.idSubFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setIdSubFilter(filter) },
                                label = { Text(filter.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("filter_id_${filter.name}")
                            )
                        }
                    }
                }
            }

            // Expiration warning banner for payment cards
            AnimatedVisibility(
                visible = uiState.selectedTab == HomeTab.CARDS && uiState.expiringCardsCount > 0,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${uiState.expiringCardsCount} payment card(s) have expired or need renewal.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Card List / Empty State
            if (uiState.filteredCards.isEmpty()) {
                val emptyScrollState = rememberScrollState()
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(emptyScrollState)
                        .testTag("home_empty_scroll_container")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = maxHeight + 160.dp)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.selectedTab == HomeTab.CARDS) Icons.Outlined.CreditCard else Icons.Filled.Badge,
                                contentDescription = "No cards",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) {
                                "No results for \"${uiState.searchQuery}\""
                            } else if (uiState.selectedTab == HomeTab.CARDS) {
                                "No Debit or Credit Cards"
                            } else {
                                "No Identity Cards Stored"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) {
                                "Try searching by cardholder name, issuer, last digits, or custom fields."
                            } else if (uiState.selectedTab == HomeTab.CARDS) {
                                "Securely store your Visa, Mastercard, RuPay credit and debit cards offline."
                            } else {
                                "Save Indian identity documents like Aadhaar, PAN, Driving Licence, Voter ID, Passport, RC, and ABHA cards."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TextButton(
                            onClick = { showAddTypeSheet = true },
                            modifier = Modifier.testTag("empty_state_add_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (uiState.selectedTab == HomeTab.CARDS) "Add Credit or Debit Card"
                                else "Add Identity Card",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredCards, key = { it.id }) { card ->
                        Column(
                            modifier = Modifier.animateItem()
                        ) {
                            when (card.cardType) {
                                CardType.ID_CARD -> {
                                    InteractiveIdCardView(
                                        card = card,
                                        isRevealed = true,
                                        onToggleReveal = null,
                                        onToggleFavorite = { viewModel.toggleFavorite(card) },
                                        onClick = { onNavigateToCardDetail(card.id) }
                                    )
                                }
                                else -> {
                                    InteractiveCreditCardView(
                                        card = card,
                                        isRevealed = true,
                                        onToggleReveal = null,
                                        onToggleFavorite = { viewModel.toggleFavorite(card) },
                                        onClick = { onNavigateToCardDetail(card.id) }
                                    )
                                }
                            }

                            // Quick Action Bar beneath each card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = card.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )

                                    if (card.tags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = card.tags.first(),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { cardToShare = card },
                                        modifier = Modifier.testTag("share_card_${card.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Share", fontSize = 11.sp)
                                    }

                                    if (card.cardNumber.isNotBlank()) {
                                        TextButton(
                                            onClick = { viewModel.copyToClipboard("Card Number", card.cardNumber) },
                                            modifier = Modifier.testTag("copy_card_${card.id}")
                                        ) {
                                            Text("Copy No", fontSize = 11.sp)
                                        }
                                    } else if (card.idNumber.isNotBlank()) {
                                        TextButton(
                                            onClick = { viewModel.copyToClipboard("ID Number", card.idNumber) },
                                            modifier = Modifier.testTag("copy_id_${card.id}")
                                        ) {
                                            Text("Copy ID", fontSize = 11.sp)
                                        }
                                    }

                                    if (card.cvv.isNotBlank()) {
                                        TextButton(
                                            onClick = { viewModel.copyToClipboard("CVV", card.cvv) }
                                        ) {
                                            Text("Copy CVV", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Spacer at bottom ensures scroll gesture always has leeway to toggle topbar
                    item {
                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }
    }

    // Modal Sheet to select Card / Indian ID to add (filtered by selected tab)
    if (showAddTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTypeSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.selectedTab == HomeTab.CARDS) {
                    item {
                        Text(
                            text = "Add Payment Card",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select payment card type to add",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Credit Card",
                            subtitle = "Visa, Mastercard, RuPay, Amex & Credit Limits",
                            icon = Icons.Outlined.CreditCard,
                            iconBg = Color(0xFF1E5BB8),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.CREDIT_CARD, null)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Debit / ATM Card",
                            subtitle = "Bank debit cards with ATM PIN & CVV",
                            icon = Icons.Filled.Payment,
                            iconBg = Color(0xFF006C47),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.DEBIT_CARD, null)
                            }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Add Identity Document",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select identity document category to add",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Aadhaar Card (UIDAI)",
                            subtitle = "12-digit Unique Identification & Virtual ID (VID)",
                            icon = Icons.Filled.Badge,
                            iconBg = Color(0xFFE65100),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.AADHAAR)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "PAN Card (Income Tax Dept)",
                            subtitle = "Permanent Account Number for banking & taxes",
                            icon = Icons.Filled.Badge,
                            iconBg = Color(0xFF1565C0),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.PAN)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Voter ID / EPIC Card",
                            subtitle = "Election Commission Voter ID & Assembly info",
                            icon = Icons.Filled.HowToVote,
                            iconBg = Color(0xFF6A1B9A),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.VOTER_ID)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Ration Card (Food & Supplies)",
                            subtitle = "AAY, PHH, SPHH, RKSY category & family card details",
                            icon = Icons.Filled.Badge,
                            iconBg = Color(0xFFD84315),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.RATION_CARD)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Driving Licence (MoRTH / RTO)",
                            subtitle = "DL Number, Vehicle Class, Expiry & Blood Group",
                            icon = Icons.Filled.DirectionsCar,
                            iconBg = Color(0xFF2E7D32),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.DRIVING_LICENSE)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Indian Passport",
                            subtitle = "Passport Number, Ministry of External Affairs",
                            icon = Icons.Filled.Badge,
                            iconBg = Color(0xFF004D40),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.PASSPORT)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Vehicle RC (Registration Certificate)",
                            subtitle = "Vehicle Registration Smart Card & Chassis info",
                            icon = Icons.Filled.DirectionsCar,
                            iconBg = Color(0xFF455A64),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.VEHICLE_RC)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "ABHA Health Card (Ayushman Bharat)",
                            subtitle = "14-digit ABHA Number & National Health Authority",
                            icon = Icons.Filled.LocalHospital,
                            iconBg = Color(0xFF00838F),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.ABHA_HEALTH)
                            }
                        )
                    }

                    item {
                        CardTypeOptionRow(
                            title = "Other Identity Document",
                            subtitle = "Custom document or other government identification",
                            icon = Icons.Filled.Badge,
                            iconBg = Color(0xFF5D4037),
                            onClick = {
                                showAddTypeSheet = false
                                onNavigateToAddCard(CardType.ID_CARD, IdCardType.OTHER)
                            }
                        )
                    }
                }
            }
        }
    }

    // Share Options Bottom Sheet
    cardToShare?.let { card ->
        CardShareBottomSheet(
            card = card,
            onDismiss = { cardToShare = null }
        )
    }
}

@Composable
private fun CardTypeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().testTag("add_type_${title.replace(" ", "_").replace("/", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
