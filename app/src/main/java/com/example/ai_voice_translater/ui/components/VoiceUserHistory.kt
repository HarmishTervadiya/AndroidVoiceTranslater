package com.example.ai_voice_translater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


data class Translation(val original: String, val translated: String)
val translationList = listOf(
    Translation("Hello", "Hola"),
    Translation("Hello", "Bonjour"),
    Translation("Hello", "Gutenia Tag"),
    Translation("Hello", "Ciao"),
    Translation("Hello", "Konnichiwa"),
    Translation("Hello", "Ni hao"),
    Translation("Hello", "Hola"),
    Translation("Hello", "Bonjour"),
    Translation("Hello", "Gutenia Tag"),
    Translation("Hello", "Ciao"),
    Translation("Hello", "Konnichiwa"),
    Translation("Hello", "Ni hao"),
    Translation("Hello", "Bonjour"),
    Translation("Hello", "Gutenia Tag"),
    Translation("Hello", "Ciao"),
    Translation("Hello", "Konnichiwa"),
    Translation("Hello", "Ni hao"),
)


@Composable
fun UserHistory(modifier: Modifier = Modifier) {
    UserHistoryContent(modifier = modifier)
}


@Composable
fun UserHistoryContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "History",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(height = 20.dp))
        TranslationListView()
    }
}

@Preview(showBackground = true)
@Composable
private fun UserHistoryPreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
    UserHistory()
    }
}



@Composable
fun TranslationListView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(translationList) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.original,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = item.translated,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }
    }
}

