package org.dcache.nfs4j.server;

import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v3.MountServer;
import org.dcache.nfs.v3.NfsServerV3;
import org.dcache.nfs.v3.xdr.mount_prot;
import org.dcache.nfs.v3.xdr.nfs3_prot;
import org.dcache.nfs.v4.MDSOperationExecutor;
import org.dcache.nfs.v4.NFSServerV41;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.dcache.oncrpc4j.rpc.OncRpcProgram;
import org.dcache.oncrpc4j.rpc.OncRpcSvc;
import org.dcache.oncrpc4j.rpc.OncRpcSvcBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class SimpleNfsServer implements Closeable {
    private final OncRpcSvc nfsSvc;
    private final Path root;
    private final int port;
    private final String name;

    public SimpleNfsServer(Path root) {
        this(0, 2049, root, null, null);
    }

    public SimpleNfsServer(int nfsVers, int port, Path root, ExportFile exportFile, String name) {
        try {
            NfsServerV3 nfs3 = null;
            NFSServerV41 nfs4 = null;
            boolean startNfsV3 = ((nfsVers == 0) || (nfsVers == 3));
            boolean startNfsV4 = ((nfsVers == 0) || (nfsVers == 4));

            if (exportFile == null) {
                exportFile = new ExportFile(new InputStreamReader(SimpleNfsServer.class.getClassLoader().getResourceAsStream("exports")));
            }

            this.port = port;

            if (root == null) {
                root = Files.createTempDirectory(null);
            }
            this.root = root;

            if (name == null) {
                name = "nfs@" + this.port;
            }
            this.name = name;

            VirtualFileSystem vfs = new LocalFileSystem(this.root, exportFile.exports().collect(Collectors.toList()));

            nfsSvc = new OncRpcSvcBuilder()
                    .withPort(this.port)
                    .withTCP()
                    .withAutoPublish()
                    .withWorkerThreadIoStrategy()
                    .withServiceName(this.name)
                    .build();

            if (startNfsV4) {
                nfs4 = new NFSServerV41.Builder()
                        .withVfs(vfs)
                        .withOperationExecutor(new MDSOperationExecutor())
                        .withExportTable(exportFile)
                        .build();
            }

            if (startNfsV3) {
                nfs3 = new NfsServerV3(exportFile, vfs);
            }

            MountServer mountd = new MountServer(exportFile, vfs);

            if (startNfsV3) {
                nfsSvc.register(new OncRpcProgram(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V3), mountd);
                nfsSvc.register(new OncRpcProgram(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V1), mountd);
                nfsSvc.register(new OncRpcProgram(nfs3_prot.NFS_PROGRAM, nfs3_prot.NFS_V3), nfs3);
            }

            if (startNfsV4) {
                nfsSvc.register(new OncRpcProgram(nfs4_prot.NFS4_PROGRAM, nfs4_prot.NFS_V4), nfs4);
            }

            nfsSvc.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        nfsSvc.stop();
    }

    public Path getRoot() {
        return root;
    }

    public int getPort() {
        return port;
    }
}
