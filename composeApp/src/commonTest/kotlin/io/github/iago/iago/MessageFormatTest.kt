package io.github.iago.iago

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MessageFormatTest {

    @Test
    fun parseMessageBlocks_parsesCodeAndMathBlocks() {
        val text = """
            Texto inicial

            ```kotlin
            println("oi")
            ```

            $$
            E = mc^2
            $$
        """.trimIndent()

        val blocks = parseMessageBlocks(text)

        assertEquals(3, blocks.size)
        assertIs<MessageBlock.Paragraph>(blocks[0])
        assertIs<MessageBlock.CodeBlock>(blocks[1])
        assertIs<MessageBlock.MathBlock>(blocks[2])

        assertEquals("kotlin", (blocks[1] as MessageBlock.CodeBlock).language)
        assertEquals("println(\"oi\")", (blocks[1] as MessageBlock.CodeBlock).code)
        assertEquals("E = mc^2", (blocks[2] as MessageBlock.MathBlock).expression)
    }

    @Test
    fun parseMessageBlocks_parsesFenceAtEndOfText() {
        val text = """
            Antes

            ```json
            {"ok": true}
            ```
        """.trimIndent()

        val blocks = parseMessageBlocks(text)

        assertEquals(2, blocks.size)
        assertIs<MessageBlock.Paragraph>(blocks[0])
        assertIs<MessageBlock.CodeBlock>(blocks[1])
        assertEquals("json", (blocks[1] as MessageBlock.CodeBlock).language)
        assertEquals("{\"ok\": true}", (blocks[1] as MessageBlock.CodeBlock).code)
    }

    @Test
    fun parseInlineSegments_parsesMarkdownCodeAndInlineMath() {
        val segments = parseInlineSegments("**negrito** *italico* `codigo` \$x+y\$")

        assertEquals(7, segments.size)
        assertIs<InlineSegment.Bold>(segments[0])
        assertIs<InlineSegment.Plain>(segments[1])
        assertIs<InlineSegment.Italic>(segments[2])
        assertIs<InlineSegment.Plain>(segments[3])
        assertIs<InlineSegment.InlineCode>(segments[4])
        assertIs<InlineSegment.Plain>(segments[5])
        assertIs<InlineSegment.InlineMath>(segments[6])
    }

    @Test
    fun parseInlineSegments_parsesLatexParenthesesSyntax() {
        val segments = parseInlineSegments("Formula \\(x^2 + y^2\\) fim")

        assertEquals(3, segments.size)
        assertIs<InlineSegment.Plain>(segments[0])
        assertIs<InlineSegment.InlineMath>(segments[1])
        assertIs<InlineSegment.Plain>(segments[2])
        assertEquals("x^2 + y^2", (segments[1] as InlineSegment.InlineMath).text)
    }

    @Test
    fun parseInlineSegments_ignoresEscapedMarkers() {
        val escaped = "\\*nao italico\\* e \\${'$'}nao math\\${'$'}"
        val segments = parseInlineSegments(escaped)

        assertEquals(1, segments.size)
        assertIs<InlineSegment.Plain>(segments[0])
        assertEquals(escaped, (segments[0] as InlineSegment.Plain).text)
    }

    @Test
    fun parseInlineSegments_parsesMarkdownLink() {
        val segments = parseInlineSegments("Veja [site](https://example.com) agora")

        assertEquals(3, segments.size)
        assertIs<InlineSegment.Plain>(segments[0])
        assertIs<InlineSegment.Link>(segments[1])
        assertIs<InlineSegment.Plain>(segments[2])

        val link = segments[1] as InlineSegment.Link
        assertEquals("site", link.label)
        assertEquals("https://example.com", link.url)
    }
}






