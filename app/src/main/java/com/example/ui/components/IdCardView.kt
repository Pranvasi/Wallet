package com.example.ui.components

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardThemes
import com.example.data.model.IdCardType
import com.example.data.model.WalletCard

@Composable
fun InteractiveIdCardView(
    card: WalletCard,
    modifier: Modifier = Modifier,
    isRevealed: Boolean = true,
    isFlipped: Boolean? = null,
    onToggleReveal: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val preset = remember(card.themePresetId) { CardThemes.getPreset(card.themePresetId) }
    val cardBrush = remember(preset) { preset.createBrush() }
    val textColor = Color(preset.textColor)
    val accentColor = Color(preset.accentColor)
    val customMap = remember(card.customFields) { card.customFields.associate { it.label to it.value } }

    var internalFlipped by remember { mutableStateOf(false) }
    val effectiveFlipped = isFlipped ?: internalFlipped
    val density = LocalDensity.current.density

    val rotation = if (isFlipped != null) {
        if (effectiveFlipped) 180f else 0f
    } else {
        animateFloatAsState(
            targetValue = if (effectiveFlipped) 180f else 0f,
            animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
            label = "id_card_flip"
        ).value
    }
    val flipScale = if (isFlipped != null) {
        1.0f
    } else {
        animateFloatAsState(
            targetValue = if (rotation in 45f..135f) 0.95f else 1.0f,
            animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
            label = "id_card_flip_scale"
        ).value
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .graphicsLayer {
                rotationY = rotation
                scaleX = flipScale
                scaleY = flipScale
                cameraDistance = 14f * density
            }
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBrush)
            .clickable {
                if (onClick != null) {
                    onClick()
                } else if (isFlipped == null) {
                    internalFlipped = !internalFlipped
                }
            }
            .testTag("interactive_id_card")
    ) {
        // Subtle background emblem/watermark
        Box(
            modifier = Modifier
                .size(170.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    translationX = 40.dp.value
                    translationY = 30.dp.value
                    alpha = 0.07f
                }
                .background(textColor, CircleShape)
        )

        if (rotation <= 90f) {
            // FRONT SIDE
            when (card.idCardType) {
                IdCardType.AADHAAR -> AadhaarFront(
                    card = card,
                    customMap = customMap,
                    textColor = textColor,
                    accentColor = accentColor,
                    isRevealed = isRevealed,
                    onToggleReveal = onToggleReveal,
                    onToggleFavorite = onToggleFavorite,
                    onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                )
                IdCardType.VOTER_ID -> VoterFront(
                    card = card,
                    customMap = customMap,
                    textColor = textColor,
                    accentColor = accentColor,
                    isRevealed = isRevealed,
                    onToggleReveal = onToggleReveal,
                    onToggleFavorite = onToggleFavorite,
                    onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                )
                IdCardType.PAN -> PanFront(
                    card = card,
                    customMap = customMap,
                    textColor = textColor,
                    accentColor = accentColor,
                    isRevealed = isRevealed,
                    onToggleReveal = onToggleReveal,
                    onToggleFavorite = onToggleFavorite,
                    onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                )
                IdCardType.RATION_CARD -> RationFront(
                    card = card,
                    customMap = customMap,
                    textColor = textColor,
                    accentColor = accentColor,
                    isRevealed = isRevealed,
                    onToggleReveal = onToggleReveal,
                    onToggleFavorite = onToggleFavorite,
                    onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                )
                else -> GenericIdFront(
                    card = card,
                    customMap = customMap,
                    textColor = textColor,
                    accentColor = accentColor,
                    isRevealed = isRevealed,
                    onToggleReveal = onToggleReveal,
                    onToggleFavorite = onToggleFavorite,
                    onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                )
            }
        } else {
            // BACK SIDE (Reversed 180 deg)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                when (card.idCardType) {
                    IdCardType.AADHAAR -> AadhaarBack(
                        card = card,
                        customMap = customMap,
                        textColor = textColor,
                        accentColor = accentColor,
                        onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                    )
                    IdCardType.VOTER_ID -> VoterBack(
                        card = card,
                        customMap = customMap,
                        textColor = textColor,
                        accentColor = accentColor,
                        onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                    )
                    IdCardType.PAN -> PanBack(
                        card = card,
                        customMap = customMap,
                        textColor = textColor,
                        accentColor = accentColor,
                        onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                    )
                    IdCardType.RATION_CARD -> RationBack(
                        card = card,
                        customMap = customMap,
                        textColor = textColor,
                        accentColor = accentColor,
                        onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                    )
                    else -> GenericIdBack(
                        card = card,
                        customMap = customMap,
                        textColor = textColor,
                        accentColor = accentColor,
                        onFlip = { if (isFlipped == null) internalFlipped = !internalFlipped }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// AADHAAR CARD (Front & Back)
// -----------------------------------------------------------------------------------------

@Composable
private fun AadhaarFront(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    isRevealed: Boolean,
    onToggleReveal: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)?,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "Emblem",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "भारत सरकार",
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GOVERNMENT OF INDIA",
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp).testTag("fav_button")) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFFF5252) else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_back")) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip to Back",
                        tint = textColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Photo + Details (Name, DOB, Gender)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 62.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(textColor.copy(alpha = 0.12f))
                    .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Photo",
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.cardholderName.ifBlank { card.title },
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (card.dateOfBirth.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "जन्म तारीख / DOB: ${card.dateOfBirth}",
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Front Gender
                val gender = customMap["Gender"] ?: "Male"
                val hindiGender = when (gender.lowercase()) {
                    "male" -> "पुरुष"
                    "female" -> "महिला"
                    else -> "अन्य"
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$hindiGender / $gender",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Number (Masked / Revealed) + Slogan
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val num = card.idNumber.filter { it.isDigit() }
                val display = if (num.isNotBlank()) {
                    num.chunked(4).joinToString(" ")
                } else if (card.idNumber.isNotBlank()) {
                    card.idNumber
                } else {
                    ""
                }

                Text(
                    text = display,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "मेरा आधार, मेरी पहचान",
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "FRONT",
                    color = textColor.copy(alpha = 0.45f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AadhaarBack(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "भारतीय विशिष्ट पहचान प्राधिकरण",
                    color = textColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unique Identification Authority of India",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_front")) {
                Icon(
                    imageVector = Icons.Outlined.FlipCameraAndroid,
                    contentDescription = "Flip to Front",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Back: Address & Mobile + QR Code
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "पता / ADDRESS:",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = card.address.ifBlank { "Address not provided" },
                    color = textColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val mobile = customMap["Mobile Number"] ?: ""
                Text(
                    text = "मोबाइल / MOBILE:",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = mobile.ifBlank { "—" },
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Simulated Secure QR Code
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(textColor.copy(alpha = 0.12f))
                    .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = "Secure QR",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Help: 1947 | uidai.gov.in",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "BACK",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// VOTER CARD (Front & Back)
// -----------------------------------------------------------------------------------------

@Composable
private fun VoterFront(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    isRevealed: Boolean,
    onToggleReveal: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)?,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header: Commission + EPIC No
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "भारत निर्वाचन आयोग",
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ELECTION COMMISSION OF INDIA",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp).testTag("fav_button")) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFFF5252) else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_back")) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip to Back",
                        tint = textColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Photo + Elector Name + Relation Name (Both Langs) + Gender
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(textColor.copy(alpha = 0.12f))
                    .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Photo",
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name (Both Langs)
                val regName = customMap["Name (Regional)"]
                val nameDisplay = if (!regName.isNullOrBlank()) "${card.cardholderName} / $regName" else card.cardholderName
                Text(
                    text = nameDisplay.ifBlank { card.title },
                    color = textColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Relation Type & Name (Both Languages)
                val relType = customMap["Relation Type"] ?: "Father"
                val relEn = customMap["${relType}'s Name (English)"]
                    ?: customMap["Father / Husband / Guardian Name (English)"]
                    ?: customMap["Relation Name (English)"]
                    ?: ""
                val relReg = customMap["${relType}'s Name (Regional)"]
                    ?: customMap["Father / Husband / Guardian Name (Regional)"]
                    ?: customMap["Relation Name (Regional)"]
                    ?: ""
                val relCombined = listOf(relEn, relReg).filter { it.isNotBlank() }.joinToString(" / ")
                if (relCombined.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "${relType.uppercase()}: $relCombined",
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Front Gender
                val gender = customMap["Gender"] ?: "Male"
                Spacer(modifier = Modifier.height(1.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GENDER: $gender",
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (card.dateOfBirth.isNotBlank()) {
                        Text(
                            text = "DOB: ${card.dateOfBirth}",
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 9.5.sp
                        )
                    } else if (!customMap["Age"].isNullOrBlank()) {
                        Text(
                            text = "AGE: ${customMap["Age"]}",
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
        }

        // EPIC No & Reveal Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val epic = card.idNumber
            val epicDisplay = epic

            Column {
                Text(
                    text = "EPIC NUMBER",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = epicDisplay.ifBlank { "" },
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "FRONT",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VoterBack(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ELECTOR DETAILS / निर्वाचक विवरण",
                    color = textColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ELECTION COMMISSION OF INDIA",
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_front")) {
                Icon(
                    imageVector = Icons.Outlined.FlipCameraAndroid,
                    contentDescription = "Flip to Front",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Back: Address (Eng & Reg) + All other details entered
        Column(modifier = Modifier.fillMaxWidth()) {
            val addrEn = card.address
            val addrReg = customMap["Address (Regional)"] ?: ""
            val fullAddr = listOf(addrEn, addrReg).filter { it.isNotBlank() }.joinToString(" / ")

            Text(
                text = "पता / ADDRESS:",
                color = textColor.copy(alpha = 0.65f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = fullAddr.ifBlank { "Address not provided" },
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Other details: AC, Part, Serial, Polling Station
            val acNo = customMap["Assembly Constituency No."] ?: "—"
            val acName = customMap["Assembly Constituency Name"] ?: "—"
            val partNo = customMap["Part No."] ?: "—"
            val partName = customMap["Part Name"] ?: "—"
            val serial = customMap["Serial Number"] ?: "—"
            val station = customMap["Polling Station"] ?: "—"

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "AC: $acNo - $acName",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Part: $partNo - $partName",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Serial: $serial",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Station: $station",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Official Identity Card",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "BACK",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// PAN CARD (Front & Back)
// -----------------------------------------------------------------------------------------

@Composable
private fun PanFront(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    isRevealed: Boolean,
    onToggleReveal: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)?,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "आयकर विभाग",
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "INCOME TAX DEPARTMENT",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp).testTag("fav_button")) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFFF5252) else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_back")) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip to Back",
                        tint = textColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Photo + Name + Father's Name + DOB
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 62.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(textColor.copy(alpha = 0.12f))
                    .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Photo",
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NAME",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = card.cardholderName.ifBlank { card.title }.uppercase(),
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Father's Name (Explicitly requested by user)
                val fatherName = customMap["Father's Name"] ?: ""
                if (fatherName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "FATHER'S NAME",
                        color = textColor.copy(alpha = 0.65f),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = fatherName.uppercase(),
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (card.dateOfBirth.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "DOB: ${card.dateOfBirth}",
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // PAN Number
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pan = card.idNumber.uppercase()
            val panDisplay = pan

            Column {
                Text(
                    text = "PERMANENT ACCOUNT NUMBER (PAN)",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = panDisplay.ifBlank { "" },
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "GOVT. OF INDIA",
                color = accentColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PanBack(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INCOME TAX DEPARTMENT",
                    color = textColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Government of India",
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_front")) {
                Icon(
                    imageVector = Icons.Outlined.FlipCameraAndroid,
                    contentDescription = "Flip to Front",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "1. This card is valid throughout India.",
                color = textColor.copy(alpha = 0.8f),
                fontSize = 8.5.sp,
                lineHeight = 12.sp
            )
            Text(
                text = "2. Having more than one PAN is illegal under IT Act.",
                color = textColor.copy(alpha = 0.8f),
                fontSize = 8.5.sp,
                lineHeight = 12.sp
            )
            if (card.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Address: ${card.address}",
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 8.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Helpline: 1800 180 1961 | incometax.gov.in",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "BACK",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// RATION CARD (Front & Back)
// -----------------------------------------------------------------------------------------

@Composable
private fun RationFront(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    isRevealed: Boolean,
    onToggleReveal: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)?,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "खाद्य एवं नागरिक आपूर्ति विभाग",
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "FOOD & CIVIL SUPPLIES DEPARTMENT",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val cat = customMap["Category"] ?: ""
                if (cat.isNotBlank()) {
                    Surface(
                        color = textColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = cat,
                            color = textColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp).testTag("fav_button")) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFFF5252) else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_back")) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip to Back",
                        tint = textColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Photo + Cardholder Name + Father/Husband Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(textColor.copy(alpha = 0.12f))
                    .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Photo",
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CARD HOLDER",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = card.cardholderName.ifBlank { card.title },
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Front: Name of Father / Husband
                val relType = customMap["Relation Type"] ?: "Father"
                val fatherName = customMap["Name of $relType"]
                    ?: customMap["Name of Father / Husband"]
                    ?: customMap["Father's Name"]
                    ?: ""
                if (fatherName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "${relType.uppercase()}: $fatherName",
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Head of family / Age
                val head = customMap["Head of the Family"] ?: ""
                val age = customMap["Age"] ?: ""
                if (head.isNotBlank() || age.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    val info = listOfNotNull(
                        if (head.isNotBlank()) "Head: $head" else null,
                        if (age.isNotBlank()) "Age: $age Yrs" else null
                    ).joinToString(" | ")
                    Text(
                        text = info,
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 8.5.sp
                    )
                }
            }
        }

        // Card Number & Reveal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cardNo = card.idNumber
            val cardNoDisplay = cardNo

            Column {
                Text(
                    text = "RATION CARD NO.",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = cardNoDisplay.ifBlank { "" },
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "FRONT",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RationBack(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FAIR PRICE SHOP & RESIDENTIAL DETAILS",
                    color = textColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "National Food Security Act (NFSA)",
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_front")) {
                Icon(
                    imageVector = Icons.Outlined.FlipCameraAndroid,
                    contentDescription = "Flip to Front",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Back: Card Holder Address + Dealer Name + Dealer Address
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Holder Address
            Text(
                text = "CARD HOLDER ADDRESS:",
                color = textColor.copy(alpha = 0.65f),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = card.address.ifBlank { "Address not recorded" },
                color = textColor,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Dealer Name
            val dealerName = customMap["Dealer Name"] ?: "—"
            Text(
                text = "FPS DEALER NAME:",
                color = textColor.copy(alpha = 0.65f),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dealerName,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Dealer Address
            val dealerAddr = customMap["Dealer Address"] ?: "—"
            Text(
                text = "DEALER ADDRESS:",
                color = textColor.copy(alpha = 0.65f),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dealerAddr,
                color = textColor,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PUBLIC DISTRIBUTION SYSTEM",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "BACK",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// GENERIC / OTHER ID CARDS (Front & Back)
// -----------------------------------------------------------------------------------------

@Composable
private fun GenericIdFront(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    isRevealed: Boolean,
    onToggleReveal: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)?,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "Official ID",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = card.bankOrIssuer.ifBlank { card.idCardType?.displayName ?: "IDENTIFICATION CARD" }.uppercase(),
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp).testTag("fav_button")) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFFF5252) else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_back")) {
                    Icon(
                        imageVector = Icons.Outlined.FlipCameraAndroid,
                        contentDescription = "Flip to Back",
                        tint = textColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 62.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(textColor.copy(alpha = 0.12f))
                    .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Photo",
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NAME",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = card.cardholderName.ifBlank { card.title }.uppercase(),
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "ID NUMBER",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                val idDisp = card.idNumber
                Text(
                    text = idDisp.ifBlank { "" },
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            if (card.dateOfBirth.isNotBlank()) {
                Text(
                    text = "DOB: ${card.dateOfBirth}",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = "FRONT",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GenericIdBack(
    card: WalletCard,
    customMap: Map<String, String>,
    textColor: Color,
    accentColor: Color,
    onFlip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IDENTIFICATION DETAILS",
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onFlip, modifier = Modifier.size(28.dp).testTag("id_flip_to_front")) {
                Icon(
                    imageVector = Icons.Outlined.FlipCameraAndroid,
                    contentDescription = "Flip to Front",
                    tint = textColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            if (card.address.isNotBlank()) {
                Text(
                    text = "ADDRESS:",
                    color = textColor.copy(alpha = 0.65f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = card.address,
                    color = textColor,
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (card.issueDate.isNotBlank()) {
                Text(
                    text = "ISSUE DATE: ${card.issueDate}",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SECURE IDENTITY",
                color = textColor.copy(alpha = 0.65f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "BACK",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
