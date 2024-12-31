#!/bin/sh

exec /jlink-runtime/bin/java \
	${JAVA_OPT} ${JMX} ${JAVA_ARGS} \
	-cp "/usr/share/nfs4j/jars/*" org.dcache.nfs4j.server.Main "$@"
