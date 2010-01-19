package org.dcache.chimera.nfs.v4.client;

import org.dcache.chimera.nfs.v4.xdr.SEQUENCE4args;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.sequenceid4;
import org.dcache.chimera.nfs.v4.xdr.sessionid4;
import org.dcache.chimera.nfs.v4.xdr.slotid4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;

public class SequenceStub {

    public static nfs_argop4 generateRequest(boolean CacheThis, byte[] SessId,
            int SeqId, int HighestSlot, int SlotId) {

        nfs_argop4 op = new nfs_argop4();
        op.argop = nfs_opnum4.OP_SEQUENCE;
        op.opsequence = new SEQUENCE4args();
        op.opsequence.sa_cachethis = CacheThis;

        slotid4 sId = new slotid4();
        sId.value = new uint32_t(SlotId);
        op.opsequence.sa_slotid = sId;

        slotid4 HsId = new slotid4();
        HsId.value = new uint32_t(HighestSlot);
        op.opsequence.sa_highest_slotid = HsId;

        sequenceid4 seq = new sequenceid4();
        seq.value = new uint32_t(++SeqId);
        op.opsequence.sa_sequenceid = seq;

        sessionid4 sess = new sessionid4();
        sess.value = SessId;
        op.opsequence.sa_sessionid = sess;

        return op;
    }
}