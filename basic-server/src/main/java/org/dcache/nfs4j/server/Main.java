package org.dcache.nfs4j.server;

import org.dcache.nfs.ExportFile;
import org.dcache.oncrpc4j.portmap.OncRpcEmbeddedPortmap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine;

@CommandLine.Command(name = "nfs4j", mixinStandardHelpOptions = true, version = "0.0.1", showDefaultValues = true)
public class Main implements Callable<Void> {

    @CommandLine.Option(names = "-root", description = "root of the file system to export", paramLabel = "<path>")
    private Path root;
    @CommandLine.Option(names = "-exports", description = "path to file with export tables", paramLabel = "<file>")
    private Path exportsFile;
    @CommandLine.Option(names = "-nfsvers", description = "NFS version (3, 4, 0==3+4) to use", paramLabel = "<int>")
    private int nfsVers = 0;
    @CommandLine.Option(names = "-port", description = "TCP port to use", paramLabel = "<port>")
    private int rpcPort = 2049;
    @CommandLine.Option(names = "-with-portmap", description = "start embedded portmap")
    private boolean withPortmap;

    public static void main(String[] args) throws Exception {
        int rc = new CommandLine(new Main()).execute(args);
        System.exit(rc);
    }

    public Void call() throws IOException, InterruptedException {

        ExportFile exportFile = null;
        if (exportsFile != null) {
            exportFile = new ExportFile(exportsFile.toFile());
        }

        if (withPortmap) {
            new OncRpcEmbeddedPortmap();
        }

        try (SimpleNfsServer ignored = new SimpleNfsServer(nfsVers, rpcPort, root, exportFile, null)) {
            System.out.println("Press Ctrl-C to stop the server...");
            Thread.currentThread().join();
        }

        return null;
    }
}
