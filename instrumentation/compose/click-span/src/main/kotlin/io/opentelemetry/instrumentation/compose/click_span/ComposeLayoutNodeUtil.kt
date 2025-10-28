@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.opentelemetry.instrumentation.compose.click_span

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.LayoutNode

const val APP_SCREEN_CLICK_SPAN_NAME = "app.screen.click"

internal class ComposeLayoutNodeUtil {
    internal fun getLayoutNodeBoundsInWindow(node: LayoutNode): Rect? =
        try {
            node.layoutDelegate.outerCoordinator.coordinates
                .boundsInWindow()
        } catch (_: Exception) {
            null
        }

    internal fun getLayoutNodePositionInWindow(node: LayoutNode): Offset? =
        try {
            node.layoutDelegate.outerCoordinator.coordinates
                .positionInWindow()
        } catch (_: Exception) {
            null
        }
}
