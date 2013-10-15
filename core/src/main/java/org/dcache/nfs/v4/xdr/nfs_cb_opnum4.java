/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.xdr;
/**
 * Enumeration (collection of constants).
 */
public interface nfs_cb_opnum4 {

    public static final int OP_CB_GETATTR = 3;
    public static final int OP_CB_RECALL = 4;
    public static final int OP_CB_LAYOUTRECALL = 5;
    public static final int OP_CB_NOTIFY = 6;
    public static final int OP_CB_PUSH_DELEG = 7;
    public static final int OP_CB_RECALL_ANY = 8;
    public static final int OP_CB_RECALLABLE_OBJ_AVAIL = 9;
    public static final int OP_CB_RECALL_SLOT = 10;
    public static final int OP_CB_SEQUENCE = 11;
    public static final int OP_CB_WANTS_CANCELLED = 12;
    public static final int OP_CB_NOTIFY_LOCK = 13;
    public static final int OP_CB_NOTIFY_DEVICEID = 14;
    public static final int OP_CB_ILLEGAL = 10044;

}
// End of nfs_cb_opnum4.java
