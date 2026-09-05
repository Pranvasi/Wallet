package com.example.ui.screens.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WalletDatabase
import com.example.data.model.WalletCard
import com.example.data.repository.WalletRepository
import com.example.data.security.BackupCrypto
import com.example.data.security.BiometricAuthHelper
import com.example.data.security.SecurityPreferences
import com.example.data.security.SecuritySettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val settings: SecuritySettings = SecuritySettings(),
    val isBiometricHardwareAvailable: Boolean = false,
    val totalCardsCount: Int = 0,
    val showClearVaultDialog: Boolean = false,

    // Backup Export State
    val isExporting: Boolean = false,
    val exportError: String? = null,
    val exportSuccessMessage: String? = null,
    val generatedBackupFile: File? = null,
    val showExportDialog: Boolean = false,

    // Backup Import State
    val isImporting: Boolean = false,
    val importError: String? = null,
    val importSuccessMessage: String? = null,
    val importedCardsPreview: List<WalletCard>? = null,
    val selectedImportUri: Uri? = null,
    val selectedImportFileName: String? = null,
    val showImportDialog: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val securityPreferences: SecurityPreferences = SecurityPreferences(application)
    private val repository: WalletRepository

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isBiometricHardwareAvailable = BiometricAuthHelper.isBiometricHardwareAvailable(application)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val db = WalletDatabase.getDatabase(application)
        repository = WalletRepository(db.walletDao())

        viewModelScope.launch {
            securityPreferences.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }

        viewModelScope.launch {
            repository.cardCount.collect { count ->
                _uiState.update { it.copy(totalCardsCount = count) }
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityPreferences.setBiometricEnabled(enabled)
        }
    }

    fun setAutoLockDelay(seconds: Int) {
        viewModelScope.launch {
            securityPreferences.setAutoLockDelay(seconds)
        }
    }

    fun setSecureScreenEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityPreferences.setSecureScreenEnabled(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            securityPreferences.setThemeMode(mode)
        }
    }

    fun setThemePalette(palette: String) {
        viewModelScope.launch {
            securityPreferences.setThemePalette(palette)
        }
    }

    fun clearVault() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // --- Backup & Restore Functionality in Settings ---

    fun openExportDialog() {
        _uiState.update {
            it.copy(
                showExportDialog = true,
                exportError = null,
                generatedBackupFile = null
            )
        }
    }

    fun dismissExportDialog() {
        _uiState.update {
            it.copy(
                showExportDialog = false,
                exportError = null,
                generatedBackupFile = null
            )
        }
    }

    fun createBackupFile(password: String, confirmPassword: String, onFileCreated: (File) -> Unit) {
        if (password.length < 4) {
            _uiState.update { it.copy(exportError = "Password must be at least 4 characters long") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(exportError = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            try {
                val cards = repository.allCards.first()
                val backupFile = BackupCrypto.createCacheBackupFile(
                    context = getApplication(),
                    cards = cards,
                    password = password
                )
                securityPreferences.updateLastBackupTimestamp()
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        generatedBackupFile = backupFile,
                        exportError = null
                    )
                }
                onFileCreated(backupFile)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportError = "Failed to create backup: ${e.message}"
                    )
                }
            }
        }
    }

    fun saveBackupToUri(destinationUri: Uri, file: File) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    file.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                securityPreferences.updateLastBackupTimestamp()
                _uiState.update {
                    it.copy(
                        showExportDialog = false,
                        generatedBackupFile = null,
                        exportSuccessMessage = "Encrypted backup (.walletbackup) saved successfully!"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(exportError = "Failed to write backup file: ${e.message}")
                }
            }
        }
    }

    fun shareBackupFile(context: Context, file: File) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Wallet Encrypted Backup (.walletbackup)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Share Wallet Backup").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            viewModelScope.launch {
                securityPreferences.updateLastBackupTimestamp()
                _uiState.update {
                    it.copy(
                        showExportDialog = false,
                        generatedBackupFile = null,
                        exportSuccessMessage = "Backup file shared successfully!"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update {
                it.copy(exportError = "Failed to share backup: ${e.message}")
            }
        }
    }

    fun onFileSelectedForImport(uri: Uri, fileName: String) {
        _uiState.update {
            it.copy(
                selectedImportUri = uri,
                selectedImportFileName = fileName,
                showImportDialog = true,
                importError = null,
                importedCardsPreview = null
            )
        }
    }

    fun dismissImportDialog() {
        _uiState.update {
            it.copy(
                showImportDialog = false,
                selectedImportUri = null,
                selectedImportFileName = null,
                importError = null,
                importedCardsPreview = null
            )
        }
    }

    fun decryptAndPreviewImport(password: String) {
        val uri = _uiState.value.selectedImportUri ?: return
        if (password.isBlank()) {
            _uiState.update { it.copy(importError = "Please enter your backup decryption password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importError = null) }
            try {
                val context = getApplication<Application>()
                val cards = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BackupCrypto.readBackupFromStream(context, stream, password)
                } ?: throw IllegalArgumentException("Cannot open backup file")

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importedCardsPreview = cards,
                        importError = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importError = "Decryption failed. Please check your password and verify the .walletbackup file."
                    )
                }
            }
        }
    }

    fun applyImport(replaceExisting: Boolean) {
        val cards = _uiState.value.importedCardsPreview ?: return
        viewModelScope.launch {
            try {
                repository.importCards(cards, replaceExisting = replaceExisting)
                _uiState.update {
                    it.copy(
                        showImportDialog = false,
                        selectedImportUri = null,
                        selectedImportFileName = null,
                        importedCardsPreview = null,
                        importSuccessMessage = "Successfully restored ${cards.size} card(s) and photos into your vault!"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(importError = "Failed to import cards: ${e.message}")
                }
            }
        }
    }

    fun clearFeedbackMessages() {
        _uiState.update {
            it.copy(
                exportSuccessMessage = null,
                importSuccessMessage = null
            )
        }
    }
}
