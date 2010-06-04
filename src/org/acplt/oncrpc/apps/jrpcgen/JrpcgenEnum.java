/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenEnum.java,v 1.1.1.1 2003/08/13 12:03:45 haraldalbrecht Exp $
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
 * The <code>JrpcgenEnum</code> class represents a single enumeration
 * from an rpcgen "x"-file. It is a "container" for the elements (constants)
 * belonging to this enumeration.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2003/08/13 12:03:45 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
public class JrpcgenEnum {

    /**
     * Enumeration identifier.
     */
    public String identifier;

    /**
     * Contains enumeration elements as well as their values. The elements
     * are of class {@link JrpcgenConst}.
     */
    public Vector enums;

    /**
     * Returns the fully qualified identifier.
     *
     * return fully qualified identifier.
     */
    public String toString() {
        return identifier;
    }

    /**
     * Constructs a <code>JrpcgenEnum</code> and sets the identifier and all
     * its enumeration elements.
     *
     * @param identifier Identifier to be declared.
     * @param enums Vector of enumeration elements of class {@link JrpcgenConst}.
     */
    public JrpcgenEnum(String identifier, Vector enums) {
        this.identifier = identifier;
        this.enums = enums;
    }

    /**
     * Dumps the enumeration together with its elements to
     * <code>System.out</code>.
     */
    public void dump() {
        System.out.println("ENUM " + identifier);
        int size = enums.size();
        for ( int idx = 0; idx < size; ++idx ) {
            JrpcgenConst c = (JrpcgenConst) enums.elementAt(idx);
            System.out.print("  ");
            c.dump();
        }
        System.out.println();
    }

}

// End of JrpcgenEnum.java
