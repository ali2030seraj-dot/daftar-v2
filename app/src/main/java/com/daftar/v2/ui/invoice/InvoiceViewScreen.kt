package com.daftar.v2.ui.invoice

import android.content.Context
import android.content.Intent
import androidx.core.content.res.ResourcesCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.R
import com.daftar.v2.branding.BrandingManager
import com.daftar.v2.data.entity.InvoiceEntity
import com.daftar.v2.data.entity.InvoiceItemEntity
import com.daftar.v2.data.entity.InvoiceStatus
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.theme.DangerRed
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber
import com.daftar.v2.util.MoneyFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BrandingEntryPoint {
    fun brandingManager(): BrandingManager
}

private fun getBrandingManager(context: Context): BrandingManager {
    val appContext = context.applicationContext
    return EntryPointAccessors.fromApplication(
        appContext,
        BrandingEntryPoint::class.java
    ).brandingManager()
}

// ═══════════════════════════════════════════════════════════
//              الواجهة (Compose)
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceViewScreen(
    navController: NavHostController,
    viewModel: InvoiceViewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("عرض الفاتورة", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        state.invoice?.let { inv ->
                            IconButton(onClick = {
                                navController.navigate(Destinations.invoiceEdit(inv.id))
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "تعديل")
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    shareInvoiceAsPdf(context, inv, state.items, state.person)
                                }
                            }) {
                                Icon(Icons.Filled.Share, contentDescription = "مشاركة")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                )
            }
        ) { padding ->
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                val inv = state.invoice
                if (inv != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 40.dp)
                    ) {
                        item { InvoiceHeader(inv) }
                        item { PersonCard(state.person) }
                        item {
                            Text(
                                "البنود",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        items(state.items) { item ->
                            InvoiceItemRow(item)
                        }
                        item { InvoiceTotals(inv) }
                        item { InvoiceNote(inv) }

                        if (inv.status != InvoiceStatus.PAID) {
                            item {
                                Button(
                                    onClick = { viewModel.markAsPaid() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SuccessGreen
                                    )
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("تعليم كمدفوعة", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        GlassToast(
            message = state.message,
            isError = state.isError,
            onDismiss = { viewModel.clearMessage() }
        )
    }
}

@Composable
private fun InvoiceHeader(inv: InvoiceEntity) {
    val dateFmt = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    val dateStr = dateFmt.format(Date(inv.issuedAt))

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    inv.number,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default
                )
                val statusColor = when (inv.status) {
                    InvoiceStatus.PAID -> SuccessGreen
                    InvoiceStatus.ISSUED -> TertiaryAmber
                    InvoiceStatus.DRAFT -> MaterialTheme.colorScheme.onSurfaceVariant
                    InvoiceStatus.CANCELLED -> DangerRed
                }
                val statusLabel = when (inv.status) {
                    InvoiceStatus.PAID -> "مدفوعة"
                    InvoiceStatus.ISSUED -> "صادرة"
                    InvoiceStatus.DRAFT -> "مسودة"
                    InvoiceStatus.CANCELLED -> "ملغاة"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusLabel,
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "التاريخ: $dateStr",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PersonCard(person: PersonEntity?) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                "العميل",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                person?.name ?: "—",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            if (!person?.phone.isNullOrBlank()) {
                Text(
                    person.phone,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InvoiceItemRow(item: InvoiceItemEntity) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.description, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    "الكمية: ${formatQty(item.quantity)} × ${MoneyFormatter.formatShort(item.unitPriceCents)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                MoneyFormatter.formatShort(item.totalCents),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = TertiaryAmber
            )
        }
    }
}

@Composable
private fun InvoiceTotals(inv: InvoiceEntity) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            TotalRow("المجموع", inv.subtotalCents)
            if (inv.discountCents > 0) TotalRow("الخصم", -inv.discountCents)
            if (inv.taxCents > 0) TotalRow("الضريبة", inv.taxCents)
            Spacer(Modifier.height(6.dp))
            TotalRow("الإجمالي", inv.totalCents, color = SuccessGreen, bold = true)
        }
    }
}

@Composable
private fun InvoiceNote(inv: InvoiceEntity) {
    if (inv.note.isNotBlank()) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    "ملاحظات",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(inv.note, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TotalRow(
    label: String,
    cents: Long,
    color: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = if (bold) 14.sp else 12.sp,
            fontWeight = if (bold) FontWeight.Black else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            MoneyFormatter.formatShort(cents),
            fontSize = if (bold) 16.sp else 13.sp,
            fontWeight = if (bold) FontWeight.Black else FontWeight.SemiBold,
            color = color
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              أدوات مساعدة
// ═══════════════════════════════════════════════════════════

private fun formatQty(q: Double): String {
    return if (q == q.toLong().toDouble()) {
        q.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", q).trimEnd('0').trimEnd('.')
    }
}

private fun loadFontSafely(context: Context, resId: Int, style: Int): Typeface {
    return try {
        ResourcesCompat.getFont(context, resId) ?: Typeface.create(Typeface.SERIF, style)
    } catch (e: Exception) {
        Typeface.create(Typeface.SERIF, style)
    }
}

private fun makeCircularBitmap(source: Bitmap, size: Int): Bitmap {
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint().apply {
        isAntiAlias = true
        color = AndroidColor.WHITE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    val srcRect = Rect(0, 0, source.width, source.height)
    val dstRect = Rect(0, 0, size, size)
    canvas.drawBitmap(source, srcRect, dstRect, paint)
    return output
}

private suspend fun loadStoreLogo(context: Context): Bitmap? {
    return try {
        val branding = getBrandingManager(context)
        val raw = branding.getLogoBitmap()
        raw?.let { makeCircularBitmap(it, 400) }
    } catch (e: Exception) {
        null
    }
}

private fun drawCentered(canvas: Canvas, text: String, centerX: Float, y: Float, paint: Paint) {
    val w = paint.measureText(text)
    canvas.drawText(text, centerX - w / 2, y, paint)
}

private fun drawRightAligned(canvas: Canvas, text: String, rightX: Float, y: Float, paint: Paint) {
    val w = paint.measureText(text)
    canvas.drawText(text, rightX - w, y, paint)
}

// ═══════════════════════════════════════════════════════════
//              ⭐⭐⭐ PDF احترافي — مُحسَّن
// ═══════════════════════════════════════════════════════════

private suspend fun shareInvoiceAsPdf(
    context: Context,
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    person: PersonEntity?
) {
    try {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // ═════ هوية المتجر ═════
        val branding = getBrandingManager(context)
        val storeName = try {
            branding.getStoreName().ifBlank { "دفتر حساباتي" }
        } catch (e: Exception) { "دفتر حساباتي" }

        val storePhone = try { branding.getStorePhone() } catch (e: Exception) { "" }
        val storeAddress = try { branding.getStoreAddress() } catch (e: Exception) { "" }
        val storeLogo = loadStoreLogo(context)

        // ═════ الخطوط ═════
        val cairoRegular = loadFontSafely(context, R.font.cairo_regular, Typeface.NORMAL)
        val cairoSemiBold = loadFontSafely(context, R.font.cairo_semibold, Typeface.BOLD)
        val cairoBold = loadFontSafely(context, R.font.cairo_bold, Typeface.BOLD)
        val cairoBlack = loadFontSafely(context, R.font.cairo_black, Typeface.BOLD)

        // ═════ الألوان ═════
        val goldColor = AndroidColor.rgb(212, 175, 55)
        val goldLight = AndroidColor.rgb(250, 243, 224)
        val colorTextDark = AndroidColor.rgb(30, 30, 30)
        val colorTextMedium = AndroidColor.rgb(80, 80, 80)
        val colorTextMuted = AndroidColor.rgb(120, 120, 120)
        val colorLineSoft = AndroidColor.rgb(220, 220, 220)
        val colorHeaderBg = AndroidColor.rgb(40, 40, 40)
        val colorZebraBg = AndroidColor.rgb(250, 250, 250)
        val colorGreenText = AndroidColor.rgb(22, 101, 52)

        // ═════ الفرش ═════
        val paintRegular = Paint().apply {
            color = colorTextDark; textSize = 10f
            isAntiAlias = true; typeface = cairoRegular
        }
        val paintSemi = Paint().apply {
            color = colorTextDark; textSize = 10f
            isAntiAlias = true; typeface = cairoSemiBold; isFakeBoldText = true
        }
        val paintBold = Paint().apply {
            color = colorTextDark; textSize = 11f
            isAntiAlias = true; typeface = cairoBold
        }
        val paintTitle = Paint().apply {
            color = colorTextDark; textSize = 18f
            isAntiAlias = true; typeface = cairoBlack
        }
        val paintSmall = Paint().apply {
            color = colorTextMuted; textSize = 9f
            isAntiAlias = true; typeface = cairoRegular
        }
        val paintTiny = Paint().apply {
            color = colorTextMuted; textSize = 8f
            isAntiAlias = true; typeface = cairoRegular
        }
        val paintWhiteBold = Paint().apply {
            color = AndroidColor.WHITE; textSize = 10f
            isAntiAlias = true; typeface = cairoBold; isFakeBoldText = true
        }
        val paintGold = Paint().apply {
            color = goldColor; textSize = 14f
            isAntiAlias = true; typeface = cairoBlack
        }
        val paintGreenBig = Paint().apply {
            color = colorGreenText; textSize = 14f
            isAntiAlias = true; typeface = cairoBlack; isFakeBoldText = true
        }

        val lineSoft = Paint().apply {
            color = colorLineSoft; strokeWidth = 0.6f
        }
        val lineGold = Paint().apply {
            color = goldColor; strokeWidth = 1.5f
        }
        val headerBg = Paint().apply {
            color = colorHeaderBg; style = Paint.Style.FILL
        }
        val zebraBg = Paint().apply {
            color = colorZebraBg; style = Paint.Style.FILL
        }

        // ═════ الإحداثيات ═════
        val margin = 32f
        val pageWidth = 595
        val pageHeight = 842
        val contentWidth = pageWidth - margin * 2
        val pageCenter = pageWidth / 2f

        var y = margin + 10f

        // ═══════════════════════════════════════════════════════
        //              1) الشعار الدائري المتمركز
        // ═══════════════════════════════════════════════════════

        val logoSize = 80f
        val logoCenterX = pageCenter
        val logoCenterY = y + logoSize / 2f
        val logoRadius = logoSize / 2f

        canvas.drawCircle(
            logoCenterX, logoCenterY, logoRadius,
            Paint().apply { color = goldLight; style = Paint.Style.FILL; isAntiAlias = true }
        )

        canvas.drawCircle(
            logoCenterX, logoCenterY, logoRadius,
            Paint().apply {
                color = goldColor; style = Paint.Style.STROKE
                strokeWidth = 2f; isAntiAlias = true
            }
        )

        if (storeLogo != null) {
            val innerSize = logoSize - 12
            val dstRect = RectF(
                logoCenterX - innerSize / 2f,
                logoCenterY - innerSize / 2f,
                logoCenterX + innerSize / 2f,
                logoCenterY + innerSize / 2f
            )
            canvas.drawBitmap(storeLogo, null, dstRect,
                Paint().apply { isAntiAlias = true; isFilterBitmap = true })
        } else {
            val defaultPaint = Paint().apply {
                color = goldColor; textSize = 36f; isAntiAlias = true
                typeface = cairoBlack; textAlign = Paint.Align.CENTER
            }
            canvas.drawText("د", logoCenterX, logoCenterY + 12f, defaultPaint)
        }

        y = logoCenterY + logoRadius + 14f

        // ═══════════════════════════════════════════════════════
        //              2) اسم المتجر + الهاتف
        // ═══════════════════════════════════════════════════════

        drawCentered(canvas, storeName, pageCenter, y, paintTitle)
        y += 18f

        val infoParts = mutableListOf<String>()
        if (storePhone.isNotBlank()) infoParts.add("📞 ${storePhone}")
        if (storeAddress.isNotBlank()) infoParts.add("📍 ${storeAddress}")
        if (infoParts.isNotEmpty()) {
            drawCentered(canvas, infoParts.joinToString("   |   "), pageCenter, y, paintSmall)
            y += 12f
        }

        // ═══════════════════════════════════════════════════════
        //              3) الخط الذهبي + العنوان
        // ═══════════════════════════════════════════════════════

        y += 4f
        canvas.drawLine(margin, y, pageWidth - margin, y, lineGold)
        y += 16f

        val titlePaint = Paint().apply {
            color = goldColor; textSize = 16f; isAntiAlias = true
            typeface = cairoBlack; isFakeBoldText = true
        }
        drawCentered(canvas, "فــاتــورة", pageCenter, y, titlePaint)
        y += 20f

        // ═══════════════════════════════════════════════════════
        //              4) صندوق معلومات العميل
        // ═══════════════════════════════════════════════════════

        val infoBoxTop = y
        val infoBoxHeight = 58f
        val infoBoxRect = RectF(margin, infoBoxTop, pageWidth - margin, infoBoxTop + infoBoxHeight)

        canvas.drawRoundRect(infoBoxRect, 8f, 8f,
            Paint().apply { color = AndroidColor.rgb(250, 250, 250); isAntiAlias = true })
        canvas.drawRoundRect(infoBoxRect, 8f, 8f,
            Paint().apply {
                color = colorLineSoft; style = Paint.Style.STROKE
                strokeWidth = 0.8f; isAntiAlias = true
            })

        val dateFmt = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        val dateStr = dateFmt.format(Date(invoice.issuedAt))

        val rightX = pageWidth - margin - 12f
        val leftX = margin + 12f
        var infoY = infoBoxTop + 20f

        val personName = person?.name ?: "—"
        val personPhone = person?.phone ?: ""

        drawRightAligned(canvas, "العميل: $personName", rightX, infoY, paintSemi)
        canvas.drawText("رقم الفاتورة: ${invoice.number}", leftX, infoY, paintRegular)

        infoY += 16f

        if (personPhone.isNotBlank()) {
            drawRightAligned(canvas, "الهاتف: $personPhone", rightX, infoY, paintRegular)
        }

        canvas.drawText("التاريخ: $dateStr", leftX, infoY, paintRegular)

        y = infoBoxTop + infoBoxHeight + 14f

        // ═══════════════════════════════════════════════════════
        //              5) رأس جدول البنود
        // ═══════════════════════════════════════════════════════

        val headerRowHeight = 28f
        val tableHeaderTop = y
        val tableHeaderBottom = y + headerRowHeight

        canvas.drawRect(margin, tableHeaderTop, pageWidth - margin, tableHeaderBottom, headerBg)

        val colDescRight = pageWidth - margin - 8f
        val colQtyRight = colDescRight - contentWidth * 0.48f
        val colPriceRight = colQtyRight - contentWidth * 0.15f
        val colTotalRight = colPriceRight - contentWidth * 0.20f

        val headerTextY = tableHeaderTop + 19f

        drawRightAligned(canvas, "البيان", colDescRight, headerTextY, paintWhiteBold)
        drawRightAligned(canvas, "الكمية", colQtyRight, headerTextY, paintWhiteBold)
        drawRightAligned(canvas, "السعر", colPriceRight, headerTextY, paintWhiteBold)
        drawRightAligned(canvas, "الإجمالي", colTotalRight, headerTextY, paintWhiteBold)

        y = tableHeaderBottom

        // ═══════════════════════════════════════════════════════
        //              6) صفوف البنود
        // ═══════════════════════════════════════════════════════

        val rowHeight = 26f
        items.forEachIndexed { index, item ->
            if (index % 2 == 1) {
                canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, zebraBg)
            }

            val rowTextY = y + 17f

            val desc = if (item.description.length > 28) {
                item.description.take(26) + "..."
            } else item.description
            drawRightAligned(canvas, desc, colDescRight, rowTextY, paintRegular)

            drawRightAligned(canvas, formatQty(item.quantity), colQtyRight, rowTextY, paintRegular)

            drawRightAligned(
                canvas,
                MoneyFormatter.formatShort(item.unitPriceCents),
                colPriceRight, rowTextY, paintRegular
            )

            drawRightAligned(
                canvas,
                MoneyFormatter.formatShort(item.totalCents),
                colTotalRight, rowTextY, paintSemi
            )

            canvas.drawLine(margin, y + rowHeight, pageWidth - margin, y + rowHeight, lineSoft)

            y += rowHeight
        }

        y += 20f

        // ═══════════════════════════════════════════════════════
        //              7) ⭐⭐⭐ جدول الإجماليات — مُوحَّد مع الجدول
        // ═══════════════════════════════════════════════════════

        // ⭐ عرض كامل — مطابق للجدول تماماً
        val totalsLeft = margin
        val totalsRight = pageWidth - margin

        // ⭐ حساب الصفوف
        data class TotalLine(val label: String, val value: Long, val isFinal: Boolean)

        val totalLines = mutableListOf<TotalLine>()
        totalLines.add(TotalLine("المجموع", invoice.subtotalCents, false))
        if (invoice.discountCents > 0) {
            totalLines.add(TotalLine("الخصم", -invoice.discountCents, false))
        }
        if (invoice.taxCents > 0) {
            totalLines.add(TotalLine("الضريبة", invoice.taxCents, false))
        }
        totalLines.add(TotalLine("الإجمالي", invoice.totalCents, true))

        // ⭐ ارتفاع الصف الواحد — مطابق لجدول البنود
        val totalRowHeight = 28f
        val totalsHeight = totalRowHeight * totalLines.size

        // ⭐ رسم الجدول — بلا زوايا دائرية، بلا إطار ذهبي
        // فقط نفس نمط الجدول الأساسي

        var totalY = y

        totalLines.forEachIndexed { index, line ->
            val isFinal = line.isFinal

            // ⭐ خلفية الصف الأخير (الإجمالي) — ذهبية فاتحة
            if (isFinal) {
                canvas.drawRect(
                    margin, totalY,
                    pageWidth - margin, totalY + totalRowHeight,
                    Paint().apply {
                        color = goldLight
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                )
            }

            // ⭐ حدود سفلية فقط — مطابقة لجدول البنود
            canvas.drawLine(
                margin, totalY + totalRowHeight,
                pageWidth - margin, totalY + totalRowHeight,
                if (isFinal) lineGold else lineSoft
            )

            // ⭐ النص
            val textY = totalY + totalRowHeight - 9f

            val labelPaint = if (isFinal) {
                Paint().apply {
                    color = goldColor
                    textSize = 12f
                    isAntiAlias = true
                    typeface = cairoBlack
                    isFakeBoldText = true
                }
            } else {
                Paint().apply {
                    color = colorTextMedium
                    textSize = 10f
                    isAntiAlias = true
                    typeface = cairoRegular
                }
            }

            val valuePaint = if (isFinal) {
                Paint().apply {
                    color = colorGreenText
                    textSize = 14f
                    isAntiAlias = true
                    typeface = cairoBlack
                    isFakeBoldText = true
                }
            } else {
                Paint().apply {
                    color = colorTextDark
                    textSize = 11f
                    isAntiAlias = true
                    typeface = cairoSemiBold
                    isFakeBoldText = true
                }
            }

            // ⭐ التسمية على اليمين
            drawRightAligned(
                canvas,
                "${line.label}:",
                pageWidth - margin - 12f,
                textY,
                labelPaint
            )

            // ⭐ القيمة على اليسار
            val valueText = if (line.value < 0) {
                "- ${MoneyFormatter.formatShort(abs(line.value))}"
            } else {
                MoneyFormatter.formatShort(line.value)
            }
            canvas.drawText(valueText, margin + 12f, textY, valuePaint)

            totalY += totalRowHeight
        }

        // ⭐ إطار خارجي رفيع للجدول (نفس نمط جدول البنود)
        canvas.drawRect(
            margin, y,
            pageWidth - margin, totalY,
            Paint().apply {
                color = colorLineSoft
                style = Paint.Style.STROKE
                strokeWidth = 0.6f
                isAntiAlias = true
            }
        )

        y = totalY + 24f

        // ═══════════════════════════════════════════════════════
        //              8) الملاحظات
        // ═══════════════════════════════════════════════════════

        if (invoice.note.isNotBlank()) {
            drawRightAligned(canvas, "ملاحظات:", pageWidth - margin, y, paintBold)
            y += 16f
            drawRightAligned(canvas, invoice.note, pageWidth - margin, y, paintSmall)
            y += 20f
        }

        // ═══════════════════════════════════════════════════════
        //              9) التذييل
        // ═══════════════════════════════════════════════════════

        val footerY = pageHeight - 30f

        canvas.drawLine(margin, footerY - 12f, pageWidth - margin, footerY - 12f, lineGold)

        val footerPaint = Paint().apply {
            color = goldColor; textSize = 11f; isAntiAlias = true
            typeface = cairoBold; isFakeBoldText = true
        }
        drawCentered(canvas, "شكراً لتعاملكم معنا", pageCenter, footerY + 2f, footerPaint)

        val tinyFooterPaint = Paint().apply {
            color = colorTextMuted; textSize = 8f
            isAntiAlias = true; typeface = cairoRegular
        }
        val footerSub = if (storePhone.isNotBlank())
            "للتواصل: $storePhone" else "صادر من تطبيق دفتر حساباتي"
        drawCentered(canvas, footerSub, pageCenter, footerY + 14f, tinyFooterPaint)

        pdf.finishPage(page)

        // ═════ حفظ ومشاركة ═════
        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val safeNumber = invoice.number.replace(Regex("[^A-Za-z0-9\\-]"), "_")
        val file = File(dir, "$safeNumber.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة"))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}