package com.example.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardType
import com.example.data.model.WalletCard
import com.example.ui.util.CardShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardShareBottomSheet(
    card: WalletCard,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isPreAuthenticated: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val frontGraphicsLayer = rememberGraphicsLayer()
    val backGraphicsLayer = rememberGraphicsLayer()
    var isSharing by remember { mutableStateOf(false) }

    val hasPhotos = remember(card.frontPhotoUri, card.backPhotoUri) {
        CardShareHelper.hasCardPhotos(card)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 6.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier.testTag("card_share_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Share Card",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = card.title.ifBlank { "Card Details" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .testTag("button_close_share_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Card Visual Preview (Front & Back rendered and recorded for two-sided image export)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "CARD PREVIEW (FRONT & BACK)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    // Front Side Preview
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Front Side",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawWithContent {
                                    frontGraphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawLayer(frontGraphicsLayer)
                                }
                                .testTag("share_card_preview_box")
                        ) {
                            if (card.cardType == CardType.ID_CARD) {
                                InteractiveIdCardView(
                                    card = card,
                                    isRevealed = true,
                                    isFlipped = false
                                )
                            } else {
                                InteractiveCreditCardView(
                                    card = card,
                                    isRevealed = true,
                                    isFlipped = false
                                )
                            }
                        }
                    }

                    // Back Side Preview
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Back Side",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawWithContent {
                                    backGraphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawLayer(backGraphicsLayer)
                                }
                                .testTag("share_card_back_preview_box")
                        ) {
                            if (card.cardType == CardType.ID_CARD) {
                                InteractiveIdCardView(
                                    card = card,
                                    isRevealed = true,
                                    isFlipped = true
                                )
                            } else {
                                InteractiveCreditCardView(
                                    card = card,
                                    isRevealed = true,
                                    isFlipped = true
                                )
                            }
                        }
                    }
                }
            }

            // Section Label
            Text(
                text = "CHOOSE SHARE METHOD",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // Option 1: Share card details as plain text
            ShareOptionItem(
                icon = Icons.AutoMirrored.Filled.Article,
                title = "Share card details as plain text",
                subtitle = "Format card attributes, numbers, and notes into clean text",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                testTag = "share_option_plain_text",
                onClick = {
                    if (isSharing) return@ShareOptionItem
                    isSharing = true
                    onDismiss()
                    CardShareHelper.sharePlaintext(context, card)
                }
            )

            // Option 2: Share card preview as image (front & back at once as two images)
            ShareOptionItem(
                icon = Icons.Filled.Image,
                title = "Share card preview as image",
                subtitle = "Share both front and back side at once (as two images)",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                testTag = "share_option_preview_image",
                onClick = {
                    if (isSharing) return@ShareOptionItem
                    isSharing = true
                    coroutineScope.launch {
                        try {
                            val frontBitmap = frontGraphicsLayer.toImageBitmap().asAndroidBitmap()
                            val backBitmap = backGraphicsLayer.toImageBitmap().asAndroidBitmap()
                            isSharing = false
                            onDismiss()
                            CardShareHelper.sharePreviewImages(context, card, frontBitmap, backBitmap)
                        } catch (e: Exception) {
                            isSharing = false
                            Toast.makeText(context, "Unable to capture card preview: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            // Option 3: Share card photos (if attachment photos exist)
            if (hasPhotos) {
                val photosSubtitle = when {
                    !card.frontPhotoUri.isNullOrBlank() && !card.backPhotoUri.isNullOrBlank() ->
                        "Share original attached front and rear card photos"
                    !card.frontPhotoUri.isNullOrBlank() ->
                        "Share original attached front card photo"
                    else ->
                        "Share original attached rear card photo"
                }

                ShareOptionItem(
                    icon = Icons.Filled.PhotoLibrary,
                    title = "Share card photos",
                    subtitle = photosSubtitle,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    testTag = "share_option_card_photos",
                    onClick = {
                        if (isSharing) return@ShareOptionItem
                        isSharing = true
                        onDismiss()
                        CardShareHelper.shareCardPhotos(context, card)
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
