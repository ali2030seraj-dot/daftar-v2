package com.daftar.v2.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.daftar.v2.R
import com.daftar.v2.ui.receipt.ReceiptUiState

/**
 * ⭐⭐⭐⭐⭐ مُولّد صورة الإيصال — موحّد مع كشف الحساب.
 *
 * ✅ الإصلاحات:
 *  - لا تداخل بين النصوص
 *  - صندوق معلومات بـ 6 صفوف منفصلة
 *  - التاريخ مقسم إلى سطرين (ميلادي + هجري)
 *  - محاذاة يمينية كاملة (RTL)
 *  - مسافات كافية بين الأسطر
 */
object ReceiptImageGenerator {

    private const val WIDTH = 1080
    private const val PAD_OUTER = 40f
    private const val PAD_INNER = 50f
    private const val SECTION_GAP = 24f

    // الألوان
    private val GOLD = AndroidColor.rgb(212, 168, 75)
    private val GOLD_BG = AndroidColor.rgb(250, 243, 224)
    private val GOLD_DARK = AndroidColor.rgb(184, 148, 31)
    private val TEXT_DARK = AndroidColor.rgb(30, 30, 30)
    private val TEXT_MEDIUM = AndroidColor.rgb(80, 80, 80)
    private val TEXT_MUTED = AndroidColor.rgb(120, 120, 120)
    private val LINE_SOFT = AndroidColor.rgb(220, 220, 220)
    private val WHITE = AndroidColor.WHITE

    // ═══════════════════════════════════════════════════════
    //              الدالة الرئيسية
    // ═══════════════════════════════════════════════════════

