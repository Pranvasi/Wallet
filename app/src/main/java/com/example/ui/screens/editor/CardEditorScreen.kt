package com.example.ui.screens.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.example.ui.components.MaterialYouSwitch
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.data.model.CardNetwork
import com.example.data.model.CardType
import com.example.data.model.IdCardType
import com.example.ui.components.CardColorPicker
import com.example.ui.components.CardEditorSkeletonView
import com.example.ui.components.InteractiveCreditCardView
import com.example.ui.components.InteractiveIdCardView
import com.example.ui.util.CardImageHelper
import com.example.ui.util.CardNumberVisualTransformation
import com.example.ui.util.CardValidationUtils
import com.example.ui.util.DateVisualTransformation
import com.example.ui.util.ExpiryDateVisualTransformation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val EditorFieldShape = RoundedCornerShape(16.dp)

@Composable
private fun editorTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)

@Composable
private fun EditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: androidx.compose.ui.graphics.Shape = EditorFieldShape,
    colors: androidx.compose.material3.TextFieldColors = editorTextFieldColors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors
    )
}

@Composable
private fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = { Text("DD/MM/YYYY") },
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    onOpenDatePicker: (() -> Unit)? = null,
    datePickerDescription: String = "Pick Date",
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    ),
    testTag: String = ""
) {
    // Extract raw digits (max 8) so Compose's underlying text buffer
    // only holds the unformatted digits. This ensures that typing, backspacing,
    // and middle-cursor editing never cause cursor jumping or jitter.
    val rawDigits = remember(value) { value.filter { it.isDigit() }.take(8) }

    EditorTextField(
        value = rawDigits,
        onValueChange = { newText ->
            val digits = newText.filter { it.isDigit() }.take(8)
            val resolvedDigits = if (newText.endsWith('/') || newText.endsWith('-') || newText.endsWith('.')) {
                when (digits.length) {
                    1 -> "0$digits"
                    3 -> "${digits.take(2)}0${digits.takeLast(1)}"
                    else -> digits
                }
            } else {
                digits
            }
            onValueChange(CardValidationUtils.formatFullDate(resolvedDigits))
        },
        label = label,
        placeholder = placeholder,
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = DateVisualTransformation(),
        keyboardOptions = keyboardOptions,
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (rawDigits.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (onOpenDatePicker != null) {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = datePickerDescription,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        modifier = if (testTag.isNotBlank()) modifier.testTag(testTag) else modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(
    viewModel: CardEditorViewModel,
    cardId: Long,
    initialType: CardType?,
    initialIdType: IdCardType? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var activeDatePickerTarget by remember { mutableStateOf<String?>(null) } // "aadhaar_dob", "aadhaar_issue", "pan_dob", "voter_dob", "generic_issue", "generic_dob"
    var previewImageUri by remember { mutableStateOf<String?>(null) }

    remember(cardId, initialType, initialIdType) {
        viewModel.initialize(cardId, initialType, initialIdType)
        true
    }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onNavigateBack()
        }
    }

    // Calendar DatePicker Modal
    if (activeDatePickerTarget != null) {
        AppDatePickerDialog(
            onDateSelected = { selectedDate ->
                when (activeDatePickerTarget) {
                    "aadhaar_dob" -> viewModel.onAadhaarDobChange(selectedDate)
                    "aadhaar_issue" -> viewModel.onAadhaarIssueDateChange(selectedDate)
                    "pan_dob" -> viewModel.onPanDobChange(selectedDate)
                    "voter_dob" -> viewModel.onVoterDobChange(selectedDate)
                    "generic_issue" -> viewModel.onIssueDateChange(selectedDate)
                    "generic_dob" -> viewModel.onDateOfBirthChange(selectedDate)
                }
                activeDatePickerTarget = null
            },
            onDismiss = { activeDatePickerTarget = null }
        )
    }

    // Fullscreen Photo Viewer Modal
    if (previewImageUri != null) {
        FullscreenPhotoViewerDialog(
            imageUri = previewImageUri!!,
            onDismiss = { previewImageUri = null }
        )
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
            .testTag("card_editor_screen"),
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
                        text = if (cardId > 0) "Edit Card" else "Add ${if (uiState.cardType == CardType.ID_CARD) uiState.idCardType.displayName else uiState.cardType.displayName}",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveCard() },
                        enabled = !uiState.isSaving && !uiState.isLoading,
                        modifier = Modifier.padding(end = 8.dp).testTag("editor_save_button")
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saving...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = uiState.isLoading,
            animationSpec = tween(durationMillis = 400),
            modifier = Modifier.padding(innerPadding),
            label = "cardEditorLoadingCrossfade"
        ) { loading ->
            if (loading) {
                CardEditorSkeletonView()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Live Preview of the Card
                    val previewCard = uiState.toWalletCard()

                    Text(
                        text = "LIVE WALLET PREVIEW",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    if (uiState.cardType == CardType.ID_CARD) {
                        InteractiveIdCardView(
                            card = previewCard,
                            isRevealed = true
                        )
                    } else {
                        InteractiveCreditCardView(
                            card = previewCard,
                            isRevealed = true
                        )
                    }

            // Validation Error Alert
            AnimatedVisibility(visible = uiState.validationError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.validationError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Category Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().testTag("locked_category_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (uiState.cardType) {
                            CardType.CREDIT_CARD -> Icons.Filled.CreditCard
                            CardType.DEBIT_CARD -> Icons.Filled.Payment
                            CardType.ID_CARD -> Icons.Filled.Badge
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.cardType == CardType.ID_CARD) uiState.idCardType.displayName else "Item Category: ${uiState.cardType.displayName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (uiState.cardType == CardType.ID_CARD) "Issuer: ${uiState.idCardType.defaultIssuer}" else "Category is fixed for this item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Category Locked",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Appearance Gradient Theme Picker
            CardColorPicker(
                selectedPresetId = uiState.themePresetId,
                onPresetSelected = { viewModel.onThemePresetChange(it) }
            )

            // Section Header
            Text(
                text = "Required Card Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (uiState.cardType == CardType.ID_CARD) {
                // If creating a new ID or editing ID type
                if (cardId == 0L) {
                    var idTypeDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = idTypeDropdownExpanded,
                        onExpandedChange = { idTypeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.idCardType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Document Type") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (uiState.idCardType) {
                                            IdCardType.AADHAAR -> Icons.Filled.Fingerprint
                                            IdCardType.PAN -> Icons.Filled.CreditCard
                                            IdCardType.DRIVING_LICENSE -> Icons.Filled.DirectionsCar
                                            IdCardType.VOTER_ID -> Icons.Filled.HowToVote
                                            IdCardType.PASSPORT -> Icons.Filled.Flight
                                            IdCardType.VEHICLE_RC -> Icons.Filled.DirectionsCar
                                            IdCardType.ABHA_HEALTH -> Icons.Filled.LocalHospital
                                            IdCardType.RATION_CARD -> Icons.Filled.Restaurant
                                            IdCardType.OTHER -> Icons.Filled.Badge
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = idTypeDropdownExpanded) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("input_id_type")
                        )

                        ExposedDropdownMenu(
                            expanded = idTypeDropdownExpanded,
                            onDismissRequest = { idTypeDropdownExpanded = false },
                            shape = RoundedCornerShape(18.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            IdCardType.entries.forEach { type ->
                                val isSelected = uiState.idCardType == type
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                type.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                type.defaultIssuer,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (type) {
                                                    IdCardType.AADHAAR -> Icons.Filled.Fingerprint
                                                    IdCardType.PAN -> Icons.Filled.CreditCard
                                                    IdCardType.DRIVING_LICENSE -> Icons.Filled.DirectionsCar
                                                    IdCardType.VOTER_ID -> Icons.Filled.HowToVote
                                                    IdCardType.PASSPORT -> Icons.Filled.Flight
                                                    IdCardType.VEHICLE_RC -> Icons.Filled.DirectionsCar
                                                    IdCardType.ABHA_HEALTH -> Icons.Filled.LocalHospital
                                                    IdCardType.RATION_CARD -> Icons.Filled.Restaurant
                                                    IdCardType.OTHER -> Icons.Filled.Badge
                                                },
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        viewModel.onIdCardTypeChange(type)
                                        idTypeDropdownExpanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                            else Color.Transparent
                                        )
                                )
                            }
                        }
                    }
                }

                when (uiState.idCardType) {
                    // 1. AADHAAR CARD FORM
                    IdCardType.AADHAAR -> {
                        AadhaarCardForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenDatePicker = { target -> activeDatePickerTarget = target }
                        )
                    }

                    // 2. PAN CARD FORM
                    IdCardType.PAN -> {
                        PanCardForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenDatePicker = { target -> activeDatePickerTarget = target }
                        )
                    }

                    // 3. VOTER CARD FORM
                    IdCardType.VOTER_ID -> {
                        VoterCardForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenDatePicker = { target -> activeDatePickerTarget = target }
                        )
                    }

                    // 4. RATION CARD FORM
                    IdCardType.RATION_CARD -> {
                        RationCardForm(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    // Other generic ID cards
                    else -> {
                        GenericIdCardForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenDatePicker = { target -> activeDatePickerTarget = target }
                        )
                    }
                }

            } else {
                // Payment Cards (Credit / Debit)
                PaymentCardForm(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Custom fields section only for generic ID cards or payment cards
            if (uiState.cardType != CardType.ID_CARD || (uiState.idCardType != IdCardType.AADHAAR && uiState.idCardType != IdCardType.PAN && uiState.idCardType != IdCardType.VOTER_ID && uiState.idCardType != IdCardType.RATION_CARD)) {
                CustomFieldsSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Attached Card Photos (Optional Front & Rear)
            CardPhotosSection(
                frontPhotoUri = uiState.frontPhotoUri,
                backPhotoUri = uiState.backPhotoUri,
                onFrontPhotoSelected = { uri -> viewModel.onFrontPhotoChanged(uri) },
                onBackPhotoSelected = { uri -> viewModel.onBackPhotoChanged(uri) },
                onPreviewPhoto = { uri -> previewImageUri = uri }
            )

            // Tags & Notes
            EditorTextField(
                value = uiState.tagsString,
                onValueChange = { viewModel.onTagsStringChange(it) },
                label = { Text("Tags") },
                placeholder = { Text("e.g. Personal, Important, Travel") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_tags")
            )

            EditorTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                label = { Text("Secure Notes") },
                placeholder = { Text("e.g. Emergency helpline, registered email, remarks") },
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("input_notes")
            )

            // Favorite toggle (Material You style)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.onFavoriteChange(!uiState.isFavorite) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (uiState.isFavorite) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Pin to Favorites",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Show at the top of your wallet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    MaterialYouSwitch(
                        checked = uiState.isFavorite,
                        onCheckedChange = { viewModel.onFavoriteChange(it) },
                        modifier = Modifier.testTag("switch_favorite")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// AADHAAR CARD FORM
// -------------------------------------------------------------
@Composable
private fun AadhaarCardForm(
    uiState: CardEditorUiState,
    viewModel: CardEditorViewModel,
    onOpenDatePicker: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Issuing authority badge
        FixedIssuerBadge(issuer = "UIDAI")

        // Aadhaar Card Number
        EditorTextField(
            value = uiState.aadhaarNumber,
            onValueChange = { viewModel.onAadhaarNumberChange(it) },
            label = { Text("Aadhaar Card Number*") },
            placeholder = { Text("1234 5678 9012") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            supportingText = {
                Text(
                    text = "${uiState.aadhaarNumber.length}/12 digits ${if (uiState.aadhaarNumber.length == 12) "✓" else ""}",
                    color = if (uiState.aadhaarNumber.length == 12) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("input_aadhaar_number")
        )

        // Full Name
        EditorTextField(
            value = uiState.aadhaarFullName,
            onValueChange = { viewModel.onAadhaarFullNameChange(it) },
            label = { Text("Full Name*") },
            placeholder = { Text("e.g. Rajesh Kumar Sharma") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_aadhaar_name")
        )

        // Date of Birth (DD/MM/YYYY with calendar)
        DateInputField(
            value = uiState.aadhaarDob,
            onValueChange = { viewModel.onAadhaarDobChange(it) },
            label = { Text("Date of Birth*") },
            onOpenDatePicker = { onOpenDatePicker("aadhaar_dob") },
            datePickerDescription = "Select Date of Birth from Calendar",
            testTag = "input_aadhaar_dob",
            modifier = Modifier.fillMaxWidth()
        )

        // Gender (Male / Female / Others -> text field)
        Column {
            Text(
                text = "Gender*",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Male", "Female", "Others").forEach { option ->
                    FilterChip(
                        selected = uiState.aadhaarGender == option,
                        onClick = { viewModel.onAadhaarGenderChange(option) },
                        label = { Text(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(visible = uiState.aadhaarGender == "Others") {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditorTextField(
                        value = uiState.aadhaarGenderCustom,
                        onValueChange = { viewModel.onAadhaarGenderCustomChange(it) },
                        label = { Text("Specify Gender*") },
                        placeholder = { Text("e.g. Transgender / Non-binary") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_aadhaar_gender_custom")
                    )
                }
            }
        }

        // Address
        EditorTextField(
            value = uiState.aadhaarAddress,
            onValueChange = { viewModel.onAadhaarAddressChange(it) },
            label = { Text("Address*") },
            placeholder = { Text("Full residential address as printed on Aadhaar card") },
            singleLine = false,
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag("input_aadhaar_address")
        )

        // Issue Date (DD/MM/YYYY with calendar)
        DateInputField(
            value = uiState.aadhaarIssueDate,
            onValueChange = { viewModel.onAadhaarIssueDateChange(it) },
            label = { Text("Issue Date*") },
            onOpenDatePicker = { onOpenDatePicker("aadhaar_issue") },
            datePickerDescription = "Select Issue Date from Calendar",
            testTag = "input_aadhaar_issue_date",
            modifier = Modifier.fillMaxWidth()
        )

        // Mobile Number (10 Digits only)
        EditorTextField(
            value = uiState.aadhaarMobile,
            onValueChange = { viewModel.onAadhaarMobileChange(it) },
            label = { Text("Mobile Number*") },
            placeholder = { Text("9876543210") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            supportingText = {
                Text(
                    text = "${uiState.aadhaarMobile.length}/10 digits ${if (uiState.aadhaarMobile.length == 10) "✓" else ""}",
                    color = if (uiState.aadhaarMobile.length == 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("input_aadhaar_mobile")
        )
    }
}

// -------------------------------------------------------------
// PAN CARD FORM
// -------------------------------------------------------------
@Composable
private fun PanCardForm(
    uiState: CardEditorUiState,
    viewModel: CardEditorViewModel,
    onOpenDatePicker: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Issuing authority badge (Fixed to INCOME TAX DEPARTMENT)
        FixedIssuerBadge(issuer = "INCOME TAX DEPARTMENT")

        // Permanent Account Number
        EditorTextField(
            value = uiState.panNumber,
            onValueChange = { viewModel.onPanNumberChange(it) },
            label = { Text("Permanent Account Number*") },
            placeholder = { Text("ABCDE1234F") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
            supportingText = {
                Text(
                    text = "${uiState.panNumber.length}/10 characters",
                    color = if (uiState.panNumber.length == 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("input_pan_number")
        )

        // Name (BLOCK LETTERS ONLY)
        EditorTextField(
            value = uiState.panName,
            onValueChange = { viewModel.onPanNameChange(it) },
            label = { Text("Name*") },
            placeholder = { Text("e.g. RAJESH KUMAR SHARMA") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_pan_name")
        )

        // Father's Name (BLOCK LETTERS ONLY)
        EditorTextField(
            value = uiState.panFatherName,
            onValueChange = { viewModel.onPanFatherNameChange(it) },
            label = { Text("Father's Name*") },
            placeholder = { Text("e.g. SURESH KUMAR SHARMA") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_pan_father")
        )

        // Date of Birth (DD/MM/YYYY with calendar)
        DateInputField(
            value = uiState.panDob,
            onValueChange = { viewModel.onPanDobChange(it) },
            label = { Text("Date of Birth*") },
            onOpenDatePicker = { onOpenDatePicker("pan_dob") },
            datePickerDescription = "Select Date of Birth from Calendar",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            testTag = "input_pan_dob",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -------------------------------------------------------------
// VOTER CARD FORM
// -------------------------------------------------------------
@Composable
private fun VoterCardForm(
    uiState: CardEditorUiState,
    viewModel: CardEditorViewModel,
    onOpenDatePicker: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Issuing authority badge (Fixed to ELECTION COMMISSION OF INDIA)
        FixedIssuerBadge(issuer = "ELECTION COMMISSION OF INDIA")

        // EPIC Number (Block letters, digits, and '/' only)
        EditorTextField(
            value = uiState.voterEpicNumber,
            onValueChange = { viewModel.onVoterEpicNumberChange(it) },
            label = { Text("EPIC Number*") },
            placeholder = { Text("e.g. WBF1234567 or WB/01/123/456789") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_voter_epic")
        )

        // Name (Two fields: English and Regional language)
        Text("Name*", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorTextField(
                value = uiState.voterNameEn,
                onValueChange = { viewModel.onVoterNameEnChange(it) },
                label = { Text("Name in English*") },
                placeholder = { Text("e.g. Amit Sen") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).testTag("input_voter_name_en")
            )
            EditorTextField(
                value = uiState.voterNameRegional,
                onValueChange = { viewModel.onVoterNameRegionalChange(it) },
                label = { Text("Name in Regional Script*") },
                placeholder = { Text("e.g. অমিত সেন") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).testTag("input_voter_name_regional")
            )
        }

        // Relation Selection (Father / Husband / Guardian)
        Column {
            Text(
                text = "Select Relation*",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Father", "Husband", "Guardian").forEach { relation ->
                    FilterChip(
                        selected = uiState.voterRelationType == relation,
                        onClick = { viewModel.onVoterRelationTypeChange(relation) },
                        label = { Text(relation) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Relation's Name (English & Regional)
        Text("${uiState.voterRelationType}'s Name*", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorTextField(
                value = uiState.voterRelationNameEn,
                onValueChange = { viewModel.onVoterRelationNameEnChange(it) },
                label = { Text("${uiState.voterRelationType}'s Name in English*") },
                placeholder = { Text("e.g. Bimal Sen") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).testTag("input_voter_relation_en")
            )
            EditorTextField(
                value = uiState.voterRelationNameRegional,
                onValueChange = { viewModel.onVoterRelationNameRegionalChange(it) },
                label = { Text("${uiState.voterRelationType}'s Name in Regional Script*") },
                placeholder = { Text("e.g. বিমল সেন") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).testTag("input_voter_relation_regional")
            )
        }

        // Gender (Choose Male / Female / Others -> type)
        Column {
            Text(
                text = "Gender*",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Male", "Female", "Others").forEach { option ->
                    FilterChip(
                        selected = uiState.voterGender == option,
                        onClick = { viewModel.onVoterGenderChange(option) },
                        label = { Text(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(visible = uiState.voterGender == "Others") {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditorTextField(
                        value = uiState.voterGenderCustom,
                        onValueChange = { viewModel.onVoterGenderCustomChange(it) },
                        label = { Text("Specify Gender*") },
                        placeholder = { Text("e.g. Third Gender") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_voter_gender_custom")
                    )
                }
            }
        }

        // Date of Birth or Age
        Column {
            Text("Date of Birth or Age*", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.voterAgeType == "DOB",
                    onClick = { viewModel.onVoterAgeTypeChange("DOB") },
                    label = { Text("Date of Birth") }
                )
                FilterChip(
                    selected = uiState.voterAgeType == "AGE",
                    onClick = { viewModel.onVoterAgeTypeChange("AGE") },
                    label = { Text("Age") }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            if (uiState.voterAgeType == "DOB") {
                DateInputField(
                    value = uiState.voterDob,
                    onValueChange = { viewModel.onVoterDobChange(it) },
                    label = { Text("Date of Birth*") },
                    onOpenDatePicker = { onOpenDatePicker("voter_dob") },
                    datePickerDescription = "Select DOB from Calendar",
                    testTag = "input_voter_dob",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EditorTextField(
                    value = uiState.voterAge,
                    onValueChange = { viewModel.onVoterAgeChange(it) },
                    label = { Text("Age*") },
                    placeholder = { Text("e.g. 28") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("input_voter_age")
                )
            }
        }

        // Address (English & Regional)
        Text("Address*", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        EditorTextField(
            value = uiState.voterAddressEn,
            onValueChange = { viewModel.onVoterAddressEnChange(it) },
            label = { Text("Address in English*") },
            placeholder = { Text("Residential address in English") },
            singleLine = false,
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("input_voter_addr_en")
        )
        EditorTextField(
            value = uiState.voterAddressRegional,
            onValueChange = { viewModel.onVoterAddressRegionalChange(it) },
            label = { Text("Address in Regional Script*") },
            placeholder = { Text("Residential address in regional script") },
            singleLine = false,
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("input_voter_addr_reg")
        )

        // Serial Number & Assembly Constituency No.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorTextField(
                value = uiState.voterSerialNo,
                onValueChange = { viewModel.onVoterSerialNoChange(it) },
                label = { Text("Serial No.*") },
                placeholder = { Text("e.g. 524") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).testTag("input_voter_serial")
            )
            EditorTextField(
                value = uiState.voterAcNo,
                onValueChange = { viewModel.onVoterAcNoChange(it) },
                label = { Text("AC No.*") },
                placeholder = { Text("e.g. 115") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f).testTag("input_voter_ac_no")
            )
        }

        // Assembly Constituency Name
        EditorTextField(
            value = uiState.voterAcName,
            onValueChange = { viewModel.onVoterAcNameChange(it) },
            label = { Text("Assembly Constituency Name*") },
            placeholder = { Text("e.g. Bhowanipore / New Delhi") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_voter_ac_name")
        )

        // Part No. & Part Name
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorTextField(
                value = uiState.voterPartNo,
                onValueChange = { viewModel.onVoterPartNoChange(it) },
                label = { Text("Part No.*") },
                placeholder = { Text("e.g. 78") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.weight(0.4f).testTag("input_voter_part_no")
            )
            EditorTextField(
                value = uiState.voterPartName,
                onValueChange = { viewModel.onVoterPartNameChange(it) },
                label = { Text("Part Name*") },
                placeholder = { Text("e.g. Sector 4 East Block") },
                singleLine = true,
                modifier = Modifier.weight(0.6f).testTag("input_voter_part_name")
            )
        }

        // Polling Station
        EditorTextField(
            value = uiState.voterPollingStation,
            onValueChange = { viewModel.onVoterPollingStationChange(it) },
            label = { Text("Polling Station*") },
            placeholder = { Text("e.g. Primary School Room No. 2") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_voter_polling")
        )
    }
}

// -------------------------------------------------------------
// RATION CARD FORM
// -------------------------------------------------------------
@Composable
private fun RationCardForm(
    uiState: CardEditorUiState,
    viewModel: CardEditorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Issuing authority badge
        FixedIssuerBadge(issuer = "FOOD & SUPPLIES DEPARTMENT")

        // Category ("AAY", "PHH", "SPHH", "RKSY-I", "RKSY-II", "NPHH", "Others")
        Column {
            Text(
                text = "Category*",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val categories = listOf("AAY", "PHH", "SPHH", "RKSY-I", "RKSY-II", "NPHH", "Others")
                categories.forEach { cat ->
                    FilterChip(
                        selected = uiState.rationCategory == cat,
                        onClick = { viewModel.onRationCategoryChange(cat) },
                        label = { Text(cat, fontWeight = if (uiState.rationCategory == cat) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            AnimatedVisibility(visible = uiState.rationCategory == "Others") {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    EditorTextField(
                        value = uiState.rationCategoryCustom,
                        onValueChange = { viewModel.onRationCategoryCustomChange(it) },
                        label = { Text("Specify Category*") },
                        placeholder = { Text("e.g. BPL / APL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_ration_cat_custom")
                    )
                }
            }
        }

        // Card Number (Shown as "Category" <space> "Card Number")
        EditorTextField(
            value = uiState.rationCardNumber,
            onValueChange = { viewModel.onRationCardNumberChange(it) },
            label = { Text("Card Number*") },
            placeholder = { Text("e.g. 1234567890") },
            singleLine = true,
            supportingText = {
                val selectedCat = if (uiState.rationCategory == "Others") uiState.rationCategoryCustom.ifBlank { "CAT" } else uiState.rationCategory
                Text(
                    text = "Full Card ID: $selectedCat ${uiState.rationCardNumber.ifBlank { "••••••" }}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_ration_card_no")
        )

        // Name of Card Holder
        EditorTextField(
            value = uiState.rationCardholderName,
            onValueChange = { viewModel.onRationCardholderNameChange(it) },
            label = { Text("Name of Card Holder*") },
            placeholder = { Text("e.g. Sunita Devi") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_ration_holder_name")
        )

        // Relation Selection (Father / Husband / Guardian)
        Column {
            Text(
                text = "Select Relation*",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Father", "Husband", "Guardian").forEach { relation ->
                    FilterChip(
                        selected = uiState.rationRelationType == relation,
                        onClick = { viewModel.onRationRelationTypeChange(relation) },
                        label = { Text(relation) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Name of Father / Husband / Guardian
        EditorTextField(
            value = uiState.rationFatherOrHusbandName,
            onValueChange = { viewModel.onRationFatherOrHusbandNameChange(it) },
            label = { Text("Name of ${uiState.rationRelationType}*") },
            placeholder = { Text("e.g. Ramesh Kumar") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_ration_father")
        )

        // Head of the Family
        EditorTextField(
            value = uiState.rationHeadOfFamily,
            onValueChange = { viewModel.onRationHeadOfFamilyChange(it) },
            label = { Text("Head of the Family*") },
            placeholder = { Text("e.g. Sunita Devi") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_ration_head")
        )

        // Age
        EditorTextField(
            value = uiState.rationAge,
            onValueChange = { viewModel.onRationAgeChange(it) },
            label = { Text("Age*") },
            placeholder = { Text("e.g. 42") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("input_ration_age")
        )

        // Dealer Name
        EditorTextField(
            value = uiState.rationDealerName,
            onValueChange = { viewModel.onRationDealerNameChange(it) },
            label = { Text("Dealer Name*") },
            placeholder = { Text("e.g. M/S City Food Corner") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_ration_dealer_name")
        )

        // Dealer Address
        EditorTextField(
            value = uiState.rationDealerAddress,
            onValueChange = { viewModel.onRationDealerAddressChange(it) },
            label = { Text("Dealer Address*") },
            placeholder = { Text("e.g. Shop 12, Market Complex, Ward 4") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_ration_dealer_addr")
        )

        // Card Holder Address
        EditorTextField(
            value = uiState.rationAddress,
            onValueChange = { viewModel.onRationAddressChange(it) },
            label = { Text("Card Holder Address*") },
            placeholder = { Text("e.g. Flat 302, Green Avenue, Main Road") },
            singleLine = false,
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag("input_ration_holder_addr")
        )
    }
}

// -------------------------------------------------------------
// GENERIC ID CARD FORM (For other IDs like DL, Passport, etc.)
// -------------------------------------------------------------
@Composable
private fun GenericIdCardForm(
    uiState: CardEditorUiState,
    viewModel: CardEditorViewModel,
    onOpenDatePicker: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EditorTextField(
            value = uiState.cardholderName,
            onValueChange = { viewModel.onCardholderNameChange(it) },
            label = { Text("Full Name on ID") },
            placeholder = { Text("e.g. Rajesh Kumar Sharma") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth().testTag("input_id_name")
        )

        EditorTextField(
            value = uiState.idNumber,
            onValueChange = { viewModel.onIdNumberChange(it) },
            label = { Text("Document Number") },
            placeholder = { Text(uiState.idCardType.numberHint) },
            supportingText = { Text("Format: ${uiState.idCardType.numberHint}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_id_number")
        )

        EditorTextField(
            value = uiState.bankOrIssuer,
            onValueChange = { viewModel.onBankOrIssuerChange(it) },
            label = { Text("Issuing Authority / State") },
            placeholder = { Text(uiState.idCardType.defaultIssuer) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_id_issuer")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateInputField(
                value = uiState.issueDate,
                onValueChange = { viewModel.onIssueDateChange(it) },
                label = { Text("Issue Date") },
                onOpenDatePicker = { onOpenDatePicker("generic_issue") },
                datePickerDescription = "Calendar",
                testTag = "input_id_issue_date",
                modifier = Modifier.weight(1f)
            )

            DateInputField(
                value = uiState.dateOfBirth,
                onValueChange = { viewModel.onDateOfBirthChange(it) },
                label = { Text("Date of Birth") },
                onOpenDatePicker = { onOpenDatePicker("generic_dob") },
                datePickerDescription = "Calendar",
                testTag = "input_id_dob",
                modifier = Modifier.weight(1f)
            )
        }

        EditorTextField(
            value = uiState.address,
            onValueChange = { viewModel.onAddressChange(it) },
            label = { Text("Registered Address") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("input_id_address")
        )
    }
}

// -------------------------------------------------------------
// PAYMENT CARDS FORM (Credit / Debit)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentCardForm(
    uiState: CardEditorUiState,
    viewModel: CardEditorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EditorTextField(
            value = uiState.bankOrIssuer,
            onValueChange = { viewModel.onBankOrIssuerChange(it) },
            label = { Text("Bank / Card Issuer") },
            placeholder = { Text("e.g. HDFC Bank, ICICI Bank, SBI, Axis Bank") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_bank")
        )

        var networkDropdownExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = networkDropdownExpanded,
            onExpandedChange = { networkDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.cardNetwork.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Card Network") },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = networkDropdownExpanded) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag("dropdown_card_network")
            )

            ExposedDropdownMenu(
                expanded = networkDropdownExpanded,
                onDismissRequest = { networkDropdownExpanded = false },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                CardNetwork.entries.forEach { network ->
                    val isSelected = uiState.cardNetwork == network
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = network.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHighest
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CreditCard,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            viewModel.onCardNetworkChange(network)
                            networkDropdownExpanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                else Color.Transparent
                            )
                    )
                }
            }
        }

        EditorTextField(
            value = uiState.cardholderName,
            onValueChange = { viewModel.onCardholderNameChange(it) },
            label = { Text("Cardholder Name") },
            placeholder = { Text("e.g. RAJESH K SHARMA") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_cardholder")
        )

        val isAmex = uiState.cardNetwork == CardNetwork.AMEX
        val isValidLuhn = remember(uiState.cardNumber) { CardValidationUtils.isValidLuhn(uiState.cardNumber) }

        EditorTextField(
            value = uiState.cardNumber,
            onValueChange = { viewModel.onCardNumberChange(it) },
            label = { Text("Card Number") },
            placeholder = { Text(if (isAmex) "•••• •••••• •••••" else "•••• •••• •••• ••••") },
            singleLine = true,
            visualTransformation = CardNumberVisualTransformation(isAmex = isAmex),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = if (isValidLuhn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (uiState.cardNetwork != CardNetwork.OTHER) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = uiState.cardNetwork.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (uiState.cardNumber.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onCardNumberChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            supportingText = {
                if (uiState.cardNumber.length >= 13) {
                    if (isValidLuhn) {
                        Text(
                            text = "${uiState.cardNumber.length} digits • Valid card format",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "${uiState.cardNumber.length} digits • Verify card number",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("input_card_number")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isExpiryInvalidMonth = uiState.isMonthInvalid

            EditorTextField(
                value = uiState.rawExpiry,
                onValueChange = { viewModel.onExpiryChange(it) },
                label = { Text("Expiry Date") },
                placeholder = { Text("MM/YY") },
                singleLine = true,
                isError = isExpiryInvalidMonth,
                visualTransformation = ExpiryDateVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    if (uiState.rawExpiry.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onExpiryChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                supportingText = {
                    if (isExpiryInvalidMonth) {
                        Text("Invalid month (01-12)", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.weight(1f).testTag("input_expiry")
            )

            EditorTextField(
                value = uiState.cvv,
                onValueChange = { viewModel.onCvvChange(it) },
                label = { Text(if (isAmex) "CID" else "CVV / CVC") },
                placeholder = { Text(if (isAmex) "1234" else "123") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = if (uiState.cardType == CardType.DEBIT_CARD) ImeAction.Next else ImeAction.Done
                ),
                trailingIcon = {
                    if (uiState.cvv.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onCvvChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f).testTag("input_cvv")
            )
        }

        if (uiState.cardType == CardType.DEBIT_CARD) {
            EditorTextField(
                value = uiState.pin,
                onValueChange = { viewModel.onPinChange(it) },
                label = { Text("ATM PIN") },
                placeholder = { Text("••••") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    if (uiState.pin.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onPinChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("input_pin")
            )
        }
    }
}

// -------------------------------------------------------------
// CUSTOM FIELDS SECTION
// -------------------------------------------------------------
@Composable
private fun CustomFieldsSection(
    uiState: CardEditorUiState,
    viewModel: CardEditorViewModel
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("custom_fields_section")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Custom Fields",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add any custom labels, numbers, or details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.addCustomField("", "") },
                    modifier = Modifier.testTag("btn_add_custom_field")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Field")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.customFields.isEmpty()) {
                Text(
                    text = "No custom fields added yet. Tap '+ Add Field' to add custom details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                uiState.customFields.forEachIndexed { index, field ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EditorTextField(
                            value = field.label,
                            onValueChange = { viewModel.updateCustomFieldLabel(index, it) },
                            label = { Text("Field Name") },
                            placeholder = { Text("e.g. Father's Name") },
                            singleLine = true,
                            modifier = Modifier.weight(0.45f).testTag("custom_field_label_$index")
                        )

                        EditorTextField(
                            value = field.value,
                            onValueChange = { viewModel.updateCustomFieldValue(index, it) },
                            label = { Text("Value") },
                            placeholder = { Text("Field Value") },
                            singleLine = true,
                            modifier = Modifier.weight(0.55f).testTag("custom_field_value_$index")
                        )

                        IconButton(
                            onClick = { viewModel.removeCustomField(index) },
                            modifier = Modifier.size(36.dp).testTag("custom_field_delete_$index")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove Field",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fixed Issuing Authority Pill
@Composable
private fun FixedIssuerBadge(issuer: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Fixed Authority",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "ISSUING AUTHORITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = issuer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Reusable DatePicker Dialog for DD/MM/YYYY formatting
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        onDateSelected(sdf.format(Date(millis)))
                    }
                    onDismiss()
                },
                enabled = confirmEnabled.value
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// -------------------------------------------------------------
// CARD PHOTOS (FRONT & REAR) SECTION
// -------------------------------------------------------------
@Composable
private fun CardPhotosSection(
    frontPhotoUri: String?,
    backPhotoUri: String?,
    onFrontPhotoSelected: (String?) -> Unit,
    onBackPhotoSelected: (String?) -> Unit,
    onPreviewPhoto: (String) -> Unit
) {
    val context = LocalContext.current

    val frontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = CardImageHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                // Delete previous file if any
                CardImageHelper.deleteInternalImage(frontPhotoUri)
                onFrontPhotoSelected(savedPath)
            }
        }
    }

    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = CardImageHelper.saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                CardImageHelper.deleteInternalImage(backPhotoUri)
                onBackPhotoSelected(savedPath)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_photos_section")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Card Photos (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Attach actual front and rear photos of your card",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Front Photo
                PhotoUploadCard(
                    title = "Front Side",
                    photoUri = frontPhotoUri,
                    onPickPhoto = {
                        frontLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDeletePhoto = {
                        CardImageHelper.deleteInternalImage(frontPhotoUri)
                        onFrontPhotoSelected(null)
                    },
                    onPreviewPhoto = { uri -> onPreviewPhoto(uri) },
                    modifier = Modifier.weight(1f),
                    testTagPrefix = "front"
                )

                // Back / Rear Photo
                PhotoUploadCard(
                    title = "Rear Side",
                    photoUri = backPhotoUri,
                    onPickPhoto = {
                        backLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDeletePhoto = {
                        CardImageHelper.deleteInternalImage(backPhotoUri)
                        onBackPhotoSelected(null)
                    },
                    onPreviewPhoto = { uri -> onPreviewPhoto(uri) },
                    modifier = Modifier.weight(1f),
                    testTagPrefix = "back"
                )
            }
        }
    }
}

@Composable
private fun PhotoUploadCard(
    title: String,
    photoUri: String?,
    onPickPhoto: () -> Unit,
    onDeletePhoto: () -> Unit,
    onPreviewPhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "photo"
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (photoUri != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (photoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onPreviewPhoto(photoUri) }
                        .testTag("preview_${testTagPrefix}_photo")
                ) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "$title photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Zoom indicator badge
                    Surface(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(24.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onPickPhoto,
                        modifier = Modifier.testTag("change_${testTagPrefix}_photo")
                    ) {
                        Text("Change", style = MaterialTheme.typography.labelSmall)
                    }

                    IconButton(
                        onClick = onDeletePhoto,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_${testTagPrefix}_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete $title",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onPickPhoto() }
                        .testTag("btn_pick_${testTagPrefix}_photo"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Add $title",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+ Add Photo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No photo attached",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// Fullscreen Photo Viewer Dialog
@Composable
fun FullscreenPhotoViewerDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.95f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Card Photo Fullscreen",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
                        .testTag("close_fullscreen_photo")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
