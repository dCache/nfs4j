/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenProgramInfo.java,v 1.1.1.1 2003/08/13 12:03:46 haraldalbrecht Exp $
 *
 * Copyright (c) 1999, 2000
 * Lehrstuhl fuer Prozessleittechnik (PLT), RWTH Aachen
 * D-52064 Aachen, Germany.
 * All rights reserved.
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

package org.acplt.oncrpc.apps.jrpcgen;

import java.util.Vector;
import java.io.PrintWriter;

/**
 * The <code>JrpcgenProgramInfo</code> class contains information about a
 * single ONC/RPC program as defined in an rpcgen "x"-file.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2003/08/13 12:03:46 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
class JrpcgenProgramInfo {

    /**
     * Program number assigned to an ONC/RPC program. This attribute contains
     * either an integer literal or an identifier (which must resolve to an
     * integer).
     */
    public String programNumber;

    /**
     * Identifier assigned to the program number of an ONC/RPC program.
     */
    public String programId;

    /**
     * Set of versions specified for a particular ONC/RPC program.
     * The elements in the set are of class {@link JrpcgenVersionInfo}.
     */
    public Vector versions;

    /**
     * Construct a new <code>JrpcgenProgramInfo</code> object containing the
     * programs's identifier and number, as well as the versions defined
     * for this particular ONC/RPC program.
     *
     * @param programId Identifier defined for this ONC/RPC program.
     * @param programNumber Program number assigned to this ONC/RPC program.
     * @param versions Vector of versions defined for this ONC/RPC program.
     */
    public JrpcgenProgramInfo(String programId, String programNumber,
                              Vector versions) {
        this.programId = programId;
        this.programNumber = programNumber;
        this.versions = versions;
    }

    /**
     * Generates source code to define all constants belonging to this
     * program.
     *
     * @param out PrintWriter to send source code to.
     */
    public void dumpConstants(PrintWriter out) {
        out.println("    /* ONC/RPC program number definition */");
        out.println("    public final static int "
                    + programId + " = " + programNumber + ";");

        int size = versions.size();
        for ( int idx = 0; idx < size; ++idx ) {
            JrpcgenVersionInfo version =
                (JrpcgenVersionInfo) versions.elementAt(idx);
            version.dumpConstants(out);
        }
    }

}

// End of JrpcgenProgramInfo.java
