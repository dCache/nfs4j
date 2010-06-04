/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenParserException.java,v 1.1.1.1 2003/08/13 12:03:46 haraldalbrecht Exp $
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
 * The <code>JrpcgenParserException</code> class represents a parser
 * exception indicating to abort parsing the x-file.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2003/08/13 12:03:46 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
class JrpcgenParserException extends RuntimeException {

    /**
     * Constructs a <code>JrpcgenParserException</code> with no detail message.
     */
    public JrpcgenParserException() {
        super();
    }

}

// End of JrpcgenParserException.java
