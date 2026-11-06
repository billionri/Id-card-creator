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

        val brandBoldTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val brandMediumTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }

        if (isFront) {
            // FRONT SIDE: Pure White Background with Rounded Corners
            bgPaint.color = Color.WHITE
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // TOP HEADER: White section with Company Logo (Custom uploaded or DTDC default)
            val customLogoBmp = loadBitmapFromPath(card.companyLogoPath)
            val logoBmp = customLogoBmp ?: try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val maxW = width - 60f
                val maxH = 175f
                val aspect = logoBmp.width.toFloat() / logoBmp.height.toFloat()
                var drawW = maxH * aspect
                var drawH = maxH
                if (drawW > maxW) {
                    drawW = maxW
                    drawH = drawW / aspect
                }
                val left = (width - drawW) / 2f
                val top = 16f + (175f - drawH) / 2f
                val logoRect = RectF(left, top, left + drawW, top + drawH)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            }

            // Dark Navy Horizontal Banner: Dynamic Tagline & Branch
            val navyPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRect(0f, 200f, width.toFloat(), 260f, navyPaint)

            val partnerTextPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#FFB300")
                textSize = 34f
                letterSpacing = 0.03f
                textAlign = Paint.Align.CENTER
            }
            val partnerText = "${card.companyTagline.uppercase()}: ${card.branchName.uppercase()}"
            canvas.drawText(partnerText, width / 2f, 242f, partnerTextPaint)

            // Red Accent Stripe
            val redPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#D32F2F")
            }
            canvas.drawRect(0f, 260f, width.toFloat(), 268f, redPaint)

            // CENTERED PASSPORT PHOTO - Harmonious Standard Proportion & Dark Navy Border
            val photoW = 430f
            val photoH = 500f
            val photoLeft = (width - photoW) / 2f
            val photoTop = 295f
            val photoBox = RectF(photoLeft, photoTop, photoLeft + photoW, photoTop + photoH)
            val photoBgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#F1F5F9")
            }
            canvas.drawRoundRect(photoBox, 24f, 24f, photoBgPaint)

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
                    textSize = 40f
                    typeface = brandBoldTypeface
                }
                canvas.drawText("PASSPORT PHOTO", photoBox.centerX(), photoBox.centerY() + 14f, placeholderPaint)
            }

            // Dark Navy Border on Photo
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 6f
                color = Color.parseColor("#0A235C")
                isAntiAlias = true
            }
            canvas.drawRoundRect(photoBox, 24f, 24f, borderPaint)

            // EMPLOYEE FULL NAME - Centered, Dark Navy, Spacious Separation
            val namePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#0A235C")
                textSize = 58f
                textAlign = Paint.Align.CENTER
            }
            val displayName = card.fullName.ifBlank { "Rashid Alam" }
            canvas.drawText(displayName, width / 2f, 856f, namePaint)

            // RED DELIVERY ASSOCIATE CIRCULAR CAPSULE
            val desText = card.designation.uppercase().ifBlank { "DELIVERY ASSOCIATE" }
            val desPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.WHITE
                textSize = 31f
                letterSpacing = 0.04f
                textAlign = Paint.Align.CENTER
            }
            val desWidth = desPaint.measureText(desText) + 80f
            val desHeight = 52f
            val desTop = 880f
            val desBottom = desTop + desHeight
            val desBox = RectF((width - desWidth) / 2f, desTop, (width + desWidth) / 2f, desBottom)
            canvas.drawRoundRect(desBox, desHeight / 2f, desHeight / 2f, redPaint)
            canvas.drawText(desText, width / 2f, desTop + 37f, desPaint)

            // FULL-WIDTH HUB DELIVERY OPTIONS / DEPARTMENT DIV
            val deptBgPaint = Paint().apply {
                color = Color.parseColor("#F1F5F9")
            }
            val deptBorderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                color = Color.parseColor("#CBD5E1")
            }
            val deptTop = 948f
            val deptBottom = 1000f
            canvas.drawRect(0f, deptTop, width.toFloat(), deptBottom, deptBgPaint)
            canvas.drawLine(0f, deptTop, width.toFloat(), deptTop, deptBorderPaint)
            canvas.drawLine(0f, deptBottom, width.toFloat(), deptBottom, deptBorderPaint)

            val deptPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#0F172A")
                textSize = 31f
                letterSpacing = 0.02f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(card.department.ifBlank { "Hub Dispatch Operations" }, width / 2f, 985f, deptPaint)

            // 2-COLUMN EMPLOYEE DETAILS (Generous Vertical Gaps - No Sticking!)
            val labelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#475569")
                textSize = 26f
                letterSpacing = 0.03f
                textAlign = Paint.Align.LEFT
            }
            val valPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#0F172A")
                textSize = 38f
                textAlign = Paint.Align.LEFT
            }

            val col1X = 75f
            val col2X = 640f
            val gridRow1Y = 1046f
            val gridRow2Y = 1138f
            val gridRow3Y = 1230f

            // ROW 1: EMP ID and BLOOD GRP
            canvas.drawText("EMP ID:", col1X, gridRow1Y, labelPaint)
            canvas.drawText(card.employeeCode.ifBlank { "DTDC/HE/2024/1609" }, col1X, gridRow1Y + 44f, valPaint)

            canvas.drawText("BLOOD GRP:", col2X, gridRow1Y, labelPaint)
            canvas.drawText(card.bloodGroup.ifBlank { "B+" }, col2X, gridRow1Y + 44f, valPaint)

            // ROW 2: CONTACT and VALID TILL
            canvas.drawText("CONTACT:", col1X, gridRow2Y, labelPaint)
            canvas.drawText(card.phoneNumber.ifBlank { "8655707563" }, col1X, gridRow2Y + 44f, valPaint)

            canvas.drawText("VALID TILL:", col2X, gridRow2Y, labelPaint)
            canvas.drawText(card.validTill.ifBlank { "31 Dec 2027" }, col2X, gridRow2Y + 44f, valPaint)

            // ROW 3: BRANCH ID
            canvas.drawText("BRANCH ID:", col1X, gridRow3Y, labelPaint)
            canvas.drawText(card.branchId.ifBlank { "1057" }, col1X, gridRow3Y + 44f, valPaint)

            // BRANCH / FRANCHISEE NAME ABOVE FOOTER: Big Font, DTDC Red, Camel Case
            val hariomPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 44f
                textAlign = Paint.Align.CENTER
            }
            val camelCaseBranch = card.branchName.ifBlank { "Hariom Enterprises" }
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
            canvas.drawText(camelCaseBranch, width / 2f, 1362f, hariomPaint)

            // SOLID DARK NAVY FOOTER
            val footerTop = 1435f
            canvas.save()
            canvas.clipRect(0f, footerTop, width.toFloat(), height.toFloat())
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, navyPaint)
            canvas.restore()

            val footerWhitePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.WHITE
                textSize = 32f
                letterSpacing = 0.02f
                textAlign = Paint.Align.CENTER
            }
            val footerGoldPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#FFB300")
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            val badgeCompany = card.companyName.ifBlank { "DTDC" }.uppercase()
            canvas.drawText("AUTHORIZED IDENTITY BADGE • $badgeCompany", width / 2f, 1488f, footerWhitePaint)
            canvas.drawText("Branch: ${card.branchId.ifBlank { "1057" }} | ${card.branchName.ifBlank { "Hariom Enterprises" }}", width / 2f, 1532f, footerGoldPaint)

        } else {
            // BACK SIDE (Matching Pure White Corporate Theme)
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Top Header: Clean White Header with Company Logo (Custom or DTDC default)
            val backCustomLogoBmp = loadBitmapFromPath(card.companyLogoPath)
            val logoBmp = backCustomLogoBmp ?: try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val maxW = width - 60f
                val maxH = 175f
                val aspect = logoBmp.width.toFloat() / logoBmp.height.toFloat()
                var drawW = maxH * aspect
                var drawH = maxH
                if (drawW > maxW) {
                    drawW = maxW
                    drawH = drawW / aspect
                }
                val left = (width - drawW) / 2f
                val top = 16f + (175f - drawH) / 2f
                val logoRect = RectF(left, top, left + drawW, top + drawH)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            }

            // Dark Navy Ribbon
            val backNavyPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRect(0f, 200f, width.toFloat(), 260f, backNavyPaint)

            val ribbonTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#FFB300")
                typeface = brandBoldTypeface
                textAlign = Paint.Align.CENTER
                textSize = 32f
                letterSpacing = 0.03f
            }
            canvas.drawText("TERMS OF IDENTITY & CONSIGNMENT AUTHORITY", width / 2f, 242f, ribbonTextPaint)

            // Red Stripe
            val backRedPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#D32F2F")
            }
            canvas.drawRect(0f, 260f, width.toFloat(), 268f, backRedPaint)

            // Terms Content with Proportional Fonts & Word-Wrapping within Symmetrical Margins
            val leftMargin = 55f
            val maxTextWidth = width - (leftMargin * 2)

            val headerPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#0A235C")
                textSize = 30f
                letterSpacing = 0.02f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("AUTHORIZATION & POLICIES:", leftMargin, 305f, headerPaint)

            val bodyPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandMediumTypeface
                color = Color.parseColor("#1E293B")
                textSize = 25f
                textAlign = Paint.Align.LEFT
            }

            var textY = 344f
            val terms = listOf(
                "1. Authorized delivery representative of ${card.branchName.uppercase()} (${card.companyName.uppercase()} Partner).",
                "2. Identity card is strictly non-transferable and must be presented on demand.",
                "3. Authorized to handle courier, parcels, and express mail delivery.",
                "4. If found, please return to the Hub Office address listed below or call helpline."
            )

            val termLineHeight = 35f
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

            // Hub Address Box (Dynamic Sizing and Multi-Line Address with Prominent Branch Name)
            textY += 12f
            val hubBgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#F8FAFC")
            }
            val hubBorderPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.parseColor("#CBD5E1")
            }

            val hubTitlePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#0A235C")
                textSize = 26f
                letterSpacing = 0.02f
                textAlign = Paint.Align.LEFT
            }

            val branchNamePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 36f
                textAlign = Paint.Align.LEFT
            }

            val hubAddrPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandMediumTypeface
                color = Color.parseColor("#0F172A")
                textSize = 25f
                textAlign = Paint.Align.LEFT
            }

            val innerPad = 22f
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

            val addrLineHeight = 33f
            val totalAddrHeight = addrLines.size * addrLineHeight
            val contactBoxTop = textY + 118f + totalAddrHeight + 10f
            val contactBoxBottom = contactBoxTop + 96f
            val hubBox = RectF(leftMargin, textY, width - leftMargin, contactBoxBottom + 16f)

            canvas.drawRoundRect(hubBox, 18f, 18f, hubBgPaint)
            canvas.drawRoundRect(hubBox, 18f, 18f, hubBorderPaint)

            canvas.drawText("HUB OFFICE & RETURN ADDRESS (BRANCH ID: ${card.branchId.ifBlank { "1057" }}):", leftMargin + innerPad, textY + 36f, hubTitlePaint)
            canvas.drawText(card.branchName.uppercase(), leftMargin + innerPad, textY + 78f, branchNamePaint)

            var lineY = textY + 118f
            for (line in addrLines) {
                canvas.drawText(line, leftMargin + innerPad, lineY, hubAddrPaint)
                lineY += addrLineHeight
            }

            // Contact Box inside Hub Box (Normal Contact + Emergency Helpline)
            val contactBox = RectF(leftMargin + innerPad, contactBoxTop, width - leftMargin - innerPad, contactBoxBottom)
            canvas.drawRoundRect(contactBox, 12f, 12f, Paint().apply { color = Color.WHITE })
            canvas.drawRoundRect(contactBox, 12f, 12f, hubBorderPaint)

            val contactLabelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#0A235C")
                textSize = 25f
                textAlign = Paint.Align.LEFT
            }
            val contactValPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#0F172A")
                textSize = 27f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("CONTACT NO:", leftMargin + innerPad + 18f, contactBoxTop + 38f, contactLabelPaint)
            canvas.drawText(card.phoneNumber, width - leftMargin - innerPad - 18f, contactBoxTop + 38f, contactValPaint)

            val emergLabelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 25f
                textAlign = Paint.Align.LEFT
            }
            val emergValPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#D32F2F")
                textSize = 27f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("EMERGENCY HELPLINE:", leftMargin + innerPad + 18f, contactBoxTop + 76f, emergLabelPaint)
            canvas.drawText(card.emergencyContact, width - leftMargin - innerPad - 18f, contactBoxTop + 76f, emergValPaint)

            // Barcode Graphic container at bottom of back
            val barcodeTop = (hubBox.bottom + 16f).coerceAtMost(1245f)
            val barcodeBottom = 1415f
            val barcodeBox = RectF(leftMargin, barcodeTop, width - leftMargin, barcodeBottom)
            canvas.drawRoundRect(barcodeBox, 18f, 18f, hubBgPaint)
            canvas.drawRoundRect(barcodeBox, 18f, 18f, hubBorderPaint)

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
                    canvas.drawRect(bLeft, barcodeTop + 16f, bLeft + (barW * 0.85f), barcodeTop + 96f, barPaint)
                }
            }

            val barCodeTextPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.BLACK
                textSize = 29f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("* ${card.employeeCode} • BRANCH: ${card.branchId.ifBlank { "1057" }} *", width / 2f, barcodeTop + 138f, barCodeTextPaint)

            // Solid Dark Navy Edge-to-Edge Footer
            val footerBgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0A235C")
            }
            canvas.drawRect(0f, 1435f, width.toFloat(), 1580f, footerBgPaint)

            val footerWhitePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.WHITE
                textSize = 32f
                letterSpacing = 0.02f
                textAlign = Paint.Align.CENTER
            }
            val footerGoldPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandBoldTypeface
                color = Color.parseColor("#FFB300")
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            val badgeCompany = card.companyName.ifBlank { "DTDC" }.uppercase()
            canvas.drawText("AUTHORIZED IDENTITY BADGE • $badgeCompany", width / 2f, 1488f, footerWhitePaint)
            canvas.drawText("Branch: ${card.branchId.ifBlank { "1057" }} | ${card.branchName.ifBlank { "Hariom Enterprises" }}", width / 2f, 1532f, footerGoldPaint)
        }

        return bitmap
    }

    fun saveCardToGallery(context: Context, card: EmployeeIdCard, isFront: Boolean): Boolean {
        val bitmap = createCardBitmap(context, card, isFront)
        val safeCompanyName = card.companyName.ifBlank { "Company" }.replace(" ", "_")
        val filename = "${safeCompanyName}_ID_${card.fullName.replace(" ", "_")}_${if (isFront) "FRONT" else "BACK"}_${System.currentTimeMillis()}.png"

        return try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ID_Cards")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val dir = File(imagesDir, "ID_Cards")
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
            val safeCompanyName = card.companyName.ifBlank { "Badge" }.replace(" ", "_")
            val file = File(cachePath, "${safeCompanyName}_Badge_${if (isFront) "FRONT" else "BACK"}.png")
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
