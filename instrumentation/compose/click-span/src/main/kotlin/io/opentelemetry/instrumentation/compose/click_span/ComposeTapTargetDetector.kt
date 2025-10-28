@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.opentelemetry.instrumentation.compose.click_span

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.node.Owner
import androidx.compose.ui.semantics.SemanticsModifier
import java.util.LinkedList

internal class ComposeTapTargetDetector(
    private val composeLayoutNodeUtil: ComposeLayoutNodeUtil,
) {
    fun nodeToName(node: LayoutNode): String =
        try {
            getNodeName(node) ?: node.semanticsId.toString()
        } catch (_: Throwable) {
            node.semanticsId.toString()
        }

    fun findTapTarget(
        decorView: View,
        x: Float,
        y: Float,
    ): LayoutNode? {
        val queue = LinkedList<View>()
        queue.addFirst(decorView)

        var target: LayoutNode? = null
        while (queue.isNotEmpty()) {
            val view = queue.removeFirst()
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) {
                    queue.add(view.getChildAt(index))
                }
                (view as? Owner)?.let {
                    try {
                        target =
                            findTapTarget(
                                view as Owner,
                                x,
                                y,
                            )
                    } catch (_: Throwable) {
                        // We rely on visibility suppression to access internal fields and
                        // classes any runtime exception must be caught here.
                    }
                }
            }
        }
        return target
    }

    private fun findTapTarget(
        owner: Owner,
        x: Float,
        y: Float,
    ): LayoutNode? {
        val queue = LinkedList<LayoutNode>()
        queue.addFirst(owner.root)
        var target: LayoutNode? = null

        println("DEBUG-CLICK - Click coordinates: x=$x, y=$y")

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            println(
                "DEBUG-ALL-NODES - Visited Node: ${node.semanticsId} : ${
                node.getModifierInfo().joinToString(separator = " ") { info ->
                    val modifier = info.modifier
                    when (modifier::class.qualifiedName) {
                        "androidx.compose.ui.platform.TestTagElement" -> {
                            val fields = modifier::class.java.declaredFields
                            fields.forEach { it.isAccessible = true }
                            val fieldValues = fields.joinToString { field ->
                                "${field.name}=${field.get(modifier)}"
                            }
                            "TestTagElement: {$fieldValues}"
                        }
                        else -> {
                            if (modifier is SemanticsModifier) {
                                "Semantics: ${modifier.semanticsConfiguration}"
                            } else {
//                                "Modifier: ${modifier::class.qualifiedName} - $modifier"
                                ""
                            }
                        }
                    }
                }
            } node.isPlaced = ${node.isPlaced}  hitTest = ${hitTest(node, x, y)}")

            if (node.isPlaced && hitTest(node, x, y)) {
                target = node
            }

            queue.addAll(node.zSortedChildren.asMutableList())
        }
        return target
    }

    private fun isValidClickTarget(node: LayoutNode): Boolean {
        for (info in node.getModifierInfo()) {
            val modifier = info.modifier
            if (modifier::class.qualifiedName == CLASS_NAME_TEST_TAG_ELEMENT) {
                val tagField = modifier::class.java.getDeclaredField("tag")
                tagField.isAccessible = true
                if (tagField.get(modifier) != null && (tagField.get(modifier) as String).startsWith("button_")) {
                    return true
                }
            }
        }
        return false
    }

    private fun getNodeName(node: LayoutNode): String? {
        for (info in node.getModifierInfo()) {
            val modifier = info.modifier
            when (modifier::class.qualifiedName) {
                "androidx.compose.ui.platform.TestTagElement" -> {
                    val tagField = modifier::class.java.getDeclaredField("tag")
                    tagField.isAccessible = true
                    if (tagField.get(modifier) != null) {
                        return tagField.get(modifier) as String
                    }
                }
            }
        }
        return null
    }

    private fun hitTest(
        node: LayoutNode,
        x: Float,
        y: Float,
    ): Boolean {
        val bounded =
            composeLayoutNodeUtil.getLayoutNodeBoundsInWindow(node)?.let { bounds ->
                x >= bounds.left && x <= bounds.right && y >= bounds.top && y <= bounds.bottom
            } == true

        return bounded && isValidClickTarget(node)
    }

    companion object {
        private const val CLASS_NAME_TEST_TAG_ELEMENT =
            "androidx.compose.ui.platform.TestTagElement"
    }
}
