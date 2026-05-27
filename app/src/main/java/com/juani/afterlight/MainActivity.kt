package com.juani.afterlight

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juani.afterlight.data.ArticleEntity
import com.juani.afterlight.domain.ArticleIntentParser
import com.juani.afterlight.domain.MarkdownArchive
import com.juani.afterlight.ui.AfterlightTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private var latestIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        latestIntent = intent
        setContent {
            val app = application as AfterlightApp
            val viewModel: AfterlightViewModel = viewModel(factory = AfterlightViewModel.factory(app))
            LaunchedEffect(latestIntent) { viewModel.capture(latestIntent) }
            AfterlightTheme { AfterlightAppScreen(viewModel) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        latestIntent = intent
    }
}

class AfterlightViewModel(private val app: AfterlightApp) : ViewModel() {
    val articles = app.repository.observeArticles()
    var lastImportMessage by mutableStateOf<String?>(null)
        private set

    suspend fun capture(intent: Intent?) {
        val incoming = ArticleIntentParser.parse(intent) ?: return
        val id = app.repository.save(incoming)
        lastImportMessage = "Guardado en Afterlight · #$id"
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
            CenterAlignedTopAppBar(
                title = { Text("Afterlight", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { InboxHero(count = articles.size, message = message) }
            if (articles.isEmpty()) {
                item { EmptyInboxCard() }
            } else {
                items(articles) { article -> ArticleCard(article = article, onOpen = { onOpen(article) }) }
            }
        }
    }
}

@Composable
private fun InboxHero(count: Int, message: String?) {
    val gradient = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer),
    )
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.background(gradient).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(28.dp))
                Text("Lectura local, limpia y offline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Guardá desde PaywallReader; Afterlight extrae el cuerpo, lo convierte a Markdown durable y lo muestra como reader, no como archivo crudo.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("$count artículos") })
                AssistChip(onClick = {}, label = { Text("Markdown local") })
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun EmptyInboxCard() {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.Inbox, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Inbox vacío", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Compartí una URL, texto o HTML desde PaywallReader. Cuando llegue algo, aparece acá con tiempo de lectura y una vista de lectura minimalista.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ArticleCard(article: ArticleEntity, onOpen: () -> Unit) {
    ElevatedCard(
        onClick = onOpen,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(
                listOfNotNull(article.source, "${article.estimatedReadingMinutes} min", article.status.lowercase()).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            article.originalUrl?.let {
                Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReaderScreen(article: ArticleEntity, markdown: String, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var nightMode by remember { mutableStateOf(true) }
    var fontScale by remember { mutableStateOf(18f) }
    var controlsExpanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val background = if (nightMode) Color(0xFF11100E) else MaterialTheme.colorScheme.background
    val foreground = if (nightMode) Color(0xFFEDE4D4) else MaterialTheme.colorScheme.onBackground
    val secondary = if (nightMode) Color(0xFFBDB2A0) else MaterialTheme.colorScheme.onSurfaceVariant
    val body = remember(markdown) { MarkdownArchive.displayBody(markdown) }
    val blocks = remember(body) { parseMarkdownBlocks(body) }

    Box(Modifier.fillMaxSize().background(background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(article.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver") } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = background, titleContentColor = foreground, navigationIconContentColor = foreground),
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    ReaderHeader(article = article, foreground = foreground, secondary = secondary)
                }
                items(blocks) { block -> MarkdownBlockView(block, foreground, secondary, fontScale) }
            }
        }
        HorizontalFloatingToolbar(
            expanded = controlsExpanded,
            floatingActionButton = {
                FloatingActionButton(onClick = { controlsExpanded = !controlsExpanded }) {
                    Icon(Icons.Rounded.TextFields, contentDescription = "Controles de lectura")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().height(76.dp),
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            floatingActionButtonPosition = FloatingToolbarHorizontalFabPosition.End,
        ) {
            IconButton(onClick = { nightMode = !nightMode }) {
                Icon(if (nightMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, contentDescription = "Modo noche")
            }
            Slider(value = fontScale, onValueChange = { fontScale = it }, valueRange = 15f..26f, modifier = Modifier.widthIn(min = 150.dp, max = 220.dp))
            IconButton(onClick = {
                val file = File(article.markdownPath)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/markdown"
                    putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.juani.afterlight.files", file))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Exportar Markdown"))
            }) { Icon(Icons.Rounded.FileDownload, contentDescription = "Exportar Markdown") }
        }
    }
}

@Composable
private fun ReaderHeader(article: ArticleEntity, foreground: Color, secondary: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(article.title, color = foreground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(
            listOfNotNull(article.source, "${article.wordCount} palabras", "${article.estimatedReadingMinutes} min").joinToString(" · "),
            color = secondary,
            style = MaterialTheme.typography.labelLarge,
        )
        article.originalUrl?.let { Text(it, color = secondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        HorizontalDivider(color = secondary.copy(alpha = 0.28f), modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun MarkdownBlockView(block: MarkdownBlock, foreground: Color, secondary: Color, fontScale: Float) {
    when (block) {
        is MarkdownBlock.Heading -> Text(
            block.text,
            color = foreground,
            fontSize = (fontScale + (7 - block.level).coerceAtLeast(1)).sp,
            lineHeight = (fontScale * 1.35f).sp,
            fontWeight = FontWeight.SemiBold,
        )
        is MarkdownBlock.Paragraph -> Text(block.text, color = foreground, fontSize = fontScale.sp, lineHeight = (fontScale * 1.6f).sp, fontFamily = FontFamily.Serif)
        is MarkdownBlock.Bullet -> Text("• ${block.text}", color = foreground, fontSize = fontScale.sp, lineHeight = (fontScale * 1.55f).sp, fontFamily = FontFamily.Serif)
        is MarkdownBlock.Quote -> Text(block.text, color = secondary, fontSize = fontScale.sp, lineHeight = (fontScale * 1.55f).sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(start = 14.dp))
        is MarkdownBlock.Code -> Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(18.dp)) {
            Text(block.text, modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
        }
    }
}

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data class Code(val text: String) : MarkdownBlock
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraph = StringBuilder()
    var inCode = false
    val code = StringBuilder()

    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotBlank()) blocks += MarkdownBlock.Paragraph(text.cleanupInlineMarkdown())
        paragraph.clear()
    }

    markdown.lineSequence().forEach { raw ->
        val line = raw.trimEnd()
        if (line.trim().startsWith("```")) {
            if (inCode) {
                blocks += MarkdownBlock.Code(code.toString().trimEnd())
                code.clear()
                inCode = false
            } else {
                flushParagraph()
                inCode = true
            }
            return@forEach
        }
        if (inCode) {
            code.appendLine(line)
            return@forEach
        }
        when {
            line.isBlank() -> flushParagraph()
            line.startsWith("#") -> {
                flushParagraph()
                val level = line.takeWhile { it == '#' }.length.coerceIn(1, 6)
                blocks += MarkdownBlock.Heading(level, line.drop(level).trim().cleanupInlineMarkdown())
            }
            line.trimStart().startsWith("- ") -> {
                flushParagraph()
                blocks += MarkdownBlock.Bullet(line.trimStart().drop(2).trim().cleanupInlineMarkdown())
            }
            line.trimStart().matches(Regex("\\d+\\.\\s+.*")) -> {
                flushParagraph()
                blocks += MarkdownBlock.Bullet(line.trimStart().replaceFirst(Regex("\\d+\\.\\s+"), "").cleanupInlineMarkdown())
            }
            line.trimStart().startsWith(">") -> {
                flushParagraph()
                blocks += MarkdownBlock.Quote(line.trimStart().removePrefix(">").trim().cleanupInlineMarkdown())
            }
            else -> paragraph.append(line).append(' ')
        }
    }
    flushParagraph()
    if (code.isNotBlank()) blocks += MarkdownBlock.Code(code.toString().trimEnd())
    return blocks.ifEmpty { listOf(MarkdownBlock.Paragraph(markdown.trim())) }
}

private fun String.cleanupInlineMarkdown(): String =
    replace(Regex("!\\[([^]]*)]\\([^)]*\\)"), "$1")
        .replace(Regex("\\[([^]]+)]\\(([^)]+)\\)"), "$1")
        .replace("**", "")
        .replace("*", "")
        .replace("`", "")
        .trim()
