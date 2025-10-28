package io.opentelemetry.instrumentation.compose.click_span

import com.google.auto.service.AutoService
import io.opentelemetry.android.instrumentation.AndroidInstrumentation
import io.opentelemetry.android.instrumentation.InstallationContext
import io.opentelemetry.api.incubator.trace.ExtendedTracer

@AutoService(AndroidInstrumentation::class)
class ComposeClickSpanInstrumentation : AndroidInstrumentation {
    override val name: String = "compose.click_span"

    override fun install(ctx: InstallationContext) {
        ctx.application.registerActivityLifecycleCallbacks(
            ComposeClickSpanActivityCallback(
                ComposeClickSpanGenerator(
                    ctx.openTelemetry
                        .tracerProvider
                        .tracerBuilder("io.opentelemetry.android.instrumentation.compose.click_span")
                        .build() as ExtendedTracer,
                ),
            ),
        )
    }
}
