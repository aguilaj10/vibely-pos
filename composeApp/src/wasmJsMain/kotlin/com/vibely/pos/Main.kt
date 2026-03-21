@file:OptIn(ExperimentalWasmJsInterop::class)

package com.vibely.pos

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.vibely.pos.config.DebugConfig
import kotlinx.browser.document

@JsFun("() => new URLSearchParams(window.location.search).get('debug') === 'true' ? 1 : 0")
private external fun jsIsDebugMode(): Int

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (jsIsDebugMode() != 0) {
        DebugConfig.enableDebugMode()
    }
    ComposeViewport(document.body!!) {
        App()
    }
}
