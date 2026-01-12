FROM almalinux/10-minimal

RUN microdnf install -y java-21-openjdk-headless && \
    microdnf clean all && \
    rm -rf /var/cache/yum

RUN mkdir /nfs4j
COPY basic-server/target/basic-server.jar /nfs4j/basic-server.jar
COPY jacoco-0.8.14/lib/jacocoagent.jar /nfs4j/jacocoagent.jar


CMD ["java", "-jar", "-javaagent:/nfs4j/jacocoagent.jar=dumponexit=true,destfile=/coverage-reports/jacoco-ut.exec", "/nfs4j/basic-server.jar"]