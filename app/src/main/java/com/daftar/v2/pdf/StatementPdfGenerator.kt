package com.daftar.v2.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.daftar.v2.R
import com.daftar.v2.domain.usecase.Statement
import com.daftar.v2.domain.usecase.StatementRow
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.DateFormatter
import com.daftar.v2.util.MoneyFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatementPdfGenerator @Inject constructor() {

    // ═══════════════════════════════════════════════════════
    //              أحجام الصفحة
    // ═══════════════════════════════════════════════════════

    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 32f
    private val contentWidth = pageWidth - margin * 2

    // ═══════════════════════════════════════════════════════
    //              الألوان
    // ═══════════════════════════════════════════════════════

    private val goldColor = Color.rgb(212, 175, 55)
    private val goldLight = Color.rgb(250, 243, 224)

    private val colorBlueBg = Color.rgb(239, 246, 255)
    private val colorGreenBg = Color.rgb(240, 253, 244)
    private val colorRedBg = Color.rgb(254, 242, 242)

    private val colorBlueBorder = Color.rgb(96, 165, 250)
    private val colorGreenBorder = Color.rgb(74, 222, 128)
    private val colorRedBorder = Color.rgb(248, 113, 113)

    private val colorBlueText = Color.rgb(30, 64, 175)
    private val colorGreenText = Color.rgb(22, 101, 52)
    private val colorRedText = Color.rgb(153, 27, 27)

    private val colorTextDark = Color.rgb(30, 30, 30)
    private val colorTextMedium = Color.rgb(80, 80, 80)
    private val colorTextMuted = Color.rgb(120, 120, 120)
    private val colorLineSoft = Color.rgb(220, 220, 220)
    private val colorHeaderBg = Color.rgb(40, 40, 40)
    private val colorZebraBg = Color.rgb(250, 250, 250)

    // ═══════════════════════════════════════════════════════
    //              الدالة الرئيسية
    // ═══════════════════════════════════════════════════════

    fun generate(
        context: Context,
        statement: Statement,
        branding: PdfBranding
    ): File {
        val doc = PdfDocument()

        // ═════ الخطوط ═════
        val cairoRegular = loadFont(context, R.font.cairo_regular, Typeface.SANS_SERIF)
        val cairoSemiBold = loadFont(context, R.font.cairo_semibold, Typeface.DEFAULT_BOLD)
        val cairoBold = loadFont(context, R.font.cairo_bold, Typeface.DEFAULT_BOLD)
        val cairoBlack = loadFont(context, R.font.cairo_black, Typeface.DEFAULT_BOLD)

        // ═════ الفرش ═════
        val paintRegular = Paint().apply {
            color = colorTextDark
            textSize = 10f
            isAntiAlias = true
            typeface = cairoRegular
        }
        val paintSemi = Paint().apply {
            color = colorTextDark
            textSize = 10f
            isAntiAlias = true
            typeface = cairoSemiBold
            isFakeBoldText = true
        }
        val paintBold = Paint().apply {
            color = colorTextDark
            textSize = 11f
            isAntiAlias = true
            typeface = cairoBold
        }
        val paintTitle = Paint().apply {
            color = colorTextDark
            textSize = 18f
            isAntiAlias = true
            typeface = cairoBlack
        }
        val paintSmall = Paint().apply {
            color = colorTextMuted
            textSize = 9f
            isAntiAlias = true
            typeface = cairoRegular
        }
        val paintTiny = Paint().apply {
            color = colorTextMuted
            textSize = 8f
            isAntiAlias = true
            typeface = cairoRegular
        }
        val paintWhiteBold = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isAntiAlias = true
            typeface = cairoBold
            isFakeBoldText = true
        }
        val paintGreenSemi = Paint().apply {
            color = colorGreenText
            textSize = 10f
            isAntiAlias = true
            typeface = cairoSemiBold
            isFakeBoldText = true
        }
        val paintBlueSemi = Paint().apply {
            color = colorBlueText
            textSize = 10f
            isAntiAlias = true
            typeface = cairoSemiBold
            isFakeBoldText = true
        }

        // ═════ الحدود ═════
        val lineSoft = Paint().apply {
            color = colorLineSoft
            strokeWidth = 0.6f
        }
        val lineGold = Paint().apply {
            color = goldColor
            strokeWidth = 1.5f
        }
        val headerBg = Paint().apply {
            color = colorHeaderBg
            style = Paint.Style.FILL
        }
        val zebraBg = Paint().apply {
            color = colorZebraBg
            style = Paint.Style.FILL
        }

        // ═════ الصفحات ═════
        var pageNumber = 1
        var page = doc.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas = page.canvas

        val statementNumber = buildStatementNumber(statement)

        var y = drawHeader(
            canvas = canvas,
            statement = statement,
            branding = branding,
            statementNumber = statementNumber,
            paintTitle = paintTitle,
            paintBold = paintBold,
            paintRegular = paintRegular,
            paintSemi = paintSemi,
            paintSmall = paintSmall,
            lineGold = lineGold
        )

        y = drawSummaryBoxes(
            canvas = canvas,
            statement = statement,
            y = y
        )

        y += 14f

        // ═════ الأعمدة ═════
        val colDate = 0.15f
        val colStatement = 0.33f
        val colAmount = 0.17f
        val colPayment = 0.17f
        val colBalance = 0.18f

        y = drawTableHeader(
            canvas = canvas,
            y = y,
            headerBg = headerBg,
            paintWhiteBold = paintWhiteBold,
            colDate = colDate,
            colStatement = colStatement,
            colAmount = colAmount,
            colPayment = colPayment,
            colBalance = colBalance
        )

        // ═════ الصفوف ═════
        val rowHeight = 26f
        var rowIndex = 0

        // ⭐⭐⭐ تخطي المعاملات الصفرية والـ voided
        val validRows = statement.rows.filter { row ->
            !row.transaction.isVoided && row.transaction.amountCents > 0L
        }

        for (row in validRows) {
            if (y + rowHeight > pageHeight - margin - 60f) {
                drawFooter(canvas, pageNumber, paintTiny, branding.storeName)
                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = page.canvas
                y = margin + 10f
                y = drawTableHeader(
                    canvas = canvas,
                    y = y,
                    headerBg = headerBg,
                    paintWhiteBold = paintWhiteBold,
                    colDate = colDate,
                    colStatement = colStatement,
                    colAmount = colAmount,
                    colPayment = colPayment,
                    colBalance = colBalance
                )
            }

            y = drawRow(
                canvas = canvas,
                row = row,
                y = y,
                rowIndex = rowIndex,
                rowHeight = rowHeight,
                paintRegular = paintRegular,
                paintSemi = paintSemi,
                paintGreenSemi = paintGreenSemi,
                paintBlueSemi = paintBlueSemi,
                zebraBg = zebraBg,
                lineSoft = lineSoft,
                colDate = colDate,
                colStatement = colStatement,
                colAmount = colAmount,
                colPayment = colPayment,
                colBalance = colBalance
            )
            rowIndex++
        }

        // ═════ الرصيد النهائي ═════
        y += 14f
        drawFinalBalanceBox(
            canvas = canvas,
            statement = statement,
            y = y
        )

        drawFooter(canvas, pageNumber, paintTiny, branding.storeName)

        doc.finishPage(page)

        // ═════ حفظ الملف ═════
        val dir = File(context.cacheDir, "statements").apply { mkdirs() }
        val personId = statement.person?.id ?: 0L
        val fileName = "statement_${personId}_${System.currentTimeMillis()}.pdf"
        val file = File(dir, fileName)

        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return file
    }

    // ═══════════════════════════════════════════════════════
    //              الرأس
    // ═══════════════════════════════════════════════════════

    private fun drawHeader(
        canvas: Canvas,
        statement: Statement,
        branding: PdfBranding,
        statementNumber: String,
        paintTitle: Paint,
        paintBold: Paint,
        paintRegular: Paint,
        paintSemi: Paint,
        paintSmall: Paint,
        lineGold: Paint
    ): Float {
        var y = margin + 10f

        val pageCenterX = pageWidth / 2f
        val logoSize = 80f

        // ═════ الشعار الدائري ═════
        val logo = branding.logoBitmap
        val logoCenterX = pageCenterX
        val logoCenterY = y + logoSize / 2f
        val logoRadius = logoSize / 2f

        canvas.drawCircle(
            logoCenterX,
            logoCenterY,
            logoRadius,
            Paint().apply {
                color = goldLight
                style = Paint.Style.FILL
                isAntiAlias = true
            }
        )

        canvas.drawCircle(
            logoCenterX,
            logoCenterY,
            logoRadius,
            Paint().apply {
                color = goldColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
        )

        if (logo != null && !logo.isRecycled) {
            val circularLogo = makeCircularBitmap(logo, (logoSize - 12).toInt())
            val dstRect = RectF(
                logoCenterX - (logoSize - 12) / 2f,
                logoCenterY - (logoSize - 12) / 2f,
                logoCenterX + (logoSize - 12) / 2f,
                logoCenterY + (logoSize - 12) / 2f
            )
            canvas.drawBitmap(
                circularLogo,
                null,
                dstRect,
                Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                }
            )
        } else {
            val defaultPaint = Paint().apply {
                color = goldColor
                textSize = 36f
                isAntiAlias = true
                typeface = paintTitle.typeface
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("د", logoCenterX, logoCenterY + 12f, defaultPaint)
        }

        y = logoCenterY + logoRadius + 14f

        // ═════ اسم المتجر ═════
        val storeName = branding.storeName.ifBlank { "دفتر حساباتي" }
        drawCentered(canvas, storeName, pageCenterX, y, paintTitle)
        y += 18f

        // ═════ بيانات المتجر ═════
        val infoParts = mutableListOf<String>()
        if (branding.storePhone.isNotBlank()) {
            infoParts.add("📞 ${branding.storePhone}")
        }
        if (branding.storeAddress.isNotBlank()) {
            infoParts.add("📍 ${branding.storeAddress}")
        }
        if (infoParts.isNotEmpty()) {
            drawCentered(canvas, infoParts.joinToString("   |   "), pageCenterX, y, paintSmall)
            y += 12f
        }

        // ═════ الخط الذهبي ═════
        y += 4f
        canvas.drawLine(margin, y, pageWidth - margin, y, lineGold)
        y += 16f

        // ═════ عنوان "كشف حساب" ═════
        val titlePaint = Paint().apply {
            color = goldColor
            textSize = 13f
            isAntiAlias = true
            typeface = paintBold.typeface
            isFakeBoldText = true
        }
        drawCentered(canvas, "كشف حساب تفصيلي", pageCenterX, y, titlePaint)
        y += 18f

        // ═════ صندوق المعلومات ═════
        val infoBoxTop = y
        val infoBoxHeight = 58f
        val infoBoxRect = RectF(
            margin,
            infoBoxTop,
            pageWidth - margin,
            infoBoxTop + infoBoxHeight
        )

        canvas.drawRoundRect(
            infoBoxRect,
            8f, 8f,
            Paint().apply {
                color = Color.rgb(250, 250, 250)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
        )

        canvas.drawRoundRect(
            infoBoxRect,
            8f, 8f,
            Paint().apply {
                color = colorLineSoft
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
                isAntiAlias = true
            }
        )

        val personName = statement.person?.name ?: "—"
        val personPhone = statement.person?.phone ?: ""
        val personCurrency = statement.person?.currency ?: statement.displayCurrency
        val currencyArabic = CurrencyUtils.arabicFullName(personCurrency)

        val now = System.currentTimeMillis()

        // ⭐⭐⭐ التاريخ الكامل (مع اليوم)
        val dateStr = try {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("ar", "SA"))
            sdf.format(Date(now))
        } catch (e: Exception) {
            DateFormatter.date(now)
        }

        val rightX = pageWidth - margin - 12f
        val leftX = margin + 12f

        var infoY = infoBoxTop + 20f

        val nameLabel = "الاسم: $personName"
        drawRightAligned(canvas, nameLabel, rightX, infoY, paintSemi)

        val numberLabel = "رقم الكشف: $statementNumber"
        canvas.drawText(numberLabel, leftX, infoY, paintRegular)

        infoY += 16f

        if (personPhone.isNotBlank()) {
            val phoneLabel = "الهاتف: $personPhone"
            drawRightAligned(canvas, phoneLabel, rightX, infoY, paintRegular)
        }

        val dateLabel = "التاريخ: $dateStr"
        canvas.drawText(dateLabel, leftX, infoY, paintRegular)

        infoY += 16f

        val currencyLabel = "العملة: $currencyArabic"
        drawRightAligned(canvas, currencyLabel, rightX, infoY, paintRegular)

        return infoBoxTop + infoBoxHeight + 14f
    }

    // ═══════════════════════════════════════════════════════
    //              صناديق الملخص
    // ═══════════════════════════════════════════════════════

    private fun drawSummaryBoxes(
        canvas: Canvas,
        statement: Statement,
        y: Float
    ): Float {
        val boxHeight = 68f
        val gap = 8f
        val boxWidth = (contentWidth - gap * 2) / 3f
        val currency = statement.person?.currency ?: statement.displayCurrency

        val boxTop = y
        val boxBottom = y + boxHeight

        // القسم 1 (يمين): الرصيد المتبقي
        val rightBoxLeft = pageWidth - margin - boxWidth
        val rightBoxRect = RectF(rightBoxLeft, boxTop, pageWidth - margin, boxBottom)
        drawColorBox(canvas, rightBoxRect, colorBlueBg, colorBlueBorder)

        drawBoxContent(
            canvas = canvas,
            boxRect = rightBoxRect,
            title = "الرصيد المتبقي",
            value = MoneyFormatter.format(statement.finalBalanceCents, currency),
            titleColor = colorBlueText,
            valueColor = colorBlueText,
            dividerColor = colorBlueBorder
        )

        // القسم 2 (وسط): إجمالي السداد
        val centerBoxLeft = rightBoxLeft - gap - boxWidth
        val centerBoxRect = RectF(centerBoxLeft, boxTop, rightBoxLeft - gap, boxBottom)
        drawColorBox(canvas, centerBoxRect, colorGreenBg, colorGreenBorder)

        drawBoxContent(
            canvas = canvas,
            boxRect = centerBoxRect,
            title = "إجمالي السداد",
            value = MoneyFormatter.format(statement.totalPaymentCents, currency),
            titleColor = colorGreenText,
            valueColor = colorGreenText,
            dividerColor = colorGreenBorder
        )

        // القسم 3 (يسار): إجمالي المبالغ
        val leftBoxRect = RectF(margin, boxTop, centerBoxLeft - gap, boxBottom)
        drawColorBox(canvas, leftBoxRect, colorRedBg, colorRedBorder)

        drawBoxContent(
            canvas = canvas,
            boxRect = leftBoxRect,
            title = "إجمالي المبالغ",
            value = MoneyFormatter.format(statement.totalDebtCents, currency),
            titleColor = colorRedText,
            valueColor = colorRedText,
            dividerColor = colorRedBorder
        )

        return boxBottom + 12f
    }

    private fun drawColorBox(
        canvas: Canvas,
        rect: RectF,
        bgColor: Int,
        borderColor: Int
    ) {
        canvas.drawRoundRect(
            rect, 10f, 10f,
            Paint().apply {
                color = bgColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
        )
        canvas.drawRoundRect(
            rect, 10f, 10f,
            Paint().apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }
        )
    }

    private fun drawBoxContent(
        canvas: Canvas,
        boxRect: RectF,
        title: String,
        value: String,
        titleColor: Int,
        valueColor: Int,
        dividerColor: Int
    ) {
        val centerX = (boxRect.left + boxRect.right) / 2f

        val titlePaint = Paint().apply {
            color = titleColor
            textSize = 10.5f
            isAntiAlias = true
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = valueColor
            textSize = 12.5f
            isAntiAlias = true
            isFakeBoldText = true
        }
        val dividerPaint = Paint().apply {
            color = dividerColor
            strokeWidth = 0.8f
        }

        drawCentered(canvas, title, centerX, boxRect.top + 24f, titlePaint)

        canvas.drawLine(
            boxRect.left + 15f,
            boxRect.top + 34f,
            boxRect.right - 15f,
            boxRect.top + 34f,
            dividerPaint
        )

        drawCentered(canvas, value, centerX, boxRect.top + 56f, valuePaint)
    }

    // ═══════════════════════════════════════════════════════
    //              رأس الجدول
    // ═══════════════════════════════════════════════════════

    private fun drawTableHeader(
        canvas: Canvas,
        y: Float,
        headerBg: Paint,
        paintWhiteBold: Paint,
        colDate: Float,
        colStatement: Float,
        colAmount: Float,
        colPayment: Float,
        colBalance: Float
    ): Float {
        val headerHeight = 28f
        val top = y
        val bottom = y + headerHeight

        canvas.drawRect(margin, top, pageWidth - margin, bottom, headerBg)

        val textY = top + 19f
        var right = pageWidth - margin - 8f

        drawRightAligned(canvas, "التاريخ", right, textY, paintWhiteBold)
        right -= contentWidth * colDate

        drawRightAligned(canvas, "البيان", right, textY, paintWhiteBold)
        right -= contentWidth * colStatement

        drawRightAligned(canvas, "المبلغ", right, textY, paintWhiteBold)
        right -= contentWidth * colAmount

        drawRightAligned(canvas, "السداد", right, textY, paintWhiteBold)
        right -= contentWidth * colPayment

        drawRightAligned(canvas, "الرصيد", right, textY, paintWhiteBold)

        return bottom
    }

    // ═══════════════════════════════════════════════════════
    //              الصفوف
    // ═══════════════════════════════════════════════════════

    private fun drawRow(
        canvas: Canvas,
        row: StatementRow,
        y: Float,
        rowIndex: Int,
        rowHeight: Float,
        paintRegular: Paint,
        paintSemi: Paint,
        paintGreenSemi: Paint,
        paintBlueSemi: Paint,
        zebraBg: Paint,
        lineSoft: Paint,
        colDate: Float,
        colStatement: Float,
        colAmount: Float,
        colPayment: Float,
        colBalance: Float
    ): Float {
        val tx = row.transaction

        if (rowIndex % 2 == 1) {
            canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, zebraBg)
        }

        val textY = y + 17f
        var right = pageWidth - margin - 8f

        // ⭐ التاريخ — بصيغة dd/MM
        val dateStr = formatShortDate(tx.timestamp)
        drawRightAligned(canvas, dateStr, right, textY, paintRegular)
        right -= contentWidth * colDate

        // ⭐ البيان
        val statementText = if (tx.statement.length > 22) {
            tx.statement.take(20) + "..."
        } else tx.statement
        drawRightAligned(canvas, statementText, right, textY, paintRegular)
        right -= contentWidth * colStatement

        // ⭐ المبلغ (DEBT فقط)
        val amountText = if (tx.type.name == "DEBT") {
            MoneyFormatter.formatShort(tx.amountCents)
        } else "-"
        val amountPaint = if (tx.type.name == "DEBT") paintSemi else paintRegular
        drawRightAligned(canvas, amountText, right, textY, amountPaint)
        right -= contentWidth * colAmount

        // ⭐ السداد (PAYMENT فقط)
        val paymentText = if (tx.type.name == "PAYMENT") {
            MoneyFormatter.formatShort(tx.amountCents)
        } else "-"
        val paymentPaint = if (tx.type.name == "PAYMENT") paintGreenSemi else paintRegular
        drawRightAligned(canvas, paymentText, right, textY, paymentPaint)
        right -= contentWidth * colPayment

        // ⭐ الرصيد التراكمي
        val balanceText = MoneyFormatter.formatShort(row.runningBalanceCents)
        drawRightAligned(canvas, balanceText, right, textY, paintBlueSemi)

        canvas.drawLine(margin, y + rowHeight, pageWidth - margin, y + rowHeight, lineSoft)

        return y + rowHeight
    }

    // ═══════════════════════════════════════════════════════
    //              الرصيد النهائي
    // ═══════════════════════════════════════════════════════

    private fun drawFinalBalanceBox(
        canvas: Canvas,
        statement: Statement,
        y: Float
    ) {
        val boxHeight = 42f
        val rect = RectF(margin, y, pageWidth - margin, y + boxHeight)

        canvas.drawRoundRect(
            rect, 10f, 10f,
            Paint().apply {
                color = Color.rgb(250, 250, 250)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
        )

        canvas.drawRoundRect(
            rect, 10f, 10f,
            Paint().apply {
                color = goldColor
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
            }
        )

        val labelPaint = Paint().apply {
            color = colorTextDark
            textSize = 12f
            isAntiAlias = true
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = colorGreenText
            textSize = 14f
            isAntiAlias = true
            isFakeBoldText = true
        }

        val label = "الرصيد النهائي:"
        val currency = statement.person?.currency ?: statement.displayCurrency
        val currencyArabic = CurrencyUtils.arabicFullName(currency)
        val value = "${MoneyFormatter.formatShort(statement.finalBalanceCents)} $currencyArabic"

        val labelWidth = labelPaint.measureText(label)
        val valueWidth = valuePaint.measureText(value)

        // ⭐ الترتيب: التسمية يمين + القيمة يسارها
        val labelX = pageWidth - margin - 16f - labelWidth
        val valueX = labelX - 12f - valueWidth

        canvas.drawText(label, labelX, y + 26f, labelPaint)
        canvas.drawText(value, valueX, y + 26f, valuePaint)
    }

    // ═══════════════════════════════════════════════════════
    //              التذييل
    // ═══════════════════════════════════════════════════════

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        paintTiny: Paint,
        storeName: String
    ) {
        val footerY = pageHeight - 30f

        canvas.drawLine(
            margin,
            footerY - 12f,
            pageWidth - margin,
            footerY - 12f,
            Paint().apply {
                color = goldColor
                strokeWidth = 1f
            }
        )

        val appText = storeName.ifBlank { "دفتر حساباتي" }
        canvas.drawText(appText, margin, footerY + 2f, paintTiny)

        val pageText = "صفحة $pageNumber"
        drawRightAligned(canvas, pageText, pageWidth - margin, footerY + 2f, paintTiny)
    }

    // ═══════════════════════════════════════════════════════
    //              أدوات
    // ═══════════════════════════════════════════════════════

    private fun loadFont(context: Context, resId: Int, fallback: Typeface): Typeface {
        return try {
            ResourcesCompat.getFont(context, resId) ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }

    private fun makeCircularBitmap(source: Bitmap, size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val srcRect = Rect(0, 0, source.width, source.height)
        val dstRect = Rect(0, 0, size, size)
        canvas.drawBitmap(source, srcRect, dstRect, paint)

        return output
    }

    private fun drawRightAligned(
        canvas: Canvas,
        text: String,
        rightX: Float,
        y: Float,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text)
        canvas.drawText(text, rightX - textWidth, y, paint)
    }

    private fun drawCentered(
        canvas: Canvas,
        text: String,
        centerX: Float,
        y: Float,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text)
        canvas.drawText(text, centerX - textWidth / 2, y, paint)
    }

    private fun formatShortDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM", Locale.US)
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    private fun buildStatementNumber(statement: Statement): String {
        val personId = statement.person?.id ?: 0L
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
        val idStr = personId.toString().padStart(4, '0')
        return "KS-$year-$idStr"
    }
}