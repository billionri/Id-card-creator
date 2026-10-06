package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EmployeeIdCard
import com.example.data.remote.GeminiService
import com.example.data.repository.CardRepository
import com.example.util.PhotoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class GeneratorUiState(
    val currentCard: EmployeeIdCard = createDefaultCard(),
    val isAnalyzing: Boolean = false,
    val statusMessage: String? = null,
    val isFlipped: Boolean = false,
    val showLanyard: Boolean = true,
    val activePhotoBitmap: Bitmap? = null,
    val cardSaveSuccess: Boolean = false,
    val searchQuery: String = ""
)

private fun createDefaultCard(): EmployeeIdCard {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val today = sdf.format(Date())
    return EmployeeIdCard(
        id = 0,
        fullName = "Vikash Kumar",
        employeeCode = "DTDC/HE/2024/" + Random.nextInt(1000, 9999),
        designation = "Delivery Associate",
        department = "Express Dispatch & Delivery",
        branchName = "Hariom Enterprises",
        branchCode = "DTDC-HE-4102",
        phoneNumber = "+91 98765 43210",
        emergencyContact = "+91 98765 00000",
        bloodGroup = "B+",
        dateOfJoining = today,
        validTill = "31 Dec 2027",
        aadhaarMasked = "XXXX-XXXX-8921",
        branchAddress = "Shop No 2, 317-A, Shinde Niwas, Kasturba Cross Road No 6, Opposite Platform No 10, Borivali East, Mumbai — 400066, Maharashtra",
        photoPath = null,
        aiVerified = false,
        aiNote = "Upload passport photo to enable AI verification"
    )
}

class IdCardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CardRepository
    private val geminiService = GeminiService()

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    private val _searchFilter = MutableStateFlow("")

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CardRepository(db.employeeCardDao())
    }

    val savedCards: StateFlow<List<EmployeeIdCard>> = combine(
        repository.allCards,
        _searchFilter
    ) { cards, query ->
        if (query.isBlank()) cards
        else {
            val q = query.lowercase().trim()
            cards.filter {
                it.fullName.lowercase().contains(q) ||
                        it.employeeCode.lowercase().contains(q) ||
                        it.designation.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPassportPhotoSelected(bitmap: Bitmap) {
        val context = getApplication<Application>()
        val savedPath = PhotoUtils.savePassportPhoto(context, bitmap)

        _uiState.update {
            it.copy(
                activePhotoBitmap = bitmap,
                currentCard = it.currentCard.copy(photoPath = savedPath),
                isAnalyzing = true,
                statusMessage = "AI analyzing passport photo..."
            )
        }

        viewModelScope.launch {
            try {
                val result = geminiService.analyzePassportPhoto(bitmap)
                result.onSuccess { profile ->
                    _uiState.update { current ->
                        current.copy(
                            isAnalyzing = false,
                            statusMessage = "AI Assessment: ${profile.assessment} (${profile.score}% match)",
                            currentCard = current.currentCard.copy(
                                fullName = if (current.currentCard.fullName.isBlank() || current.currentCard.fullName == "Vikash Kumar") profile.name else current.currentCard.fullName,
                                designation = profile.designation,
                                department = profile.department,
                                employeeCode = profile.employeeCode,
                                bloodGroup = profile.bloodGroup,
                                aiVerified = true,
                                aiNote = "${profile.assessment} • Readiness Score: ${profile.score}%"
                            )
                        )
                    }
                }.onFailure {
                    _uiState.update { it.copy(isAnalyzing = false, statusMessage = "Photo saved. Complete details below.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAnalyzing = false, statusMessage = "Photo saved successfully.") }
            }
        }
    }

    fun triggerAiAutoFill() {
        val bitmap = _uiState.value.activePhotoBitmap
        _uiState.update { it.copy(isAnalyzing = true, statusMessage = "Gemini AI generating official credentials...") }
        viewModelScope.launch {
            val result = geminiService.analyzePassportPhoto(bitmap)
            result.onSuccess { profile ->
                _uiState.update { current ->
                    current.copy(
                        isAnalyzing = false,
                        statusMessage = "Credentials updated: ${profile.designation} (${profile.employeeCode})",
                        currentCard = current.currentCard.copy(
                            fullName = profile.name,
                            designation = profile.designation,
                            department = profile.department,
                            employeeCode = profile.employeeCode,
                            bloodGroup = profile.bloodGroup,
                            aiVerified = true,
                            aiNote = "${profile.assessment} • Security Score: ${profile.score}%"
                        )
                    )
                }
            }
        }
    }

    fun updateFullName(name: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(fullName = name)) }
    }

    fun updateDesignation(desig: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(designation = desig)) }
    }

    fun updateDepartment(dept: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(department = dept)) }
    }

    fun updateEmployeeCode(code: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(employeeCode = code)) }
    }

    fun generateNewCode() {
        val newCode = "DTDC/HE/2024/" + Random.nextInt(1000, 9999)
        _uiState.update { it.copy(currentCard = it.currentCard.copy(employeeCode = newCode)) }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(phoneNumber = phone)) }
    }

    fun updateEmergencyContact(contact: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(emergencyContact = contact)) }
    }

    fun updateBloodGroup(blood: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(bloodGroup = blood)) }
    }

    fun updateValidTill(valid: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(validTill = valid)) }
    }

    fun updateBranchAddress(addr: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(branchAddress = addr)) }
    }

    fun toggleCardFlip() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun toggleLanyard() {
        _uiState.update { it.copy(showLanyard = !it.showLanyard) }
    }

    fun saveCurrentCard() {
        viewModelScope.launch {
            val card = _uiState.value.currentCard
            val newId = repository.saveCard(card)
            _uiState.update {
                it.copy(
                    currentCard = card.copy(id = newId),
                    cardSaveSuccess = true,
                    statusMessage = "ID Card saved successfully!"
                )
            }
        }
    }

    fun dismissSaveSuccess() {
        _uiState.update { it.copy(cardSaveSuccess = false) }
    }

    fun loadCardForEditing(card: EmployeeIdCard) {
        val bitmap = PhotoUtils.loadBitmapFromPath(card.photoPath)
        _uiState.update {
            it.copy(
                currentCard = card,
                activePhotoBitmap = bitmap,
                isFlipped = false,
                statusMessage = "Loaded card: ${card.fullName} (${card.employeeCode})"
            )
        }
    }

    fun createNewCard() {
        _uiState.update {
            it.copy(
                currentCard = createDefaultCard(),
                activePhotoBitmap = null,
                isFlipped = false,
                statusMessage = null
            )
        }
    }

    fun deleteCard(card: EmployeeIdCard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchFilter.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }
}
