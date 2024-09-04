package org.dcache.rquota;

import static org.dcache.rquota.QuotaVfs.GROUP_QUOTA;
import static org.dcache.rquota.QuotaVfs.USER_QUOTA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import org.dcache.nfs.util.UnixSubjects;
import org.dcache.oncrpc4j.rpc.RpcAuth;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.rquota.xdr.ext_getquota_args;
import org.dcache.rquota.xdr.getquota_rslt;
import org.dcache.rquota.xdr.qr_status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class QuotaSvcTest {

    private QuotaVfs quotaVfs;
    private QuotaSvc quotaSvc;
    private RpcCall call;
    private RpcAuth auth;

    @Before
    public void setUp() {
        quotaVfs = mock(QuotaVfs.class);
        quotaSvc = new QuotaSvc(quotaVfs);
        call = mock(RpcCall.class);
        auth = mock(RpcAuth.class);
    }

    @Test
    public void testGetQuotaWrongUser() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = USER_QUOTA;

        Mockito.when(call.getCredential()).thenReturn(auth);
        Mockito.when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(2, 2));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_EPERM, result.status);
    }

    @Test
    public void testGetQuotaWrongGroup() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = GROUP_QUOTA;

        Mockito.when(call.getCredential()).thenReturn(auth);
        Mockito.when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(2, 2));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_EPERM, result.status);
    }

    @Test
    public void testGetUserQuota() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = USER_QUOTA;

        Mockito.when(call.getCredential()).thenReturn(auth);
        Mockito.when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(1, 1));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_OK, result.status);
    }

    @Test
    public void testGetGroupQuota() {
        ext_getquota_args args = new ext_getquota_args();
        args.gqa_id = 1;
        args.gqa_type = GROUP_QUOTA;

        Mockito.when(call.getCredential()).thenReturn(auth);
        Mockito.when(auth.getSubject()).thenReturn(UnixSubjects.toSubject(1, 1));
        getquota_rslt result = quotaSvc.RQUOTAPROC_GETQUOTA_2(call, args);
        assertEquals(qr_status.Q_OK, result.status);
    }
}
