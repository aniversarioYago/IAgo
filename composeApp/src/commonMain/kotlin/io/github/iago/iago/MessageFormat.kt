package io.github.iago.iago

sealed interface MessageBlock {
    data class Paragraph(val text: String) : MessageBlock
    data class CodeBlock(val code: String, val language: String?) : MessageBlock
    data class MathBlock(val expression: String) : MessageBlock
}

sealed interface InlineSegment {
    data class Plain(val text: String) : InlineSegment
    data class Bold(val text: String) : InlineSegment
    data class Italic(val text: String) : InlineSegment
    data class InlineCode(val text: String) : InlineSegment
    data class InlineMath(val text: String) : InlineSegment
    data class Link(val label: String, val url: String) : InlineSegment
}

fun parseMessageBlocks(rawText: String): List<MessageBlock> {
    if (rawText.isBlank()) return listOf(MessageBlock.Paragraph(""))

    val blocks = mutableListOf<MessageBlock>()
    var cursor = 0

    while (cursor < rawText.length) {
        val codeStart = rawText.indexOf("```", cursor).takeIf { it >= 0 }
        val mathStart = rawText.indexOf("$$", cursor).takeIf { it >= 0 }
        val nextTokenStart = listOfNotNull(codeStart, mathStart).minOrNull()

        if (nextTokenStart == null) {
            addParagraphBlocks(rawText.substring(cursor), blocks)
            break
        }

        if (nextTokenStart > cursor) {
            addParagraphBlocks(rawText.substring(cursor, nextTokenStart), blocks)
        }

        if (codeStart != null && codeStart == nextTokenStart) {
            val headerEnd = rawText.indexOf('\n', codeStart + 3)
            if (headerEnd == -1) {
                addParagraphBlocks(rawText.substring(codeStart), blocks)
                break
            }

            val language = rawText.substring(codeStart + 3, headerEnd).trim().ifBlank { null }
            val codeEnd = findClosingFence(rawText, headerEnd + 1)
            if (codeEnd == -1) {
                addParagraphBlocks(rawText.substring(codeStart), blocks)
                break
            }

            val code = rawText.substring(headerEnd + 1, codeEnd)
                .trimEnd('\n', '\r')
                .trimIndent()
            blocks.add(MessageBlock.CodeBlock(code = code, language = language))
            cursor = codeEnd + 3
            continue
        }

        val mathEnd = rawText.indexOf("$$", nextTokenStart + 2)
        if (mathEnd == -1) {
            addParagraphBlocks(rawText.substring(nextTokenStart), blocks)
            break
        }

        val expression = rawText.substring(nextTokenStart + 2, mathEnd).trim()
        if (expression.isNotBlank()) {
            blocks.add(MessageBlock.MathBlock(expression = expression))
        }
        cursor = mathEnd + 2
    }

    return blocks.ifEmpty { listOf(MessageBlock.Paragraph(rawText)) }
}

fun parseInlineSegments(text: String): List<InlineSegment> {
    if (text.isEmpty()) return emptyList()

    val segments = mutableListOf<InlineSegment>()
    var index = 0

    while (index < text.length) {
        if (text.startsWith("**", index) && !isEscaped(text, index)) {
            val end = text.indexOf("**", index + 2)
            if (end > index + 2) {
                segments.add(InlineSegment.Bold(text.substring(index + 2, end)))
                index = end + 2
                continue
            }
        }

        if (text[index] == '*' && !isEscaped(text, index)) {
            val end = text.indexOf('*', index + 1)
            if (end > index + 1) {
                segments.add(InlineSegment.Italic(text.substring(index + 1, end)))
                index = end + 1
                continue
            }
        }

        if (text[index] == '`' && !isEscaped(text, index)) {
            val end = text.indexOf('`', index + 1)
            if (end > index + 1) {
                segments.add(InlineSegment.InlineCode(text.substring(index + 1, end)))
                index = end + 1
                continue
            }
        }

        if (text[index] == '$' && !isEscaped(text, index)) {
            val end = text.indexOf('$', index + 1)
            if (end > index + 1) {
                segments.add(InlineSegment.InlineMath(text.substring(index + 1, end)))
                index = end + 1
                continue
            }
        }

        if (text.startsWith("\\(", index) && !isEscaped(text, index)) {
            val end = text.indexOf("\\)", index + 2)
            if (end > index + 2) {
                segments.add(InlineSegment.InlineMath(text.substring(index + 2, end)))
                index = end + 2
                continue
            }
        }

        if (text[index] == '[' && !isEscaped(text, index)) {
            val labelEnd = text.indexOf(']', index + 1)
            if (labelEnd > index + 1 && labelEnd + 1 < text.length && text[labelEnd + 1] == '(') {
                val urlEnd = text.indexOf(')', labelEnd + 2)
                if (urlEnd > labelEnd + 2) {
                    val label = text.substring(index + 1, labelEnd)
                    val url = text.substring(labelEnd + 2, urlEnd).trim()
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        segments.add(InlineSegment.Link(label = label, url = url))
                        index = urlEnd + 1
                        continue
                    }
                }
            }
        }

        val nextSpecial = findNextSpecialIndex(text, index + 1)
        val end = nextSpecial ?: text.length
        segments.add(InlineSegment.Plain(text.substring(index, end)))
        index = end
    }

    return segments
}

private fun addParagraphBlocks(chunk: String, blocks: MutableList<MessageBlock>) {
    chunk
        .split(Regex("\\n\\s*\\n"))
        .map { it.trim('\n', '\r') }
        .filter { it.isNotBlank() }
        .forEach { blocks.add(MessageBlock.Paragraph(it)) }
}

private fun findClosingFence(text: String, codeStart: Int): Int {
    var scan = codeStart
    while (scan < text.length) {
        val fence = text.indexOf("```", scan)
        if (fence == -1) return -1
        val startsLine = fence == 0 || text[fence - 1] == '\n' || text[fence - 1] == '\r'
        if (startsLine) return fence
        scan = fence + 3
    }
    return -1
}

private fun isEscaped(text: String, index: Int): Boolean {
    if (index == 0) return false
    var slashCount = 0
    var cursor = index - 1
    while (cursor >= 0 && text[cursor] == '\\') {
        slashCount++
        cursor--
    }
    return slashCount % 2 == 1
}

private fun findNextSpecialIndex(text: String, start: Int): Int? {
    var i = start
    while (i < text.length) {
        if (
            text[i] == '*' ||
            text[i] == '`' ||
            text[i] == '$' ||
            text[i] == '[' ||
            (text[i] == '\\' && i + 1 < text.length && text[i + 1] == '(')
        ) {
            return i
        }
        i++
    }
    return null
}




