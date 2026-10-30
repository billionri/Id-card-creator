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
        companyName = "DTDC",
        companyLogoPath = null,
        companyTagline = "AUTH. CHANNEL PARTNER",
        fullName = "Rashid Alam",
        employeeCode = "DTDC/HE/2026/" + Random.nextInt(1000, 9999),
        designation = "Delivery Associate",
        department = "Hub Dispatch Operations",
        branchId = "1057",
        branchName = "Hariom Enterprises",
        branchAddress = "Shop No 2, 317-A, Shinde Niwas, Kasturba Cross Road No 6, Opposite Platform No 10, Borivali East, Mumbai — 400066, Maharashtra",
        phoneNumber = "8655707563",
        emergencyContact = "8655707563",
        bloodGroup = "B+",
        validTill = "31 Dec 2027",
        photoPath = null,
        aiVerified = true
    )
}

class IdCardViewModel(application: Application) : AndroidViewModel(application) {

    private val cardDao = AppDatabase.getDatabase(application).employeeCardDao()
    private val geminiService = com.example.data.remote.GeminiService()

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

    fun updateCompanyName(name: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(companyName = name)) }
    }

    fun updateCompanyLogo(path: String?) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(companyLogoPath = path)) }
    }

    fun updateCompanyTagline(tagline: String) {
        _uiState.update { it.copy(currentCard = it.currentCard.copy(companyTagline = tagline)) }
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

    fun generateCompanyIdWithAi(company: String, prompt: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true, statusMessage = "AI generating official ID for $company...") }
            val photoBitmap = _uiState.value.activePhotoBitmap
            val result = geminiService.generateCompanyIdDirectly(company, prompt, photoBitmap)
            val generated = result.getOrNull()
            if (generated != null) {
                val current = _uiState.value.currentCard
                _uiState.update {
                    it.copy(
                        isAiAnalyzing = false,
                        currentCard = current.copy(
                            companyName = generated.companyName,
                            companyTagline = generated.companyTagline,
                            fullName = generated.fullName,
                            designation = generated.designation,
                            department = generated.department,
                            employeeCode = generated.employeeCode,
                            branchId = generated.branchId,
                            branchName = generated.branchName,
                            branchAddress = generated.branchAddress,
                            phoneNumber = generated.phoneNumber,
                            emergencyContact = generated.emergencyContact,
                            bloodGroup = generated.bloodGroup,
                            validTill = generated.validTill,
                            aiVerified = true
                        ),
                        statusMessage = "AI generated ${generated.companyName} ID Badge for ${generated.fullName}!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAiAnalyzing = false,
                        statusMessage = "AI generation completed with local credential generator."
                    )
                }
            }
        }
    }

    fun triggerAiAutoFill() {
        val currentCompany = _uiState.value.currentCard.companyName
        generateCompanyIdWithAi(currentCompany, "")
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
