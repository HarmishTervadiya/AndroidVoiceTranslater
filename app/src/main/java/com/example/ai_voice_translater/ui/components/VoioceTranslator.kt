// TranslatorScreen.kt
package com.example.ai_voice_translater.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voicetranslator.ui.MainViewModel
import com.example.voicetranslator.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    navController: NavController,
    mainViewModel: MainViewModel = viewModel()
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val selectedLanguageCode by mainViewModel.selectedLanguage.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Color theme
    val primaryBlue = Color(0xFF007BFF)
    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0056B3)

    // Permission handling
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                mainViewModel.toggleRecording()
            } else {
                Toast.makeText(context, "Permission denied. Cannot record audio.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Language map
    val languages = mapOf(
        "English" to "en-IN",
        "Hindi" to "hi-IN",
        "Gujarati" to "gu-IN",
        "Spanish" to "es-ES"
    )
    val selectedLanguageName = languages.entries.find { it.value == selectedLanguageCode }?.key ?: "Select Language"

    // Reset state when screen opens
    LaunchedEffect(Unit) {
        mainViewModel.resetState()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Voice Translator",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Language Selection Card
            LanguageSelectionCard(
                selectedLanguageName = selectedLanguageName,
                languages = languages,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onLanguageSelected = { code ->
                    mainViewModel.setLanguage(code)
                    expanded = false
                },
                primaryBlue = primaryBlue,
                lightBlue = lightBlue
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Recording Section
            RecordingSection(
                uiState = uiState,
                onMicClick = {
                    if (uiState is UiState.Recording) {
                        mainViewModel.toggleRecording()
                    } else {
                        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            mainViewModel.toggleRecording()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                primaryBlue = primaryBlue,
                darkBlue = darkBlue
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Translation Result Section
            TranslationResultSection(
                uiState = uiState,
                onCopy = { text ->
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                primaryBlue = primaryBlue,
                lightBlue = lightBlue
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LanguageSelectionCard(
    selectedLanguageName: String,
    languages: Map<String, String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit,
    primaryBlue: Color,
    lightBlue: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(lightBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = primaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedButton(
                    onClick = { onExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(primaryBlue, primaryBlue))),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryBlue
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedLanguageName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    languages.forEach { (name, code) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    name,
                                    color = if (name == selectedLanguageName) primaryBlue else Color.Black,
                                    fontWeight = if (name == selectedLanguageName) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onLanguageSelected(code)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingSection(
    uiState: UiState,
    onMicClick: () -> Unit,
    primaryBlue: Color,
    darkBlue: Color
) {
    val isRecording = uiState is UiState.Recording
    val isLoading = uiState is UiState.Loading

    // Animation for recording state
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mic Button with Animation
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Outer pulse ring for recording
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .background(
                            primaryBlue.copy(alpha = 0.2f),
                            CircleShape
                        )
                )
            }

            // Main mic button
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = when {
                                isRecording -> listOf(Color(0xFFFF4444), Color(0xFFCC0000))
                                isLoading -> listOf(darkBlue, primaryBlue)
                                else -> listOf(primaryBlue, darkBlue)
                            }
                        ),
                        shape = CircleShape
                    )
                    .clickable { onMicClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(50.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Text
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "status"
        ) { state ->
            Text(
                text = when (state) {
                    is UiState.Recording -> "Recording... Speak now"
                    is UiState.Loading -> "Processing translation..."
                    else -> "Tap to start recording"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = when (state) {
                    is UiState.Recording -> Color(0xFFFF4444)
                    is UiState.Loading -> darkBlue
                    else -> primaryBlue
                },
                textAlign = TextAlign.Center
            )
        }

        if (uiState !is UiState.Recording && uiState !is UiState.Loading) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hold and speak clearly for best results",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TranslationResultSection(
    uiState: UiState,
    onCopy: (String) -> Unit,
    primaryBlue: Color,
    lightBlue: Color
) {
    val displayText = when (val state = uiState) {
        is UiState.Success -> state.translation
        is UiState.Error -> state.message
        else -> null
    }

    AnimatedVisibility(
        visible = displayText != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(500)
        ) + fadeIn(animationSpec = tween(500)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (uiState is UiState.Error) Color(0xFFFFEBEE) else lightBlue
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState is UiState.Error) "Translation Error" else "Translation Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState is UiState.Error) Color(0xFFD32F2F) else primaryBlue
                    )

                    if (uiState is UiState.Success && displayText?.isNotBlank() == true) {
                        IconButton(
                            onClick = { onCopy(displayText) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    primaryBlue.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy translation",
                                tint = primaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (displayText?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = displayText,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (uiState is UiState.Error) Color(0xFFD32F2F) else Color.Black,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }
                }
            }
        }
    }
}