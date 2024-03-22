package io.opentelemetry.instrumentation.api.semconv.http;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_HttpServerMetrics_State extends HttpServerMetrics.State {

  private final Attributes startAttributes;

  private final long startTimeNanos;

  AutoValue_HttpServerMetrics_State(
      Attributes startAttributes,
      long startTimeNanos) {
    if (startAttributes == null) {
      throw new NullPointerException("Null startAttributes");
    }
    this.startAttributes = startAttributes;
    this.startTimeNanos = startTimeNanos;
  }

  @Override
  Attributes startAttributes() {
    return startAttributes;
  }

  @Override
  long startTimeNanos() {
    return startTimeNanos;
  }

  @Override
  public String toString() {
    return "State{"
        + "startAttributes=" + startAttributes + ", "
        + "startTimeNanos=" + startTimeNanos
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof HttpServerMetrics.State) {
      HttpServerMetrics.State that = (HttpServerMetrics.State) o;
      return this.startAttributes.equals(that.startAttributes())
          && this.startTimeNanos == that.startTimeNanos();
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= startAttributes.hashCode();
    h$ *= 1000003;
    h$ ^= (int) ((startTimeNanos >>> 32) ^ startTimeNanos);
    return h$;
  }

}
