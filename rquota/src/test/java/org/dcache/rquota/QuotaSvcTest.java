package org.dcache.rquota;

import static org.dcache.rquota.QuotaVfs.GROUP_QUOTA;
import static org.dcache.rquota.QuotaVfs.USER_QUOTA;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.dcache.nfs.ExportTable;
import org.dcache.nfs.FsExport;
import org.dcache.nfs.util.UnixSubjects;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.oncrpc4j.rpc.RpcTransport;
import org.dcache.rquota.xdr.ext_getquota_args;
import org.dcache.rquota.xdr.ext_setquota_args;
import org.dcache.rquota.xdr.getquota_rslt;
import org.dcache.rquota.xdr.qr_status;
import org.dcache.rquota.xdr.setquota_rslt;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class QuotaSvcTest {

    private QuotaVfs quotaVfs;
    private QuotaSvc quotaSvc;
    private RpcCall call;
    private RpcAuth auth;
    private ExportTable exportTable;
    private RpcTransport transport;

    @Before
    public void setUp() {
        quotaVfs = mock(QuotaVfs.class);
        exportTable = mock(ExportTable.class);
        quotaSvc = new QuotaSvc(quotaVfs, exportTable);
        call = mock(RpcCall.class);
        auth = mock(RpcAuth.class);
        when(call.getCredential()).thenReturn(auth);

        transport = mock(RpcTransport.class);
        when(transport.getRemoteSocketAddress()).thenReturn(new InetSocketAddress(0));
        when(call.getTransport()).thenReturn(transport);
    }

    @Test
    public void testGetQuotaWrongUser() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = USER_QUOTA;

        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(2, 2));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_EPERM, result.status);
    }

    @Test
    public void testGetQuotaWrongGroup() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = GROUP_QUOTA;

        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(2, 2));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_EPERM, result.status);
    }

    @Test
    public void testGetUserQuota() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = USER_QUOTA;

        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(1, 1));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_OK, result.status);
    }

    @Test
    public void testGetGroupQuota() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = GROUP_QUOTA;

        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(1, 1));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_OK, result.status);
    }

    @Test
    public void testSetQuotaNoExport() {
        ext_setquota_args args = new ext_setquota_args();
        args.sqa_id = 1;
        args.sqa_type = GROUP_QUOTA;

        when(exportTable.getExport(anyString(), any())).thenReturn(null);
        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(0, 0));
        setquota_rslt result = quotaSvc.RQUOTAPROC_SETQUOTA_2(call, args);
        assertEquals(qr_status.Q_EPERM, result.status);
    }

    @Test
    public void testSetQuotaNotRoot() {
        ext_setquota_args args = new ext_setquota_args();
        args.sqa_id = 1;
        args.sqa_type = GROUP_QUOTA;

        when(exportTable.getExport(anyString(), any())).thenReturn(mock(FsExport.class));
        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(2, 2));
        setquota_rslt result = quotaSvc.RQUOTAPROC_SETQUOTA_2(call, args);
        assertEquals(qr_status.Q_EPERM, result.status);
    }

    @Test
    public void testSetQuotaRootSquashed() throws UnknownHostException {
        ext_setquota_args args = new ext_setquota_args();
        args.sqa_id = 1;
        args.sqa_type = GROUP_QUOTA;

        when(exportTable.getExport(anyString(), any())).thenReturn(new FsExport.FsExportBuilder()
                .notTrusted()
                .forClient("*")
                .build("/"));

        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(0, 0));
        setquota_rslt result = quotaSvc.RQUOTAPROC_SETQUOTA_2(call, args);
        assertEquals(qr_status.Q_EPERM, result.status);
    }

    @Test
    public void testSetQuotaAsRoot() throws UnknownHostException {
        ext_setquota_args args = new ext_setquota_args();
        args.sqa_id = 1;
        args.sqa_type = GROUP_QUOTA;

        when(exportTable.getExport(anyString(), any())).thenReturn(new FsExport.FsExportBuilder()
                .trusted()
                .forClient("*")
                .build("/"));

        when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(0, 0));
        setquota_rslt result = quotaSvc.RQUOTAPROC_SETQUOTA_2(call, args);
        assertEquals(qr_status.Q_OK, result.status);
    }
}
