# the first stage of our build will extract the layers
FROM adoptopenjdk:11-jdk-openj9 as builder
WORKDIR /app
ARG JAR_FILE=fiul-rest/fiul-rest-app/target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the second stage of our build will copy the extracted layers
FROM gcr.io/distroless/java:11-debug
LABEL "vendor"="Finarkein Analytics Private Limited"
LABEL "component"="NBFC-AA Financial Information User Layer (FIUL) "
LABEL "version"="0.4.0"

WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./
ENV JDK_JAVA_OPTIONS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:+UnlockExperimentalVMOptions"
ENV SECRET_KEYSET=
EXPOSE 7065
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]