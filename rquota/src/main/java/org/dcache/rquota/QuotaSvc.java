/*
 * Copyright (c) 2024 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.rquota;

import static org.dcache.rquota.QuotaVfs.GROUP_QUOTA;
import static org.dcache.rquota.QuotaVfs.USER_QUOTA;
import org.dcache.nfs.util.SubjectHolder;
import org.dcache.nfs.util.UnixSubjects;
import org.dcache.oncrpc4j.rpc.RpcCall;
import org.dcache.rquota.xdr.ext_getquota_args;
import org.dcache.rquota.xdr.ext_setquota_args;
import org.dcache.rquota.xdr.getquota_args;
import org.dcache.rquota.xdr.getquota_rslt;
import org.dcache.rquota.xdr.qr_status;
import org.dcache.rquota.xdr.rquotaServerStub;
import org.dcache.rquota.xdr.setquota_args;
import org.dcache.rquota.xdr.setquota_rslt;
import org.slf4j.Logger;

import javax.security.auth.Subject;

public class QuotaSvc extends rquotaServerStub {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(QuotaSvc.class);

    private final QuotaVfs _qfs;

    public QuotaSvc(QuotaVfs _qfs) {
        this._qfs = _qfs;
    }

    @Override
    public getquota_rslt RQUOTAPROC_GETQUOTA_1(RpcCall call$, getquota_args arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public getquota_rslt RQUOTAPROC_GETACTIVEQUOTA_1(RpcCall call$, getquota_args arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public setquota_rslt RQUOTAPROC_SETQUOTA_1(RpcCall call$, setquota_args arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public setquota_rslt RQUOTAPROC_SETACTIVEQUOTA_1(RpcCall call$, setquota_args arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public getquota_rslt RQUOTAPROC_GETQUOTA_2(RpcCall call$, ext_getquota_args arg1) {
        var r = new getquota_rslt();

        if (!canQuery(call$.getCredential().getSubject(), arg1.gqa_id, arg1.gqa_type)) {
            r.status = qr_status.Q_EPERM;
            return r;
        }

        r.status = qr_status.Q_OK;
        r.gqr_rquota = _qfs.getQuota(arg1.gqa_id, arg1.gqa_type);
        return r;
    }

    @Override
    public getquota_rslt RQUOTAPROC_GETACTIVEQUOTA_2(RpcCall call$, ext_getquota_args arg1) {
        var r = new getquota_rslt();

        if (!canQuery(call$.getCredential().getSubject(), arg1.gqa_id, arg1.gqa_type)) {
            r.status = qr_status.Q_EPERM;
            return r;
        }

        r.status = qr_status.Q_OK;
        r.gqr_rquota = _qfs.getQuota(arg1.gqa_id, arg1.gqa_type);
        return r;
    }

    @Override
    public setquota_rslt RQUOTAPROC_SETQUOTA_2(RpcCall call$, ext_setquota_args arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public setquota_rslt RQUOTAPROC_SETACTIVEQUOTA_2(RpcCall call$, ext_setquota_args arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Check if the given subject can query the quota for the given id and type.
     *
     * @param subject the subject to check
     * @param id      the id to check
     * @param type    the type to check
     * @return true if the subject can query the quota, false otherwise
     */
    private boolean canQuery(Subject subject, int id, int type) {

        boolean canQuery = UnixSubjects.isRootSubject(subject) ||
                (type == USER_QUOTA && UnixSubjects.hasUid(subject, id)) ||
                (type == GROUP_QUOTA && UnixSubjects.hasGid(subject, id));

        LOGGER.debug("Request by {} to query quota for id {} and type {}: {}",
                new SubjectHolder(subject), id, type, canQuery ? "granted" : "denied");
        return canQuery;
    }
}
