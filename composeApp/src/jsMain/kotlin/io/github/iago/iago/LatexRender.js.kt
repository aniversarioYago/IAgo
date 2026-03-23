package io.github.iago.iago

actual fun latexPreviewUrl(expression: String): String? {
    val safeExpression = expression
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")

    val html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>IAgo LaTeX</title>
          <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.css" />
          <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.js"></script>
        </head>
        <body style="font-family: sans-serif; margin: 24px;">
          <div id="math"></div>
          <script>
            const expr = `$safeExpression`;
            const node = document.getElementById("math");
            katex.render(expr, node, { displayMode: true, throwOnError: false });
          </script>
        </body>
        </html>
    """.trimIndent()

    val encoded = js("encodeURIComponent(html)") as String
    return "data:text/html;charset=utf-8,$encoded"
}


