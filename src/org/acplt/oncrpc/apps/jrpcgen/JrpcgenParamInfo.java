/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenParamInfo.java,v 1.2 2003/08/14 08:09:59 haraldalbrecht Exp $
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

/**
 * The <code>JrpcgenParamInfo</code> class contains information about the
 * data type of a procedure's parameter, as well as the parameter's optional
 * name.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/14 08:09:59 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
class JrpcgenParamInfo {

    /**
     *
     */
    public String parameterType;

    /**
     *
     */
    public String parameterName;

    /**
     * Constructs a new <code>JrpcgenParamInfo</code> object containing
     * information about ...
     *
     */
    public JrpcgenParamInfo(String parameterType, String parameterName) {
        this.parameterType = parameterType;
        this.parameterName = parameterName;
    }

}

// End of JrpcgenParamInfo.java