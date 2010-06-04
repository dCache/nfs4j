/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenProcedureInfo.java,v 1.3 2003/08/14 11:26:50 haraldalbrecht Exp $
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

/**
 * The <code>JrpcgenProcedureInfo</code> class contains information about a
 * specific version of an ONC/RPC program as defined in an rpcgen "x"-file.
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/14 11:26:50 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
class JrpcgenProcedureInfo {

    /**
     * Procedure number assigned to the procedure of a particular verions of
     * an ONC/RPC program. This attribute contains either an integer literal
     * or an identifier (which must resolve to an integer).
     */
    public String procedureNumber;

    /**
     * Identifier assigned to the procedure number of an a particular
     * procedure of a particular version of an ONC/RPC program.
     */
    public String procedureId;

    /**
     * Type specifier of the result returned by the remote procedure.
     */
    public String resultType;

    /**
     * Parameter(s) to the remote procedure.
     */
    public Vector parameters;

    /**
     * Constructs a new <code>JrpcgenProcedureInfo</code> object containing
     * information about a programs' version and a set of procedures
     * defined by this program version.
     *
     * @param procedureId Identifier assigned to the procedure of a particular
     *   version of an ONC/RPC program.
     * @param procedureNumber Procedure number assigned to remote procedure.
     * @param resultType Type specifier of result returned by remote procedure.
     * @param parameters Type specifier of parameter to the remote procedure.
     */
    public JrpcgenProcedureInfo(String procedureId, String procedureNumber,
                                String resultType, Vector parameters) {
        this.procedureId = procedureId;
        this.procedureNumber = procedureNumber;
        this.resultType = resultType;
        this.parameters = parameters;
    }

}

// End of JrpcgenProcedureInfo.java