FROM almalinux:9-minimal as builder

# Add JRE
RUN microdnf -y install java-17-openjdk-devel java-17-openjdk-jmods binutils
RUN jlink -v --compress=2 --strip-debug --no-header-files --no-man-pages --add-modules java.base,java.compiler,java.instrument,java.logging,java.management,java.naming,java.security.jgss,java.transaction.xa,java.xml,jdk.jfr,jdk.security.auth,jdk.unsupported --output /jlink-runtime


FROM almalinux:9-minimal
COPY --from=builder /jlink-runtime /jlink-runtime

# add external files into container at the build time
COPY run.sh /run.sh
RUN chmod +x /run.sh

# where we store the data
RUN mkdir -p /usr/share/nfs4j

# Add JARS
COPY maven /usr/share/nfs4j/jars


# Post-install brutal cleanup
RUN microdnf clean all && rm -rf /var/cache/yum /var/lib/dnf /var/lib/rpm

# expose TCP ports for network services
EXPOSE 2049

# by default we start server
CMD ["/run.sh"]
