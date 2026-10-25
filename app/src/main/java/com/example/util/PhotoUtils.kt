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
            // Pure White Card Background with Rounded Corners
            bgPaint.color = Color.WHITE
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // TOP HEADER: White section with DTDC Official Logo
            val logoBmp = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val maxW = width - 80f
                val maxH = 175f
                val aspect = logoBmp.width.toFloat() / logoBmp.height.toFloat()
                var drawW = maxH * aspect
                var drawH = maxH
                if (drawW > maxW) {
                    drawW = maxW
                    drawH = drawW / aspect
                }
                val left = (width - drawW) / 2f
                val top = 36f + (175f - drawH) / 2f
                val logoRect = RectF(left, top, left + drawW, top + drawH)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            }

            // Dark Navy Horizontal Banner: AUTH. CHANNEL PARTNER: HARIOM ENTERPRISES
            val navyPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRect(0f, 235f, width.toFloat(), 300f, navyPaint)

            val partnerTextPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#FFB300")
                textSize = 33f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val partnerText = "AUTH. CHANNEL PARTNER: ${card.branchName.uppercase()}"
            canvas.drawText(partnerText, width / 2f, 278f, partnerTextPaint)

            // Red Accent Stripe
            val redPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#D32F2F")
            }
            canvas.drawRect(0f, 300f, width.toFloat(), 310f, redPaint)

            // CENTERED PASSPORT PHOTO - Matching Reference Proportions & Dark Navy Border
            val photoW = 540f
            val photoH = 650f
            val photoLeft = (width - photoW) / 2f
            val photoTop = 345f
            val photoBox = RectF(photoLeft, photoTop, photoLeft + photoW, photoTop + photoH)
            val photoBgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#F1F5F9")
            }
            canvas.drawRoundRect(photoBox, 28f, 28f, photoBgPaint)

            val photoBitmap = loadBitmapFromPath(card.photoPath)
            if (photoBitmap != null) {
                val bRatio = photoBitmap.width.toFloat() / photoBitmap.height.toFloat()
                val targetRatio = photoBox.width() / photoBox.height()
                val cropSrc = if (bRatio > targetRatio) {
                    val newW = (photoBitmap.height * targetRatio).toInt()
                    val startX = (photoBitmap.width - newW) / 2
                    Rect(startX, 0, startX + newW, photoBitmap.height)
                } else {
                    val newH = (photoBitmap.width / targetRatio).toInt()
                    val startY = (photoBitmap.height - newH) / 2
                    Rect(0, startY, photoBitmap.width, startY + newH)
                }
                canvas.drawBitmap(photoBitmap, cropSrc, photoBox, null)
            } else {
                val placeholderPaint = Paint().apply {
                    color = Color.parseColor("#94A3B8")
                    textAlign = Paint.Align.CENTER
                    textSize = 46f
                    typeface = brandTypeface
                    isFakeBoldText = true
                }
                canvas.drawText("PASSPORT PHOTO", photoBox.centerX(), photoBox.centerY() + 16f, placeholderPaint)
            }

            // Dark Navy Border on Photo
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 7f
                color = Color.parseColor("#0A235C")
                isAntiAlias = true
            }
            canvas.drawRoundRect(photoBox, 28f, 28f, borderPaint)

            // EMPLOYEE FULL NAME - Deep Dark Navy, Centered
            val namePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 54f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val displayName = card.fullName.ifBlank { "Rashid Alam" }
            canvas.drawText(displayName, width / 2f, 1048f, namePaint)

            // RED DESIGNATION CAPSULE PILL - Centered
            val desText = card.designation.uppercase().ifBlank { "DELIVERY ASSOCIATE" }
            val desPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.WHITE
                textSize = 29f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val desWidth = desPaint.measureText(desText) + 80f
            val desBox = RectF((width - desWidth) / 2f, 1076f, (width + desWidth) / 2f, 1140f)
            canvas.drawRoundRect(desBox, 32f, 32f, redPaint)
            canvas.drawText(desText, width / 2f, 1118f, desPaint)

            // DEPARTMENT SUBTITLE - Slate Navy, Centered
            val deptPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#1E293B")
                textSize = 31f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(card.department.ifBlank { "Hub Dispatch Operations" }, width / 2f, 1184f, deptPaint)

            // 2-COLUMN EMPLOYEE DETAILS (Exact match to reference image!)
            val labelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#475569")
                textSize = 28f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val valPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0F172A")
                textSize = 38f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val col1X = 110f
            val col2X = 540f
            val gridRow1Y = 1242f
            val gridRow2Y = 1338f

            // ROW 1: EMP ID and BLOOD GRP
            canvas.drawText("EMP ID:", col1X, gridRow1Y, labelPaint)
            canvas.drawText(card.employeeCode.ifBlank { "DTDC/HE/2024/1609" }, col1X, gridRow1Y + 42f, valPaint)

            canvas.drawText("BLOOD GRP:", col2X, gridRow1Y, labelPaint)
            canvas.drawText(card.bloodGroup.ifBlank { "B+" }, col2X, gridRow1Y + 42f, valPaint)

            // ROW 2: CONTACT and VALID TILL
            canvas.drawText("CONTACT:", col1X, gridRow2Y, labelPaint)
            canvas.drawText(card.phoneNumber.ifBlank { "8655707563" }, col1X, gridRow2Y + 42f, valPaint)

            canvas.drawText("VALID TILL:", col2X, gridRow2Y, labelPaint)
            canvas.drawText(card.validTill.ifBlank { "31 Dec 2027" }, col2X, gridRow2Y + 42f, valPaint)

            // SOLID DARK NAVY FOOTER (Edge-to-edge as in reference image)
            val footerTop = 1435f
            canvas.save()
            canvas.clipRect(0f, footerTop, width.toFloat(), height.toFloat())
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, navyPaint)
            canvas.restore()

            val footerWhitePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.WHITE
                textSize = 32f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val footerGoldPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#FFB300")
                textSize = 27f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("AUTHORIZED IDENTITY BADGE • DTDC", width / 2f, 1495f, footerWhitePaint)
            canvas.drawText("Branch: ${card.branchId.ifBlank { "DTDC-HE-4102" }} | ${card.branchName.ifBlank { "Hariom Enterprises" }}", width / 2f, 1540f, footerGoldPaint)

        } else {
            // BACK SIDE (Matching Pure White Corporate Theme)
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Top Header: Clean White Header with Official DTDC Logo (matching front side)
            val logoBmp = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val maxW = width - 80f
                val maxH = 175f
                val aspect = logoBmp.width.toFloat() / logoBmp.height.toFloat()
                var drawW = maxH * aspect
                var drawH = maxH
                if (drawW > maxW) {
                    drawW = maxW
                    drawH = drawW / aspect
                }
                val left = (width - drawW) / 2f
                val top = 36f + (175f - drawH) / 2f
                val logoRect = RectF(left, top, left + drawW, top + drawH)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            }

            // Dark Navy Ribbon
            val backNavyPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRect(0f, 235f, width.toFloat(), 300f, backNavyPaint)

            val ribbonTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#FFB300")
                typeface = brandTypeface
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                textSize = 30f
            }
            canvas.drawText("TERMS OF IDENTITY & CONSIGNMENT AUTHORITY", width / 2f, 278f, ribbonTextPaint)

            // Red Stripe
            val backRedPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#D32F2F")
            }
            canvas.drawRect(0f, 300f, width.toFloat(), 310f, backRedPaint)

            // Terms Content with Proportional Fonts & Word-Wrapping within Symmetrical Margins
            val leftMargin = 55f
            val maxTextWidth = width - (leftMargin * 2)

            val bodyPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = Color.parseColor("#1E293B")
                textSize = 28f
                textAlign = Paint.Align.LEFT
            }

            var textY = 352f

            val headerPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("AUTHORIZATION & POLICIES:", leftMargin, textY, headerPaint)
            textY += 42f

            val terms = listOf(
                "1. Authorized delivery representative of HARIOM ENTERPRISES (DTDC Partner).",
                "2. Identity card is strictly non-transferable and must be presented on demand.",
                "3. Authorized to handle courier, parcels, and express mail delivery.",
                "4. If found, please return to the Hub Office address listed below or call helpline."
            )

            val termLineHeight = 36f
            terms.forEach { term ->
                val words = term.split(" ")
                var curLine = StringBuilder()
                for (word in words) {
                    val testLine = if (curLine.isEmpty()) word else "$curLine $word"
                    if (bodyPaint.measureText(testLine) <= maxTextWidth) {
                        curLine = StringBuilder(testLine)
                    } else {
                        if (curLine.isNotEmpty()) {
                            canvas.drawText(curLine.toString(), leftMargin, textY, bodyPaint)
                            textY += termLineHeight
                        }
                        curLine = StringBuilder(word)
                    }
                }
                if (curLine.isNotEmpty()) {
                    canvas.drawText(curLine.toString(), leftMargin, textY, bodyPaint)
                    textY += termLineHeight
                }
                textY += 6f
            }

            // Hub Address Box (Dynamic Sizing and Multi-Line Address with Prominent HARIOM ENTERPRISES)
            textY += 16f
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
                textSize = 29f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val branchNamePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 42f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val hubAddrPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = Color.parseColor("#0F172A")
                textSize = 29f
                textAlign = Paint.Align.LEFT
            }

            // Wrap address text within card margins
            val innerPad = 25f
            val maxAddrWidth = width - (leftMargin * 2) - (innerPad * 2)
            val rawAddrText = card.branchAddress
            val addrWords = rawAddrText.split(" ")
            val addrLines = mutableListOf<String>()
            var currentLine = StringBuilder()
            for (word in addrWords) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (hubAddrPaint.measureText(testLine) <= maxAddrWidth) {
                    currentLine = StringBuilder(testLine)
                } else {
                    if (currentLine.isNotEmpty()) addrLines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) addrLines.add(currentLine.toString())

            val addrLineHeight = 36f
            val totalAddrHeight = addrLines.size * addrLineHeight
            val contactBoxTop = textY + 110f + totalAddrHeight + 14f
            val contactBoxBottom = contactBoxTop + 124f
            val hubBox = RectF(leftMargin, textY, width - leftMargin, contactBoxBottom + 18f)

            canvas.drawRoundRect(hubBox, 20f, 20f, hubBgPaint)
            canvas.drawRoundRect(hubBox, 20f, 20f, hubBorderPaint)

            canvas.drawText("HUB OFFICE & RETURN ADDRESS (BRANCH ID: ${card.branchId}):", leftMargin + innerPad, textY + 42f, hubTitlePaint)
            canvas.drawText(card.branchName.uppercase(), leftMargin + innerPad, textY + 90f, branchNamePaint)

            var lineY = textY + 132f
            for (line in addrLines) {
                canvas.drawText(line, leftMargin + innerPad, lineY, hubAddrPaint)
                lineY += addrLineHeight
            }

            // Contact Box inside Hub Box (Normal Contact + Emergency Helpline)
            val contactBox = RectF(leftMargin + innerPad, contactBoxTop, width - leftMargin - innerPad, contactBoxBottom)
            canvas.drawRoundRect(contactBox, 14f, 14f, Paint().apply { color = Color.WHITE })
            canvas.drawRoundRect(contactBox, 14f, 14f, hubBorderPaint)

            val contactLabelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#0A235C")
                textSize = 32f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val contactValPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = Color.parseColor("#0F172A")
                textSize = 32f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("CONTACT NO:", leftMargin + innerPad + 20f, contactBoxTop + 45f, contactLabelPaint)
            canvas.drawText(card.phoneNumber, width - leftMargin - innerPad - 20f, contactBoxTop + 45f, contactValPaint)

            val emergLabelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 32f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val emergValPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 32f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("EMERGENCY HELPLINE:", leftMargin + innerPad + 20f, contactBoxTop + 98f, emergLabelPaint)
            canvas.drawText(card.emergencyContact, width - leftMargin - innerPad - 20f, contactBoxTop + 98f, emergValPaint)

            // Barcode Graphic container at bottom of back (Enlarged and well-aligned with equal margins)
            val barcodeTop = hubBox.bottom + 22f
            val barcodeBottom = (barcodeTop + 200f).coerceAtMost(1415f)
            val barcodeBox = RectF(leftMargin, barcodeTop, width - leftMargin, barcodeBottom)
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
                    canvas.drawRect(bLeft, barcodeTop + 25f, bLeft + (barW * 0.85f), barcodeTop + 130f, barPaint)
                }
            }

            val barCodeTextPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.BLACK
                textSize = 33f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("* ${card.employeeCode} • BRANCH: ${card.branchId} *", width / 2f, barcodeTop + 175f, barCodeTextPaint)

            // Solid Dark Navy Edge-to-Edge Footer (Exact match to reference image & front side!)
            val footerBgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRect(0f, 1435f, width.toFloat(), 1580f, footerBgPaint)

            val footerWhitePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.WHITE
                textSize = 31f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val footerGoldPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = Color.parseColor("#FFB300")
                textSize = 27f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("AUTHORIZED IDENTITY BADGE • DTDC", width / 2f, 1495f, footerWhitePaint)
            canvas.drawText("Branch: ${card.branchId.ifBlank { "DTDC-HE-4102" }} | ${card.branchName.ifBlank { "Hariom Enterprises" }}", width / 2f, 1540f, footerGoldPaint)
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
