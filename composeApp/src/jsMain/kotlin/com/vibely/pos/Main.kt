package com.vibely.pos

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.vibely.pos.config.DebugConfig
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (js("new URLSearchParams(window.location.search).get('debug') === 'true'") as Boolean) {
        DebugConfig.enableDebugMode()
    }
    ComposeViewport(document.body!!) {
        App()
    }
}
