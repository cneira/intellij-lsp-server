package com.ruin.intel.commands.completion

class CompletionCommandTestCase : CompletionCommandTestBase() {
    fun `test function completion`() = checkContainsCompletion(13, 29, "boring() -> void")
}
