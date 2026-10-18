package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveIdCard
import com.example.ui.theme.DtdcNavy
import com.example.ui.theme.DtdcRed
import com.example.ui.theme.SecurityGreen
import com.example.ui.viewmodel.IdCardViewModel
import com.example.util.PhotoUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardGeneratorScreen(
    viewModel: IdCardViewModel,
    onNavigateToSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val card = uiState.currentCard
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val file = File(context.filesDir, "passport_photo_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, fos)
                fos.close()

                viewModel.updatePhoto(bitmap, file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "App Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hariom ID Card",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DtdcNavy
                ),
                actions = {
                    IconButton(
                        onClick = onNavigateToSaved,
                        modifier = Modifier.testTag("saved_badges_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = "Saved Badges",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Photo Upload & AI Badge Verification Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Employee Passport Photo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = DtdcNavy,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("upload_photo_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DtdcNavy)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Photo", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.triggerAiAutoFill() },
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("ai_autofill_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DtdcRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Auto-Build",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Auto-Build", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = uiState.statusMessage != null) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (card.aiVerified) Color(0xFFE8F5E9) else Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (card.aiVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (card.aiVerified) SecurityGreen else DtdcNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.statusMessage ?: "",
                                    fontSize = 14.sp,
                                    color = if (card.aiVerified) Color(0xFF1B5E20) else DtdcNavy,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ID Card Live Preview Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Live Official Badge Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = DtdcNavy
                )

                // Lanyard Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lanyard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = uiState.showLanyard,
                        onCheckedChange = { viewModel.toggleLanyard() },
                        modifier = Modifier.testTag("lanyard_toggle_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive 3D Card
            InteractiveIdCard(
                card = card,
                photoBitmap = uiState.activePhotoBitmap,
                isFlipped = uiState.isFlipped,
                showLanyard = uiState.showLanyard,
                onFlipClick = { viewModel.toggleCardFlip() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Export & Save Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveCurrentCard() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_card_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DtdcNavy)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        PhotoUtils.saveCardToGallery(context, card, isFront = !uiState.isFlipped)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        PhotoUtils.shareCard(context, card, isFront = !uiState.isFlipped)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Employee Details Customizer Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Customize Employee Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DtdcNavy
                        )
                        IconButton(
                            onClick = { viewModel.createNewCard() },
                            modifier = Modifier.testTag("reset_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Form",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name (default Rashid Alam)
                    OutlinedTextField(
                        value = card.fullName,
                        onValueChange = { viewModel.updateFullName(it) },
                        label = { Text("Employee Full Name", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 17.5.sp, fontWeight = FontWeight.SemiBold),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_full_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Employee Code with Auto-Generate Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = card.employeeCode,
                            onValueChange = { viewModel.updateEmployeeCode(it) },
                            label = { Text("Employee ID Code (EMP ID)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            textStyle = TextStyle(fontSize = 17.5.sp, fontWeight = FontWeight.SemiBold),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_employee_code")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.updateEmployeeCode("DTDC/HE/2026/${Random.nextInt(1000, 9999)}") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Generate ID",
                                tint = DtdcNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Designation
                    OutlinedTextField(
                        value = card.designation,
                        onValueChange = { viewModel.updateDesignation(it) },
                        label = { Text("Designation / Role", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_designation")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Department
                    OutlinedTextField(
                        value = card.department,
                        onValueChange = { viewModel.updateDepartment(it) },
                        label = { Text("Department", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_department")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact Number
                    OutlinedTextField(
                        value = card.phoneNumber,
                        onValueChange = { viewModel.updatePhoneNumber(it) },
                        label = { Text("Contact Number (Phone)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_contact_phone")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Emergency Contact
                    OutlinedTextField(
                        value = card.emergencyContact,
                        onValueChange = { viewModel.updateEmergencyContact(it) },
                        label = { Text("Emergency Contact Number", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_emergency_contact")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Blood Group
                    OutlinedTextField(
                        value = card.bloodGroup,
                        onValueChange = { viewModel.updateBloodGroup(it) },
                        label = { Text("Blood Group", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_blood_group")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Branch ID
                    OutlinedTextField(
                        value = card.branchId,
                        onValueChange = { viewModel.updateBranchId(it) },
                        label = { Text("DTDC Branch ID", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_branch_id")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Branch Name
                    OutlinedTextField(
                        value = card.branchName,
                        onValueChange = { viewModel.updateBranchName(it) },
                        label = { Text("Branch / Franchisee Name", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_branch_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Branch Address
                    OutlinedTextField(
                        value = card.branchAddress,
                        onValueChange = { viewModel.updateBranchAddress(it) },
                        label = { Text("Branch Return Address", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_branch_address")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Valid Till
                    OutlinedTextField(
                        value = card.validTill,
                        onValueChange = { viewModel.updateValidTill(it) },
                        label = { Text("Card Validity Date", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_valid_till")
                    )
                }
            }
        }
    }
}
