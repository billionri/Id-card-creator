package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmployeeIdCard
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

    var empIdQuery by remember { mutableStateOf("") }
    var employeePendingDelete by remember { mutableStateOf<EmployeeIdCard?>(null) }

    val matchedEmployee = remember(empIdQuery, uiState.savedCards) {
        if (empIdQuery.isBlank()) null
        else {
            val q = empIdQuery.trim().lowercase()
            uiState.savedCards.firstOrNull {
                it.employeeCode.lowercase().contains(q) ||
                it.employeeCode.filter { ch -> ch.isDigit() }.contains(q) ||
                it.fullName.lowercase().contains(q)
            }
        }
    }

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

    val companyLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val file = File(context.filesDir, "company_logo_${System.currentTimeMillis()}.png")
                val fos = FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()

                viewModel.updateCompanyLogo(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var aiTargetCompany by remember { mutableStateOf(card.companyName.ifBlank { "DTDC" }) }
    var aiInstructionPrompt by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dtdc_app_icon),
                            contentDescription = "Official App Icon",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
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
                    Surface(
                        onClick = onNavigateToSaved,
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("saved_badges_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = "Employee Directory",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Directory (${uiState.savedCards.size})",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            Spacer(modifier = Modifier.height(10.dp))

            // ID Card Live Preview Section Header (Positioned at top to utilize full page immediately)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = DtdcNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Official Badge Preview (Fit to Page)",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Black,
                        color = DtdcNavy
                    )
                }

                // Lanyard Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lanyard",
                        fontSize = 13.5.sp,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive 3D Card - Front & Back (occupies entire page width)
            InteractiveIdCard(
                card = card,
                photoBitmap = uiState.activePhotoBitmap,
                isFlipped = uiState.isFlipped,
                showLanyard = uiState.showLanyard,
                onFlipClick = { viewModel.toggleCardFlip() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Export & Print Action Bar (Responsive Fit to Page & Save)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Action: Print (Fit to Page) & Download High-Res Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            PhotoUtils.printCard(context, card, isFront = !uiState.isFlipped)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("print_card_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DtdcNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print Card",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print (Fit to Page)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                        Text("Download", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Secondary Action: Save to Local Database & Share Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.saveCurrentCard() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Employee",
                            modifier = Modifier.size(18.dp),
                            tint = DtdcNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Record", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = DtdcNavy)
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
                            contentDescription = "Share Badge",
                            modifier = Modifier.size(18.dp),
                            tint = DtdcNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Badge", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = DtdcNavy)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick EMP ID Lookup & Manage Employee Card (Details, Regenerate, Delete)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .testTag("emp_id_quick_lookup_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, if (matchedEmployee != null) SecurityGreen else Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search EMP ID",
                                tint = DtdcNavy,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ask EMP ID to App",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = DtdcNavy
                            )
                        }

                        Surface(
                            color = DtdcNavy.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Details • Regenerate • Delete",
                                color = DtdcNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter any Employee ID or Name to view details, regenerate badge, or remove if employee left.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = empIdQuery,
                        onValueChange = { empIdQuery = it },
                        placeholder = { Text("Enter EMP ID (e.g. 8941, DTDC/HE/...)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = DtdcNavy)
                        },
                        trailingIcon = {
                            if (empIdQuery.isNotEmpty()) {
                                IconButton(onClick = { empIdQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emp_id_quick_search_input")
                    )

                    // Results if Matched
                    if (matchedEmployee != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "✓ EMPLOYEE RECORD FOUND",
                                                color = Color(0xFF1B5E20),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = matchedEmployee.fullName,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = DtdcNavy
                                        )
                                        Text(
                                            text = "EMP ID: ${matchedEmployee.employeeCode}",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DtdcRed
                                        )
                                        Text(
                                            text = "${matchedEmployee.designation} • ${matchedEmployee.department}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155)
                                        )
                                        Text(
                                            text = "Contact: ${matchedEmployee.phoneNumber} • Valid Till: ${matchedEmployee.validTill}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Options for Found Employee: Regenerate, Delete if left, or Preview
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.loadCard(matchedEmployee)
                                            empIdQuery = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DtdcNavy),
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .testTag("quick_regenerate_badge_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Regenerate",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Regenerate ID", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { employeePendingDelete = matchedEmployee },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DtdcRed),
                                        border = BorderStroke(1.dp, DtdcRed),
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .testTag("quick_delete_employee_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = DtdcRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete (Left)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else if (empIdQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "No saved employee matches '$empIdQuery'",
                                    color = Color(0xFF92400E),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateEmployeeCode(empIdQuery.trim())
                                        empIdQuery = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Create New Badge with this EMP ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DYNAMIC COMPANY CREATION & AI DIRECT ID GENERATOR
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEEF2FF),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = "AI Direct ID",
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Direct ID Creator",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DtdcNavy
                                )
                                Text(
                                    text = "Generate ID directly for any company",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF4F46E5).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "✨ Dynamic AI",
                                color = Color(0xFF4F46E5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Quick Company Presets:",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DtdcNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("DTDC", "Hariom Ent.", "Amazon", "FedEx", "Blue Dart").forEach { comp ->
                            val isSelected = card.companyName.equals(comp, ignoreCase = true) || aiTargetCompany.equals(comp, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    aiTargetCompany = comp
                                    viewModel.updateCompanyName(comp)
                                },
                                label = { Text(comp, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DtdcNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Company Name input
                    OutlinedTextField(
                        value = card.companyName,
                        onValueChange = {
                            aiTargetCompany = it
                            viewModel.updateCompanyName(it)
                        },
                        label = { Text("Company Name (e.g. DTDC, Amazon, FedEx)", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = DtdcNavy)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_company_name_ai")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional AI instruction prompt
                    OutlinedTextField(
                        value = aiInstructionPrompt,
                        onValueChange = { aiInstructionPrompt = it },
                        label = { Text("Prompt / Role Instructions (Optional)", fontSize = 14.sp) },
                        placeholder = { Text("e.g. Senior Delivery Partner, Blood O+, Delhi South Hub", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF4F46E5))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_company_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Direct ID Action Button
                    Button(
                        onClick = {
                            viewModel.generateCompanyIdWithAi(
                                company = if (card.companyName.isNotBlank()) card.companyName else aiTargetCompany,
                                prompt = aiInstructionPrompt
                            )
                        },
                        enabled = !uiState.isAiAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_direct_id_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isAiAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI is Generating ID Directly...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ AI Will ID Directly (${card.companyName.ifBlank { "Company" }})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DYNAMIC COMPANY LOGO & BRANDING SECTION
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CorporateFare,
                                contentDescription = "Company Branding",
                                tint = DtdcNavy,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Company Logo & Branding",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = DtdcNavy
                            )
                        }

                        if (card.companyLogoPath != null) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Custom Logo",
                                    color = Color(0xFF1B5E20),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Current Logo Preview + Upload Action
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .size(70.dp, 44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (card.companyLogoPath != null) {
                                            AsyncImage(
                                                model = File(card.companyLogoPath),
                                                contentDescription = "Uploaded Company Logo",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_dtdc_logo),
                                                contentDescription = "Default DTDC Logo",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = if (card.companyLogoPath != null) "Custom Logo Uploaded" else "Default DTDC Logo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = DtdcNavy
                                    )
                                    Text(
                                        text = "Visible on Front & Back header",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        companyLogoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DtdcNavy),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("upload_company_logo_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = "Upload Logo",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload Logo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (card.companyLogoPath != null) {
                                    IconButton(
                                        onClick = { viewModel.updateCompanyLogo(null) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reset Logo",
                                            tint = DtdcRed
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Company Tagline / Partner designation
                    OutlinedTextField(
                        value = card.companyTagline,
                        onValueChange = { viewModel.updateCompanyTagline(it) },
                        label = { Text("Header Tagline (Under Logo)", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        placeholder = { Text("AUTH. CHANNEL PARTNER", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_company_tagline")
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("AUTH. CHANNEL PARTNER", "OFFICIAL IDENTITY BADGE", "DELIVERY PARTNER").forEach { tag ->
                            val isSelected = card.companyTagline.equals(tag, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateCompanyTagline(tag) },
                                label = { Text(tag, fontSize = 10.5.sp) }
                            )
                        }
                    }
                }
            }

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

            Spacer(modifier = Modifier.height(16.dp))

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

                    // Company Name
                    OutlinedTextField(
                        value = card.companyName,
                        onValueChange = { viewModel.updateCompanyName(it) },
                        label = { Text("Company Name", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 17.5.sp, fontWeight = FontWeight.SemiBold),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = DtdcNavy)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_company_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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

                    // Branch ID (Default 1057) - Placed directly after Contact Number as requested
                    OutlinedTextField(
                        value = card.branchId,
                        onValueChange = { viewModel.updateBranchId(it) },
                        label = { Text("DTDC Branch ID (Default: 1057)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_branch_id")
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
                        singleLine = false,
                        maxLines = 4,
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

    // Delete Employee Confirmation Dialog (when an employee has left)
    employeePendingDelete?.let { emp ->
        AlertDialog(
            onDismissRequest = { employeePendingDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Employee",
                    tint = DtdcRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Employee Record?",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = DtdcNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete ${emp.fullName}?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "EMP ID: ${emp.employeeCode}\nDesignation: ${emp.designation}\nBranch: ${emp.branchName}",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Use this when an employee has left Hariom Enterprises. Their saved badges and records will be completely removed.",
                        fontSize = 12.sp,
                        color = DtdcRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCard(emp.id)
                        employeePendingDelete = null
                        empIdQuery = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DtdcRed),
                    modifier = Modifier.testTag("confirm_delete_emp_btn")
                ) {
                    Text("Delete Employee", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeePendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
}

