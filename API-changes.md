# Changes to NFS4J public API

## 0.27

- change the signature of `ClientCB#cbLayoutRecallFile` to accept layout type.

## 0.24

- update org.dcache.nfs.util.Cache and org.dcache.nfs.v4.NFSv4StateHandler to use java.time.Duration instead of a _long in millis_ to describe various amounts of time.
- update org.dcache.nfs.v4.{NFSv4StateHandler,NFS4Client} to use java.time.Clock as a time source

## 0.23

- dropped Stat#get/setFileId methods
  - use `Stat#get/setIno` instead
- Update the signature of `Stat#getIno` to return _long_
- Update the signature of `Stat#setIno` to accept _long_
- Update the signature of VirtualFileSystem#access to accept additional `Subject`

## 0.22

- removed deprecated CompoundContextBuilder#withExportFile
- removed interface org.dcache.nfs.v4.NfsLoginService
- drop dependency on org.dcache.auth package. Now only UnixNumericUserPrincipal and UnixNumericGroupPrincipal are used.
  - this change is not backward compatible due to license incompatibility of org.dcache.auth package.
  - added new class org.dcache.nfs.util.UnixSubjects that provides functionality used form org.dcache.auth package.Subjects

## 0.21

- deprecated methods in *org.dcache.nfs.vfs.VirtualFileSystem*
  - VirtualFileSystem#read(org.dcache.nfs.vfs.Inode, byte[], long, int)
  - VirtualFileSystem#write(org.dcache.nfs.vfs.Inode, byte[], long, int, org.dcache.nfs.vfs.VirtualFileSystem.StabilityLevel)

- VirtualFileSystem extended with new methods
  - getCaseInsensitive
  - getCasePreserving

- stateid4 modified to use primitive int as seqid field.

## 0.20

- VirtualFileSystem extended with methods to handle extended attributes
 - getXattr
 - setXattr
 - listXattrs
 - removeXattr

## 0.19

- NFSv41DeviceManager methods updated to take raw XDR arguments:
  - layoutGet(...) -> layoutGet(CompoundContext context, LAYOUTGET4args args)
  - layoutReturn(...) -> layoutReturn(CompoundContext context, LAYOUTRETURN4args args)
  - getDeviceInfo(...) -> getDeviceInfo(CompoundContext context, GETDEVICEINFO4args args)
  - getDeviceList(...) -> getDeviceList(CompoundContext context, GETDEVICELIST4args args)

- NFSv41DeviceManager extended with additional methods:
  - layoutCommit
  - layoutError
  - layoutStats

- io_info4 modified to use primitive longs as member fields
- LayoutDriver#acceptLayoutReturnData accepts CompoundContext as argument
- Introduced `ExportTable` interface to allow alternative ways for file system export management.
- removed NFSServerV1#getStatistics and OperationFactoryMXBeanImpl. The same functionality can be achieved by decorating OperationExecutor.

The following example uses io.dropwizard.metrics to collect and publish statistics:

```java
public class MetricAwareOperationExecutor implements OperationExecutor {

    private final OperationExecutor inner;
    private final MetricRegistry metrics;
    private final JmxReporter reporter;

    public MetricAwareOperationExecutor(OperationExecutor inner) {
        this.inner = inner;
        this.metrics = new MetricRegistry();
        this.reporter = JmxReporter
            .forRegistry(metrics)
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .inDomain(OperationExecutor.class.getPackageName())
            .build();
    }

    @Override
    public nfs_resop4 execute(CompoundContext context, nfs_argop4 arg) throws IOException, OncRpcException {

        final Timer requests = metrics.timer(nfs_opnum4.toString(arg.argop));
        final Timer.Context time = requests.time();
        try {
            return inner.execute(context, arg);
        } finally {
            time.stop();
        }
    }
}
```
