package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import com.example.R
import com.example.data.model.EmployeeIdCard
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PhotoUtils {

    fun createCardBitmap(context: Context, card: EmployeeIdCard, isFront: Boolean): Bitmap {
        val width = 1000
        val height = 1580
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val brandTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val corporateTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }

        val textPaint = Paint().apply {
            isAntiAlias = true
            typeface = brandTypeface
        }

        if (isFront) {
            // FRONT SIDE: Pure White Background with Rounded Corners
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Header Section: Prominent, fully visible header with enlarged DTDC logo
            bgPaint.color = Color.WHITE
            canvas.drawRect(0f, 0f, width.toFloat(), 340f, bgPaint)

            // Top Corporate Title above Logo: "DTDC Private Limited" (black) + "Cargo" (red)
            val titleTextBlack = "DTDC Private Limited "
            val titleTextRed = "Cargo"
            val titleBlackPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0F172A") // Black
                textSize = 36f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val titleRedPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F") // Red
                textSize = 36f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val totalTitleWidth = titleBlackPaint.measureText(titleTextBlack) + titleRedPaint.measureText(titleTextRed)
            val titleStartX = (width - totalTitleWidth) / 2f
            canvas.drawText(titleTextBlack, titleStartX, 64f, titleBlackPaint)
            canvas.drawText(titleTextRed, titleStartX + titleBlackPaint.measureText(titleTextBlack), 64f, titleRedPaint)

            val logoBmp = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val maxW = width - 60f
                val maxH = 185f
                val aspect = logoBmp.width.toFloat() / logoBmp.height.toFloat()
                var drawW = maxH * aspect
                var drawH = maxH
                if (drawW > maxW) {
                    drawW = maxW
                    drawH = drawW / aspect
                }
                // DTDC logo big and right-aligned
                val rightMargin = 35f
                val left = width - rightMargin - drawW
                val top = 76f + (185f - drawH) / 2f
                val logoRect = RectF(left, top, left + drawW, top + drawH)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            }

            // Franchisee Partner Ribbon (Solid Navy with Gold & White Typography matching header)
            bgPaint.color = Color.parseColor("#0A235C")
            canvas.drawRect(0f, 268f, width.toFloat(), 332f, bgPaint)

            val ribbonLabelPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#E2E8F0")
                typeface = brandTypeface
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
                textSize = 25f
            }
            val ribbonBranchPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#FFB300")
                typeface = brandTypeface
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
                textSize = 30f
            }
            val ribbonIdPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#E2E8F0")
                typeface = brandTypeface
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
                textSize = 25f
            }
            val lblText = "AUTH. CHANNEL PARTNER: "
            val branchText = card.branchName.uppercase()
            val idText = " • BRANCH: ${card.branchId}"
            val totalRibbonW = ribbonLabelPaint.measureText(lblText) + ribbonBranchPaint.measureText(branchText) + ribbonIdPaint.measureText(idText)
            var curX = (width - totalRibbonW) / 2f
            canvas.drawText(lblText, curX, 308f, ribbonLabelPaint)
            curX += ribbonLabelPaint.measureText(lblText)
            canvas.drawText(branchText, curX, 308f, ribbonBranchPaint)
            curX += ribbonBranchPaint.measureText(branchText)
            canvas.drawText(idText, curX, 308f, ribbonIdPaint)

            // Red Chevron Accent Stripe
            bgPaint.color = Color.parseColor("#D32F2F")
            canvas.drawRect(0f, 332f, width.toFloat(), 340f, bgPaint)

            // CENTERED PASSPORT PHOTO - positioned cleanly below enlarged header
            val photoW = 370f
            val photoH = 435f
            val photoLeft = (width - photoW) / 2f
            val photoTop = 370f
            val photoBox = RectF(photoLeft, photoTop, photoLeft + photoW, photoTop + photoH)
            val photoPaint = Paint().apply { isAntiAlias = true }
            photoPaint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(photoBox, 18f, 18f, photoPaint)

            val photoBitmap = loadBitmapFromPath(card.photoPath)
            if (photoBitmap != null) {
                val srcRect = Rect(0, 0, photoBitmap.width, photoBitmap.height)
                canvas.drawBitmap(photoBitmap, srcRect, photoBox, null)
            } else {
                val placeholderPaint = Paint().apply {
                    color = Color.parseColor("#94A3B8")
                    textAlign = Paint.Align.CENTER
                    textSize = 38f
                    typeface = brandTypeface
                    isFakeBoldText = true
                }
                canvas.drawText("PASSPORT PHOTO", photoBox.centerX(), photoBox.centerY() + 14f, placeholderPaint)
            }

            // Photo Border (Navy)
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 5f
                color = Color.parseColor("#0A235C")
                isAntiAlias = true
            }
            canvas.drawRoundRect(photoBox, 18f, 18f, borderPaint)

            // Holographic Seal Overlay at top-right corner of photo
            val holoBmp = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_hologram_badge)
            } catch (e: Exception) {
                null
            }
            if (holoBmp != null) {
                val holoBox = RectF(photoBox.right - 68f, photoBox.top + 12f, photoBox.right - 12f, photoBox.top + 68f)
                canvas.drawBitmap(holoBmp, null, holoBox, null)
            }

            // AI Verified Badge in bottom-left corner of photo if verified
            if (card.aiVerified) {
                val badgeBox = RectF(photoBox.left + 12f, photoBox.bottom - 42f, photoBox.left + 145f, photoBox.bottom - 10f)
                val badgePaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#16A34A")
                }
                canvas.drawRoundRect(badgeBox, 8f, 8f, badgePaint)
                val badgeTextPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.WHITE
                    typeface = brandTypeface
                    textSize = 20f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("✔ AI PASS", badgeBox.centerX(), badgeBox.centerY() + 7f, badgeTextPaint)
            }

            // EMPLOYEE DETAILS BELOW PHOTO (Cleanly centered and stacked)
            textPaint.apply {
                color = Color.parseColor("#0A235C")
                textSize = 48f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            // Employee Full Name
            val displayName = card.fullName.ifBlank { "Rashid Alam" }
            canvas.drawText(displayName, width / 2f, 860f, textPaint)

            // Designation Badge Pill Centered
            val desText = card.designation.uppercase().ifBlank { "DELIVERY ASSOCIATE" }
            val desPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.WHITE
                textSize = 27f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val desWidth = desPaint.measureText(desText) + 44f
            val desBox = RectF((width - desWidth) / 2f, 885f, (width + desWidth) / 2f, 938f)
            bgPaint.color = Color.parseColor("#D32F2F")
            canvas.drawRoundRect(desBox, 12f, 12f, bgPaint)
            canvas.drawText(desText, width / 2f, 924f, desPaint)

            // Department Centered
            val deptPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = Color.parseColor("#475569")
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(card.department.ifBlank { "Express Logistics & Dispatch" }, width / 2f, 974f, deptPaint)

            // Thin Accent Separator Line
            val sepPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 2.5f
            }
            canvas.drawLine(100f, 1004f, 900f, 1004f, sepPaint)

            // Employee Key-Value Detail Table Below Separator
            val labelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val valuePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0F172A")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val leftColX = 140f
            val valColX = 450f
            var rowY = 1052f
            val rowSpacing = 46f

            // EMP ID
            canvas.drawText("EMP ID:", leftColX, rowY, labelPaint)
            canvas.drawText(card.employeeCode, valColX, rowY, valuePaint)
            rowY += rowSpacing

            // CONTACT
            canvas.drawText("CONTACT:", leftColX, rowY, labelPaint)
            canvas.drawText(card.phoneNumber, valColX, rowY, valuePaint)
            rowY += rowSpacing

            // BLOOD GRP
            canvas.drawText("BLOOD GRP:", leftColX, rowY, labelPaint)
            canvas.drawText(card.bloodGroup, valColX, rowY, valuePaint)
            rowY += rowSpacing

            // BRANCH ID
            canvas.drawText("BRANCH ID:", leftColX, rowY, labelPaint)
            canvas.drawText(card.branchId, valColX, rowY, valuePaint)
            rowY += rowSpacing

            // VALID TILL
            canvas.drawText("VALID TILL:", leftColX, rowY, labelPaint)
            canvas.drawText(card.validTill, valColX, rowY, valuePaint)

            // DEDICATED SPACIOUS FOOTER SECTION FOR HARIOM ENTERPRISES
            val footerDividerPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 2.5f
            }
            canvas.drawLine(100f, 1280f, 900f, 1280f, footerDividerPaint)

            val footerSubPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("AUTHORIZED CHANNEL PARTNER & LOGISTICS HUB", width / 2f, 1324f, footerSubPaint)

            // Prominent Bottom Brand matching user request: "Hariom Enterprises" in bold red
            val bottomBrandPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 66f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(card.branchName.ifBlank { "Hariom Enterprises" }, width / 2f, 1410f, bottomBrandPaint)

            // Bottom Navy Accent Bar with rounded corners
            val bottomBarPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRoundRect(
                RectF(70f, 1455f, width - 70f, 1480f),
                9f,
                9f,
                bottomBarPaint
            )

        } else {
            // BACK SIDE (Matching Pure White Corporate Theme)
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Top Header: Clean White Header with Corporate Title & Enlarged Official DTDC Logo
            bgPaint.color = Color.WHITE
            canvas.drawRect(0f, 0f, width.toFloat(), 340f, bgPaint)

            // Top Corporate Title above Logo: "DTDC Private Limited" (black) + "Cargo" (red)
            val titleTextBlack = "DTDC Private Limited "
            val titleTextRed = "Cargo"
            val titleBlackPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0F172A") // Black
                textSize = 36f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val titleRedPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F") // Red
                textSize = 36f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val totalTitleWidth = titleBlackPaint.measureText(titleTextBlack) + titleRedPaint.measureText(titleTextRed)
            val titleStartX = (width - totalTitleWidth) / 2f
            canvas.drawText(titleTextBlack, titleStartX, 64f, titleBlackPaint)
            canvas.drawText(titleTextRed, titleStartX + titleBlackPaint.measureText(titleTextBlack), 64f, titleRedPaint)

            val logoBmp = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val maxW = width - 60f
                val maxH = 185f
                val aspect = logoBmp.width.toFloat() / logoBmp.height.toFloat()
                var drawW = maxH * aspect
                var drawH = maxH
                if (drawW > maxW) {
                    drawW = maxW
                    drawH = drawW / aspect
                }
                // DTDC logo big and right-aligned
                val rightMargin = 35f
                val left = width - rightMargin - drawW
                val top = 76f + (185f - drawH) / 2f
                val logoRect = RectF(left, top, left + drawW, top + drawH)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            }

            // Franchisee Navy Ribbon
            bgPaint.color = Color.parseColor("#0A235C")
            canvas.drawRect(0f, 268f, width.toFloat(), 332f, bgPaint)

            val ribbonTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#FFB300")
                typeface = brandTypeface
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                textSize = 28f
            }
            canvas.drawText("TERMS OF IDENTITY & CONSIGNMENT AUTHORITY", width / 2f, 308f, ribbonTextPaint)

            // Red Chevron Accent Stripe
            bgPaint.color = Color.parseColor("#D32F2F")
            canvas.drawRect(0f, 332f, width.toFloat(), 340f, bgPaint)

            // Terms Content with Proportional Fonts
            val bodyPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = Color.parseColor("#1E293B")
                textSize = 31f
                textAlign = Paint.Align.LEFT
            }

            var textY = 385f
            val leftMargin = 60f

            val headerPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 38f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("AUTHORIZATION & POLICIES:", leftMargin, textY, headerPaint)
            textY += 46f

            val terms = listOf(
                "1. Authorized delivery representative of HARIOM ENTERPRISES (DTDC Partner).",
                "2. Identity card is strictly non-transferable and must be presented on demand.",
                "3. Authorized to handle courier, parcels, and express mail delivery.",
                "4. If found, please return to the Hub Office address listed below or call helpline."
            )

            terms.forEach { term ->
                canvas.drawText(term, leftMargin, textY, bodyPaint)
                textY += 44f
            }

            // Hub Address Box (Dynamic Sizing and Multi-Line Address with Prominent HARIOM ENTERPRISES)
            textY += 30f
            val hubBgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#F8FAFC")
            }
            val hubBorderPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = Color.parseColor("#CBD5E1")
            }

            val hubTitlePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val branchNamePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 44f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val hubAddrPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = Color.parseColor("#0F172A")
                textSize = 30f
                textAlign = Paint.Align.LEFT
            }

            // Wrap address text within card margins
            val maxAddrWidth = width - (leftMargin * 2) - 50f
            val rawAddrText = card.branchAddress
            val words = rawAddrText.split(" ")
            val addrLines = mutableListOf<String>()
            var currentLine = StringBuilder()
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (hubAddrPaint.measureText(testLine) <= maxAddrWidth) {
                    currentLine = StringBuilder(testLine)
                } else {
                    if (currentLine.isNotEmpty()) addrLines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) addrLines.add(currentLine.toString())

            val addrLineHeight = 38f
            val totalAddrHeight = addrLines.size * addrLineHeight
            val contactBoxTop = textY + 110f + totalAddrHeight + 15f
            val contactBoxBottom = contactBoxTop + 130f
            val hubBox = RectF(leftMargin, textY, width - leftMargin, contactBoxBottom + 20f)

            canvas.drawRoundRect(hubBox, 20f, 20f, hubBgPaint)
            canvas.drawRoundRect(hubBox, 20f, 20f, hubBorderPaint)

            canvas.drawText("HUB OFFICE & RETURN ADDRESS (BRANCH ID: ${card.branchId}):", leftMargin + 25f, textY + 45f, hubTitlePaint)
            canvas.drawText(card.branchName.uppercase(), leftMargin + 25f, textY + 95f, branchNamePaint)

            var lineY = textY + 140f
            for (line in addrLines) {
                canvas.drawText(line, leftMargin + 25f, lineY, hubAddrPaint)
                lineY += addrLineHeight
            }

            // Contact Box inside Hub Box (Normal Contact + Emergency Helpline)
            val contactBox = RectF(leftMargin + 25f, contactBoxTop, width - leftMargin - 25f, contactBoxBottom)
            canvas.drawRoundRect(contactBox, 14f, 14f, Paint().apply { color = Color.WHITE })
            canvas.drawRoundRect(contactBox, 14f, 14f, hubBorderPaint)

            val contactLabelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val contactValPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = Color.parseColor("#0F172A")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("CONTACT NO:", leftMargin + 45f, contactBoxTop + 48f, contactLabelPaint)
            canvas.drawText(card.phoneNumber, width - leftMargin - 45f, contactBoxTop + 48f, contactValPaint)

            val emergLabelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val emergValPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("EMERGENCY HELPLINE:", leftMargin + 45f, contactBoxTop + 102f, emergLabelPaint)
            canvas.drawText(card.emergencyContact, width - leftMargin - 45f, contactBoxTop + 102f, emergValPaint)

            // Barcode Graphic container at bottom of back (Enlarged and well-aligned)
            val barcodeBox = RectF(60f, 1260f, width - 60f, 1500f)
            canvas.drawRoundRect(barcodeBox, 20f, 20f, hubBgPaint)
            canvas.drawRoundRect(barcodeBox, 20f, 20f, hubBorderPaint)

            val barPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
            }
            val numBars = 55
            val barW = (barcodeBox.width() - 60f) / numBars
            val seed = card.employeeCode.hashCode()
            var prng = kotlin.math.abs(seed)
            for (i in 0 until numBars) {
                prng = (prng * 31 + 17) and 0x7fffffff
                if ((i % 2 == 0) || (prng % 3 != 0)) {
                    val bLeft = barcodeBox.left + 30f + (i * barW)
                    canvas.drawRect(bLeft, 1290f, bLeft + (barW * 0.85f), 1400f, barPaint)
                }
            }

            val barCodeTextPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.BLACK
                textSize = 36f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("* ${card.employeeCode} • BRANCH: ${card.branchId} *", width / 2f, 1460f, barCodeTextPaint)

            // Bottom Navy Accent Bar with rounded corners matching front side
            val bottomBarPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRoundRect(
                RectF(80f, 1515f, width - 80f, 1533f),
                9f,
                9f,
                bottomBarPaint
            )
        }

        return bitmap
    }

    fun saveCardToGallery(context: Context, card: EmployeeIdCard, isFront: Boolean): Boolean {
        val bitmap = createCardBitmap(context, card, isFront)
        val filename = "DTDC_ID_${card.fullName.replace(" ", "_")}_${if (isFront) "FRONT" else "BACK"}_${System.currentTimeMillis()}.png"

        return try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Hariom_DTDC_IDs")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val dir = File(imagesDir, "Hariom_DTDC_IDs")
                if (!dir.exists()) dir.mkdirs()
                val image = File(dir, filename)
                outputStream = FileOutputStream(image)
            }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            Toast.makeText(context, "ID Card downloaded to Gallery!", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun shareCard(context: Context, card: EmployeeIdCard, isFront: Boolean) {
        try {
            val bitmap = createCardBitmap(context, card, isFront)
            val cachePath = File(context.cacheDir, "shared_cards")
            cachePath.mkdirs()
            val file = File(cachePath, "DTDC_Badge_${if (isFront) "FRONT" else "BACK"}.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "DTDC Employee Identity Credential: ${card.fullName}")
                putExtra(Intent.EXTRA_TEXT, "Official DTDC Courier Associate Identity Badge: ${card.fullName} (${card.employeeCode})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share DTDC Identity Card"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing badge: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Prints the ID card using Android's PrintManager with FIT TO PAGE responsive scaling.
     * Supports physical printers as well as "Save as PDF".
     */
    fun printCard(context: Context, card: EmployeeIdCard, isFront: Boolean) {
        try {
            val printHelper = PrintHelper(context).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT // Responsive Fit to Page
                colorMode = PrintHelper.COLOR_MODE_COLOR
            }
            val bitmap = createCardBitmap(context, card, isFront)
            val sideName = if (isFront) "FRONT" else "BACK"
            val jobName = "DTDC_ID_${card.employeeCode}_$sideName"
            printHelper.printBitmap(jobName, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadBitmapFromPath(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }
}
