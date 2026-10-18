package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmployeeIdCard
import com.example.ui.theme.DtdcNavy
import com.example.ui.theme.DtdcRed
import com.example.ui.viewmodel.IdCardViewModel
import com.example.util.PhotoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCardsScreen(
    viewModel: IdCardViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val savedCards = uiState.savedCards
    var searchQuery by remember { mutableStateOf("") }
    var cardPendingDelete by remember { mutableStateOf<EmployeeIdCard?>(null) }

    BackHandler {
        onNavigateBack()
    }

    val filteredCards = remember(savedCards, searchQuery) {
        if (searchQuery.isBlank()) {
            savedCards
        } else {
            val q = searchQuery.trim().lowercase()
            savedCards.filter { card ->
                card.employeeCode.lowercase().contains(q) ||
                card.fullName.lowercase().contains(q) ||
                card.phoneNumber.lowercase().contains(q) ||
                card.designation.lowercase().contains(q) ||
                card.department.lowercase().contains(q) ||
                card.branchId.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Employee Directory & Badges",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DtdcNavy)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // EMP ID & Name Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by EMP ID (e.g. DTDC/HE/...), Name, Phone...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = DtdcNavy)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .testTag("search_emp_id_input")
            )

            if (savedCards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "No Badges",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved employees yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate and save employee badges from the editor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else if (filteredCards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Not found",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No employee found matching '$searchQuery'",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Check the Employee ID or name and try again",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Employees: ${filteredCards.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Tap 'Regenerate' to edit or re-issue ID",
                        fontSize = 11.5.sp,
                        color = DtdcNavy,
                        fontWeight = FontWeight.Medium
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCards, key = { it.id }) { cardItem ->
                        SavedCardRow(
                            card = cardItem,
                            onRegenerate = {
                                viewModel.loadCard(cardItem)
                                onNavigateBack()
                            },
                            onDelete = { cardPendingDelete = cardItem },
                            onDownload = { PhotoUtils.saveCardToGallery(context, cardItem, isFront = true) },
                            onShare = { PhotoUtils.shareCard(context, cardItem, isFront = true) }
                        )
                    }
                }
            }
        }
    }

    // Delete Employee Confirmation Dialog (when employee has left)
    cardPendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardPendingDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Icon",
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
                        text = "Are you sure you want to delete ${card.fullName}?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EMP ID: ${card.employeeCode}\nDesignation: ${card.designation}\nBranch: ${card.branchName}",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Use this when an employee has left the company. Their saved badge and records will be removed.",
                        fontSize = 12.sp,
                        color = DtdcRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCard(card.id)
                        cardPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DtdcRed),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete Employee", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SavedCardRow(
    card: EmployeeIdCard,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_card_item_${card.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.fullName,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = DtdcNavy
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // EMP ID Pill Badge
                    Surface(
                        color = DtdcNavy.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "EMP ID: ${card.employeeCode}",
                            color = DtdcNavy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${card.designation} • ${card.department}",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Phone: ${card.phoneNumber} • Blood Grp: ${card.bloodGroup}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Branch: ${card.branchId} • Valid Till: ${card.validTill}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Regenerate/Edit, Download, Share, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRegenerate,
                    modifier = Modifier.testTag("edit_card_button_${card.id}")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Regenerate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDownload) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download Card", tint = DtdcNavy)
                    }
                    IconButton(onClick = onShare) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Card", tint = DtdcNavy)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DtdcRed),
                        border = BorderStroke(1.dp, DtdcRed.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("delete_card_button_${card.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = DtdcRed, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete (Left)", fontSize = 12.sp, color = DtdcRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
