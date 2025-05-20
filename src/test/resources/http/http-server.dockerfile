FROM docker.io/bellsoft/liberica-runtime-container:jdk-21-slim-musl

USER 1000

# TODO Mount these as volumes
#COPY src/test/resources/jwt/openid-configuration.json /configuration.json
#COPY src/test/resources/jwt/certs.json /certs.json
COPY src/test/java/httpServer.java /httpServer.java

HEALTHCHECK --interval=10s CMD \
  wget -O/dev/stdout --tries=1 http://localhost:9876 2>/dev/null | grep index || exit 1

ENTRYPOINT [ "java", "/httpServer.java" ]
