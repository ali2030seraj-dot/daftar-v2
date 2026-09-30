package com.daftar.v2.ui.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.components.AmbientBackground
import com.daftar.v2.ui.components.EmptyState
import com.daftar.v2.ui.components.GlassCard
import com.daftar.v2.ui.components.GlassToast
import com.daftar.v2.ui.components.StaggeredAppear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    navController: NavHostController,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val people by viewModel.people.collectAsState()
    val message by viewModel.message.collectAsState()
    val tokens = LocalAdaptiveTokens.current

    AmbientBackground {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("الأرشيف", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                )
            )

            if (people.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = "📦",
                        title = "الأرشيف فارغ",
                        subtitle = "لا توجد حسابات مؤرشفة حاليًا"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = tokens.screenPaddingHorizontal,
                            vertical = tokens.screenPaddingVertical
                        ),
                    verticalArrangement = Arrangement.spacedBy(tokens.cardSpacing),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(people, key = { it.id }) { person ->
                        StaggeredAppear(index = 0) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            person.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            person.currency,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = { viewModel.restore(person.id) }) {
                                        Text("استعادة")
                                    }
                                    TextButton(onClick = { viewModel.delete(person.id) }) {
                                        Text("حذف", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        GlassToast(
            message = message,
            onDismiss = { viewModel.clearMessage() }
        )
    }
}