    fun generate(context: Context, state: ReceiptUiState): Bitmap? {
        try {
            val person = state.person ?: return null
            val tx = state.transaction ?: return null
            val isPayment = state.isPayment

            // ⭐ الخطوط
            val cairoRegular = loadFontSafely(context, R.font.cairo_regular, Typeface.NORMAL)
            val cairoSemiBold = loadFontSafely(context, R.font.cairo_semibold, Typeface.BOLD)
            val cairoBold = loadFontSafely(context, R.font.cairo_bold, Typeface.BOLD)
            val cairoBlack = loadFontSafely(context, R.font.cairo_black, Typeface.BOLD)

            // ⭐ التاريخ — مقسم
            val fullDate = try {
                DateFormatter.fullDualLabel(tx.timestamp)
            } catch (e: Exception) {
                DateFormatter.fullLabel(tx.timestamp)
            }
            val dateParts = fullDate.split("•").map { it.trim() }
            val gregorianDate = dateParts.getOrNull(0) ?: fullDate
            val hijriDate = dateParts.getOrNull(1) ?: ""

            // ⭐ حساب الارتفاع
            var totalHeight = PAD_OUTER * 2 + PAD_INNER * 2
            totalHeight += 430f   // header
            totalHeight += 300f   // صندوق المعلومات (6 صفوف)
            totalHeight += 320f   // جدول المبالغ
            if (tx.statement.isNotBlank() || tx.note.isNotBlank()) totalHeight += 180f
            totalHeight += 300f   // QR
            totalHeight += 120f   // footer

            val height = totalHeight.toInt()
            val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(WHITE)

            // ⭐ الإطار الخارجي الذهبي
            canvas.drawRoundRect(
                RectF(PAD_OUTER, PAD_OUTER, WIDTH - PAD_OUTER, height - PAD_OUTER),
                24f, 24f,
                Paint().apply {
                    color = GOLD
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                    isAntiAlias = true
                }
            )

            val pageCenter = WIDTH / 2f
            var y = PAD_OUTER + PAD_INNER

            // ═══════════════════════════════════════════════════
            //              1) الشعار الدائري
            // ═══════════════════════════════════════════════════

            val logoSize = 200f
            val logoCenterX = pageCenter
            val logoCenterY = y + logoSize / 2f
            val logoRadius = logoSize / 2f

            // ⭐ الحلقة الذهبية الخارجية
            canvas.drawCircle(
                logoCenterX, logoCenterY, logoRadius + 5f,
                Paint().apply {
                    color = GOLD
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    isAntiAlias = true
                }
            )

            // ⭐ الخلفية البيضاء
            canvas.drawCircle(
                logoCenterX, logoCenterY, logoRadius,
                Paint().apply {
                    color = WHITE
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
            )

            // ⭐ الشعار
            if (state.logoBitmap != null) {
                val circularLogo = makeCircularBitmap(state.logoBitmap, (logoSize * 2).toInt())
                val dstRect = RectF(
                    logoCenterX - logoRadius,
                    logoCenterY - logoRadius,
                    logoCenterX + logoRadius,
                    logoCenterY + logoRadius
                )
                canvas.drawBitmap(
                    circularLogo, null, dstRect,
                    Paint().apply { isAntiAlias = true; isFilterBitmap = true }
                )
            } else {
                val defaultPaint = Paint().apply {
                    color = GOLD
                    textSize = 80f
                    isAntiAlias = true
                    typeface = cairoBlack
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("د", logoCenterX, logoCenterY + 28f, defaultPaint)
            }

            y = logoCenterY + logoRadius + 30f

            // ═══════════════════════════════════════════════════
            //              2) اسم المتجر
            // ═══════════════════════════════════════════════════

            val namePaint = Paint().apply {
                color = TEXT_DARK
                textSize = 48f
                textAlign = Paint.Align.CENTER
                typeface = cairoBlack
                isAntiAlias = true
                isFakeBoldText = true
            }
            canvas.drawText(state.storeName, pageCenter, y + 48f, namePaint)
            y += 70f

            // ═══════════════════════════════════════════════════
            //              3) عنوان الإيصال
            // ═══════════════════════════════════════════════════

            val titlePaint = Paint().apply {
                color = GOLD_DARK
                textSize = 36f
                textAlign = Paint.Align.CENTER
                typeface = cairoBlack
                isAntiAlias = true
                isFakeBoldText = true
            }
            val titleText = if (isPayment) "إيصال سداد" else "إيصال دين"
            canvas.drawText(titleText, pageCenter, y + 40f, titlePaint)
            y += 60f

            // ═══════════════════════════════════════════════════
            //              4) خط ذهبي
            // ═══════════════════════════════════════════════════

            canvas.drawLine(
                PAD_OUTER + PAD_INNER, y,
                WIDTH - PAD_OUTER - PAD_INNER, y,
                Paint().apply {
                    color = GOLD
                    strokeWidth = 2f
                    isAntiAlias = true
                }
            )
            y += 24f

            // ═══════════════════════════════════════════════════
            //              5) صندوق المعلومات (6 صفوف)
            // ═══════════════════════════════════════════════════

            val infoBoxTop = y
            val infoBoxLeft = PAD_OUTER + PAD_INNER
            val infoBoxRight = WIDTH - PAD_OUTER - PAD_INNER
            val infoBoxWidth = infoBoxRight - infoBoxLeft
            val rowSpacing = 38f
            var rowCount = 4  // العميل، رقم، ميلادي، هجري
            if (person.phone.isNotBlank()) rowCount++  // هاتف
            rowCount++  // عملة
            val infoBoxHeight = (rowSpacing * rowCount) + 40f

            // ⭐ خلفية + إطار
            canvas.drawRoundRect(
                RectF(infoBoxLeft, infoBoxTop, infoBoxRight, infoBoxTop + infoBoxHeight),
                12f, 12f,
                Paint().apply { color = GOLD_BG; isAntiAlias = true }
            )
            canvas.drawRoundRect(
                RectF(infoBoxLeft, infoBoxTop, infoBoxRight, infoBoxTop + infoBoxHeight),
                12f, 12f,
                Paint().apply {
                    color = GOLD
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    isAntiAlias = true
                }
            )

            // ⭐ المحاذاة: كل النصوص على اليمين
            val rightMargin = infoBoxRight - 24f
            var infoY = infoBoxTop + 34f

            // ⭐ فرش النصوص
            val labelPaint = Paint().apply {
                color = TEXT_MEDIUM
                textSize = 24f
                typeface = cairoRegular
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
            }
            val valuePaint = Paint().apply {
                color = TEXT_DARK
                textSize = 26f
                typeface = cairoSemiBold
                isAntiAlias = true
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            val currencyPaint = Paint().apply {
                color = GOLD_DARK
                textSize = 24f
                typeface = cairoSemiBold
                isAntiAlias = true
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            // ⭐⭐⭐ الإصلاح: دالة رسم صحيحة بدون تداخل

            // السطر 1: العميل
            drawRightRow(canvas, "العميل:", person.name, rightMargin, infoY, labelPaint, valuePaint)
            infoY += rowSpacing

            // السطر 2: رقم الإيصال
            drawRightRow(canvas, "رقم الإيصال:", state.receiptNumber, rightMargin, infoY, labelPaint, valuePaint)
            infoY += rowSpacing

            // السطر 3: التاريخ الميلادي
            drawRightRow(canvas, "التاريخ:", gregorianDate, rightMargin, infoY, labelPaint, valuePaint)
            infoY += rowSpacing

            // السطر 4: التاريخ الهجري (إن وُجد)
            if (hijriDate.isNotBlank()) {
                drawRightRow(canvas, "هجري:", hijriDate, rightMargin, infoY, labelPaint, valuePaint)
                infoY += rowSpacing
            }

            // السطر 5: الهاتف
            if (person.phone.isNotBlank()) {
                drawRightRow(canvas, "الهاتف:", person.phone, rightMargin, infoY, labelPaint, valuePaint)
                infoY += rowSpacing
            }

            // السطر 6: العملة
            drawRightRow(
                canvas,
                "العملة:",
                CurrencyUtils.arabicFullName(person.currency),
                rightMargin,
                infoY,
                labelPaint,
                currencyPaint
            )

            y = infoBoxTop + infoBoxHeight + 30f

            // ═══════════════════════════════════════════════════
            //              6) جدول المبالغ
            // ═══════════════════════════════════════════════════

            val tableLeft = PAD_OUTER + PAD_INNER
            val tableRight = WIDTH - PAD_OUTER - PAD_INNER
            val rowHeight = 90f

            data class AmountRow(
                val label: String,
                val value: String,
                val isHighlight: Boolean,
                val isBold: Boolean,
                val color: Int
            )

            val rows = listOf(
                AmountRow(
                    label = "الرصيد السابق",
                    value = MoneyFormatter.format(state.balanceBeforeCents, person.currency),
                    isHighlight = false,
                    isBold = false,
                    color = TEXT_MEDIUM
                ),
                AmountRow(
                    label = if (isPayment) "المبلغ المسدد" else "المبلغ المضاف",
                    value = MoneyFormatter.format(tx.amountCents, person.currency),
                    isHighlight = true,
                    isBold = true,
                    color = GOLD_DARK
                ),
                AmountRow(
                    label = "الرصيد المتبقي",
                    value = MoneyFormatter.format(state.balanceAfterCents, person.currency),
                    isHighlight = false,
                    isBold = true,
                    color = TEXT_DARK
                )
            )

            val tableHeight = rowHeight * rows.size

            // ⭐ إطار الجدول
            canvas.drawRoundRect(
                RectF(tableLeft, y, tableRight, y + tableHeight),
                12f, 12f,
                Paint().apply {
                    color = LINE_SOFT
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    isAntiAlias = true
                }
            )

            var rowY = y
            rows.forEachIndexed { index, row ->
                // ⭐ خلفية الصف المميز
                if (row.isHighlight) {
                    canvas.drawRect(
                        RectF(tableLeft, rowY, tableRight, rowY + rowHeight),
                        Paint().apply { color = GOLD_BG; isAntiAlias = true }
                    )
                }

                val rowLabelPaint = Paint().apply {
                    color = if (row.isHighlight) GOLD_DARK else TEXT_MEDIUM
                    textSize = 26f
                    typeface = if (row.isBold) cairoSemiBold else cairoRegular
                    isAntiAlias = true
                    isFakeBoldText = row.isBold
                    textAlign = Paint.Align.RIGHT
                }

                val rowValuePaint = Paint().apply {
                    color = row.color
                    textSize = if (row.isBold) 30f else 26f
                    typeface = if (row.isBold) cairoBold else cairoSemiBold
                    isAntiAlias = true
                    isFakeBoldText = row.isBold
                    textAlign = Paint.Align.RIGHT
                }

                val textY = rowY + rowHeight / 2f + 10f

                // ⭐ التسمية على اليمين
                canvas.drawText(row.label, tableRight - 24f, textY, rowLabelPaint)

                // ⭐ القيمة في المنتصف (المساحة اليسرى)
                val centerX = tableLeft + ((tableRight - tableLeft) * 0.25f)
                canvas.drawText(row.value, centerX + 60f, textY, rowValuePaint)

                // ⭐ خط فاصل
                if (index < rows.size - 1) {
                    canvas.drawLine(
                        tableLeft + 12f, rowY + rowHeight,
                        tableRight - 12f, rowY + rowHeight,
                        Paint().apply { color = LINE_SOFT; strokeWidth = 1f }
                    )
                }

                rowY += rowHeight
            }

            y += tableHeight + 30f

            // ═══════════════════════════════════════════════════
            //              7) البيان
            // ═══════════════════════════════════════════════════

            if (tx.statement.isNotBlank() || tx.note.isNotBlank()) {
                val lines = mutableListOf<Pair<String, String>>()
                if (tx.statement.isNotBlank()) lines.add("البيان:" to tx.statement)
                if (tx.note.isNotBlank()) lines.add("ملاحظات:" to tx.note)

                val lineHeight = 40f
                val statementHeight = 30f + lineHeight * lines.size

                canvas.drawRoundRect(
                    RectF(tableLeft, y, tableRight, y + statementHeight),
                    12f, 12f,
                    Paint().apply { color = GOLD_BG; isAntiAlias = true }
                )
                canvas.drawRoundRect(
                    RectF(tableLeft, y, tableRight, y + statementHeight),
                    12f, 12f,
                    Paint().apply {
                        color = GOLD
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                        isAntiAlias = true
                    }
                )

                var lineY = y + 46f
                val stateLabelPaint = Paint().apply {
                    color = GOLD_DARK
                    textSize = 24f
                    typeface = cairoSemiBold
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = Paint.Align.LEFT
                }
                val stateValuePaint = Paint().apply {
                    color = TEXT_DARK
                    textSize = 24f
                    typeface = cairoRegular
                    isAntiAlias = true
                    textAlign = Paint.Align.LEFT
                }

                lines.forEach { (label, value) ->
                    drawRightRow(
                        canvas,
                        label,
                        value,
                        tableRight - 20f,
                        lineY,
                        stateLabelPaint,
                        stateValuePaint
                    )
                    lineY += lineHeight
                }

                y += statementHeight + 30f
            }

            // ═══════════════════════════════════════════════════
            //              8) QR Code
            // ═══════════════════════════════════════════════════

            val qrContent = buildQrContent(state, person.name, tx.amountCents)
            val qrSize = 220
            val qrBitmap = QrCodeGenerator.generate(qrContent, qrSize)

            if (qrBitmap != null) {
                val qrX = (WIDTH - qrSize) / 2f

                canvas.drawRoundRect(
                    RectF(qrX - 12f, y - 12f, qrX + qrSize + 12f, y + qrSize + 12f),
                    12f, 12f,
                    Paint().apply { color = WHITE; isAntiAlias = true }
                )
                canvas.drawRoundRect(
                    RectF(qrX - 12f, y - 12f, qrX + qrSize + 12f, y + qrSize + 12f),
                    12f, 12f,
                    Paint().apply {
                        color = GOLD
                        style = Paint.Style.STROKE
                        strokeWidth = 1.5f
                        isAntiAlias = true
                    }
                )

                canvas.drawBitmap(
                    qrBitmap, null,
                    RectF(qrX, y, qrX + qrSize, y + qrSize),
                    Paint().apply { isAntiAlias = true; isFilterBitmap = true }
                )

                y += qrSize + 30f

                val scanPaint = Paint().apply {
                    color = TEXT_MUTED
                    textSize = 22f
                    textAlign = Paint.Align.CENTER
                    typeface = cairoRegular
                    isAntiAlias = true
                }
                canvas.drawText("امسح للتحقق", pageCenter, y + 24f, scanPaint)
                y += 50f
            }

            // ═══════════════════════════════════════════════════
            //              9) الفوتر
            // ═══════════════════════════════════════════════════

            canvas.drawLine(
                PAD_OUTER + PAD_INNER, y,
                WIDTH - PAD_OUTER - PAD_INNER, y,
                Paint().apply { color = GOLD; strokeWidth = 2f; isAntiAlias = true }
            )
            y += 34f

            val footerPaint = Paint().apply {
                color = GOLD_DARK
                textSize = 28f
                textAlign = Paint.Align.CENTER
                typeface = cairoBold
                isAntiAlias = true
                isFakeBoldText = true
            }
            canvas.drawText("شكراً لتعاملكم معنا", pageCenter, y, footerPaint)

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // ═══════════════════════════════════════════════════════
    //              ⭐⭐⭐ الدالة الأساسية — بدون تداخل
    // ═══════════════════════════════════════════════════════

    /**
     * ⭐ يرسم "التسمية: القيمة" بمحاذاة يمينية كاملة.
     *
     * ✅ الإصلاح: لا يستخدم Align.RIGHT (يسبب التداخل).
     * يحسب العروض يدوياً ثم يرسم بـ Align.LEFT.
     *
     * التوزيع البصري (RTL):
     *   [اليمين] التسمية: القيمة [اليسار]
     *
     * أي:
     *   - التسمية على يمين السطر
     *   - القيمة على يسار التسمية (نحو الوسط)
     */
    private fun drawRightRow(
        canvas: Canvas,
        label: String,
        value: String,
        rightX: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        // ⭐ تأكد من Align.LEFT
        labelPaint.textAlign = Paint.Align.LEFT
        valuePaint.textAlign = Paint.Align.LEFT

        val labelWidth = labelPaint.measureText(label)
        val valueWidth = valuePaint.measureText(value)
        val gap = 8f
        val totalWidth = labelWidth + gap + valueWidth

        // ⭐ بداية الرسم
        val startX = rightX - totalWidth

        // ⭐ القيمة على اليسار
        canvas.drawText(value, startX, y, valuePaint)

        // ⭐ التسمية على اليمين (بعد القيمة + مسافة)
        canvas.drawText(label, startX + valueWidth + gap, y, labelPaint)
    }

    // ═══════════════════════════════════════════════════════
    //              أدوات مساعدة
    // ═══════════════════════════════════════════════════════

    private fun makeCircularBitmap(source: Bitmap, size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            color = WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(0, 0, source.width, source.height)
        val dstRect = Rect(0, 0, size, size)
        canvas.drawBitmap(source, srcRect, dstRect, paint)
        return output
    }

    private fun loadFontSafely(context: Context, resId: Int, style: Int): Typeface {
        return try {
            ResourcesCompat.getFont(context, resId) ?: Typeface.create(Typeface.SERIF, style)
        } catch (e: Exception) {
            Typeface.create(Typeface.SERIF, style)
        }
    }

    private fun buildQrContent(
        state: ReceiptUiState,
        personName: String,
        amountCents: Long
    ): String {
        val currency = state.person?.currency ?: ""
        val amountText = MoneyFormatter.format(amountCents, currency)
        val remainingText = MoneyFormatter.format(state.balanceAfterCents, currency)
        val dateText = try {
            DateFormatter.fullDualLabel(state.transaction?.timestamp ?: 0L)
        } catch (e: Exception) {
            DateFormatter.fullLabel(state.transaction?.timestamp ?: 0L)
        }
        return buildString {
            appendLine(state.storeName)
            appendLine("إيصال: ${state.receiptNumber}")
            appendLine("العميل: $personName")
            appendLine("المبلغ: $amountText")
            appendLine("المتبقي: $remainingText")
            appendLine("التاريخ: $dateText")
        }.trim()
    }
}