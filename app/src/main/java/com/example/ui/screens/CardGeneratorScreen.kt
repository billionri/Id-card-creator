package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.InteractiveIdCard
import com.example.ui.theme.DtdcGold
import com.example.ui.theme.DtdcNavy
import com.example.ui.theme.DtdcRed
import com.example.ui.theme.SecurityGreen
import com.example.ui.viewmodel.IdCardViewModel
import com.example.util.PhotoUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardGeneratorScreen(
    viewModel: IdCardViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val card = uiState.currentCard

    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    // Official Android zero-permission Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = PhotoUtils.uriToBitmap(context, uri)
            if (bitmap != null) {
                viewModel.onPassportPhotoSelected(bitmap)
            }
        }
    }

    // Camera take-picture thumbnail launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onPassportPhotoSelected(bitmap)
        }
    }

    LaunchedEffect(uiState.cardSaveSuccess) {
        if (uiState.cardSaveSuccess) {
            snackbarHostState.showSnackbar("ID Card saved to collection successfully!")
            viewModel.dismissSaveSuccess()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // Corporate Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.dtdc_hero_banner),
                contentDescription = "DTDC Hariom Enterprises Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xDD0A235C))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = DtdcRed,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "DTDC FRANCHISEE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HUB: DTDC-HE-4102",
                        color = DtdcGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Hariom Enterprises ID Studio",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Passport Photo Upload Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .testTag("photo_upload_section_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Passport Photo & AI Builder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Upload photo to auto-generate employee badge",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Upload Buttons Row
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
                            .testTag("upload_gallery_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DtdcNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery")
                    }

                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("take_camera_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Take Photo")
                    }
                }

                // AI Auto-Fill / Re-analyze Button
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.triggerAiAutoFill() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_autofill_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DtdcRed,
                        contentColor = Color.White
                    ),
                    enabled = !uiState.isAnalyzing
                ) {
                    if (uiState.isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Analyzing Passport Photo...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Generate",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Auto-Build Credentials")
                    }
                }

                // AI Status Message Banner
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
                                style = MaterialTheme.typography.bodySmall,
                                color = if (card.aiVerified) Color(0xFF1B5E20) else DtdcNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                fontWeight = FontWeight.Bold,
                color = DtdcNavy
            )

            // Lanyard Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lanyard",
                    style = MaterialTheme.typography.labelSmall,
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
                Text("Save")
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
                Text("Export")
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
                Text("Share")
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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

                Spacer(modifier = Modifier.height(12.dp))

                // Full Name
                OutlinedTextField(
                    value = card.fullName,
                    onValueChange = { viewModel.updateFullName(it) },
                    label = { Text("Employee Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_full_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Designation with Preset Chips
                Text(
                    text = "Designation / Role",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val presetRoles = listOf(
                    "Delivery Associate",
                    "Field Operations Executive",
                    "Hub Supervisor",
                    "Senior Courier Specialist",
                    "Logistics Coordinator"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetRoles.forEach { role ->
                        FilterChip(
                            selected = card.designation.equals(role, ignoreCase = true),
                            onClick = { viewModel.updateDesignation(role) },
                            label = { Text(role, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DtdcNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = card.designation,
                    onValueChange = { viewModel.updateDesignation(it) },
                    label = { Text("Custom Designation") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("input_designation")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Department
                OutlinedTextField(
                    value = card.department,
                    onValueChange = { viewModel.updateDepartment(it) },
                    label = { Text("Department") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_department")
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
                        label = { Text("Employee ID Code") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_employee_code")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.generateNewCode() },
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .testTag("generate_code_button")
                    ) {
                        Text("New ID")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Blood Group Selection Chips
                Text(
                    text = "Blood Group",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val bloodGroups = listOf("O+", "B+", "A+", "AB+", "O-", "B-", "A-", "AB-")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bloodGroups.forEach { bg ->
                        FilterChip(
                            selected = card.bloodGroup == bg,
                            onClick = { viewModel.updateBloodGroup(bg) },
                            label = { Text(bg, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DtdcRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Phone & Emergency Contact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = card.phoneNumber,
                        onValueChange = { viewModel.updatePhone(it) },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_phone")
                    )
                    OutlinedTextField(
                        value = card.emergencyContact,
                        onValueChange = { viewModel.updateEmergencyContact(it) },
                        label = { Text("Emergency Contact") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_emergency")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Valid Till
                OutlinedTextField(
                    value = card.validTill,
                    onValueChange = { viewModel.updateValidTill(it) },
                    label = { Text("Badge Valid Till") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_valid_till")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Branch Address
                OutlinedTextField(
                    value = card.branchAddress,
                    onValueChange = { viewModel.updateBranchAddress(it) },
                    label = { Text("Branch Hub Address (Hariom Enterprises)") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_branch_address")
                )
            }
        }
    }
}
