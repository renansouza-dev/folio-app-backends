# -----------------------------------------------------
# STAGE 1 — BUILD: Create minimal Java runtime with jlink
# -----------------------------------------------------
ARG ALPINE_VERSION=alpine:3.22.2
FROM ${ALPINE_VERSION} AS build

ENV JAVA_HOME=/opt/jdk/jdk-25.0.1+8
ENV PATH="${JAVA_HOME}/bin:${PATH}"

ADD https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.1%2B8/OpenJDK25U-jdk_x64_alpine-linux_hotspot_25.0.1_8.tar.gz /opt/jdk/
RUN tar -xzf /opt/jdk/OpenJDK25U-jdk_x64_alpine-linux_hotspot_25.0.1_8.tar.gz -C /opt/jdk/ && \
    rm /opt/jdk/OpenJDK25U-jdk_x64_alpine-linux_hotspot_25.0.1_8.tar.gz && \
    jlink --compress=2 \
          --no-header-files \
          --no-man-pages \
          --module-path "${JAVA_HOME}/jmods" \
          --add-modules java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.sql,jdk.unsupported \
          --output /springboot-runtime

# -----------------------------------------------------
# STAGE 2 — RUNTIME: Alpine minimal image
# -----------------------------------------------------
FROM ${ALPINE_VERSION}

ARG SERVICE_NAME
ARG JAR_FILE

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy minimal JRE created by jlink
COPY --from=build /springboot-runtime /opt/jdk

# Prepare application folder
RUN mkdir -p /opt/app && chown -R appuser:appgroup /opt/app

# Copy application JAR
COPY ${JAR_FILE} /opt/app/app.jar
RUN chown appuser:appgroup /opt/app/app.jar

# Add jlink runtime to PATH
ENV PATH="/opt/jdk/bin:${PATH}"

USER appuser
EXPOSE 8080
CMD ["java", "-showversion", "-jar", "/opt/app/app.jar"]