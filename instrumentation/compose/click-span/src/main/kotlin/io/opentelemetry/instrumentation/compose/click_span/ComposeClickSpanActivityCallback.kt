package io.opentelemetry.instrumentation.compose.click_span

import android.app.Activity
import io.opentelemetry.android.internal.services.visiblescreen.activities.DefaultingActivityLifecycleCallbacks

internal class ComposeClickSpanActivityCallback(
    private val composeClickSpanGenerator: ComposeClickSpanGenerator,
) : DefaultingActivityLifecycleCallbacks {
    override fun onActivityResumed(activity: Activity) {
        composeClickSpanGenerator.startTracking(activity.window)
    }

    override fun onActivityPaused(activity: Activity) {
        composeClickSpanGenerator.stopTracking()
    }
}
