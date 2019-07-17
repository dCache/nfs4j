# Changes to NFS4J public API

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
