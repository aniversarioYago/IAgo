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
}



