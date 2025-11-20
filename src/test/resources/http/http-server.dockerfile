FROM docker.io/bellsoft/liberica-runtime-container:jdk-25-slim-musl

USER 1000

COPY src/test/java/httpServer.java /httpServer.java

HEALTHCHECK --interval=10s CMD \
  wget -O/dev/stdout --tries=1 http://localhost:12345 2>/dev/null | grep index || exit 1

ENTRYPOINT [ "java", "/httpServer.java" ]
