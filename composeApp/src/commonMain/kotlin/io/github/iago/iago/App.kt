package io.github.iago.iago

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
    val isError: Boolean = false,
)

private val IagoDarkBlueColorScheme = darkColorScheme(
    primary = Color(0xFF7FB8EE),
    onPrimary = Color(0xFF04213A),
    primaryContainer = Color(0xFF0A223A),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = Color(0xFF9FB8D8),
    onSecondary = Color(0xFF12273E),
    secondaryContainer = Color(0xFF1C3248),
    onSecondaryContainer = Color(0xFFD4E6FF),
    tertiary = Color(0xFF89CFEF),
    onTertiary = Color(0xFF003447),
    surface = Color(0xFF071421),
    onSurface = Color(0xFFE4EDF9),
    surfaceVariant = Color(0xFF162638),
    onSurfaceVariant = Color(0xFFB8C9DC),
    background = Color(0xFF050E18),
    onBackground = Color(0xFFE4EDF9),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val IagoBotTextColor = Color(0xFFE6F1FF)

@Composable
@Preview
fun App() {
    val repository = remember { GeminiRepository() }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Oi! Eu sou o IAgo. Como posso ajudar?",
                fromUser = false,
            ),
        )
    }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = IagoDarkBlueColorScheme) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
        ) {
            Text(
                text = "IAgo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                items(messages) { message ->
                    val alignment = if (message.fromUser) Alignment.End else Alignment.Start
                    val bubbleColor = when {
                        message.isError -> MaterialTheme.colorScheme.errorContainer
                        message.fromUser -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val bubbleContentColor = when {
                        message.isError -> MaterialTheme.colorScheme.onErrorContainer
                        message.fromUser -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> IagoBotTextColor
                    }
                    val bubbleAccentColor = when {
                        message.isError -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = alignment,
                    ) {
                        Card(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 80.dp)
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = bubbleColor,
                                contentColor = bubbleContentColor,
                            ),
                        ) {
                            MessageContent(
                                message = message,
                                textColor = bubbleContentColor,
                                accentColor = bubbleAccentColor,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Digite sua mensagem") },
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
                Button(
                    enabled = input.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    onClick = {
                        val prompt = input.trim()
                        input = ""
                        messages.add(ChatMessage(prompt, fromUser = true))
                        isLoading = true

                        scope.launch {
                            val result = repository.sendMessage(prompt)
                            messages.add(
                                if (result.isSuccess) {
                                    ChatMessage(result.getOrThrow(), fromUser = false)
                                } else {
                                    ChatMessage(
                                        text = result.exceptionOrNull()?.message
                                            ?: "Falha ao obter resposta do Gemini.",
                                        fromUser = false,
                                        isError = true,
                                    )
                                },
                            )
                            isLoading = false
                        }
                    },
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Enviar")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageContent(
    message: ChatMessage,
    textColor: Color,
    accentColor: Color,
) {
    val blocks = remember(message.text) { parseMessageBlocks(message.text) }

    Column(modifier = Modifier.padding(12.dp)) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MessageBlock.Paragraph -> {
                    RenderParagraphBlock(
                        text = block.text,
                        isError = message.isError,
                        textColor = textColor,
                        accentColor = accentColor,
                    )
                }

                is MessageBlock.CodeBlock -> {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            block.language?.takeIf { it.isNotBlank() }?.let { language ->
                                Text(
                                    text = language,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = block.code,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = textColor,
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            )
                        }
                    }
                }

                is MessageBlock.MathBlock -> {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "LaTeX",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$$${block.expression}$$",
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                                color = textColor,
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            )

                            latexPreviewUrl(block.expression)?.let { previewUrl ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withLink(
                                            LinkAnnotation.Url(
                                                url = previewUrl,
                                                styles = TextLinkStyles(
                                                    style = SpanStyle(
                                                        color = accentColor,
                                                        textDecoration = TextDecoration.Underline,
                                                    ),
                                                ),
                                            ),
                                        ) {
                                            append("Abrir renderização")
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }

            if (index < blocks.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RenderParagraphBlock(
    text: String,
    isError: Boolean,
    textColor: Color,
    accentColor: Color,
) {
    val lines = text.lines().map { it.trimEnd() }.filter { it.isNotBlank() }

    if (lines.isEmpty()) {
        Text(
            text = "",
            color = textColor,
        )
        return
    }

    Column {
        lines.forEachIndexed { index, line ->
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = buildFormattedParagraph(
                            text = line.removePrefix("### "),
                            isError = isError,
                            normalColor = textColor,
                            codeColor = accentColor,
                            linkColor = accentColor,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor,
                    )
                }

                line.startsWith("## ") -> {
                    Text(
                        text = buildFormattedParagraph(
                            text = line.removePrefix("## "),
                            isError = isError,
                            normalColor = textColor,
                            codeColor = accentColor,
                            linkColor = accentColor,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                    )
                }

                line.startsWith("# ") -> {
                    Text(
                        text = buildFormattedParagraph(
                            text = line.removePrefix("# "),
                            isError = isError,
                            normalColor = textColor,
                            codeColor = accentColor,
                            linkColor = accentColor,
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                    )
                }

                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row {
                        Text(
                            text = "• ",
                            color = textColor,
                        )
                        FormattedTextLine(
                            text = line.drop(2),
                            isError = isError,
                            textColor = textColor,
                            accentColor = accentColor,
                        )
                    }
                }

                line.matches(Regex("^\\d+\\.\\s+.+")) -> {
                    val marker = line.substringBefore(' ') + " "
                    val itemText = line.substringAfter(' ', "")
                    Row {
                        Text(
                            text = marker,
                            color = textColor,
                        )
                        FormattedTextLine(
                            text = itemText,
                            isError = isError,
                            textColor = textColor,
                            accentColor = accentColor,
                        )
                    }
                }

                line.startsWith("> ") -> {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "| ",
                            color = accentColor,
                        )
                        FormattedTextLine(
                            text = line.removePrefix("> "),
                            isError = isError,
                            textColor = textColor,
                            accentColor = accentColor,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        )
                    }
                }

                line.matches(Regex("^(-{3,}|\\*{3,}|_{3,})$")) -> {
                    HorizontalDivider()
                }

                else -> {
                    FormattedTextLine(
                        text = line,
                        isError = isError,
                        textColor = textColor,
                        accentColor = accentColor,
                    )
                }
            }

            if (index < lines.lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun FormattedTextLine(
    text: String,
    isError: Boolean,
    textColor: Color,
    accentColor: Color,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val annotated = buildFormattedParagraph(
        text = text,
        isError = isError,
        normalColor = textColor,
        codeColor = accentColor,
        linkColor = accentColor,
    )
    Text(
        text = annotated,
        style = style.copy(color = textColor),
    )
}

@Composable
private fun buildFormattedParagraph(
    text: String,
    isError: Boolean,
    normalColor: Color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
    codeColor: Color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
    linkColor: Color = MaterialTheme.colorScheme.primary,
) = buildAnnotatedString {
    val codeBackground = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    parseInlineSegments(text).forEach { segment ->
        when (segment) {
            is InlineSegment.Plain -> append(segment.text)
            is InlineSegment.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = normalColor)) {
                append(segment.text)
            }

            is InlineSegment.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = normalColor)) {
                append(segment.text)
            }

            is InlineSegment.InlineCode -> withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    color = codeColor,
                    background = codeBackground,
                    fontSynthesis = FontSynthesis.None,
                ),
            ) {
                append(segment.text)
            }

            is InlineSegment.InlineMath -> withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                    color = codeColor,
                ),
            ) {
                append(segment.text)
            }

            is InlineSegment.Link -> {
                withLink(
                    LinkAnnotation.Url(
                        url = segment.url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ),
                    ),
                ) {
                    append(segment.label)
                }
            }
        }
    }
}