package com.ruin.lsp.commands.completion

import com.ruin.lsp.DUMMY_FILE_PATH
import com.ruin.lsp.JAVA_PROJECT
import com.ruin.lsp.model.invokeCommandAndWait
import org.eclipse.lsp4j.Position
import org.intellivim.FileEditingTestCase

abstract class CompletionCommandTestBase : FileEditingTestCase() {
    override val filePath: String
        get() = DUMMY_FILE_PATH

    override val projectName: String
        get() = JAVA_PROJECT

    protected fun checkContainsCompletion(line: Int, char: Int, snippet: Boolean, expected: String) {
        val command = CompletionCommand(Position(line, char), snippet)
        val result = invokeCommandAndWait(command, file.url)
        assertTrue("Expected $expected to be included but got: \n${result.right}",
            result.right.items.any { it.label == expected || it.insertText == expected })
    }
}
