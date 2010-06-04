/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenVersionInfo.java,v 1.1.1.1 2003/08/13 12:03:47 haraldalbrecht Exp $
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
 * The <code>JrpcgenVersionInfo</code> class contains information about a
 * specific version of an ONC/RPC program as defined in a rpcgen "x"-file.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2003/08/13 12:03:47 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
class JrpcgenVersionInfo {

    /**
     * Version number assigned to an ONC/RPC program. This attribute contains
     * either an integer literal or an identifier (which must resolve to an
     * integer).
     */
    public String versionNumber;

    /**
     * Identifier assigned to the version number of an ONC/RPC program.
     */
    public String versionId;

    /**
     * Set of procedures specified for a particular ONC/RPC program.
     * The elements in the set are of class {@link JrpcgenProcedureInfo}.
     */
    public Vector procedures;

    /**
     * Constructs a new <code>JrpcgenVersionInfo</code> object containing
     * information about a programs' version and a set of procedures
     * defined by this program version.
     *
     * @param versionId Identifier defined for this version of a
     *   particular ONC/RPC program.
     * @param versionNumber Version number.
     * @param procedures Vector of procedures defined for this ONC/RPC program.
     */
    public JrpcgenVersionInfo(String versionId, String versionNumber,
                              Vector procedures) {
        this.versionId = versionId;
        this.versionNumber = versionNumber;
        this.procedures = procedures;
    }

    /**
     * Generates source code to define the version constant belonging to this
     * program.
     *
     * @param out PrintWriter to send source code to.
     */
    public void dumpConstants(PrintWriter out) {
        out.println("    /* ONC/RPC program version number definition */");
        out.println("    public final static int "
                    + versionId + " = " + versionNumber + ";");
    }

}

// End of JrpcgenVersionInfo.java