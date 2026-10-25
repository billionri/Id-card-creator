package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.EmployeeIdCard
import com.example.ui.theme.DtdcBrandFontFamily
import com.example.ui.theme.DtdcCorporateFontFamily
import com.example.ui.theme.DtdcGold
import com.example.ui.theme.DtdcNavy
import com.example.ui.theme.DtdcRed
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.Slate800

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
        animationSpec = tween(durationMillis = 600),
        label = "CardFlipAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Lanyard & Badge Clip Holder
        if (showLanyard) {
            LanyardHeader()
        }

        // 3D Flip Card Container - dynamically uses available page width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .widthIn(max = 540.dp)
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val h = maxHeight
            val w = maxWidth
            val scale = (w / 320.dp).coerceIn(0.65f, 1.35f)
            val hScale = (h / 508.dp).coerceIn(0.65f, 1.35f)
            val minScale = minOf(scale, hScale)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP HEADER: DTDC Official Logo + Dark Navy Partner Banner + Red Stripe
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // White logo container (Enlarged DTDC logo)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((72 * minScale).dp)
                            .padding(horizontal = (6 * minScale).dp, vertical = (2 * minScale).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dtdc_logo),
                            contentDescription = "DTDC Official Brand Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Dark Navy Banner: AUTH. CHANNEL PARTNER: HARIOM ENTERPRISES
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DtdcNavy)
                            .padding(vertical = (4.5 * minScale).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AUTH. CHANNEL PARTNER: ${card.branchName.uppercase()}",
                            color = DtdcGold,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (10 * minScale).sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (0.5 * minScale).sp
                        )
                    }

                    // Red Accent Stripe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((3 * minScale).dp)
                            .background(DtdcRed)
                    )
                }

                // CENTER CONTENT: Photo, Name, Designation Capsule, Department, 2-Column Detail Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (16 * minScale).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height((4 * minScale).dp))

                    // Centered Employee Photo with Dark Navy Rounded Border
                    Box(
                        modifier = Modifier
                            .width((138 * minScale).dp)
                            .height((164 * minScale).dp)
                            .shadow(elevation = (3 * minScale).dp, shape = RoundedCornerShape((10 * minScale).dp))
                            .clip(RoundedCornerShape((10 * minScale).dp))
                            .background(Color(0xFFE2E8F0))
                            .border((2.2 * minScale).dp, DtdcNavy, RoundedCornerShape((10 * minScale).dp)),
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
                                    modifier = Modifier.size((60 * minScale).dp)
                                )
                                Text(
                                    text = "PASSPORT PHOTO",
                                    fontSize = (9.5 * minScale).sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Generous spacing below photo and above name
                    Spacer(modifier = Modifier.height((13 * minScale).dp))

                    // Employee Full Name Centered in Dark Navy
                    Text(
                        text = card.fullName.ifBlank { "Rashid Alam" },
                        color = DtdcNavy,
                        fontFamily = DtdcBrandFontFamily,
                        fontSize = (18 * minScale).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (0.2 * minScale).sp
                    )

                    Spacer(modifier = Modifier.height((4 * minScale).dp))

                    // Red Designation Capsule Pill
                    Box(
                        modifier = Modifier
                            .background(DtdcRed, shape = RoundedCornerShape((14 * minScale).dp))
                            .padding(horizontal = (18 * minScale).dp, vertical = (3.5 * minScale).dp)
                    ) {
                        Text(
                            text = card.designation.uppercase().ifBlank { "DELIVERY ASSOCIATE" },
                            color = Color.White,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (9.5 * minScale).sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (0.4 * minScale).sp
                        )
                    }

                    Spacer(modifier = Modifier.height((3 * minScale).dp))

                    // Department Subtitle
                    Text(
                        text = card.department.ifBlank { "Hub Dispatch Operations" },
                        color = Color(0xFF1E293B),
                        fontFamily = DtdcBrandFontFamily,
                        fontSize = (11 * minScale).sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height((8 * minScale).dp))

                    // 2-COLUMN KEY-VALUE GRID (Exact match to reference image!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (12 * minScale).dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Column: EMP ID and CONTACT
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "EMP ID:",
                                color = Color(0xFF475569),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (9.5 * minScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.employeeCode.ifBlank { "DTDC/HE/2024/1609" },
                                color = Color(0xFF0F172A),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (12.5 * minScale).sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height((5 * minScale).dp))

                            Text(
                                text = "CONTACT:",
                                color = Color(0xFF475569),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (9.5 * minScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.phoneNumber.ifBlank { "8655707563" },
                                color = Color(0xFF0F172A),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (12.5 * minScale).sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height((5 * minScale).dp))

                            Text(
                                text = "BRANCH ID:",
                                color = Color(0xFF475569),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (9.5 * minScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.branchId.ifBlank { "1057" },
                                color = Color(0xFF0F172A),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (12.5 * minScale).sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width((8 * minScale).dp))

                        // Right Column: BLOOD GRP and VALID TILL
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text(
                                text = "BLOOD GRP:",
                                color = Color(0xFF475569),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (9.5 * minScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.bloodGroup.ifBlank { "B+" },
                                color = Color(0xFF0F172A),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (12.5 * minScale).sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height((6 * minScale).dp))

                            Text(
                                text = "VALID TILL:",
                                color = Color(0xFF475569),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (9.5 * minScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.validTill.ifBlank { "31 Dec 2027" },
                                color = Color(0xFF0F172A),
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (12.5 * minScale).sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // SOLID DARK NAVY FOOTER (Edge-to-edge as in reference image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DtdcNavy)
                        .padding(vertical = (6 * minScale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AUTHORIZED IDENTITY BADGE • DTDC",
                            color = Color.White,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (10.5 * minScale).sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (0.5 * minScale).sp
                        )
                        Spacer(modifier = Modifier.height((1 * minScale).dp))
                        Text(
                            text = "Branch: ${card.branchId.ifBlank { "1057" }} | ${card.branchName.ifBlank { "Hariom Enterprises" }}",
                            color = DtdcGold,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (9.5 * minScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    showDivider: Boolean = true,
    isEmergency: Boolean = false,
    scale: Float = 1f
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = (1 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                color = if (isEmergency) DtdcRed else DtdcNavy,
                fontFamily = DtdcBrandFontFamily,
                fontSize = (11.5 * scale).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width((84 * scale).dp)
            )
            Text(
                text = value,
                color = if (isEmergency) DtdcRed else Color(0xFF1E293B),
                fontFamily = DtdcCorporateFontFamily,
                fontSize = (11.5 * scale).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((0.6 * scale).dp)
                    .background(Color(0xFFE2E8F0))
            )
        }
    }
}

@Composable
fun IdCardBack(card: EmployeeIdCard) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val h = maxHeight
            val w = maxWidth
            val scale = (w / 320.dp).coerceIn(0.65f, 1.35f)
            val hScale = (h / 508.dp).coerceIn(0.65f, 1.35f)
            val minScale = minOf(scale, hScale)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = (3 * minScale).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP HEADER: DTDC Official Logo + Dark Navy Banner + Red Stripe
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // White logo container (Enlarged DTDC logo)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((72 * minScale).dp)
                            .padding(horizontal = (6 * minScale).dp, vertical = (2 * minScale).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dtdc_logo),
                            contentDescription = "DTDC Official Brand Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DtdcNavy)
                            .padding(vertical = (4.5 * minScale).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TERMS OF IDENTITY & CONSIGNMENT AUTHORITY",
                            color = DtdcGold,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (9 * minScale).sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (0.4 * minScale).sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((3 * minScale).dp)
                            .background(DtdcRed)
                    )
                }

                // Center section: Authorization & Policies + Hub Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (12 * minScale).dp, vertical = (1 * minScale).dp)
                ) {
                    Text(
                        text = "AUTHORIZATION NOTICE:",
                        color = DtdcNavy,
                        fontFamily = DtdcBrandFontFamily,
                        fontSize = (11 * minScale).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (0.4 * minScale).sp
                    )

                    Spacer(modifier = Modifier.height((1.5 * minScale).dp))

                    val policies = listOf(
                        "1. Authorized delivery representative of HARIOM ENTERPRISES (DTDC Partner).",
                        "2. Identity card is non-transferable and must be presented on demand.",
                        "3. Authorized to handle courier, parcels, and express mail delivery.",
                        "4. If found, return to Hub Office address below or call helpline."
                    )

                    policies.forEach { policy ->
                        Text(
                            text = policy,
                            color = Color(0xFF1E293B),
                            fontFamily = DtdcCorporateFontFamily,
                            fontSize = (8.5 * minScale).sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = (11.5 * minScale).sp,
                            modifier = Modifier.padding(vertical = (0.5 * minScale).dp)
                        )
                    }

                    Spacer(modifier = Modifier.height((3 * minScale).dp))

                    // Hub Address Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape((6 * minScale).dp))
                            .border((1 * minScale).dp, Color(0xFFCBD5E1), RoundedCornerShape((6 * minScale).dp))
                            .padding((6 * minScale).dp)
                    ) {
                        Column {
                            Text(
                                text = "HUB OFFICE & RETURN ADDRESS (BRANCH ID: ${card.branchId}):",
                                color = DtdcNavy,
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (9 * minScale).sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (0.3 * minScale).sp
                            )
                            Spacer(modifier = Modifier.height((0.5 * minScale).dp))
                            Text(
                                text = card.branchName.uppercase(),
                                color = DtdcRed,
                                fontFamily = DtdcBrandFontFamily,
                                fontSize = (13.5 * minScale).sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (0.5 * minScale).sp
                            )
                            Spacer(modifier = Modifier.height((0.5 * minScale).dp))
                            Text(
                                text = card.branchAddress,
                                color = Color(0xFF0F172A),
                                fontFamily = DtdcCorporateFontFamily,
                                fontSize = (8.5 * minScale).sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = (11.5 * minScale).sp
                            )
                            Spacer(modifier = Modifier.height((3 * minScale).dp))

                            // Dedicated Contact Section
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape((4 * minScale).dp),
                                border = BorderStroke((0.8 * minScale).dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = (6 * minScale).dp, vertical = (3.5 * minScale).dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "CONTACT NO:",
                                            color = DtdcNavy,
                                            fontFamily = DtdcBrandFontFamily,
                                            fontSize = (9.5 * minScale).sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = card.phoneNumber,
                                            color = Color(0xFF0F172A),
                                            fontFamily = DtdcCorporateFontFamily,
                                            fontSize = (9.5 * minScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height((1.5 * minScale).dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "EMERGENCY HELPLINE:",
                                            color = DtdcRed,
                                            fontFamily = DtdcBrandFontFamily,
                                            fontSize = (9.5 * minScale).sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = card.emergencyContact,
                                            color = DtdcRed,
                                            fontFamily = DtdcBrandFontFamily,
                                            fontSize = (9.5 * minScale).sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Barcode container with employee code
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (12 * minScale).dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape((6 * minScale).dp))
                        .border((1 * minScale).dp, Color(0xFFCBD5E1), RoundedCornerShape((6 * minScale).dp))
                        .padding(horizontal = (8 * minScale).dp, vertical = (3 * minScale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DtdcBarcodeGraphic(code = card.employeeCode, modifier = Modifier.fillMaxWidth(0.92f).height((22 * minScale).dp))
                        Spacer(modifier = Modifier.height((1 * minScale).dp))
                        Text(
                            text = "* ${card.employeeCode} • BRANCH: ${card.branchId} *",
                            color = Color.Black,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (9.5 * minScale).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (0.8 * minScale).sp
                        )
                    }
                }

                // SOLID DARK NAVY FOOTER (Edge-to-edge as in reference image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DtdcNavy)
                        .padding(vertical = (6 * minScale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AUTHORIZED IDENTITY BADGE • DTDC",
                            color = Color.White,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (10.5 * minScale).sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (0.5 * minScale).sp
                        )
                        Spacer(modifier = Modifier.height((1 * minScale).dp))
                        Text(
                            text = "Branch: ${card.branchId.ifBlank { "1057" }} | ${card.branchName.ifBlank { "Hariom Enterprises" }}",
                            color = DtdcGold,
                            fontFamily = DtdcBrandFontFamily,
                            fontSize = (9.5 * minScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DtdcBarcodeGraphic(
    code: String,
    modifier: Modifier = Modifier
        .fillMaxWidth(0.9f)
        .height(30.dp)
) {
    Canvas(modifier = modifier) {
        val bars = 45
        val barWidth = size.width / bars
        val seed = code.hashCode()
        var prng = kotlin.math.abs(seed)

        for (i in 0 until bars) {
            prng = (prng * 31 + 17) and 0x7fffffff
            val isBlack = (i % 2 == 0) || (prng % 3 != 0)
            if (isBlack) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(i * barWidth, 0f),
                    size = Size(barWidth * 0.85f, size.height)
                )
            }
        }
    }
}
