package com.daftar.v2.ui.statement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatementPdfActions(
    onSavePdf: () -> Unit,
    onSharePdf: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onSavePdf,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("حفظ PDF")
        }
        OutlinedButton(
            onClick = onSharePdf,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("مشاركة")
        }
    }
}