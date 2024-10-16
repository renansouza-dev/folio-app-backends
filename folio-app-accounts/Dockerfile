# (1)
FROM alpine:3.20.3 AS build
ENV JAVA_HOME=/opt/jdk/jdk-21.0.2+13
ENV PATH=$JAVA_HOME/bin:$PATH

# (2)
ADD https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jdk_x64_alpine-linux_hotspot_21.0.2_13.tar.gz /opt/jdk/
RUN tar -xzvf /opt/jdk/OpenJDK21U-jdk_x64_alpine-linux_hotspot_21.0.2_13.tar.gz -C /opt/jdk/ && \
    jlink --compress=2 \
          --module-path /opt/jdk/jdk-21.0.2/jmods/ \
          --add-modules java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.sql,jdk.unsupported \
          --no-header-files --no-man-pages \
          --output /springboot-runtime

# (5)
FROM alpine:3.20.3
# (6)
COPY --from=build  /springboot-runtime /opt/jdk
ENV PATH=$PATH:/opt/jdk/bin
EXPOSE 8080
COPY ../target/folio-app-accounts-*.jar /opt/app/folio-app-accounts.jar
CMD ["java", "-showversion", "-jar", "/opt/app/folio-app-accounts.jar"]