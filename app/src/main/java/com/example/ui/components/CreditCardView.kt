package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardNetwork
import com.example.data.model.CardThemes
import com.example.data.model.WalletCard

@Composable
fun InteractiveCreditCardView(
    card: WalletCard,
    modifier: Modifier = Modifier,
    isRevealed: Boolean = true,
    isFlipped: Boolean? = null,
    onToggleReveal: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    var internalFlipped by remember { mutableStateOf(false) }
    val effectiveFlipped = isFlipped ?: internalFlipped

    val rotation = if (isFlipped != null) {
        if (effectiveFlipped) 180f else 0f
    } else {
        animateFloatAsState(
            targetValue = if (effectiveFlipped) 180f else 0f,
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = 320f
            ),
            label = "card_flip_anim"
        ).value
    }

    val flipScale = if (isFlipped != null) {
        1.0f
    } else {
        animateFloatAsState(
            targetValue = if (rotation > 15f && rotation < 165f) 0.965f else 1.0f,
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = 350f
            ),
            label = "card_flip_scale"
        ).value
    }

    val preset = remember(card.themePresetId) { CardThemes.getPreset(card.themePresetId) }
    val cardBrush = remember(preset) { preset.createBrush() }
    val textColor = Color(preset.textColor)
    val accentColor = Color(preset.accentColor)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // Standard ID-1 / credit card aspect ratio
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .graphicsLayer {
                rotationY = rotation
                scaleX = flipScale
                scaleY = flipScale
                cameraDistance = 14f * density
            }
            .clickable {
                if (onClick != null) {
                    onClick()
                } else if (isFlipped == null) {
                    internalFlipped = !internalFlipped
                }
            }
            .testTag("interactive_credit_card")
    ) {
        if (rotation <= 90f) {
            // Front Side
            CardFrontSide(
                card = card,
                cardBrush = cardBrush,
                textColor = textColor,
                accentColor = accentColor,
                isRevealed = isRevealed,
                onToggleReveal = onToggleReveal,
                onToggleFavorite = onToggleFavorite,
                onFlip = { if (isFlipped == null) internalFlipped = true }
            )
        } else {
            // Back Side (rotate 180 so it appears non-mirrored)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                CardBackSide(
                    card = card,
                    cardBrush = cardBrush,
                    textColor = textColor,
                    isRevealed = isRevealed,
                    onFlip = { if (isFlipped == null) internalFlipped = false }
                )
            }
        }
    }
}

@Composable
private fun CardFrontSide(
    card: WalletCard,
    cardBrush: Brush,
    textColor: Color,
    accentColor: Color,
    isRevealed: Boolean,
    onToggleReveal: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)?,
    onFlip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cardBrush)
            .padding(18.dp)
    ) {
        // Decorative background watermarks
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    translationX = 40.dp.value
                    translationY = 40.dp.value
                    alpha = 0.08f
                }
                .background(textColor, CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Bank Name + Chip/NFC + Network Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = card.bankOrIssuer.ifBlank { card.title },
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = card.cardType.displayName.uppercase(),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(32.dp).testTag("fav_button")
                        ) {
                            Icon(
                                imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (card.isFavorite) Color(0xFFFF5252) else textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    CardNetworkBadge(network = card.cardNetwork, textColor = textColor)
                }
            }

            // EMV Chip & Contactless
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Metallic Chip
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 28.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFE082))
                            )
                        )
                        .border(0.5.dp, Color(0xFFB78103), RoundedCornerShape(5.dp))
                ) {
                    // Chip inner circuit lines
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .border(0.5.dp, Color(0xFFB78103).copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Filled.Nfc,
                    contentDescription = "Contactless",
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Card Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cleanDigits = card.cardNumber.filter { it.isDigit() }
                val isAmex = card.cardNetwork == CardNetwork.AMEX || cleanDigits.length == 15
                val displayNumber = if (cleanDigits.isNotBlank()) {
                    if (isAmex) {
                        val p1 = cleanDigits.take(4)
                        val p2 = if (cleanDigits.length > 4) cleanDigits.substring(4, cleanDigits.length.coerceAtMost(10)) else ""
                        val p3 = if (cleanDigits.length > 10) cleanDigits.substring(10) else ""
                        listOf(p1, p2, p3).filter { it.isNotEmpty() }.joinToString("  ")
                    } else {
                        cleanDigits.chunked(4).joinToString("  ")
                    }
                } else if (card.lastFourDigits.isNotBlank()) {
                    if (isAmex) {
                        "••••  ••••••  ${card.lastFourDigits}"
                    } else {
                        "••••  ••••  ••••  ${card.lastFourDigits}"
                    }
                } else {
                    if (isAmex) "••••  ••••••  •••••" else "••••  ••••  ••••  ••••"
                }

                Text(
                    text = displayNumber,
                    color = textColor,
                    fontSize = if (isAmex) 16.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
            }

            // Bottom Row: Cardholder Name + Expiry + Flip Hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CARDHOLDER",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = card.cardholderName.ifBlank { "VALUED MEMBER" }.uppercase(),
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "EXPIRES",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = card.formattedExpiry.ifBlank { "••/••" },
                        color = if (card.isExpired) Color(0xFFFF5252) else textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onFlip,
                    modifier = Modifier.size(32.dp).testTag("flip_to_back")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip Card",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBackSide(
    card: WalletCard,
    cardBrush: Brush,
    textColor: Color,
    isRevealed: Boolean,
    onFlip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cardBrush)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Magnetic Stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(top = 12.dp)
                    .background(Color(0xFF1A1A1A))
            )

            // Signature Panel & CVV Box
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Signature strip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFECEFF1))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = card.cardholderName.ifBlank { "Authorized Signature" },
                            color = Color(0xFF455A64),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // CVV Box
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (card.cvv.isNotBlank()) card.cvv else "",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (card.pin.isNotBlank()) {
                    Text(
                        text = "ATM PIN: ${card.pin}",
                        color = textColor.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Bottom Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOCAL & PRIVATE",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )

                IconButton(
                    onClick = onFlip,
                    modifier = Modifier.size(32.dp).testTag("flip_to_front")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip Back",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CardNetworkBadge(network: CardNetwork, textColor: Color) {
    when (network) {
        CardNetwork.VISA -> {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "VISA",
                    color = Color(0xFF1A1F71),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
        CardNetwork.MASTERCARD -> {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(5.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(Color(0xFFEB001B), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer { translationX = -12f }
                            .background(Color(0xFFF79E1B).copy(alpha = 0.95f), CircleShape)
                    )
                }
            }
        }
        CardNetwork.RUPAY -> {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Ru",
                        color = Color(0xFF0072BB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Pay",
                        color = Color(0xFFF37021),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 9.dp)
                            .background(Color(0xFF00A651), RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        CardNetwork.AMEX -> {
            Surface(
                color = Color(0xFF006FCF),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AMERICAN",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "EXPRESS",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        CardNetwork.DISCOVER -> {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DISC",
                        color = Color(0xFF111111),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(Color(0xFFFF6000), CircleShape)
                    )
                    Text(
                        text = "VER",
                        color = Color(0xFF111111),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        else -> {
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text(
                    text = network.displayName.uppercase(),
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
