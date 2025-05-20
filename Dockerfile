
FROM docker.io/bellsoft/liberica-runtime-container:jre-21-slim-musl

ARG PROJECT="appointments"
ARG OTEL_BASE="github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download"
ARG OTEL_VERSION="v1.32.1"

WORKDIR /opt/$PROJECT

COPY target/$PROJECT-*.jar application.jar
ADD https://$OTEL_BASE/$OTEL_VERSION/opentelemetry-javaagent.jar opentelemetry.jar

USER 1000

ENV OTEL_SERVICE_NAME "$PROJECT"
ENV PERFORMANCE_OPTIONS "-XX:+AlwaysPreTouch -XX:+UseParallelGC -XX:+UseNUMA"
ENV TELEMETRY_OPTIONS "-javaagent:./opentelemetry.jar"
ENV JAVA_TOOL_OPTIONS "$PERFORMANCE_OPTIONS $TELEMETRY_OPTIONS"

HEALTHCHECK --interval=10s --start-period=10s CMD \
  wget -O/dev/stdout --tries=1 \
  http://localhost:8080/actuator/health 2>/dev/null | grep UP || exit 1

ENTRYPOINT [ "java", "-jar", "application.jar" ]
