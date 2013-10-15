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
public interface open_claim_type4 {

    public static final int CLAIM_NULL = 0;
    public static final int CLAIM_PREVIOUS = 1;
    public static final int CLAIM_DELEGATE_CUR = 2;
    public static final int CLAIM_DELEGATE_PREV = 3;
    public static final int CLAIM_FH = 4;
    public static final int CLAIM_DELEG_CUR_FH = 5;
    public static final int CLAIM_DELEG_PREV_FH = 6;

}
// End of open_claim_type4.java
