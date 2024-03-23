plugins {
  id("otel.library-instrumentation")
  id("otel.jacoco-conventions")
  id("otel.nullaway-conventions")
}

dependencies {
  library("com.alibaba:druid:1.0.0")

  testImplementation(project(":instrumentation:alibaba-druid-1.0:testing"))
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
}
