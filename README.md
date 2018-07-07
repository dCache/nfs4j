NFS4J
=====

The pure java implementation of NFS server version 3, 4.0 and 4.1 including pNFS extension
with nfs4.1-files and flex-files layout types.


Building from sources
---------------------

To build nfs4j from source code Java8 and Maven3 are required.

Implementing own NFS server
---------------------------

```java
public class App {

    public static void main(String[] args) throws Exception {

        // create an instance of a filesystem to be exported
        VirtualFileSystem vfs = new ....;

        // create the RPC service which will handle NFS requests
        OncRpcSvc nfsSvc = new OncRpcSvcBuilder()
                .withPort(2049)
                .withTCP()
                .withAutoPublish()
                .withWorkerThreadIoStrategy()
                .build();

        // specify file with export entries
        ExportFile exportFile = new ExportFile(....);

        // create NFS v4.1 server
        NFSServerV41 nfs4 = new NFSServerV41.Builder()
                .withExportFile(exportFile)
                .withVfs(vfs)
                .withOperationFactory(new MDSOperationFactory())
                .build();

        // create NFS v3 and mountd servers
        NfsServerV3 nfs3 = new NfsServerV3(exportFile, vfs);
        MountServer mountd = new MountServer(exportFile, vfs);

        // register NFS servers at portmap service
        nfsSvc.register(new OncRpcProgram(100003, 4), nfs4);
        nfsSvc.register(new OncRpcProgram(100003, 3), nfs3);
        nfsSvc.register(new OncRpcProgram(100005, 3), mountd);

        // start RPC service
        nfsSvc.start();

        System.in.read();
    }
}
```

Use NFS4J in your project
-----------------------------

```xml
<dependency>
    <groupId>org.dcache</groupId>
    <artifactId>nfs4j-core</artifactId>
    <version>0.17.2</version>
</dependency>

<repositories>
    <repository>
        <id>dcache-releases</id>
        <name>dCache.ORG maven repository</name>
        <url>https://download.dcache.org/nexus/content/repositories/releases</url>
        <layout>default</layout>
    </repository>
</repositories>
```

License:
--------

licensed under [LGPLv2](http://www.gnu.org/licenses/lgpl-2.0.txt "LGPLv2") (or later)

How to contribute
=================


**NFS4J** uses the linux kernel model where git is not only source repository,
but also the way to track contributions and copyrights.

Each submitted patch must have a "Signed-off-by" line.  Patches without
this line will not be accepted.

The sign-off is a simple line at the end of the explanation for the
patch, which certifies that you wrote it or otherwise have the right to
pass it on as an open-source patch.  The rules are pretty simple: if you
can certify the below:
```

    Developer's Certificate of Origin 1.1

    By making a contribution to this project, I certify that:

    (a) The contribution was created in whole or in part by me and I
         have the right to submit it under the open source license
         indicated in the file; or

    (b) The contribution is based upon previous work that, to the best
        of my knowledge, is covered under an appropriate open source
        license and I have the right under that license to submit that
        work with modifications, whether created in whole or in part
        by me, under the same open source license (unless I am
        permitted to submit under a different license), as indicated
        in the file; or

    (c) The contribution was provided directly to me by some other
        person who certified (a), (b) or (c) and I have not modified
        it.

    (d) I understand and agree that this project and the contribution
        are public and that a record of the contribution (including all
        personal information I submit with it, including my sign-off) is
        maintained indefinitely and may be redistributed consistent with
        this project or the open source license(s) involved.

```
then you just add a line saying ( git commit -s )

    Signed-off-by: Random J Developer <random@developer.example.org>

using your real name (sorry, no pseudonyms or anonymous contributions.)

Contact Us
---------
For help and development related discussions please contact us: *dev (@) dcache (.) org* 

