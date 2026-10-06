package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.EmployeeIdCard
import com.example.ui.components.InteractiveIdCard
import com.example.ui.theme.DtdcNavy
import com.example.ui.theme.DtdcRed
import com.example.ui.theme.SecurityGreen
import com.example.ui.viewmodel.IdCardViewModel
import com.example.util.PhotoUtils

@Composable
fun SavedCardsScreen(
    viewModel: IdCardViewModel,
    onEditCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedCards by viewModel.savedCards.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var cardToPreview by remember { mutableStateOf<EmployeeIdCard?>(null) }
    var previewIsFlipped by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<EmployeeIdCard?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search by name, ID code, or designation") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_cards_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Number of Users Tracker (for personal charging)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = DtdcNavy.copy(alpha = 0.1f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = DtdcNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TOTAL USERS ADDED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${savedCards.size}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = DtdcNavy
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (savedCards.size == 1) "Employee" else "Employees",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "YOUR CHARGEABLE TOTAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "₹${savedCards.size * 150}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "(@ ₹150 / user)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Generated Employee Badges (${savedCards.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DtdcNavy
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (savedCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "No matching employee ID found" else "No saved ID badges yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload a passport photo in the Builder tab to generate and save official DTDC Hariom ID badges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(savedCards, key = { it.id }) { card ->
                    SavedCardItem(
                        card = card,
                        onViewClick = {
                            cardToPreview = card
                            previewIsFlipped = false
                        },
                        onEditClick = {
                            viewModel.loadCardForEditing(card)
                            onEditCard()
                        },
                        onShareClick = {
                            PhotoUtils.shareCard(context, card, isFront = true)
                        },
                        onDownloadClick = {
                            PhotoUtils.saveCardToGallery(context, card, isFront = true)
                        },
                        onDeleteClick = {
                            cardToDelete = card
                        }
                    )
                }
            }
        }
    }

    // Modal Full Card Preview Dialog
    if (cardToPreview != null) {
        Dialog(onDismissRequest = { cardToPreview = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Official Badge Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DtdcNavy
                        )
                        IconButton(onClick = { cardToPreview = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val photoBmp = remember(cardToPreview?.photoPath) {
                        PhotoUtils.loadBitmapFromPath(cardToPreview?.photoPath)
                    }

                    InteractiveIdCard(
                        card = cardToPreview!!,
                        photoBitmap = photoBmp,
                        isFlipped = previewIsFlipped,
                        showLanyard = true,
                        onFlipClick = { previewIsFlipped = !previewIsFlipped }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                PhotoUtils.saveCardToGallery(context, cardToPreview!!, isFront = !previewIsFlipped)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DtdcNavy),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                PhotoUtils.shareCard(context, cardToPreview!!, isFront = !previewIsFlipped)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DtdcRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (cardToDelete != null) {
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Delete Employee Card?") },
            text = { Text("Are you sure you want to delete the ID badge for ${cardToDelete?.fullName} (${cardToDelete?.employeeCode})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        cardToDelete?.let { viewModel.deleteCard(it) }
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DtdcRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SavedCardItem(
    card: EmployeeIdCard,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() }
            .testTag("saved_card_item_${card.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar thumbnail
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0))
                        .border(1.5.dp, DtdcNavy, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!card.photoPath.isNullOrBlank()) {
                        AsyncImage(
                            model = card.photoPath,
                            contentDescription = card.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = card.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DtdcNavy
                        )
                        if (card.aiVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "AI Verified",
                                tint = SecurityGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = card.designation,
                        style = MaterialTheme.typography.bodySmall,
                        color = DtdcRed,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${card.employeeCode} • Blood: ${card.bloodGroup}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Blood Group Badge
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = card.bloodGroup,
                            color = DtdcRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ACTIVE USER",
                            color = Color(0xFF15803D),
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onViewClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View Badge",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Details",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDownloadClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Image",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Badge",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Badge",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
