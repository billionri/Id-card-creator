package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.EmployeeIdCard
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PhotoUtils {

    fun savePassportPhoto(context: Context, sourceBitmap: Bitmap): String {
        val photosDir = File(context.filesDir, "employee_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        // Crop to 3:4 passport aspect ratio from center
        val targetWidth: Int
        val targetHeight: Int
        val currentAspect = sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat()
        val desiredAspect = 3f / 4f

        val croppedBitmap = if (currentAspect > desiredAspect) {
            // Image is too wide
            targetHeight = sourceBitmap.height
            targetWidth = (targetHeight * desiredAspect).toInt()
            val xOffset = (sourceBitmap.width - targetWidth) / 2
            Bitmap.createBitmap(sourceBitmap, xOffset, 0, targetWidth, targetHeight)
        } else {
            // Image is too tall
            targetWidth = sourceBitmap.width
            targetHeight = (targetWidth / desiredAspect).toInt()
            val yOffset = (sourceBitmap.height - targetHeight) / 2
            Bitmap.createBitmap(sourceBitmap, 0, yOffset, targetWidth, targetHeight)
        }

        // Scale to a clean 600x800 passport resolution
        val finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 600, 800, true)

        val file = File(photosDir, "emp_passport_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return file.absolutePath
    }

    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmapFromPath(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a high-resolution official ID card image (Front view)
     */
    fun renderCardBitmap(context: Context, card: EmployeeIdCard, isFront: Boolean = true): Bitmap {
        val width = 1000
        val height = 1550
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val brandTypeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, com.example.R.font.rajdhani_bold)
        } catch (e: Exception) {
            android.graphics.Typeface.DEFAULT_BOLD
        }
        val corporateTypeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, com.example.R.font.montserrat_bold)
        } catch (e: Exception) {
            android.graphics.Typeface.DEFAULT_BOLD
        }

        val bgPaint = Paint().apply { isAntiAlias = true }

        if (isFront) {
            val textPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                textAlign = Paint.Align.CENTER
            }

            // Background
            bgPaint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Top Header: Clean White Header with Official DTDC Logo (Enlarged)
            bgPaint.color = android.graphics.Color.WHITE
            canvas.drawRect(0f, 0f, width.toFloat(), 300f, bgPaint)

            val logoBmp = try {
                BitmapFactory.decodeResource(context.resources, com.example.R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val logoRect = RectF(30f, 10f, width - 30f, 230f)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            } else {
                textPaint.apply {
                    color = android.graphics.Color.parseColor("#0A235C")
                    textAlign = Paint.Align.CENTER
                    typeface = brandTypeface
                    isFakeBoldText = true
                    textSize = 72f
                }
                canvas.drawText("DTDC COURIER", width / 2f, 120f, textPaint)
            }

            // Franchisee Navy Ribbon
            bgPaint.color = android.graphics.Color.parseColor("#0A235C")
            canvas.drawRect(0f, 235f, width.toFloat(), 292f, bgPaint)

            val ribbonTextPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#FFB300")
                typeface = brandTypeface
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                textSize = 30f
            }
            canvas.drawText("AUTH. CHANNEL PARTNER: ${card.branchName.uppercase()}", width / 2f, 274f, ribbonTextPaint)

            // Red Chevron Accent Stripe
            bgPaint.color = android.graphics.Color.parseColor("#D32F2F")
            canvas.drawRect(0f, 292f, width.toFloat(), 302f, bgPaint)

            // Employee Passport Photo
            val photoBox = RectF(width / 2f - 180f, 325f, width / 2f + 180f, 795f)
            val photoPaint = Paint().apply { isAntiAlias = true }
            photoPaint.color = android.graphics.Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(photoBox, 16f, 16f, photoPaint)

            val photoBitmap = loadBitmapFromPath(card.photoPath)
            if (photoBitmap != null) {
                val srcRect = Rect(0, 0, photoBitmap.width, photoBitmap.height)
                canvas.drawBitmap(photoBitmap, srcRect, photoBox, null)
            } else {
                // Placeholder portrait icon
                val placeholderPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textAlign = Paint.Align.CENTER
                    textSize = 50f
                }
                canvas.drawText("PHOTO", width / 2f, 570f, placeholderPaint)
            }

            // Photo Border (Gold/Navy)
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 6f
                color = android.graphics.Color.parseColor("#0A235C")
                isAntiAlias = true
            }
            canvas.drawRoundRect(photoBox, 16f, 16f, borderPaint)

            // Employee Full Name
            textPaint.apply {
                color = android.graphics.Color.parseColor("#0A235C")
                textSize = 56f
                isFakeBoldText = true
            }
            canvas.drawText(card.fullName, width / 2f, 880f, textPaint)

            // Designation Badge Pill
            val desBox = RectF(width / 2f - 300f, 915f, width / 2f + 300f, 985f)
            bgPaint.color = android.graphics.Color.parseColor("#D32F2F")
            canvas.drawRoundRect(desBox, 24f, 24f, bgPaint)

            textPaint.apply {
                color = android.graphics.Color.WHITE
                textSize = 36f
                isFakeBoldText = true
            }
            canvas.drawText(card.designation.uppercase(), width / 2f, 965f, textPaint)

            // Department
            textPaint.apply {
                color = android.graphics.Color.parseColor("#475569")
                typeface = corporateTypeface
                textSize = 34f
                isFakeBoldText = false
            }
            canvas.drawText(card.department, width / 2f, 1035f, textPaint)

            // Details Grid
            val labelPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 32f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val valuePaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = android.graphics.Color.parseColor("#0F172A")
                textSize = 36f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            var startY = 1110f
            val col1X = 100f
            val col2X = 540f

            // Emp Code & Blood Group
            canvas.drawText("EMP ID:", col1X, startY, labelPaint)
            canvas.drawText(card.employeeCode, col1X, startY + 45f, valuePaint)

            canvas.drawText("BLOOD GRP:", col2X, startY, labelPaint)
            canvas.drawText(card.bloodGroup, col2X, startY + 45f, valuePaint)

            startY += 105f
            // Phone & Joining
            canvas.drawText("CONTACT:", col1X, startY, labelPaint)
            canvas.drawText(card.phoneNumber, col1X, startY + 45f, valuePaint)

            canvas.drawText("VALID TILL:", col2X, startY, labelPaint)
            canvas.drawText(card.validTill, col2X, startY + 45f, valuePaint)

            // Footer Section
            bgPaint.color = android.graphics.Color.parseColor("#0A235C")
            canvas.drawRect(0f, 1420f, width.toFloat(), height.toFloat(), bgPaint)

            textPaint.apply {
                color = android.graphics.Color.WHITE
                textSize = 30f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("AUTHORIZED IDENTITY BADGE • DTDC", width / 2f, 1480f, textPaint)

            textPaint.apply {
                color = android.graphics.Color.parseColor("#FFB300")
                textSize = 24f
                isFakeBoldText = false
            }
            canvas.drawText("Branch: ${card.branchCode} | Hariom Enterprises", width / 2f, 1515f, textPaint)

        } else {
            // BACK SIDE (Matching Pure White Corporate Theme)
            bgPaint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Top Header: Clean White Header with Official DTDC Logo
            bgPaint.color = android.graphics.Color.WHITE
            canvas.drawRect(0f, 0f, width.toFloat(), 260f, bgPaint)

            val logoBmp = try {
                BitmapFactory.decodeResource(context.resources, com.example.R.drawable.ic_dtdc_logo)
            } catch (e: Exception) {
                null
            }

            if (logoBmp != null) {
                val logoRect = RectF(60f, 15f, width - 60f, 190f)
                canvas.drawBitmap(logoBmp, null, logoRect, null)
            }

            // Franchisee Navy Ribbon
            bgPaint.color = android.graphics.Color.parseColor("#0A235C")
            canvas.drawRect(0f, 200f, width.toFloat(), 255f, bgPaint)

            val ribbonTextPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#FFB300")
                typeface = brandTypeface
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                textSize = 26f
            }
            canvas.drawText("TERMS OF IDENTITY & CONSIGNMENT AUTHORITY", width / 2f, 238f, ribbonTextPaint)

            // Red Chevron Accent Stripe
            bgPaint.color = android.graphics.Color.parseColor("#D32F2F")
            canvas.drawRect(0f, 255f, width.toFloat(), 265f, bgPaint)

            // Terms Content
            val bodyPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = android.graphics.Color.parseColor("#334155")
                textSize = 28f
                textAlign = Paint.Align.LEFT
            }

            var textY = 320f
            val leftMargin = 70f

            val headerPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = android.graphics.Color.parseColor("#0A235C")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("AUTHORIZATION & POLICIES:", leftMargin, textY, headerPaint)
            textY += 45f

            val terms = listOf(
                "1. The bearer is an authorized delivery representative of Hariom Enterprises (DTDC Channel Partner).",
                "2. This identity card is strictly non-transferable and must be presented on demand.",
                "3. Authorized to handle courier, parcels, and express mail delivery.",
                "4. If found, please return to the Hub Office address listed below or call helpline."
            )
            for (line in terms) {
                canvas.drawText(line, leftMargin, textY, bodyPaint)
                textY += 46f
            }

            textY += 20f

            // Address Card Box Container
            val addressBox = RectF(leftMargin, textY, width - leftMargin, textY + 230f)
            val boxBgPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#F8FAFC")
            }
            val boxBorderPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = android.graphics.Color.parseColor("#CBD5E1")
            }
            canvas.drawRoundRect(addressBox, 16f, 16f, boxBgPaint)
            canvas.drawRoundRect(addressBox, 16f, 16f, boxBorderPaint)

            canvas.drawText("HUB OFFICE & RETURN ADDRESS:", leftMargin + 25f, textY + 45f, headerPaint)

            val addrTextPaint = Paint().apply {
                isAntiAlias = true
                typeface = corporateTypeface
                color = android.graphics.Color.parseColor("#0F172A")
                textSize = 25f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            val addressParts = card.branchAddress.split(", ")
            val formattedAddressLines = mutableListOf<String>()
            var currentLine = ""
            for (part in addressParts) {
                if (currentLine.isEmpty()) {
                    currentLine = part
                } else if ((currentLine + ", " + part).length < 42) {
                    currentLine += ", $part"
                } else {
                    formattedAddressLines.add(currentLine + ",")
                    currentLine = part
                }
            }
            if (currentLine.isNotEmpty()) {
                formattedAddressLines.add(currentLine)
            }

            var addrY = textY + 85f
            for (line in formattedAddressLines) {
                canvas.drawText(line, leftMargin + 25f, addrY, addrTextPaint)
                addrY += 34f
            }

            val emergPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = android.graphics.Color.parseColor("#D32F2F")
                textSize = 28f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("EMERGENCY HELPLINE: ${card.emergencyContact}", leftMargin + 25f, textY + 205f, emergPaint)

            textY += 270f

            // Draw Barcode visualization
            val barBg = RectF(100f, textY, width - 100f, textY + 160f)
            val barBoxBgPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#F8FAFC")
            }
            val barBoxBorderPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = android.graphics.Color.parseColor("#E2E8F0")
            }
            canvas.drawRoundRect(barBg, 12f, 12f, barBoxBgPaint)
            canvas.drawRoundRect(barBg, 12f, 12f, barBoxBorderPaint)

            val barPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                isAntiAlias = true
            }
            var barX = 140f
            val barcodeSeed = (card.employeeCode + card.phoneNumber).hashCode()
            var pseudoRand = kotlin.math.abs(barcodeSeed)

            while (barX < width - 140f) {
                val barW = ((pseudoRand % 4) + 1) * 2f
                pseudoRand = (pseudoRand * 31 + 17) and 0x7fffffff
                canvas.drawRect(barX, textY + 20f, barX + barW, textY + 120f, barPaint)
                barX += barW + ((pseudoRand % 3) + 2) * 2f
                pseudoRand = (pseudoRand * 31 + 17) and 0x7fffffff
            }

            val codePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                typeface = brandTypeface
                textSize = 28f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("* ${card.employeeCode} *", width / 2f, textY + 148f, codePaint)

            // Authorized Signatory Row
            val signLabelPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#64748B")
                typeface = brandTypeface
                textSize = 26f
                textAlign = Paint.Align.RIGHT
                isFakeBoldText = true
                isAntiAlias = true
            }
            val signScriptPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#0A235C")
                textSize = 34f
                textAlign = Paint.Align.RIGHT
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("Hariom Kr.", width - 80f, 1370f, signScriptPaint)
            val linePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#0A235C")
                strokeWidth = 3f
            }
            canvas.drawLine(width - 320f, 1390f, width - 80f, 1390f, linePaint)
            canvas.drawText("AUTHORIZED SIGNATORY", width - 80f, 1425f, signLabelPaint)

            // Bottom Navy Footer (Matching Front)
            bgPaint.color = android.graphics.Color.parseColor("#0A235C")
            canvas.drawRect(0f, 1470f, width.toFloat(), height.toFloat(), bgPaint)

            val footerTextPaint = Paint().apply {
                isAntiAlias = true
                typeface = brandTypeface
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 26f
                isFakeBoldText = true
            }
            canvas.drawText("BRANCH: ${card.branchCode} • OFFICIAL CARRIER BADGE", width / 2f, 1515f, footerTextPaint)
        }

        return bitmap
    }

    /**
     * Shares ID card bitmap to other apps (WhatsApp, Drive, Email, etc.)
     */
    fun shareCard(context: Context, card: EmployeeIdCard, isFront: Boolean = true) {
        val bitmap = renderCardBitmap(context, card, isFront)
        val cacheDir = File(context.cacheDir, "shared_cards")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val side = if (isFront) "FRONT" else "BACK"
        val file = File(cacheDir, "DTDC_ID_${card.employeeCode.replace('/', '_')}_$side.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DTDC Hariom Enterprises Employee ID Card - ${card.fullName}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Official Employee ID Card for ${card.fullName} (${card.employeeCode}) - DTDC Channel Partner: Hariom Enterprises"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Employee ID Card"))
    }

    /**
     * Saves ID card bitmap to device pictures / downloads gallery
     */
    fun saveCardToGallery(context: Context, card: EmployeeIdCard, isFront: Boolean = true): Boolean {
        return try {
            val bitmap = renderCardBitmap(context, card, isFront)
            val filename = "DTDC_ID_${card.employeeCode.replace('/', '_')}_${if (isFront) "FRONT" else "BACK"}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DTDC_Cards")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri).use { out ->
                        if (out != null) bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    Toast.makeText(context, "ID Card saved to Pictures/DTDC_Cards!", Toast.LENGTH_SHORT).show()
                    true
                } else false
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DTDC_Cards")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(context, "ID Card saved: ${file.name}", Toast.LENGTH_SHORT).show()
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
