package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Saved Identity Cards",
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
        if (savedCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                        text = "No saved badges yet",
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedCards, key = { it.id }) { cardItem ->
                    SavedCardRow(
                        card = cardItem,
                        onEdit = {
                            viewModel.loadCard(cardItem)
                            onNavigateBack()
                        },
                        onDelete = { viewModel.deleteCard(cardItem.id) },
                        onDownload = { PhotoUtils.saveCardToGallery(context, cardItem, isFront = true) },
                        onShare = { PhotoUtils.shareCard(context, cardItem, isFront = true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedCardRow(
    card: EmployeeIdCard,
    onEdit: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.fullName,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = DtdcNavy
                    )
                    Text(
                        text = "${card.designation} • ${card.employeeCode}",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Branch: ${card.branchId} (${card.branchName})",
                        fontSize = 12.sp,
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

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_card_button_${card.id}")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDownload) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = DtdcNavy)
                }
                IconButton(onClick = onShare) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = DtdcNavy)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = DtdcRed)
                }
            }
        }
    }
}
