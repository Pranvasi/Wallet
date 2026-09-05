package com.example.ui.screens.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WalletDatabase
import com.example.data.model.CardType
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard
import com.example.data.repository.WalletRepository
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import com.example.ui.components.CardDetailSkeletonView
import com.example.ui.components.CardShareBottomSheet
import com.example.ui.components.InteractiveCreditCardView
import com.example.ui.components.InteractiveIdCardView
import com.example.ui.screens.editor.FullscreenPhotoViewerDialog
import com.example.ui.util.CardShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val repository = remember {
        val db = WalletDatabase.getDatabase(context)
        WalletRepository(db.walletDao())
    }

    var card by remember(cardId) { mutableStateOf<WalletCard?>(repository.getCachedCard(cardId)) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cardId) {
        val fetched = repository.getCardById(cardId)
        if (fetched != null) {
            card = fetched
        }
    }

    fun handleShare() {
        if (card != null) {
            showShareSheet = true
        }
    }

    fun handleEdit() {
        if (card != null) {
            onNavigateToEdit(cardId)
        }
    }

    fun handleDelete() {
        if (card != null) {
            showDeleteConfirmDialog = true
        }
    }

    fun copyText(label: String, value: String) {
        if (value.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        clipboard.setPrimaryClip(clip)
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Copied $label to clipboard")
        }
    }

    fun deleteCard() {
        coroutineScope.launch {
            repository.deleteCard(cardId)
            onNavigateBack()
        }
    }

    fun toggleFavorite() {
        val current = card ?: return
        coroutineScope.launch {
            repository.toggleFavorite(current.id, current.isFavorite)
            card = current.copy(isFavorite = !current.isFavorite)
        }
    }

    // Fullscreen Photo Modal
    if (previewPhotoUri != null) {
        FullscreenPhotoViewerDialog(
            imageUri = previewPhotoUri!!,
            onDismiss = { previewPhotoUri = null }
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag("card_detail_screen"),
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
                    Text(
                        text = card?.title ?: "Card Details",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { handleShare() },
                        modifier = Modifier.testTag("detail_top_share_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Card")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val currentCard = card

        Crossfade(
            targetState = currentCard,
            animationSpec = tween(durationMillis = 400),
            modifier = Modifier.padding(innerPadding),
            label = "cardDetailTransition"
        ) { targetCard ->
            if (targetCard == null) {
                CardDetailSkeletonView()
            } else {
                val cardItem = targetCard
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card Interactive Visual Representation
                    if (cardItem.cardType == CardType.ID_CARD) {
                        InteractiveIdCardView(
                            card = cardItem,
                            isRevealed = true,
                            onToggleReveal = null,
                            onToggleFavorite = { toggleFavorite() }
                        )
                    } else {
                        InteractiveCreditCardView(
                            card = cardItem,
                            isRevealed = true,
                            onToggleReveal = null,
                            onToggleFavorite = { toggleFavorite() }
                        )
                    }

                    // Attached Photos (Front & Rear)
                    if (cardItem.frontPhotoUri != null || cardItem.backPhotoUri != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "ATTACHED CARD PHOTOS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (cardItem.frontPhotoUri != null) {
                                    CardPhotoPreviewItem(
                                        title = "Front Side",
                                        photoUri = cardItem.frontPhotoUri,
                                        onTap = { previewPhotoUri = cardItem.frontPhotoUri },
                                        modifier = Modifier.weight(1f),
                                        tag = "front"
                                    )
                                }
                                if (cardItem.backPhotoUri != null) {
                                    CardPhotoPreviewItem(
                                        title = "Rear Side",
                                        photoUri = cardItem.backPhotoUri,
                                        onTap = { previewPhotoUri = cardItem.backPhotoUri },
                                        modifier = Modifier.weight(1f),
                                        tag = "back"
                                    )
                                }
                            }
                        }
                    }

                    // Field Details List with One-Tap Copy
                    Text(
                        text = "CARD DATA & COPY SHORTCUTS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // 1. Credit / Debit Specific Fields
                    if (cardItem.cardNumber.isNotBlank()) {
                        DetailCopyItem(
                            label = "Card Number",
                            value = cardItem.cardNumber,
                            rawCopyValue = cardItem.cardNumber,
                            onCopy = { copyText("Card Number", cardItem.cardNumber) }
                        )
                    }

                    if (cardItem.formattedExpiry.isNotBlank()) {
                        DetailCopyItem(
                            label = "Expiration Date",
                            value = cardItem.formattedExpiry,
                            rawCopyValue = cardItem.formattedExpiry,
                            onCopy = { copyText("Expiry Date", cardItem.formattedExpiry) }
                        )
                    }

                    if (cardItem.cvv.isNotBlank()) {
                        DetailCopyItem(
                            label = "CVV / Security Code",
                            value = cardItem.cvv,
                            rawCopyValue = cardItem.cvv,
                            onCopy = { copyText("CVV", cardItem.cvv) }
                        )
                    }

                    if (cardItem.pin.isNotBlank()) {
                        DetailCopyItem(
                            label = "ATM PIN",
                            value = cardItem.pin,
                            rawCopyValue = cardItem.pin,
                            onCopy = { copyText("PIN", cardItem.pin) }
                        )
                    }

                    // 2. ID Number
                    if (cardItem.idNumber.isNotBlank()) {
                        val idLabel = when (cardItem.idCardType) {
                            IdCardType.AADHAAR -> "Aadhaar Card Number"
                            IdCardType.PAN -> "Permanent Account Number"
                            IdCardType.VOTER_ID -> "EPIC Number"
                            IdCardType.RATION_CARD -> "Ration Card Number"
                            else -> cardItem.idCardType?.displayName?.let { "$it Number" } ?: "Document / ID Number"
                        }

                        val formattedIdValue = if (cardItem.idCardType == IdCardType.AADHAAR) {
                            cardItem.idNumber.chunked(4).joinToString(" ")
                        } else {
                            cardItem.idNumber
                        }

                        DetailCopyItem(
                            label = idLabel,
                            value = formattedIdValue,
                            rawCopyValue = cardItem.idNumber,
                            onCopy = { copyText(idLabel, cardItem.idNumber) }
                        )
                    }

                    // 3. Name
                    if (cardItem.cardholderName.isNotBlank()) {
                        val nameLabel = when (cardItem.idCardType) {
                            IdCardType.AADHAAR -> "Full Name"
                            IdCardType.PAN -> "Name"
                            IdCardType.VOTER_ID -> "Name in English"
                            IdCardType.RATION_CARD -> "Name of Card Holder"
                            else -> "Name on Card / Document"
                        }
                        DetailCopyItem(
                            label = nameLabel,
                            value = cardItem.cardholderName,
                            rawCopyValue = cardItem.cardholderName,
                            onCopy = { copyText(nameLabel, cardItem.cardholderName) }
                        )
                    }

                    // 4. Issuing Authority
                    if (cardItem.bankOrIssuer.isNotBlank()) {
                        DetailCopyItem(
                            label = if (cardItem.cardType == CardType.ID_CARD) "Issuing Authority" else "Bank / Issuer",
                            value = cardItem.bankOrIssuer,
                            rawCopyValue = cardItem.bankOrIssuer,
                            onCopy = { copyText("Issuer", cardItem.bankOrIssuer) }
                        )
                    }

                    // 5. Date of Birth
                    if (cardItem.dateOfBirth.isNotBlank()) {
                        DetailCopyItem(
                            label = "Date of Birth",
                            value = cardItem.dateOfBirth,
                            rawCopyValue = cardItem.dateOfBirth,
                            onCopy = { copyText("Date of Birth", cardItem.dateOfBirth) }
                        )
                    }

                    // 6. Issue Date
                    if (cardItem.issueDate.isNotBlank()) {
                        DetailCopyItem(
                            label = "Issue Date",
                            value = cardItem.issueDate,
                            rawCopyValue = cardItem.issueDate,
                            onCopy = { copyText("Issue Date", cardItem.issueDate) }
                        )
                    }

                    // 7. Address
                    if (cardItem.address.isNotBlank()) {
                        val addrLabel = when (cardItem.idCardType) {
                            IdCardType.VOTER_ID -> "Address in English"
                            IdCardType.RATION_CARD -> "Card Holder Address"
                            else -> "Registered Address"
                        }
                        DetailCopyItem(
                            label = addrLabel,
                            value = cardItem.address,
                            rawCopyValue = cardItem.address,
                            onCopy = { copyText(addrLabel, cardItem.address) }
                        )
                    }

                    // 8. Custom / Specific Form Fields
                    if (cardItem.customFields.isNotEmpty()) {
                        cardItem.customFields.forEach { field ->
                            if (field.label.isNotBlank() || field.value.isNotBlank()) {
                                DetailCopyItem(
                                    label = field.label.ifBlank { "Custom Field" },
                                    value = field.value,
                                    rawCopyValue = field.value,
                                    onCopy = { copyText(field.label.ifBlank { "Field" }, field.value) }
                                )
                            }
                        }
                    }

                    // 9. Notes
                    if (cardItem.notes.isNotBlank()) {
                        DetailCopyItem(
                            label = "Notes",
                            value = cardItem.notes,
                            rawCopyValue = cardItem.notes,
                            onCopy = { copyText("Notes", cardItem.notes) }
                        )
                    }

                    // Tags
                    if (cardItem.tags.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                cardItem.tags.forEach { tag ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Action Controls: Edit, Share, Delete
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { handleEdit() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("detail_edit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Card",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        FilledTonalButton(
                            onClick = { handleShare() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("detail_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share Card",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { handleDelete() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("detail_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Card",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Card?") },
            text = { Text("Are you sure you want to permanently delete '${card?.title}' from your wallet?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        deleteCard()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Share Options Bottom Sheet
    if (showShareSheet && card != null) {
        CardShareBottomSheet(
            card = card!!,
            onDismiss = { showShareSheet = false }
        )
    }
}

@Composable
private fun CardPhotoPreviewItem(
    title: String,
    photoUri: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = "photo"
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTap() }
            .testTag("detail_card_photo_$tag")
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "$title Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = "Zoom",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCopyItem(
    label: String,
    value: String,
    rawCopyValue: String,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = if (label.contains("Number") || label.contains("Code") || label.contains("PIN") || label.contains("EPIC") || label.contains("PAN")) FontFamily.Monospace else FontFamily.Default,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier.testTag("copy_${label.replace(" ", "_").replace("(", "").replace(")", "").replace("/", "_")}")
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
