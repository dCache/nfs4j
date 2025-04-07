FROM almalinux/9-minimal

RUN microdnf install -y java-21-openjdk-headless && \
    microdnf clean all && \
    rm -rf /var/cache/yum

RUN mkdir /nfs4j
COPY basic-server/target/basic-server.jar /nfs4j/basic-server.jar


CMD ["java", "-jar", "/nfs4j/basic-server.jar"]