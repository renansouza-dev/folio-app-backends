# (1)
FROM alpine:3.22.1 AS build
ENV JAVA_HOME=/opt/jdk/jdk-25+36
ENV PATH=$JAVA_HOME/bin:$PATH

# (2)
ADD https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B36/OpenJDK25U-jdk_aarch64_alpine-linux_hotspot_25_36.tar.gz /opt/jdk/
RUN tar -xzvf /opt/jdk/OpenJDK25U-jdk_aarch64_alpine-linux_hotspot_25_36.tar.gz -C /opt/jdk/ && \
    jlink --compress=2 \
          --module-path /opt/jdk/jdk-25+36/jmods/ \
          --add-modules java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.sql,jdk.unsupported \
          --no-header-files --no-man-pages \
          --output /springboot-runtime

# (5)
FROM alpine:3.22.1
# (6)
COPY --from=build  /springboot-runtime /opt/jdk
ENV PATH=$PATH:/opt/jdk/bin
EXPOSE 8080
COPY folio-app-transactions-*.jar /opt/app/folio-app-transactions.jar
CMD ["java", "-showversion", "-jar", "/opt/app/folio-app-transactions.jar"]