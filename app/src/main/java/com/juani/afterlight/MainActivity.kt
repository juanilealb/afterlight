package com.juani.afterlight

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juani.afterlight.data.ArticleEntity
import com.juani.afterlight.domain.ArticleIntentParser
import com.juani.afterlight.ui.AfterlightTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = application as AfterlightApp
            val viewModel: AfterlightViewModel = viewModel(factory = AfterlightViewModel.factory(app))
            LaunchedEffect(intent) { viewModel.capture(intent) }
            AfterlightTheme { AfterlightAppScreen(viewModel) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

class AfterlightViewModel(private val app: AfterlightApp) : ViewModel() {
    val articles = app.repository.observeArticles()
    var lastImportMessage by mutableStateOf<String?>(null)
        private set

    suspend fun capture(intent: Intent?) {
        val incoming = ArticleIntentParser.parse(intent) ?: return
        val id = app.repository.save(incoming)
        lastImportMessage = "Guardado en Afterlight (#$id)"
    }

    suspend fun markdown(id: Long): Pair<ArticleEntity, String>? = app.repository.getMarkdown(id)

    companion object {
        fun factory(app: AfterlightApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AfterlightViewModel(app) as T
        }
    }
}

@Composable
fun AfterlightAppScreen(viewModel: AfterlightViewModel) {
    val articles by viewModel.articles.collectAsState(initial = emptyList())
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    var selectedMarkdown by remember { mutableStateOf<Pair<ArticleEntity, String>?>(null) }

    LaunchedEffect(selectedId) {
        selectedMarkdown = selectedId?.let { viewModel.markdown(it) }
    }

    if (selectedMarkdown != null) {
        ReaderScreen(
            article = selectedMarkdown!!.first,
            markdown = selectedMarkdown!!.second,
            onBack = { selectedId = null },
        )
    } else {
        InboxScreen(
            articles = articles,
            message = viewModel.lastImportMessage,
            onOpen = { selectedId = it.id },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxScreen(articles: List<ArticleEntity>, message: String?, onOpen: (ArticleEntity) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Afterlight") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        if (articles.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Inbox, contentDescription = null)
                    Text("Inbox vacío", style = MaterialTheme.typography.headlineSmall)
                    Text("Compartí una URL, texto o HTML desde PaywallReader u otra app. Afterlight lo guarda como Markdown local para leer offline.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                message?.let { item { Text(it, color = MaterialTheme.colorScheme.primary) } }
                items(articles) { article ->
                    Card(onClick = { onOpen(article) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(article.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(listOfNotNull(article.source, "${article.estimatedReadingMinutes} min", article.status).joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                            article.originalUrl?.let { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderScreen(article: ArticleEntity, markdown: String, onBack: () -> Unit) {
    var nightMode by remember { mutableStateOf(true) }
    var fontScale by remember { mutableStateOf(18f) }
    val context = LocalContext.current
    val background = if (nightMode) Color(0xFF11100E) else MaterialTheme.colorScheme.background
    val foreground = if (nightMode) Color(0xFFEDE4D4) else MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver") } },
                actions = {
                    Icon(Icons.Rounded.DarkMode, contentDescription = null)
                    Switch(checked = nightMode, onCheckedChange = { nightMode = it })
                    IconButton(onClick = {
                        val file = File(article.markdownPath)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/markdown"
                            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.juani.afterlight.files", file))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Exportar Markdown"))
                    }) { Icon(Icons.Rounded.FileDownload, contentDescription = "Exportar Markdown") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(background).padding(padding).padding(horizontal = 22.dp).verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tamaño", color = foreground, modifier = Modifier.padding(end = 12.dp))
                Slider(value = fontScale, onValueChange = { fontScale = it }, valueRange = 15f..26f, modifier = Modifier.weight(1f))
            }
            Text(markdown, color = foreground, fontSize = fontScale.sp, lineHeight = (fontScale * 1.55f).sp, fontFamily = FontFamily.Serif)
            Spacer(Modifier.height(48.dp))
        }
    }
}
