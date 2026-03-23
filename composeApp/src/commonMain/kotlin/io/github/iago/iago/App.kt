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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
    val isError: Boolean = false,
)

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

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = alignment,
                    ) {
                        Card(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 80.dp)
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            MessageContent(message = message)
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
                )
                Button(
                    enabled = input.isNotBlank() && !isLoading,
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
private fun MessageContent(message: ChatMessage) {
    val blocks = remember(message.text) { parseMessageBlocks(message.text) }

    Column(modifier = Modifier.padding(12.dp)) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MessageBlock.Paragraph -> {
                    Text(
                        text = buildFormattedParagraph(block.text, message.isError),
                        color = if (message.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )
                }

                is MessageBlock.CodeBlock -> {
                    Card(shape = RoundedCornerShape(10.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            block.language?.takeIf { it.isNotBlank() }?.let { language ->
                                Text(
                                    text = language,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = block.code,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            )
                        }
                    }
                }

                is MessageBlock.MathBlock -> {
                    Card(shape = RoundedCornerShape(10.dp)) {
                        Text(
                            text = block.expression,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .padding(10.dp)
                                .horizontalScroll(rememberScrollState()),
                        )
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
private fun buildFormattedParagraph(text: String, isError: Boolean) = buildAnnotatedString {
    val normalColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val codeColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

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
        }
    }
}