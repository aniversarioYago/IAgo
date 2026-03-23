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
            val codeEnd = rawText.indexOf("\n```", headerEnd + 1)
            if (codeEnd == -1) {
                addParagraphBlocks(rawText.substring(codeStart), blocks)
                break
            }

            val code = rawText.substring(headerEnd + 1, codeEnd)
                .trimEnd('\n', '\r')
                .trimIndent()
            blocks.add(MessageBlock.CodeBlock(code = code, language = language))
            cursor = codeEnd + 4
            continue
        }

        val mathEnd = rawText.indexOf("$$", nextTokenStart + 2)
        if (mathEnd == -1) {
            addParagraphBlocks(rawText.substring(nextTokenStart), blocks)
            break
        }

        val expression = rawText.substring(nextTokenStart + 2, mathEnd).trim()
        blocks.add(MessageBlock.MathBlock(expression = expression))
        cursor = mathEnd + 2
    }

    return blocks.ifEmpty { listOf(MessageBlock.Paragraph(rawText)) }
}

fun parseInlineSegments(text: String): List<InlineSegment> {
    if (text.isEmpty()) return emptyList()

    val segments = mutableListOf<InlineSegment>()
    var index = 0

    while (index < text.length) {
        if (text.startsWith("**", index)) {
            val end = text.indexOf("**", index + 2)
            if (end > index + 2) {
                segments.add(InlineSegment.Bold(text.substring(index + 2, end)))
                index = end + 2
                continue
            }
        }

        if (text[index] == '*') {
            val end = text.indexOf('*', index + 1)
            if (end > index + 1) {
                segments.add(InlineSegment.Italic(text.substring(index + 1, end)))
                index = end + 1
                continue
            }
        }

        if (text[index] == '`') {
            val end = text.indexOf('`', index + 1)
            if (end > index + 1) {
                segments.add(InlineSegment.InlineCode(text.substring(index + 1, end)))
                index = end + 1
                continue
            }
        }

        if (text[index] == '$') {
            val end = text.indexOf('$', index + 1)
            if (end > index + 1) {
                segments.add(InlineSegment.InlineMath(text.substring(index + 1, end)))
                index = end + 1
                continue
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

private fun findNextSpecialIndex(text: String, start: Int): Int? {
    var i = start
    while (i < text.length) {
        if (text[i] == '*' || text[i] == '`' || text[i] == '$') {
            return i
        }
        i++
    }
    return null
}


