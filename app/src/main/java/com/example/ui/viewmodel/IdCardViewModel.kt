package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EmployeeIdCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class IdCardUiState(
    val currentCard: EmployeeIdCard = createDefaultCard(),
    val activePhotoBitmap: Bitmap? = null,
    val isFlipped: Boolean = false,
    val showLanyard: Boolean = true,
    val savedCards: List<EmployeeIdCard> = emptyList(),
    val isAiAnalyzing: Boolean = false,
    val statusMessage: String? = null
)

private fun createDefaultCard(): EmployeeIdCard {
    return EmployeeIdCard(
        id = 0,
        fullName = "Rashid Alam",
        employeeCode = "DTDC/HE/2026/" + Random.nextInt(1000, 9999),
        designation = "Delivery Associate",
        department = "Express Logistics & Dispatch",
        branchId = "DEL/HE/1029",
        branchName = "Hariom Enterprises",
        branchAddress = "Shop 4, Main Market, Sector 12, Dwarka, New Delhi 110075",
        phoneNumber = "+91 98765 43210",
        emergencyContact = "+91 98111 22334",
        bloodGroup = "O+",
        validTill = "31-DEC-2027",
        photoPath = null,
        aiVerified = true
    )
}

class IdCardViewModel(application: Application) : AndroidViewModel(application) {

    private val cardDao = AppDatabase.getDatabase(application).employeeCardDao()

    private val _uiState = MutableStateFlow(IdCardUiState())
    val uiState: StateFlow<IdCardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cardDao.getAllCards().collect { cards ->
                _uiState.update { it.copy(savedCards = cards) }
            }
        }
    }

    fun createNewCard() {
        _uiState.update {
            it.copy(
                currentCard = createDefaultCard(),
                activePhotoBitmap = null,
                isFlipped = false,
                statusMessage = "New badge draft initialized"
            )
        }
    }

    fun updateFullName(name: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(fullName = name)) }
    }

    fun updateDesignation(designation: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(designation = designation)) }
    }

    fun updateDepartment(department: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(department = department)) }
    }

    fun updateEmployeeCode(code: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(employeeCode = code)) }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(phoneNumber = phone)) }
    }

    fun updateEmergencyContact(contact: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(emergencyContact = contact)) }
    }

    fun updateBloodGroup(bloodGroup: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(bloodGroup = bloodGroup)) }
    }

    fun updateBranchId(branchId: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(branchId = branchId)) }
    }

    fun updateBranchName(branchName: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(branchName = branchName)) }
    }

    fun updateBranchAddress(address: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(branchAddress = address)) }
    }

    fun updateValidTill(validTill: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(validTill = validTill)) }
    }

    fun updatePhoto(bitmap: Bitmap?, path: String?) {
        _uiState.update {
            it.copy(
                activePhotoBitmap = bitmap,
                currentCard = it.currentCard.copy(photoPath = path)
            )
        }
    }

    fun toggleCardFlip() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun toggleLanyard() {
        _uiState.update { it.copy(showLanyard = !it.showLanyard) }
    }

    fun triggerAiAutoFill() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true, statusMessage = "AI analyzing credentials...") }
            kotlinx.coroutines.delay(800)
            val current = _uiState.value.currentCard
            _uiState.update {
                it.copy(
                    isAiAnalyzing = false,
                    currentCard = current.copy(
                        employeeCode = if (current.employeeCode.isBlank() || current.employeeCode.startsWith("DTDC/HE/2026/")) "DTDC/HE/2026/${Random.nextInt(1000, 9999)}" else current.employeeCode,
                        aiVerified = true
                    ),
                    statusMessage = "AI verification complete: Security credential validated"
                )
            }
        }
    }

    fun saveCurrentCard() {
        viewModelScope.launch {
            val card = _uiState.value.currentCard
            cardDao.insertCard(card)
            _uiState.update { it.copy(statusMessage = "Badge saved to vault!") }
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            cardDao.deleteCardById(cardId)
        }
    }

    fun loadCard(card: EmployeeIdCard) {
        _uiState.update {
            it.copy(
                currentCard = card,
                activePhotoBitmap = null,
                isFlipped = false,
                statusMessage = "Loaded badge for ${card.fullName}"
            )
        }
    }
}
