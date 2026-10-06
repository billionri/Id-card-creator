package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.EmployeeIdCard
import com.example.ui.theme.DtdcGold
import com.example.ui.theme.DtdcNavy
import com.example.ui.theme.DtdcRed
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun InteractiveIdCard(
    card: EmployeeIdCard,
    photoBitmap: Bitmap?,
    isFlipped: Boolean,
    showLanyard: Boolean,
    onFlipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cardFlipAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Lanyard & Badge Clip Holder
        if (showLanyard) {
            LanyardHeader()
        }

        // 3D Flip Card Container
        Box(
            modifier = Modifier
                .width(320.dp)
                .aspectRatio(0.63f) // Standard CR80 badge ratio ~ 1 : 1.58
                .shadow(elevation = 14.dp, shape = RoundedCornerShape(16.dp))
                .clickable { onFlipClick() }
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14f * density
                }
                .testTag("id_card_preview_card")
        ) {
            if (rotation <= 90f) {
                // FRONT SIDE
                IdCardFront(
                    card = card,
                    photoBitmap = photoBitmap
                )
            } else {
                // BACK SIDE (Mirrored so text reads normally after 180 flip)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                ) {
                    IdCardBack(card = card)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tap hint with Flip icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onFlipClick() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("flip_card_pill")
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Flip Card",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isFlipped) "Showing Back (Tap to see Front)" else "Showing Front (Tap to see Back)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LanyardHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // DTDC Branded Lanyard Ribbon
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(34.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(DtdcRed, DtdcNavy, DtdcRed)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DTDC",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Metal Clip Ring
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(10.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFCBD5E1), Color(0xFFF1F5F9), Color(0xFF94A3B8))
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        )

        // Punch Slot
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(8.dp)
                .offset(y = (-2).dp)
                .background(Color(0xFF334155), shape = RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun IdCardFront(
    card: EmployeeIdCard,
    photoBitmap: Bitmap?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Official DTDC Company Logo with Franchisee Partner Ribbon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dtdc_logo),
                        contentDescription = "DTDC Official Company Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(50.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DtdcNavy, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AUTH. CHANNEL PARTNER: ${card.branchName.uppercase()}",
                            color = DtdcGold,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom Red Chevron Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(DtdcRed)
                        .align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Passport Photo Section with Security Framing
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 138.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0))
                    .border(2.5.dp, DtdcNavy, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (photoBitmap != null) {
                    Image(
                        bitmap = photoBitmap.asImageBitmap(),
                        contentDescription = "Employee Passport Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!card.photoPath.isNullOrBlank()) {
                    AsyncImage(
                        model = card.photoPath,
                        contentDescription = "Employee Passport Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No Photo",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "PASSPORT PHOTO",
                            fontSize = 8.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Holographic Seal Overlay in top-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(30.dp)
                        .clip(CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hologram_badge),
                        contentDescription = "Security Hologram",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // AI Verified Badge in bottom-left
                if (card.aiVerified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .background(SecurityGreen, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "AI PASS",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Employee Full Name
            Text(
                text = card.fullName.ifBlank { "Employee Name" },
                color = DtdcNavy,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Designation Badge Pill
            Surface(
                color = DtdcRed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text(
                    text = card.designation.uppercase().ifBlank { "DELIVERY ASSOCIATE" },
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Department
            Text(
                text = card.department.ifBlank { "Express Logistics & Dispatch" },
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, start = 8.dp, end = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of Essential Employee Details & QR Code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Key-Value Table
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(label = "EMP ID", value = card.employeeCode)
                    DetailRow(label = "BLOOD GRP", value = card.bloodGroup)
                    DetailRow(label = "CONTACT", value = card.phoneNumber)
                    DetailRow(label = "VALID TILL", value = card.validTill)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Canvas-drawn QR Code Graphic
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DtdcQrCodeGraphic(seed = card.employeeCode + card.fullName)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Official Security Microtext & Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(DtdcNavy),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Secure",
                        tint = DtdcGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BRANCH: ${card.branchCode} • AUTHENTIC DTDC IDENTITY",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            color = Color(0xFF64748B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(62.dp)
        )
        Text(
            text = value,
            color = Color(0xFF0F172A),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IdCardBack(card: EmployeeIdCard) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(DtdcRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TERMS OF IDENTIFICATION",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "AUTHORIZATION NOTICE:",
                    color = DtdcGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                val policies = listOf(
                    "1. The bearer is an authorized delivery representative of Hariom Enterprises (DTDC Channel Partner).",
                    "2. This identity card is strictly non-transferable and must be presented on demand.",
                    "3. Authorized to handle courier, parcels, and express mail delivery.",
                    "4. If found, please return to the Hub Office address listed below or call helpline."
                )

                policies.forEach { policy ->
                    Text(
                        text = policy,
                        color = Color(0xFFE2E8F0),
                        fontSize = 8.sp,
                        lineHeight = 11.sp,
                        modifier = Modifier.padding(vertical = 1.5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Hub Address Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    Column {
                        Text(
                            text = "HUB OFFICE & RETURN ADDRESS:",
                            color = DtdcGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${card.branchName}\n${card.branchAddress}",
                            color = Color.White,
                            fontSize = 8.sp,
                            lineHeight = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "EMERGENCY: ${card.emergencyContact}",
                            color = Color(0xFF38BDF8),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Barcode container with employee code
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DtdcBarcodeGraphic(code = card.employeeCode)
                    Text(
                        text = "* ${card.employeeCode} *",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Authorized Signature Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "BLOOD GROUP",
                        color = Color(0xFF94A3B8),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.bloodGroup,
                        color = DtdcRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Stylized signature simulation
                    Text(
                        text = "Hariom Kr.",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .height(1.dp)
                            .background(Color(0xFF64748B))
                    )
                    Text(
                        text = "AUTHORIZED SIGNATORY",
                        color = Color(0xFF94A3B8),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * Custom Canvas-rendered Barcode lines
 */
@Composable
fun DtdcBarcodeGraphic(code: String) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
    ) {
        val totalWidth = size.width
        val barcodeHeight = size.height
        val seed = code.hashCode()
        var currentRand = kotlin.math.abs(seed)

        var x = 4f
        while (x < totalWidth - 8f) {
            val barWidth = ((currentRand % 3) + 1) * 2.2f
            currentRand = (currentRand * 31 + 17) and 0x7fffffff
            val isGap = (currentRand % 5) == 0

            if (!isGap) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, barcodeHeight)
                )
            }
            x += barWidth + ((currentRand % 2) + 1) * 2f
            currentRand = (currentRand * 31 + 17) and 0x7fffffff
        }
    }
}

/**
 * Custom Canvas-rendered 2D QR Code Matrix
 */
@Composable
fun DtdcQrCodeGraphic(seed: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val matrixSize = 21 // 21x21 QR matrix
        val cellSize = size.width / matrixSize.toFloat()
        var rand = kotlin.math.abs(seed.hashCode())

        // Background
        drawRect(Color.White)

        for (row in 0 until matrixSize) {
            for (col in 0 until matrixSize) {
                // Draw 3 corner finder patterns
                val isTopLeftFinder = (row in 0..6 && col in 0..6)
                val isTopRightFinder = (row in 0..6 && col >= matrixSize - 7)
                val isBottomLeftFinder = (row >= matrixSize - 7 && col in 0..6)

                val shouldDraw = when {
                    isTopLeftFinder -> isFinderPatternCell(row, col)
                    isTopRightFinder -> isFinderPatternCell(row, col - (matrixSize - 7))
                    isBottomLeftFinder -> isFinderPatternCell(row - (matrixSize - 7), col)
                    else -> {
                        rand = (rand * 31 + 13) and 0x7fffffff
                        (rand % 2) == 1
                    }
                }

                if (shouldDraw) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(col * cellSize, row * cellSize),
                        size = Size(cellSize + 0.5f, cellSize + 0.5f)
                    )
                }
            }
        }
    }
}

private fun isFinderPatternCell(r: Int, c: Int): Boolean {
    if (r == 0 || r == 6 || c == 0 || c == 6) return true
    if (r in 2..4 && c in 2..4) return true
    return false
}
