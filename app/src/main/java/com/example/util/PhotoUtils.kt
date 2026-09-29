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

        val bgPaint = Paint().apply { isAntiAlias = true }

        if (isFront) {
            // Background
            bgPaint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Top Header: Deep DTDC Navy
            bgPaint.color = android.graphics.Color.parseColor("#0A235C")
            canvas.drawRect(0f, 0f, width.toFloat(), 270f, bgPaint)

            // Red Chevron Accent Stripe
            bgPaint.color = android.graphics.Color.parseColor("#D32F2F")
            canvas.drawRect(0f, 260f, width.toFloat(), 280f, bgPaint)

            // Gold accent stripe
            bgPaint.color = android.graphics.Color.parseColor("#FFB300")
            canvas.drawRect(0f, 280f, width.toFloat(), 288f, bgPaint)

            // Header DTDC text
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                textSize = 68f
            }
            canvas.drawText("DTDC COURIER", width / 2f, 95f, textPaint)

            textPaint.apply {
                textSize = 34f
                color = android.graphics.Color.parseColor("#FFB300")
            }
            canvas.drawText("EXPRESS LOGISTICS PARTNER", width / 2f, 150f, textPaint)

            textPaint.apply {
                textSize = 38f
                color = android.graphics.Color.WHITE
            }
            canvas.drawText(card.branchName.uppercase(), width / 2f, 215f, textPaint)

            // Employee Passport Photo
            val photoBox = RectF(width / 2f - 180f, 320f, width / 2f + 180f, 800f)
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
                textSize = 34f
                isFakeBoldText = false
            }
            canvas.drawText(card.department, width / 2f, 1035f, textPaint)

            // Details Grid
            val labelPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 30f
                textAlign = Paint.Align.LEFT
            }
            val valuePaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#0F172A")
                textSize = 34f
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
            // BACK SIDE
            bgPaint.color = android.graphics.Color.parseColor("#0F172A")
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 32f, 32f, bgPaint)

            // Top Header: Red stripe
            bgPaint.color = android.graphics.Color.parseColor("#D32F2F")
            canvas.drawRect(0f, 0f, width.toFloat(), 130f, bgPaint)

            val textPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                textSize = 44f
            }
            canvas.drawText("TERMS & INSTRUCTIONS", width / 2f, 85f, textPaint)

            // Terms Content
            val bodyPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#E2E8F0")
                textSize = 28f
                textAlign = Paint.Align.LEFT
            }

            var textY = 200f
            val leftMargin = 70f

            val terms = listOf(
                "1. This card is the property of Hariom Enterprises",
                "   (Authorized Channel Partner of DTDC Courier Ltd).",
                "2. The cardholder is authorized to deliver and collect",
                "   consignments on behalf of DTDC express services.",
                "3. Display this identity badge visibly during duty hours.",
                "4. Loss of card must be reported immediately to hub office.",
                "5. If found, please return to the branch office address."
            )
            for (line in terms) {
                canvas.drawText(line, leftMargin, textY, bodyPaint)
                textY += 44f
            }

            textY += 30f
            val headerPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.parseColor("#FFB300")
                textSize = 34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("BRANCH OFFICE & HUB:", leftMargin, textY, headerPaint)
            textY += 45f

            bodyPaint.textSize = 28f
            canvas.drawText(card.branchAddress, leftMargin, textY, bodyPaint)
            textY += 75f

            canvas.drawText("EMERGENCY HELPLINE: ${card.emergencyContact}", leftMargin, textY, headerPaint)
            textY += 90f

            // Draw Barcode visualization
            val barPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                strokeWidth = 6f
                isAntiAlias = true
            }
            val barBg = RectF(100f, textY, width - 100f, textY + 160f)
            val whitePaint = Paint().apply { color = android.graphics.Color.WHITE }
            canvas.drawRoundRect(barBg, 12f, 12f, whitePaint)

            barPaint.color = android.graphics.Color.BLACK
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
                textSize = 26f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("* ${card.employeeCode} *", width / 2f, textY + 150f, codePaint)

            // Authorized Signatory
            val signPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 26f
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("AUTHORIZED SIGNATORY", width - 80f, 1400f, signPaint)
            canvas.drawText("HARIOM ENTERPRISES", width - 80f, 1435f, signPaint)
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
