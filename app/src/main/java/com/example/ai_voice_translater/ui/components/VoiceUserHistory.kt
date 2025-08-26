// UserHistoryScreen.kt
package com.example.ai_voice_translater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ai_voice_translater.data.Translation
import com.example.voicetranslator.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistoryScreen(
    navController: NavController,
    mainViewModel: MainViewModel = viewModel()
) {
    val history by mainViewModel.translationHistory.collectAsState()

    // Color theme matching TranslatorScreen
    val primaryBlue = Color(0xFF007BFF)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Translation History",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryBlue
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TranslationListView(
                translations = history,
                onDelete = { translationId ->
                    mainViewModel.deleteTranslation(translationId)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationListView(
    translations: List<Translation>,
    onDelete: (String) -> Unit
) {
    var selectedTranslation by remember { mutableStateOf<Translation?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    // Color theme
    val primaryBlue = Color(0xFF007BFF)

    if (translations.isEmpty()) {
        EmptyStateView()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(translations) { translation ->
                TranslationItem(
                    translation = translation,
                    onDelete = { onDelete(translation.id) },
                    onClick = { selectedTranslation = translation }
                )
            }

            // Add some bottom padding for better scrolling experience
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Sheet for full translation view
        selectedTranslation?.let { translation ->
            ModalBottomSheet(
                onDismissRequest = { selectedTranslation = null },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                dragHandle = {
                    Surface(
                        modifier = Modifier.padding(vertical = 11.dp),
                        color = primaryBlue.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(width = 32.dp, height = 4.dp)
                        )
                    }
                }
            ) {
                TranslationDetailView(
                    translation = translation,
                    onClose = { selectedTranslation = null }
                )
            }
        }
    }
}

@Composable
fun TranslationItem(
    translation: Translation,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Color theme
    val primaryBlue = Color(0xFF007BFF)
    val lightBlue = Color(0xFFE3F2FD)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Translation icon container with blue theme
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .background(
//                        lightBlue,
//                        CircleShape
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Translate,
//                    contentDescription = "Translation",
//                    tint = primaryBlue,
//                    modifier = Modifier.size(24.dp)
//                )
//            }

            Spacer(modifier = Modifier.width(16.dp))

            // Translation content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = translation.originalText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = translation.translatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button with red accent
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .size(40.dp)
//                    .background(
//                        Color(0xFFFFEBEE),
//                        CircleShape
//                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Translation",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }

    // Delete confirmation dialog with blue theme
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(
                    "Delete Translation",
                    color = primaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this translation? This action cannot be undone.",
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = primaryBlue
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationDetailView(
    translation: Translation,
    onClose: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    // Color theme
    val primaryBlue = Color(0xFF007BFF)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header with blue theme
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Translation Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = primaryBlue
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFF5F5F5),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Original Text Section
        TranslationSection(
            title = "Original Text",
            text = translation.originalText,
            onCopy = {
                clipboardManager.setText(AnnotatedString(translation.originalText))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Translated Text Section
        TranslationSection(
            title = "Translation",
            text = translation.translatedText,
            onCopy = {
                clipboardManager.setText(AnnotatedString(translation.translatedText))
            }
        )
    }
}

@Composable
fun TranslationSection(
    title: String,
    text: String,
    onCopy: () -> Unit
) {
    // Color theme
    val primaryBlue = Color(0xFF007BFF)
    val lightBlue = Color(0xFFE3F2FD)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = primaryBlue
            )

            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        lightBlue,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $title",
                    tint = primaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = lightBlue.copy(alpha = 0.5f)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
fun EmptyStateView() {
    // Color theme
    val primaryBlue = Color(0xFF007BFF)
    val lightBlue = Color(0xFFE3F2FD)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    lightBlue,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "No History",
                modifier = Modifier.size(60.dp),
                tint = primaryBlue.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No translations yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = primaryBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your translation history will appear here once you start translating",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserHistoryPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        TranslationListView(
            translations = listOf(
                Translation(id = "1", originalText = "Hello, how are you doing today?", translatedText = "Hola, ¿cómo estás hoy?"),
                Translation(id = "2", originalText = "Goodbye my friend", translatedText = "Adiós mi amigo"),
                Translation(id = "3", originalText = "Thank you very much for your help", translatedText = "Muchas gracias por tu ayuda")
            ),
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyHistoryPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        EmptyStateView()
    }
}

@Preview(showBackground = true)
@Composable
private fun TranslationDetailPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        TranslationDetailView(
            translation = Translation(
                id = "1",
                originalText = "Hello, how are you doing today? I hope you're having a wonderful day and everything is going well for you.",
                translatedText = "Hola, ¿cómo estás hoy? Espero que tengas un día maravilloso y que todo te vaya bien."
            ),
            onClose = {}
        )
    }
}